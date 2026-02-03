package com.karshop.memberCoupon;

import com.karshop.coupon.Coupon;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "member_coupon")
@IdClass(MemberCouponId.class) // 使用複合主鍵類別
public class MemberCoupon {
    @Id
    @Column(name = "member_no")
    @NotNull(message = "會員編號不能空白")
    private Integer memberNo;

    @Id
    @Column(name = "coupon_no")
    @NotNull(message = "優惠券編號不能空白")
    private Integer couponNo;

    @Column(name = "coupon_status", nullable = false, columnDefinition = "TINYINT")
    @NotNull(message = "優惠券狀態不能空白")
    private Integer couponStatus; // 0:未使用, 1:已使用, 2:已過期

    @Column(name = "use_time", nullable = false)
    @NotNull(message = "使用期限不能空白")
    private LocalDateTime useTime;

    @ManyToOne
    @JoinColumn(name = "coupon_no", insertable = false, updatable = false)
    private Coupon coupon;
}
