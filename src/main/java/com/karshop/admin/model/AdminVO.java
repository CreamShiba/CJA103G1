package com.karshop.admin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Date;

@Setter
@Getter
@Entity
@Table(name = "adm")
public class AdminVO implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name="adm_no",updatable = false)
  private Integer admNo;

  @Column(name="adm_account")
  @NotNull
  private String admAccount;

  @Column(name="adm_password")
  @NotNull
  private String admPassword;

  @Column(name="adm_name")
  @NotNull
  private String admName;

  @Column(name="adm_email")
  @NotNull
  private String admEmail;

  @Column(name="adm_status")
  @NotNull
  private Byte admStatus;

  @Column(name="hiredate")
  @NotNull
  private Date admDate;

  @Column(name="adm_image")
  private byte[] admPic;

}
