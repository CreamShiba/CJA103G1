package com.karshop.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.karshop.jedis.JedisHandleMessage;
import com.karshop.model.entity.ChatMessage;
import com.karshop.model.repository.BlacklistRepository;
import com.karshop.model.repository.ForumMemberRepository;
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
		// 避免重複連線導致錯誤，先移除舊的
		sessionsMap.remove(userName);
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

			// 1. 獲取歷史紀錄
			if ("history".equals(type)) {
				// 這裡會呼叫我們剛修好的 buildKey，自動判斷是拿公頻還是私訊紀錄
				List<String> historyData = JedisHandleMessage.getHistoryMsg(sender, receiver);
				String historyJson = gson.toJson(historyData);

				// 回傳給前端
				ChatMessage cm = new ChatMessage("history", sender, receiver, historyJson);
				if (userSession.isOpen()) {
					userSession.getAsyncRemote().sendText(gson.toJson(cm));
				}
				return;
			}

			// 2. 公頻廣播 (Receiver 為 "all" 或 type 為 "public")
			if ("chat".equals(type) || "public".equals(type) || "all".equals(receiver)) {
				for (Session session : sessionsMap.values()) {
					if (session.isOpen()) {
						session.getAsyncRemote().sendText(message);
					}
				}
				// 🟢 強制儲存到 "public_room" Key
				JedisHandleMessage.saveChatMessage(sender, "public_room", message);
				return;
			}

			// 3. 私訊邏輯 (包含靜默封鎖)
			if ("private".equals(type)) {
				var senderVO = forumMemberRepository.findByMemUsername(sender);
				var receiverVO = forumMemberRepository.findByMemUsername(receiver);

				if (senderVO != null && receiverVO != null) {
					Integer senderId = senderVO.getMemNo();
					Integer receiverId = receiverVO.getMemNo();

					// 檢查是否被封鎖 (Shadow Ban)
					boolean isBlockedByReceiver = blacklistRepository.existsByUserIdAndBlockedUserId(receiverId, senderId);

					if (isBlockedByReceiver) {
						// 假裝發送成功 (只回傳給自己)
						if (userSession.isOpen()) userSession.getAsyncRemote().sendText(message);
						System.out.println("👻 靜默攔截: " + sender + " -> " + receiver);
						return; // ❌ 不存入 Redis，也不發給對方
					}

					// 檢查發送者是否封鎖了對方
					boolean isBlockedBySender = blacklistRepository.existsByUserIdAndBlockedUserId(senderId, receiverId);
					if (isBlockedBySender) {
						if (userSession.isOpen()) {
							userSession.getAsyncRemote().sendText(gson.toJson(new ChatMessage("private", "系統", sender, "⚠️ 您已封鎖對方，無法發送訊息。")));
						}
						return;
					}
				}

				// 正常發送
				Session receiverSession = sessionsMap.get(receiver);
				if (receiverSession != null && receiverSession.isOpen()) {
					receiverSession.getAsyncRemote().sendText(message);
				}

				// 回傳給自己 (確保畫面有顯示)
				if (userSession.isOpen()) {
					userSession.getAsyncRemote().sendText(message);
				}

				// 🟢 存入 Redis (這裡會自動用 buildKey 生成 A:B 的專屬 Key)
				JedisHandleMessage.saveChatMessage(sender, receiver, message);
			}
		} catch (Exception e) { e.printStackTrace(); }
	}

	@OnClose
	public void onClose(Session userSession) {
		sessionsMap.values().remove(userSession);
	}

	@OnError
	public void onError(Session s, Throwable e) {
		System.out.println("WebSocket 錯誤: " + e.getMessage());
	}
}