package com.karshop.memberCoupon;

import lombok.Data;

import java.io.Serializable;

/**
 * 用於定義 MemberCoupon 的複合主鍵
 */
@Data
public class MemberCouponId implements Serializable {
    private Integer memberNo;
    private Integer couponNo;
}
