package com.karshop.model.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name = "private_chat")
public class PrivateChat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "chat_id") // 確保對齊 PK 名稱
	private Integer chatId;

	// 🟢 修正：對齊 MySQL 的 sender_no
	@Column(name = "sender_no", nullable = false)
	private Integer senderNo;

	// 🟢 修正：對齊 MySQL 的 receiver_no
	@Column(name = "receiver_no", nullable = false)
	private Integer receiverNo;

	@Column(name = "message", columnDefinition = "TEXT")
	private String message;

	@Column(name = "send_time", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;

	// 空建構子 (JPA 必備)
	public PrivateChat() {}

	// 方便使用的建構子 (同步更新參數名稱)
	public PrivateChat(Integer senderNo, Integer receiverNo, String message) {
		this.senderNo = senderNo;
		this.receiverNo = receiverNo;
		this.message = message;
	}

	// --- Getters and Setters ---
	public Integer getChatId() { return chatId; }
	public void setChatId(Integer chatId) { this.chatId = chatId; }

	public Integer getSenderNo() { return senderNo; }
	public void setSenderNo(Integer senderNo) { this.senderNo = senderNo; }

	public Integer getReceiverNo() { return receiverNo; }
	public void setReceiverNo(Integer receiverNo) { this.receiverNo = receiverNo; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public Date getSendTime() { return sendTime; }
	public void setSendTime(Date sendTime) { this.sendTime = sendTime; }
}