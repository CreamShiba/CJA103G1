package com.karshop.model.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import com.karshop.members.model.MembersVO;

@Entity
@Table(name = "reports") // 🟢 對齊綜合檢舉表名稱
public class ForumReport {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "reports_no") // 🟢 對齊資料庫：reports_no
	private Integer reportsNo;

	@Column(name = "reports_type") // 🟢 區分類型，固定存入 "FORUM"
	private String reportsType = "FORUM";

	@Column(name = "reports_target")
	private String reportsTarget;

	@Column(name = "reports_reason") // 🟢 對齊資料庫：reports_reason
	private String reportsReason;

	@Column(name = "reports_description")
	private String reportsDescription;

	@Column(name = "reports_timestamp", insertable = false, updatable = false)
	private Timestamp reportsTimestamp;

	@Column(name = "status")
	private String status = "待處理";

	@Column(name = "handled")
	private Timestamp handled;

	@Column(name = "reports_response")
	private String reportsResponse;

	// 關聯部分
	@ManyToOne
	@JoinColumn(name = "member_no")
	private MembersVO member;

	@ManyToOne
	@JoinColumn(name = "post_id") // 🟢 連結到論壇文章
	private ForumPost forumPost;

	@Column(name = "adm_no") // 管理員編號
	private Integer admNo;

	public ForumReport() {}

	// Getter / Setter
	public Integer getReportsNo() { return reportsNo; }
	public void setReportsNo(Integer reportsNo) { this.reportsNo = reportsNo; }

	public String getReportsType() { return reportsType; }
	public void setReportsType(String reportsType) { this.reportsType = reportsType; }

	public String getReportsReason() { return reportsReason; }
	public void setReportsReason(String reportsReason) { this.reportsReason = reportsReason; }

	public String getReportsDescription() { return reportsDescription; }
	public void setReportsDescription(String reportsDescription) { this.reportsDescription = reportsDescription; }

	public Timestamp getReportsTimestamp() { return reportsTimestamp; }
	public void setReportsTimestamp(Timestamp reportsTimestamp) { this.reportsTimestamp = reportsTimestamp; }

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }

	public ForumPost getForumPost() { return forumPost; }
	public void setForumPost(ForumPost forumPost) { this.forumPost = forumPost; }

	public MembersVO getMember() { return member; }
	public void setMember(MembersVO member) { this.member = member; }

	public Integer getAdmNo() { return admNo; }
	public void setAdmNo(Integer admNo) { this.admNo = admNo; }
}