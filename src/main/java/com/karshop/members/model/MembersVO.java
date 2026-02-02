package com.karshop.members.model;



import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;


@Entity
@Table(name = "member")
public class MembersVO implements Serializable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column (name= "member_no", updatable = false)
  private Integer memNo;

  @Column (name = "member_account", updatable = false)
  @NotEmpty(message = "會員帳號: 請勿空白")
  @Pattern(regexp = "^[a-zA-Z0-9_]{6,10}$", message = "會員帳號: 只能是英文字母、數字和_ , 且長度必需在6到10之間")
  private String memAcc;

  @Column (name = "member_password")
  @NotEmpty(message = "會員密碼: 請勿空白")
  @Pattern(regexp = "^[a-zA-Z0-9_]{4,10}$", message = "會員密碼: 只能是英文字母、數字和_ , 且長度必需在4到10之間")
  private String memPwd;

  @Column (name = "member_name")
  @NotEmpty(message = "會員姓名: 請勿空白")
  @Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,10}$", message = "會員姓名: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
  private String memName;


  @Column (name= "member_phone")
  @NotEmpty(message = "手機號碼: 請勿空白")
  @Pattern(regexp = "^09\\d{8}$", message = "手機號碼格式錯誤，請輸入09開頭共10碼的數字")
  private String memMobile;

  @Column (name= "member_email")
  @NotEmpty(message = "電子郵件: 請勿空白")
  @Email(message = "電子郵件格式錯誤")
  private String memEmail;

  @Column (name= "address")
  @NotEmpty(message = "地址: 請勿空白")
  @Pattern(regexp = "^[\u4e00-\u9fa50-9\\s\\-巷弄號樓,\\.]{10,200}$", message = "地址: 只能包含中文、數字、空白及「- 巷 弄 號 樓 , .」符號，且長度需在10到200字元之間")
  private String memAddr;

  @Column (name= "registration_time", updatable = false)
  private Timestamp memRegTime;

  @Column (name= "account_status")
  private Byte memStatus;

  @Column (name= "member_image")
  private byte[] memAvatar;

  @Column (name= "member_username")
  @NotEmpty(message = "會員暱稱: 請勿空白")
  @Pattern(regexp = "^[(\u4e00-\u9fa5)(a-zA-Z0-9_)]{2,50}$", message = "會員暱稱: 只能是中、英文字母、數字和_ , 且長度必需在2到50之間")
  private String memUsername;

  @Column (name= "member_login_errcount")
  private Byte memLogErrCount;

  @Column (name= "member_login_errtime")
  private Timestamp memLogErrTime;

  public MembersVO() {
    super();
  }

  public Integer getMemNo() {
    return memNo;
  }

  public void setMemNo(Integer memNo) {
    this.memNo = memNo;
  }

  public String getMemAcc() {
    return memAcc;
  }

  public void setMemAcc(String memAcc) {
    this.memAcc = memAcc;
  }

  public String getMemPwd() {
    return memPwd;
  }

  public void setMemPwd(String memPwd) {
    this.memPwd = memPwd;
  }

  public String getMemName() {
    return memName;
  }

  public void setMemName(String memName) {
    this.memName = memName;
  }

  public String getMemMobile() {
    return memMobile;
  }

  public void setMemMobile(String memMobile) {
    this.memMobile = memMobile;
  }

  public String getMemEmail() {
    return memEmail;
  }

  public void setMemEmail(String memEmail) {
    this.memEmail = memEmail;
  }

  public String getMemAddr() {
    return memAddr;
  }

  public void setMemAddr(String memAddr) {
    this.memAddr = memAddr;
  }

  public Timestamp getMemRegTime() {
    return memRegTime;
  }

  public void setMemRegTime(Timestamp memRegTime) {
    this.memRegTime = memRegTime;
  }

  public Byte getMemStatus() {
    return memStatus;
  }

  public void setMemStatus(Byte memStatus) {
    this.memStatus = memStatus;
  }

  public byte[] getMemAvatar() {
    return memAvatar;
  }

  public void setMemAvatar(byte[] memAvatar) {
    this.memAvatar = memAvatar;
  }

  public String getMemUsername() {
    return memUsername;
  }

  public void setMemUsername(String memUsername) {
    this.memUsername = memUsername;
  }

  public Byte getMemLogErrCount() {
    return memLogErrCount;
  }

  public void setMemLogErrCount(Byte memLogErrCount) {
    this.memLogErrCount = memLogErrCount;
  }

  public Timestamp getMemLogErrTime() {
    return memLogErrTime;
  }

  public void setMemLogErrTime(Timestamp memLogErrTime) {
    this.memLogErrTime = memLogErrTime;
  }

  public String getMemberName() {
    return this.memName;
  }

  public Integer getMemberNo() {
    return this.memNo;
  }

}