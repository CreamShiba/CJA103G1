package com.karshop.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "post_images")
public class PostImage {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "image_id")
	private Integer imageId;

	@Column(name = "url")
	private String url;

	@Column(name = "created_at", insertable = false, updatable = false)
	private LocalDateTime createdAt;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private ForumPost forumPost;

	public PostImage() {}

	public Integer getImageId() { return imageId; }
	public void setImageId(Integer imageId) { this.imageId = imageId; }
	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public ForumPost getForumPost() { return forumPost; }
	public void setForumPost(ForumPost forumPost) { this.forumPost = forumPost; }
}