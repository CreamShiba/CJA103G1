package com.karshop.model.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "blacklist")
@IdClass(BlacklistId.class)
public class Blacklist {

	@Id
	@Column(name = "user_id")
	private Integer userId;

	@Id
	@Column(name = "blocked_user_id")
	private Integer blockedUserId;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Timestamp createdAt;

	public Blacklist() {}
	public Blacklist(Integer userId, Integer blockedUserId) {
		this.userId = userId;
		this.blockedUserId = blockedUserId;
	}

	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public Integer getBlockedUserId() { return blockedUserId; }
	public void setBlockedUserId(Integer blockedUserId) { this.blockedUserId = blockedUserId; }
}