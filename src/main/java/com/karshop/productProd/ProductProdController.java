package com.karshop.productProd;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ProductProdController {

    // 假設您已經建立 ProductRepository
    @Autowired
    private ProductProdRepository productProdRepository;

    @GetMapping("/list-page") // 假設這是您的商品列表路徑
    public String listAllProduct(Model model, HttpSession session) {
        // 【測試用】手動設定 Session，模擬 ID 為 1 的會員已登入
        session.setAttribute("memberNo", 1);

        // 原有的邏輯：取得所有商品
        model.addAttribute("products", productProdRepository.findAll());

        return "product/listAllProduct"; // 返回您的商城頁面
    }
}
