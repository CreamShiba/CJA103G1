package com.karshop.install.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "adm")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Adm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adm_no")
    private Integer admNo;

    @Column(name = "adm_account", nullable = false, length = 14)
    private String admAccount;

    @Column(name = "adm_password", nullable = false)
    private String admPassword;

    @Column(name = "adm_name", nullable = false, length = 10)
    private String admName;

    @Column(name = "adm_email", nullable = false, length = 100)
    private String admEmail;

    @Column(name = "adm_status", nullable = false)
    private Integer admStatus = 1;

    @Column(name = "hiredate", nullable = false)
    private LocalDate hiredate;

    @Lob
    @Column(name = "adm_image", columnDefinition = "MEDIUMBLOB")
    private byte[] admImage;
}
