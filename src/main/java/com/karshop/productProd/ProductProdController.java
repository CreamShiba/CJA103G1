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

    @Autowired
    private ProductProdRepository productProdRepository;

    @GetMapping("/list-page")
    public String listAllProduct(Model model, HttpSession session) {
        // 【測試用】手動設定 Session，模擬 ID 為 1 的會員已登入
        session.setAttribute("memberNo", 1);

        // 取得所有商品
        model.addAttribute("products", productProdRepository.findAll());

        return "product/listAllProduct";
    }
}
