package com.karshop.backstage.controller;


import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/audit")
    public String listAuditProducts(ModelMap model){
       List<ProductVO> productList =  productService.getProductsByProdStatus("審核中");
       model.addAttribute("productList",productList);
       model.addAttribute("activePage", "productAudit");
       return "back-end/admin/product_audit";
    }

    @PostMapping("approve")
    public String approveProduct(@RequestParam("prodNo") Integer prodNo, RedirectAttributes redirectAttributes) {
        ProductVO productVO = productService.getOneProduct(prodNo);
        productVO.setProdStatus("上架中");
        productService.updateProduct(productVO);

        redirectAttributes.addFlashAttribute("successMessage", "商品 #" + prodNo + "已重新上架!");
        return "redirect:/admin/products/audit";
    }

    @PostMapping("reject")
    public String rejectProduct(@RequestParam("prodNo") Integer prodNo, RedirectAttributes redirectAttributes) {
        ProductVO productVO = productService.getOneProduct(prodNo);
        productVO.setProdStatus("違規下架");
        productService.updateProduct(productVO);
        redirectAttributes.addFlashAttribute("errorMessage", "商品 #" + prodNo + " 審核未通過，已退回！");
        return "redirect:/admin/products/audit";
    }

}
