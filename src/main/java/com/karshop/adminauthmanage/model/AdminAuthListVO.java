package com.karshop.adminauthmanage.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "adm_auth_list")
public class AdminAuthListVO implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "auth_no", updatable = false)
  private Integer authNo;

  @Column(name = "auth_name", length = 50)
  @NotEmpty(message = "功能名稱：請勿空白")
  @Size(max = 50, message = "功能名稱最長 50 個字元")
  private String authName;

  @OneToMany(mappedBy = "adminAuthList", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<com.karshop.adminauth.model.AdminAuthVO> auths = new HashSet<>();

  public AdminAuthListVO() {
    super();
  }

  public Integer getAuthNo() {
    return authNo;
  }

  public void setAuthNo(Integer authNo) {
    this.authNo = authNo;
  }

  public String getAuthName() {
    return authName;
  }

  public void setAuthName(String authName) {
    this.authName = authName;
  }

  public Set<com.karshop.adminauth.model.AdminAuthVO> getAuths() {
    return auths;
  }

  public void setAuths(Set<com.karshop.adminauth.model.AdminAuthVO> auths) {
    this.auths = auths;
  }



}