package com.karshop.product;

import com.karshop.membertest.model.MemberVO;
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

//      先查session有沒有seller
        SellerVO seller = (SellerVO) session.getAttribute("seller");
        if(seller != null){
            return true; //pass
        }
//      從session拿member查
        MemberVO member = (MemberVO) session.getAttribute("member");
        if(member != null){
            seller = sellerService.findByMemberNo(member.getMemberNo());

            if(seller != null){
                session.setAttribute("seller", seller);
                return true; //pass
            }else{
                res.sendRedirect(req.getContextPath() + "/seller/register");
                return false; //Block
            }
        }
        // 沒登入 -> 導向會員登入頁面
        // 記錄他原本想去的頁面，登入後再導回來
        session.setAttribute("location", req.getRequestURI());
        res.sendRedirect(req.getContextPath() + "/members/login");
        return false; //Block
    }
}
