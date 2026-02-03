package com.karshop.model.entity;

import jakarta.persistence.*;
import com.karshop.members.model.MembersVO; // 🟢 引用組長實體
import java.util.Date;

@Entity
@Table(name = "post_favorites")
public class PostFavorite {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "fav_id")
	private Integer favId;

	@ManyToOne
	@JoinColumn(name = "post_id")
	private ForumPost forumPost;

	// 🟢 修正：對齊 member_no 並使用 MembersVO
	@ManyToOne
	@JoinColumn(name = "member_no")
	private MembersVO member;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Date createdAt;

	public PostFavorite() {}

	public Integer getFavId() { return favId; }
	public void setFavId(Integer favId) { this.favId = favId; }
	public ForumPost getForumPost() { return forumPost; }
	public void setForumPost(ForumPost forumPost) { this.forumPost = forumPost; }
	public MembersVO getMember() { return member; }
	public void setMember(MembersVO member) { this.member = member; }
}