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
        couponRepository.updateExpiredStatus(now);
        System.out.println("排程執行：已將過期優惠券狀態更新為失效。時間：" + now);
    }
}
