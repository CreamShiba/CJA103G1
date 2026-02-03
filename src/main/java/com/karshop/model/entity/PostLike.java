package com.karshop.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "post_likes")
public class PostLike {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "like_id")
	private Integer likeId;

	@Column(name = "post_id")
	private Integer postId;

	// 🟢 修正：對齊 MySQL 的 member_no
	@Column(name = "member_no")
	private Integer memberNo;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Date createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = new Date();
	}
}