package com.karshop.controller;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import com.google.gson.Gson;
import com.karshop.jedis.JedisHandleMessage;
import com.karshop.model.entity.ChatMessage;
import com.karshop.members.model.MembersVO;
import com.karshop.model.repository.BlacklistRepository;
import com.karshop.model.repository.ForumMemberRepository; // 🟢 修正：新的 Import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@Component("forumFriendWS") // 🟢 指定唯一 Bean 名稱
@ServerEndpoint("/FriendWS/{userName}/{roomId}")
public class ForumFriendWS { // 🟢 類別名稱統一改為 ForumFriendWS
	private static Map<String, Session> sessionsMap = new ConcurrentHashMap<>();
	private Gson gson = new Gson();

	// 靜態注入 Repository
	private static ForumMemberRepository forumMemberRepository; // 🟢 修正型態
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

			if ("private".equals(type)) {
				Session receiverSession = sessionsMap.get(receiver);
				if (receiverSession != null && receiverSession.isOpen()) receiverSession.getAsyncRemote().sendText(message);
				if (userSession.isOpen()) userSession.getAsyncRemote().sendText(message);
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