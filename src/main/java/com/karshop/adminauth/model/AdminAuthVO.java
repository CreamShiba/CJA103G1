package com.karshop.adminauth.model;

import java.io.Serializable;

import com.karshop.adminauthmanage.model.AdminAuthListVO;
import com.karshop.admins.model.AdminVO;

import jakarta.persistence.*;

@Entity
@Table(name = "adm_auth")
public class AdminAuthVO implements Serializable {

  private static final long serialVersionUID = 1L;

  // 2. 新增自增主鍵欄位
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // 設定為資料庫自增 (MySQL/MariaDB 適用)
  @Column(name = "adm_auth_no") // 假設資料庫的新主鍵欄位名稱為 AUTHID
  private Integer admAuthNo;

  // 3. 移除原本的 @Id 註解，保留關聯設定
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "adm_no")
  private AdminVO admin;

  // 3. 移除原本的 @Id 註解，保留關聯設定
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auth_no")
  private AdminAuthListVO adminAuthList;

  public AdminAuthVO() {
    super();
  }

  public AdminVO getAdmin() {
    return admin;
  }

  public void setAdmin(AdminVO admin) {
    this.admin = admin;
  }

  public AdminAuthListVO getAdminAuthList() {
    return adminAuthList;
  }

  public void setAdmAuthList(AdminAuthListVO adminAuthList) {
    this.adminAuthList = adminAuthList;
  }



}