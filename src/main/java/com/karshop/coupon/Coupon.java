package com.karshop.coupon;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_no", updatable = false)
    private Integer couponNo;

    @Column(name = "adm_no")
    @NotNull(message = "管理員編號不能空白")
    private Integer admNo;

    @Column(name = "coupon_title")
    @NotBlank(message = "標題不能空白")
    @Size(max = 50, message = "標題不能超過50字")
    private String couponTitle;

    @Column(name = "coupon_content")
    @NotBlank(message = "內容不能空白")
    @Size(max = 200, message = "內容不能超過200字")
    private String couponContent;

    @Column(name = "discount_value")
    @NotNull(message = "折價金額不可空白")
    @Min(value=1, message="金額至少要1元")
    private Integer discountValue;

    @Column(name = "coupon_start")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "開始時間不能空白")
    private LocalDateTime couponStart;

    @Column(name = "coupon_end")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @NotNull(message = "結束時間不能空白")
    private LocalDateTime couponEnd;

    @Column(name = "coupon_status")
    private Integer couponStatus; // 預設為 1 (有效)
}