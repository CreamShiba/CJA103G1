package com.karshop.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CouponScheduler {

    @Autowired
    private CouponRepository couponRepository;

    // 每20 min執行一次檢查
    @Scheduled(cron = "0 0/20 * * * *")
    public void checkExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // 1. 更新主表：優惠券定義設為失效
        couponRepository.updateExpiredStatus(now);

        // 2. 更新從表：會員手中的優惠券設為已過期 (取代原本的 DELETE)
        // 需在 CouponRepository 或 MemberCouponRepository 實作此方法
        couponRepository.updateExpiredMemberCoupons(now);

        System.out.println("排程執行完成，已同步更新主表與會員持有表之過期狀態。");
    }
}
