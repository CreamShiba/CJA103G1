package com.karshop.coupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CouponScheduler {

    @Autowired
    private CouponRepository couponRepository;

    // 每1 min執行一次檢查
    @Scheduled(cron = "0 0/1 * * * *")
    public void checkExpiredCoupons() {
        LocalDateTime now = LocalDateTime.now();

        // 記錄日誌以便追蹤排程是否有啟動
        System.out.println("開始掃描過期優惠券，當前時間: " + now);

        couponRepository.updateExpiredStatus(now);
        couponRepository.updateExpiredMemberCoupons(now);
        // 2. 處理新啟用的 (0 -> 1)
        couponRepository.startFutureCoupons(now);

        System.out.println("排程執行完畢。");

        System.out.println("排程執行完成，已同步更新主表與會員持有表之過期狀態。");
    }
}
