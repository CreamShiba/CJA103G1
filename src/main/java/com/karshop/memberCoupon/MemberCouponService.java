package com.karshop.memberCoupon;


import com.karshop.coupon.CouponRepository;
import com.karshop.memberCoupon.MemberCouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberCouponService {

    @Autowired
    private MemberCouponRepository memberCouponRepository;
    @Autowired
    private CouponRepository couponRepository;

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
                        && mc.getCoupon().getCouponEnd().isAfter(LocalDateTime.now()))
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
        if (coupon.getCouponEnd().isBefore(LocalDateTime.now())) {
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
        LocalDateTime now = LocalDateTime.now();

        return memberCouponRepository.findExpiredCoupons(memberNo, now);
    }

    //訂單結帳套用優惠券
    @Transactional(readOnly = true)
    public MemberCoupon validateAndGetCoupon(Integer memberNo, Integer couponNo, Double orderAmount) {
        MemberCouponId id = new MemberCouponId();
        id.setMemberNo(memberNo);
        id.setCouponNo(couponNo);

        return memberCouponRepository.findById(id)
                .filter(mc -> mc.getCouponStatus() == 0) // 必須是未使用
                .map(mc -> {
                    var coupon = mc.getCoupon();
                    // 1. 檢查優惠券主表狀態與有效期限
                    if (coupon == null || coupon.getCouponStatus() != 1) {
                        throw new RuntimeException("優惠券已失效");
                    }
                    if (coupon.getCouponEnd().isBefore(LocalDateTime.now())) {
                        throw new RuntimeException("優惠券已過期");
                    }

                    // 2. 核心需求：檢查優惠券金額是否超過訂單 20%
                    // 使用你提供的 discountValue 欄位
                    double maxDiscountAllowed = orderAmount * 0.2;
                    if (coupon.getDiscountValue() > maxDiscountAllowed) {
                        throw new RuntimeException("不符合使用條件：此券折扣金額 (" + coupon.getDiscountValue() +
                                ") 超過訂單金額的 20% (" + (int)maxDiscountAllowed + ")");
                    }

                    return mc;
                })
                .orElseThrow(() -> new RuntimeException("找不到該優惠券或已被使用"));
    }
}
