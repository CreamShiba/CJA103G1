// 新增一個 @Controller 類別來提供 HTML 頁面
        package com.karshop.memberCoupon;

import com.karshop.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.karshop.utils.LoginUserHolder;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/pages")
public class PageController {
    @Autowired
    private LoginUserHolder loginUserHolder;

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
    public String myOrdersPage(Model model, HttpSession session) {
        System.out.println("==========================================");
        System.out.println("✅✅✅ 開始處理訂單頁面請求");
        System.out.println("請求路徑: /pages/my-orders");
        System.out.println("時間: " + java.time.LocalDateTime.now());

        MembersVO member = loginUserHolder.get();
        System.out.println("會員資訊: " + (member != null ? "會員編號 " + member.getMemNo() : "null"));

        if (member == null) {
            System.out.println("❌ 會員為 null，重定向到登入頁");
            System.out.println("==========================================");
            return "redirect:/members/login";
        }

        model.addAttribute("member", member);
        session.setAttribute("member", member);

        String templatePath = "order/MemberOrderQuery";
        System.out.println("✅ 會員驗證通過");
        System.out.println("返回模板路徑: " + templatePath);
        System.out.println("完整路徑應為: templates/order/MemberOrderQuery.html");
        System.out.println("==========================================");

        return templatePath;
    }


}