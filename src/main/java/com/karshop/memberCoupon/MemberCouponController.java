package com.karshop.memberCoupon;

import com.karshop.members.model.MembersVO;
import com.karshop.utils.LoginUserHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/coupons")
public class MemberCouponController {

    @Autowired
    private MemberCouponService memberCouponService;
    @Autowired
    private LoginUserHolder loginUserHolder;

    /**
     * 查詢當前會員的所有優惠券
     */
    @GetMapping("/my-coupons")
    public ResponseEntity<List<MemberCoupon>> getMyCoupons() {
        MembersVO member = loginUserHolder.get();
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<MemberCoupon> coupons = memberCouponService.getAllByMember(member.getMemNo());
        return coupons.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(coupons);
    }

    /**
     * 查詢指定會員「未使用」的折價券 (status = 0)
     */
    @GetMapping("/unused") // 移除 {memberNo}，改用 Session 判斷
    public ResponseEntity<List<MemberCoupon>> getUnusedCoupons() {

        // 取得當前登入的會員物件
        MembersVO member = loginUserHolder.get();

        // 未登入則回傳 401
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 呼叫 Service，傳入從 Session 取得的 ID
        List<MemberCoupon> unusedCoupons = memberCouponService.getUnusedByMember(member.getMemNo());

        if (unusedCoupons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(unusedCoupons);
    }

    // 取得「已過期/已使用」優惠券
    @GetMapping("/expired")
    public ResponseEntity<List<MemberCoupon>> getExpiredCoupons() {
        MembersVO member = loginUserHolder.get();
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<MemberCoupon> expiredCoupons = memberCouponService.getExpiredCouponsByMember(member.getMemNo());
        return expiredCoupons.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(expiredCoupons);
    }

    // 領取優惠券
    @PostMapping("/claim")
    public ResponseEntity<String> claimCoupon(@RequestParam("couponTitle") String couponTitle) {
        MembersVO member = loginUserHolder.get();
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入");

        String result = memberCouponService.claimCouponByName(member.getMemNo(), couponTitle);
        return "領取成功！".equals(result) ? ResponseEntity.ok(result) : ResponseEntity.badRequest().body(result);
    }

//     取得當前購物車金額下「可使用」的優惠券清單
//    @GetMapping("/available")
//    public ResponseEntity<List<MemberCoupon>> getAvailableCoupons(@RequestParam Double orderAmount) {
//        // 邏輯：status=0 && 尚未過期 && 訂單金額 >= 優惠券門檻
//    }

//    前端 checkout.html 按下「套用」時，透過 AJAX 詢問後端這張券能抵扣多少錢
    @GetMapping("/validate-usage")
    public ResponseEntity<?> validateCouponUsage(
            @RequestParam Integer couponNo,
            @RequestParam Integer orderAmount) {

        MembersVO member = loginUserHolder.get(); //
        if (member == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("請先登入");

        try {
            Integer discount = memberCouponService.validateAndCalculateDiscount(
                    member.getMemNo(), couponNo, orderAmount);
            // 如果通過，回傳折扣金額
            return ResponseEntity.ok(discount);
        } catch (RuntimeException e) {
            // 如果失敗（例如超過 20%），回傳錯誤訊息
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


}
