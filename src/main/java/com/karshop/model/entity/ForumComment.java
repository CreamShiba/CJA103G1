package com.karshop.model.entity;

import jakarta.persistence.*;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.karshop.members.model.MembersVO;

@Entity
@Table(name = "forum_comment") // 🟢 修正：SQL 腳本裡叫 forum_comment，不是 comment
public class ForumComment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "comment_id")
	private Integer commentId;

	@Column(name = "content", columnDefinition = "TEXT")
	private String content;

	@Column(name = "created_at", insertable = false, updatable = false)
	@Temporal(TemporalType.TIMESTAMP)
	private Date commentDate;

	@ManyToOne
	@JoinColumn(name = "post_id")
	@JsonBackReference
	private ForumPost forumPost;

	@ManyToOne
	@JoinColumn(name = "member_no") // 🟢 對齊資料庫 member_no
	private MembersVO member;

	public ForumComment() {}

	public Integer getCommentId() { return commentId; }
	public void setCommentId(Integer commentId) { this.commentId = commentId; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public Date getCommentDate() { return commentDate; }
	public void setCommentDate(Date commentDate) { this.commentDate = commentDate; }
	public ForumPost getForumPost() { return forumPost; }
	public void setForumPost(ForumPost forumPost) { this.forumPost = forumPost; }
	public MembersVO getMember() { return member; }
	public void setMember(MembersVO member) { this.member = member; }
}