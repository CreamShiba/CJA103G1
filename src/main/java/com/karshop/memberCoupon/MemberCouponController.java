package com.karshop.memberCoupon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/member/coupons")
public class MemberCouponController {

    @Autowired
    private MemberCouponService memberCouponService;

    /**
     * 查詢指定會員擁有的所有折價券
     * GET http://localhost:8080/member/coupons/{memberNo}
     */
    @GetMapping("/{memberNo}")
    public ResponseEntity<List<MemberCoupon>> getMemberCoupons(@PathVariable Integer memberNo) {
        List<MemberCoupon> coupons = memberCouponService.getAllByMember(memberNo);

        if (coupons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(coupons);
    }

    /**
     * 查詢當前會員的所有優惠券
     * GET http://localhost:8080/member/coupons/my-coupons?memberNo=1
     */
    @GetMapping("/my-coupons")
    public ResponseEntity<List<MemberCoupon>> myCouponsPage(@RequestParam Integer memberNo) {
        List<MemberCoupon> coupons = memberCouponService.getAllByMember(memberNo);

        if (coupons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(coupons);
    }

    /**
     * 查詢指定會員「未使用」的折價券 (status = 0)
     * GET http://localhost:8080/member/coupons/unused/{memberNo}
     */
    @GetMapping("/unused/{memberNo}")
    public ResponseEntity<List<MemberCoupon>> getUnusedCoupons(@PathVariable Integer memberNo) {
        // 呼叫 Service 取得狀態為 0 的優惠券
        List<MemberCoupon> unusedCoupons = memberCouponService.getUnusedByMember(memberNo);

        if (unusedCoupons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(unusedCoupons);
    }

    @GetMapping("/expired/{memberNo}")
    public ResponseEntity<List<MemberCoupon>> getExpiredCoupons(@PathVariable Integer memberNo) {
        List<MemberCoupon> expiredCoupons = memberCouponService.getExpiredCouponsByMember(memberNo);

        if (expiredCoupons.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(expiredCoupons);
    }


    /**
     * 會員領取優惠券
     */
    @PostMapping("/claim")
    public ResponseEntity<String> claimCoupon(
            @RequestParam Integer memberNo,
            @RequestParam String couponTitle) {

        String result = memberCouponService.claimCouponByName(memberNo, couponTitle);

        if ("領取成功！".equals(result)) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.badRequest().body(result);
    }


}
