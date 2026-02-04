package com.karshop.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.karshop.jedis.JedisHandleMessage;
import com.karshop.model.entity.ChatMessage;
import com.karshop.members.model.MembersVO;
import com.karshop.model.repository.BlacklistRepository;
import com.karshop.model.repository.ForumMemberRepository;
import com.karshop.model.entity.BlacklistId; // 確保有 import 複合主鍵類別
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@Component("forumFriendWS")
@ServerEndpoint("/FriendWS/{userName}/{roomId}")
public class ForumFriendWS {
	private static Map<String, Session> sessionsMap = new ConcurrentHashMap<>();
	private Gson gson = new Gson();

	private static ForumMemberRepository forumMemberRepository;
	private static BlacklistRepository blacklistRepository;

	@Autowired
	public void setRepositories(ForumMemberRepository memberRepo, BlacklistRepository blacklistRepo) {
		ForumFriendWS.forumMemberRepository = memberRepo;
		ForumFriendWS.blacklistRepository = blacklistRepo;
	}

	@OnOpen
	public void onOpen(@PathParam("userName") String userName, @PathParam("roomId") String roomId, Session userSession) {
		sessionsMap.put(userName, userSession);
		System.out.println("🌐 WebSocket 已連線: " + userName + " 進入房間: " + roomId);
	}

	@OnMessage
	public void onMessage(Session userSession, String message) {
		try {
			ChatMessage chatMessage = gson.fromJson(message, ChatMessage.class);
			String sender = chatMessage.getSender();
			String receiver = chatMessage.getReceiver();
			String type = chatMessage.getType();

			if ("history".equals(type)) {
				List<String> historyData = JedisHandleMessage.getHistoryMsg(sender, receiver);
				userSession.getAsyncRemote().sendText(gson.toJson(new ChatMessage("history", sender, receiver, gson.toJson(historyData))));
				return;
			}

			if ("chat".equals(type) || "public".equals(type) || "all".equals(receiver)) {
				for (Session session : sessionsMap.values()) {
					if (session.isOpen()) {
						session.getAsyncRemote().sendText(message);
					}
				}
				JedisHandleMessage.saveChatMessage(sender, "public_room", message);
				return;
			}

			// 🟢 核心修正：私訊靜默封鎖邏輯 (Shadow Blocking)
			if ("private".equals(type)) {
				var senderVO = forumMemberRepository.findByMemUsername(sender);
				var receiverVO = forumMemberRepository.findByMemUsername(receiver);

				if (senderVO != null && receiverVO != null) {
					Integer senderId = senderVO.getMemNo();
					Integer receiverId = receiverVO.getMemNo();

					// 1. 檢查：接收者是否封鎖了發送者？ (靜默封鎖核心)
					boolean isBlockedByReceiver = blacklistRepository.existsByUserIdAndBlockedUserId(receiverId, senderId);

					if (isBlockedByReceiver) {
						// 動作：只將訊息回傳給發送者 (Echo)，讓發送者畫面上看起來有傳出去
						if (userSession.isOpen()) {
							userSession.getAsyncRemote().sendText(message);
						}
						// 🔴 重要：不發送給接收者，也不存入 Jedis，讓訊息在伺服器端消失
						System.out.println("👻 靜默攔截: " + sender + " 的訊息已被 " + receiver + " 屏蔽 (發送者無感)");
						return;
					}

					// 2. 檢查：發送者是否封鎖了接收者？ (避免發送者手滑)
					boolean isBlockedBySender = blacklistRepository.existsByUserIdAndBlockedUserId(senderId, receiverId);
					if (isBlockedBySender) {
						if (userSession.isOpen()) {
							userSession.getAsyncRemote().sendText(gson.toJson(new ChatMessage("private", "系統", sender, "⚠️ 您已封鎖對方，請解除封鎖後再發送訊息。")));
						}
						return;
					}
				}

				// 3. 通過檢查後才執行的原有發送邏輯 (正常通訊)
				Session receiverSession = sessionsMap.get(receiver);
				if (receiverSession != null && receiverSession.isOpen()) {
					receiverSession.getAsyncRemote().sendText(message);
				}
				if (userSession.isOpen()) {
					userSession.getAsyncRemote().sendText(message);
				}
				// 只有成功送達的訊息才會存入歷史紀錄
				JedisHandleMessage.saveChatMessage(sender, receiver, message);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	@OnClose
	public void onClose(Session userSession) {
		sessionsMap.values().remove(userSession);
	}

	@OnError
	public void onError(Session s, Throwable e) { }
}