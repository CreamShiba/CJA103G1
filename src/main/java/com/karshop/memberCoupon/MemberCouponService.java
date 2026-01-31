package com.karshop.memberCoupon;


import com.karshop.coupon.CouponRepository;
import com.karshop.memberCoupon.MemberCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberCouponService {

    @Autowired
    private MemberCouponRepository memberCouponRepository;
    @Autowired
    private CouponRepository couponRepository; // 需注入 CouponRepository

    /**
     * 獲取會員擁有的所有優惠券
     * @param memberNo 會員編號
     */
    @Transactional(readOnly = true)
    public List<MemberCoupon> getAllByMember(Integer memberNo) {
        // 取得該會員所有券後，過濾掉主表狀態已失效 (0) 或已過期的券
        return memberCouponRepository.findByMemberNo(memberNo).stream()
                .filter(mc -> mc.getCoupon() != null
                        && mc.getCoupon().getCouponStatus() == 1
                        && mc.getCoupon().getCouponEnd().isAfter(java.time.LocalDateTime.now()))
                .toList();
    }

    /**
     * 獲取會員尚未使用的優惠券
     * @param memberNo 會員編號
     */
    @Transactional(readOnly = true)
    public List<MemberCoupon> getUnusedByMember(Integer memberNo) {
        // 假設狀態 0 代表「未使用」
        return memberCouponRepository.findByMemberNoAndCouponStatus(memberNo, 0);

    }

    /**
     * 更新優惠券狀態（例如：模擬核銷優惠券）
     */
    @Transactional
    public void useCoupon(Integer memberNo, Integer couponNo) {
        MemberCouponId id = new MemberCouponId();
        id.setMemberNo(memberNo);
        id.setCouponNo(couponNo);

        memberCouponRepository.findById(id).ifPresent(mc -> {
            mc.setCouponStatus(1); // 設為已使用
            memberCouponRepository.save(mc);
        });
    }

    @Transactional
    public String claimCouponByName(Integer memberNo, String couponTitle) {
        // 透過名稱尋找優惠券

        var couponOpt = couponRepository.findByCouponTitle(couponTitle);
        if (couponOpt.isEmpty() || couponOpt.get().getCouponStatus() != 1) {
            return "找不到該名稱的有效優惠券或優惠券已失效";
        }

        com.karshop.coupon.Coupon coupon = couponOpt.get();

        // 當前時間已超過結束時間，也不允許領取
        if (coupon.getCouponEnd().isBefore(java.time.LocalDateTime.now())) {
            return "該優惠券已過期，無法領取";
        }

        Integer couponNo = coupon.getCouponNo();

        // 檢查會員是否已經領過
        MemberCouponId id = new MemberCouponId();
        id.setMemberNo(memberNo);
        id.setCouponNo(couponNo);

        if (memberCouponRepository.existsById(id)) {
            return "您已經領取過此優惠券了";
        }

        // 建立領取紀錄
        MemberCoupon newRecord = new MemberCoupon();
        newRecord.setMemberNo(memberNo);
        newRecord.setCouponNo(couponNo);
        newRecord.setCouponStatus(0); // 預設為未使用
        newRecord.setUseTime(coupon.getCouponEnd()); // 設定為優惠券的結束時間

        memberCouponRepository.save(newRecord);
        return "領取成功！";
    }

    public List<MemberCoupon> getExpiredCouponsByMember(Integer memberNo) {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        return memberCouponRepository.findExpiredCoupons(memberNo, now);
    }
}
