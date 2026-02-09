package com.karshop.memberCoupon;


import com.karshop.coupon.Coupon;
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
        return memberCouponRepository.findByMemberNoWithCoupon(memberNo);
        // 取得該會員所有券後，過濾掉主表狀態已失效 (0) 或已過期的券
//        return memberCouponRepository.findByMemberNo(memberNo).stream()
//                .filter(mc -> mc.getCoupon() != null
//                        && mc.getCoupon().getCouponStatus() == 1
//                        && mc.getCoupon().getCouponEnd().isAfter(LocalDateTime.now()))
//                .toList();
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
    @Transactional// 確保事務完整性
    public void useCoupon(Integer memberNo, Integer couponNo) {
        MemberCouponId id = new MemberCouponId();
        id.setMemberNo(memberNo);
        id.setCouponNo(couponNo);
        MemberCoupon memberCoupon = memberCouponRepository.findById(id).orElse(null);

        if (memberCoupon != null) {
            // 額外判斷：必須是未使用(0) 且 當下時間尚未過期
            // 注意：這需要 MemberCoupon 實體中有關聯 Coupon 或持有到期時間資訊
            if (memberCoupon.getCouponStatus() == 0 &&
                    LocalDateTime.now().isBefore(memberCoupon.getUseTime())) { // 此時的 useTime 仍是 coupon_end

                memberCoupon.setCouponStatus(1); // 變更為已使用
                memberCoupon.setUseTime(LocalDateTime.now()); // 覆蓋為實際使用時間
                memberCouponRepository.save(memberCoupon);
            } else {
                throw new RuntimeException("優惠券已失效或已使用");
            }
        }
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
    public Integer validateAndCalculateDiscount(Integer memberNo, Integer couponNo, Integer currentTotal) {
        if (couponNo == null) return 0;

        // 1. 取得會員優惠券關聯與主表資訊
        MemberCouponId id = new MemberCouponId();
        id.setMemberNo(memberNo);
        id.setCouponNo(couponNo);

        MemberCoupon mc = memberCouponRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到此優惠券"));

        // 2. 基礎校驗：是否使用過、是否過期
        if (mc.getCouponStatus() != 0) {
            throw new RuntimeException("此優惠券已使用或已失效");
        }

        Coupon coupon = mc.getCoupon();
        if (coupon == null || coupon.getCouponStatus() != 1) {
            throw new RuntimeException("優惠券活動已結束");
        }

        if (coupon.getCouponEnd().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("優惠券已過期");
        }

        // 3. 核心需求：檢查優惠券金額是否超過訂單金額的 20%
        // 小計 + 運費 = currentTotal
        Integer discountValue = coupon.getDiscountValue();
        double limit = currentTotal * 0.2;

        if (discountValue > limit) {
            throw new RuntimeException("不符合使用條件：折扣金額 ($" + discountValue +
                    ") 超過訂單總額的 20% ($" + (int)limit + ")");
        }

        return discountValue;
    }

    /**
     * 結帳完成後，標記優惠券為已使用 (供 cartservice 呼叫)
     */
    @Transactional
    public void markAsUsed(Integer memberNo, Integer couponNo) {
        MemberCouponId id = new MemberCouponId();
        id.setMemberNo(memberNo);
        id.setCouponNo(couponNo);
        memberCouponRepository.findById(id).ifPresent(mc -> {
            mc.setCouponStatus(1); // 1: 已使用
            mc.setUseTime(LocalDateTime.now());
            memberCouponRepository.save(mc);
        });
    }

    /**
     * 獲取會員目前可使用的優惠券 (結帳下拉選單用)
     * 條件：member_coupon.coupon_status = 0 (未使用)
     * 且 coupon.coupon_status = 1 (有效)
     * 且 coupon.coupon_end > 現在時間
     */
    @Transactional(readOnly = true)
    public List<MemberCoupon> getAvailableCoupons(Integer memberNo) {
        return memberCouponRepository.findByMemberNoWithCoupon(memberNo).stream()
                .filter(mc -> mc.getCouponStatus() == 0) // 會員券狀態：未使用
                .filter(mc -> mc.getCoupon() != null
                        && mc.getCoupon().getCouponStatus() == 1 // 優惠券主表：有效
                        && mc.getCoupon().getCouponEnd().isAfter(LocalDateTime.now())) // 未過期
                .toList();
    }

}
