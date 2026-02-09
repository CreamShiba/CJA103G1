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

	// 🟢 核心修正：終極版路徑洗衣機
	public String getUrl() {
		if (url == null) return null;

		// 1. 🚨 針對你現在遇到的 DNS 錯誤進行修正
		// 如果資料庫存成了 http://images/2001.jpg，把它改成 /images/2001.jpg
		if (url.startsWith("http://images/")) {
			return url.replace("http://images/", "/images/");
		}

		// 2. 處理舊的髒資料 http://img/...
		if (url.startsWith("http://img/")) {
			return url.replace("http://img/", "/images/");
		}

		// 3. 如果是 "images/2001.jpg" (有 images 但缺少開頭斜線)
		if (url.startsWith("images/")) {
			return "/" + url;
		}

		// 4. 如果是純檔名 "2001.jpg" (完全沒斜線)，補全套
		if (!url.contains("/") && !url.startsWith("http")) {
			return "/images/" + url;
		}

		// 5. 其他正常情況
		return url;
	}

	public void setUrl(String url) { this.url = url; }
	public LocalDateTime getCreatedAt() { return createdAt; }
	public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
	public ForumPost getForumPost() { return forumPost; }
	public void setForumPost(ForumPost forumPost) { this.forumPost = forumPost; }
}