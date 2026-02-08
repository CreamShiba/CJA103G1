package com.karshop.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

// 第一個參數是 Entity 類別，第二個是主鍵 (@Id) 的型別
public interface CouponRepository extends JpaRepository<Coupon, Integer>, JpaSpecificationExecutor<Coupon> {

    Optional<Coupon> findByCouponTitle(String couponTitle);

    // 過期的優惠券狀態改為 0
    @Transactional
    @Modifying
    @Query("UPDATE Coupon c SET c.couponStatus = 0 WHERE c.couponEnd < :now AND c.couponStatus = 1")
    void updateExpiredStatus(LocalDateTime now);

    @Transactional
    @Modifying
    @Query(value = "UPDATE member_coupon mc " +
            "JOIN coupon c ON mc.coupon_no = c.coupon_no " +
            "SET mc.coupon_status = 2 " +
            "WHERE c.coupon_end < :now AND mc.coupon_status = 0", nativeQuery = true)
    void updateExpiredMemberCoupons(LocalDateTime now);

}
