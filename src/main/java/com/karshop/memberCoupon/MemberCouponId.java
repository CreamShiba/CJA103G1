package com.karshop.memberCoupon;

import lombok.Data;

import java.io.Serializable;

/**
 *  MemberCoupon 的複合主鍵
 */
@Data
public class MemberCouponId implements Serializable {
    private Integer memberNo;
    private Integer couponNo;
}
