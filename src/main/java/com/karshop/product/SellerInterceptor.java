package com.karshop.product;

import com.karshop.members.model.MembersVO;
import com.karshop.sellertest.model.SellerService;
import com.karshop.sellertest.model.SellerVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SellerInterceptor implements HandlerInterceptor {
    @Autowired
    private SellerService sellerService;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        HttpSession session = req.getSession();

        // 先查 Session 有沒有 seller
        SellerVO seller = (SellerVO) session.getAttribute("seller");

        // 如果 Session 沒有，用memberNo查
        if (seller == null) {
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member != null) {
                seller = sellerService.findByMemberNo(member.getMemberNo());
                if (seller != null) {
                    session.setAttribute("seller", seller); // 撈到了就補回 Session
                }
            }
        }

        // 沒有賣家身分 -> 導向流程
        if (seller == null) {
            MembersVO member = (MembersVO) session.getAttribute("member");
            if (member == null) {
                // (A) 連會員都沒登入 -> 去登入頁
                session.setAttribute("location", req.getRequestURI());
                res.sendRedirect(req.getContextPath() + "/members/login");
            } else {
                // (B) 有登入會員，但還不是賣家 -> 去申請頁
                res.sendRedirect(req.getContextPath() + "/members/seller/apply");
            }
            return false; // block
        }

        String status = seller.getSellerStatus();
        String uri = req.getRequestURI();

        // (A) 如果是「已開通」，恭喜！放行所有功能 ✅
        if ("已開通".equals(status)) {
            return true; //pass
        }

        // 其他狀態(待審核、未通過、停權)訪問「賣家資料頁」和「更新資料」
        if (uri.contains("/members/seller/sellerinfo") || uri.contains("/members/seller/update")) {
            return true; // 允許訪問個人資料頁
        }

        // 其他所有頁面 (例如: 新增商品、儀表板、訂單管理)
        System.out.println(" 攔截非開通賣家 (" + status + ") 嘗試訪問: " + uri);
        res.sendRedirect(req.getContextPath() + "/members/seller/sellerinfo");
        return false; // block
    }
}