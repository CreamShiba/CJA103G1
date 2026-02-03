package com.karshop.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class BlacklistId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Integer userId;
	private Integer blockedUserId;

	public BlacklistId() {}
	public BlacklistId(Integer userId, Integer blockedUserId) {
		this.userId = userId;
		this.blockedUserId = blockedUserId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		BlacklistId that = (BlacklistId) o;
		return Objects.equals(userId, that.userId) && Objects.equals(blockedUserId, that.blockedUserId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, blockedUserId);
	}

	public Integer getUserId() { return userId; }
	public void setUserId(Integer userId) { this.userId = userId; }
	public Integer getBlockedUserId() { return blockedUserId; }
	public void setBlockedUserId(Integer blockedUserId) { this.blockedUserId = blockedUserId; }
}