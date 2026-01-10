package com.karshop.product.controller;

import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;

import java.util.List;


@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

//    @GetMapping("/getAll")
//    public List<ProductVO> getAllProducts() {
//        return productService.getAllProducts();
//    }
//
//    @GetMapping("/getOne/{productNo}")
//    public ProductVO getOneProduct(@PathVariable Integer productNo) {
//        return productService.getOneProduct(productNo);
//    }
//
//
//    @PostMapping("/addProduct")
//    public void addProduct(@RequestBody ProductVO productVO) {
//        productService.addProduct(productVO);
//    }
//
//    @PutMapping
//    public void UpdateProduct(@RequestBody ProductVO productVO) {
//        productService.updateProduct(productVO);
//    }
//
//    @DeleteMapping
//    public void deleteProduct(Integer prodNo) {
//        productService.deleteProduct(prodNo);
//    }

    @GetMapping("/dashboard")
    public String sellerDashboard(ModelMap model) {
        Integer sellerNo = 101;

        List<ProductVO> productList = productService.getProductsBySellerNo(sellerNo);

        model.addAttribute("productList", productList);

        return "seller/seller_index";
    }
//
//    public List<ProductVO> getProductsByProdStatus(String prodStatus) {
//        return productService.getProductsByProdStatus(prodStatus);
//    }
//
//    public List<ProductVO> getProductsByNameContainingAndProdStatus(String prodName,String prodStatus){
//        return productService.getProductsByNameContainingAndProdStatus(prodName,prodStatus);
//    }

}
