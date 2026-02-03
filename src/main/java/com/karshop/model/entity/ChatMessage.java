package com.karshop.model.entity;

public class ChatMessage {
	private String type;     // 訊息類型: "chat"(群聊), "private"(私訊), "history"(歷史紀錄)
	private String sender;   // 發送者 (通常是會員帳號或 ID)
	private String receiver; // 接收者 (私訊對象；如果是群聊則為 "all")
	private String message;  // 訊息內容

	// 空建構子 (JSON 轉換必備)
	public ChatMessage() {}

	// 方便使用的建構子
	public ChatMessage(String type, String sender, String receiver, String message) {
		this.type = type;
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
	}

	// Getters and Setters
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }

	public String getSender() { return sender; }
	public void setSender(String sender) { this.sender = sender; }

	public String getReceiver() { return receiver; }
	public void setReceiver(String receiver) { this.receiver = receiver; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
}