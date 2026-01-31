package com.karshop.install.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_no")
    private Integer memberNo;

    @Column(name = "member_account", nullable = false, length = 30)
    private String memberAccount;

    @Column(name = "member_password", nullable = false)
    private String memberPassword;

    @Column(name = "member_name", nullable = false, length = 50)
    private String memberName;

    @Lob
    @Column(name = "member_image", columnDefinition = "MEDIUMBLOB")
    private byte[] memberImage;

    @Column(name = "member_email", nullable = false, length = 100)
    private String memberEmail;

    @Column(name = "registration_time", nullable = false)
    private LocalDateTime registrationTime = LocalDateTime.now();

    @Column(name = "member_phone", length = 10)
    private String memberPhone;

    @Column(name = "engineer_status", nullable = false)
    private Integer engineerStatus = 0;

    @Column(name = "seller_status", nullable = false)
    private Integer sellerStatus = 0;

    @Column(name = "address", length = 40)
    private String address;

    @Column(name = "rating_amount", nullable = false)
    private Integer ratingAmount = 0;

    @Column(name = "rating_star", nullable = false)
    private Integer ratingStar = 0;
}
