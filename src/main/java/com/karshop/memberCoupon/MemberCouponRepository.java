package com.karshop.memberCoupon;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberCouponRepository extends JpaRepository<MemberCoupon, MemberCouponId> {

    // 根據會員編號查詢其擁有的所有優惠券明細
    List<MemberCoupon> findByMemberNo(Integer memberNo);

    // 如果只想查詢「未使用」的券，可以增加此方法
    List<MemberCoupon> findByMemberNoAndCouponStatus(Integer memberNo, Integer couponStatus);


    @Query("SELECT mc FROM MemberCoupon mc " +
            "JOIN mc.coupon c " +
            "WHERE mc.memberNo = :memberNo " +
            "AND mc.couponStatus = 0 " + // 0 代表未使用
            "AND mc.useTime < :now")    // couponEnd 是 CouponVO 裡的結束時間欄位
    List<MemberCoupon> findExpiredCoupons(
            @Param("memberNo") Integer memberNo,
            @Param("now") LocalDateTime now
    );


}
