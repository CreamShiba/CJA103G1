// 新增一個 @Controller 類別來提供 HTML 頁面
        package com.karshop.memberCoupon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pages")
public class PageController {

    /**
     * 提供優惠券頁面
     *  http://localhost:8080/pages/my-coupons
     */
    @GetMapping("/my-coupons")
    public String myCouponsPage() {

        return "memberCoupon/MemberCouponQuery"; // 返回 templates/MemberCouponQuery.html
    }

    @GetMapping("/front-end/index")
    public String index() {
        return "front-end/index";
    }

    @GetMapping("/my-orders")
    public String myOrdersPage() {
        //  templates/order/MemberOrderQuery.html
        return "order/MemberOrderQuery";
    }




}