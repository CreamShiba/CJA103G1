package com.karshop.adminauth.model;

import java.io.Serializable;
import java.util.Objects;

public class AdminAuthNo implements Serializable {

  private Integer admin;
  private Integer admAuth;

  public AdminAuthNo() {
    super();
  }

  public AdminAuthNo(Integer admin, Integer admAuth) {
    super();
    this.admin = admin;
    this.admAuth = admAuth;
  }

  public Integer getAdmin() {
    return admin;
  }

  public void setAdmin(Integer admin) {
    this.admin = admin;
  }

  public Integer getAdmAuth() {
    return admAuth;
  }

  public void setAdmAuth(Integer admAuth) {
    this.admAuth = admAuth;
  }

  @Override
  public int hashCode() {
    return Objects.hash(admin, admAuth);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AdminAuthNo other = (AdminAuthNo) obj;
    return Objects.equals(admin, other.admin) && Objects.equals(admAuth, other.admAuth);
  }



}