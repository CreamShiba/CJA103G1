package com.karshop.admins.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.karshop.adminauth.model.AdminAuthVO;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(name = "adm")
public class AdminVO implements Serializable {
  private static final long serialVersionUID = 1L;
//updable=fale可以設定此欄位無法更新
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "adm_no", updatable = false)
  private Integer adminNo;

  @Column(name = "adm_account")
  @NotEmpty(message = "管理員帳號: 請勿空白")
  @Pattern(regexp = "^[a-zA-Z0-9_]{4,16}$", message = "管理員帳號: 只能是英文字母、數字和_，長度必需在4到16之間")
  private String adminAcc;

  @Column(name = "adm_password")
  @NotEmpty(message = "管理員密碼: 請勿空白")
  @Pattern(regexp = "^[a-zA-Z0-9_]{4,16}$", message = "管理員密碼: 只能是英文字母、數字和_，長度必需在4到16之間")
  private String adminPwd;

  @Column(name = "adm_name")
  @NotEmpty(message = "管理員姓名: 請勿空白")
  @Pattern(regexp = "^[\u4e00-\u9fa5a-zA-Z0-9_]{2,20}$", message = "管理員姓名: 只能是中、英文字母、數字和_，長度必需在2到20之間")
  private String adminName;

  @Column(name = "adm_email")
  private String adminEmail;

  @Column(name = "hiredate", updatable = false)
  private Timestamp adminCreatedAt;

  @Column (name= "adm_image")
  private byte[] adminImage;

  @Column(name = "adm_status")
  private Byte adminStatus = 1;

  @Column(name = "adm_update")
  private Timestamp adminUpdatedAt;

  @OneToMany(
          mappedBy = "admin",
          cascade = CascadeType.ALL,
          orphanRemoval = true
  )
  private Set<AdminAuthVO> auths = new HashSet<>();

  public AdminVO() {
    super();
  }

  public Integer getAdminNo() {
    return adminNo;
  }

  public void setAdminNo(Integer adminNo) {
    this.adminNo = adminNo;
  }

  public String getAdminAcc() {
    return adminAcc;
  }

  public void setAdminAcc(String adminAcc) {
    this.adminAcc = adminAcc;
  }

  public String getAdminPwd() {
    return adminPwd;
  }

  public void setAdminPwd(String adminPwd) {
    this.adminPwd = adminPwd;
  }

  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public Timestamp getAdminCreatedAt() {
    return adminCreatedAt;
  }

  public void setAdminCreatedAt(Timestamp adminCreatedAt) {
    this.adminCreatedAt = adminCreatedAt;
  }

  public Byte getAdminStatus() {
    return adminStatus;
  }

  public void setAdminStatus(Byte adminStatus) {
    this.adminStatus = adminStatus;
  }

  public Timestamp getAdminUpdatedAt() {
    return adminUpdatedAt;
  }

  public void setAdminUpdatedAt(Timestamp adminUpdatedAt) {
    this.adminUpdatedAt = adminUpdatedAt;
  }

  public Set<AdminAuthVO> getAuths() {
    return auths;
  }

  public void setAuths(Set<AdminAuthVO> auths) {
    this.auths = auths;
  }

  public String getAdminEmail() {
    return adminEmail;
  }

  public void setAdminEmail(String adminEmail) {
    this.adminEmail = adminEmail;
  }

  public byte[] getAdminImage() {
    return adminImage;
  }

  public void setAdminImage(byte[] adminImage) {
    this.adminImage = adminImage;
  }
}
