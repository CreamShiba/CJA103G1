package com.karshop.model.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.karshop.members.model.MembersVO; // 🟢 引用組員的實體

@Entity
@Table(name = "forum_post")
public class ForumPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Integer postId;

	@OneToMany(mappedBy = "forumPost", cascade = CascadeType.ALL)
	@OrderBy("commentDate ASC")
	@JsonManagedReference
	private List<ForumComment> comments;

	@OneToMany(mappedBy = "forumPost", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PostImage> postImages;

	@ManyToOne
	@JoinColumn(name = "category_id", nullable = false)
	private Categories categories;

	// 🟢 修正：原本是 private Integer memberId;
	// 現在改為與組員的 MembersVO 直接關聯
	@ManyToOne
	@JoinColumn(name = "member_no", nullable = false) // 門牌號碼依舊是 member_no
	private MembersVO member;

	@Column(name = "title")
	private String title;

	@Column(name = "post_txt", columnDefinition = "TEXT")
	private String postTxt;

	@Column(name = "post_date", insertable = false, updatable = false)
	private Timestamp postDate;

	@Column(name = "post_like")
	private Long postLike = 0L;

	public ForumPost() {}

	// Getter & Setter 也要同步修改
	public Integer getPostId() { return postId; }
	public void setPostId(Integer postId) { this.postId = postId; }

	public List<ForumComment> getComments() { return comments; }
	public void setComments(List<ForumComment> comments) { this.comments = comments; }

	public List<PostImage> getPostImages() { return postImages; }
	public void setPostImages(List<PostImage> postImages) { this.postImages = postImages; }

	public Categories getCategories() { return categories; }
	public void setCategories(Categories categories) { this.categories = categories; }

	// 🟢 修正後的 Member 存取方法
	public MembersVO getMember() { return member; }
	public void setMember(MembersVO member) { this.member = member; }

	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }

	public String getPostTxt() { return postTxt; }
	public void setPostTxt(String postTxt) { this.postTxt = postTxt; }

	public Timestamp getPostDate() { return postDate; }
	public void setPostDate(Timestamp postDate) { this.postDate = postDate; }

	public Long getPostLike() { return postLike; }
	public void setPostLike(Long postLike) { this.postLike = postLike; }
}