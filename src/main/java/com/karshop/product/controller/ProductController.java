package com.karshop.product.controller;

import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import com.karshop.productimage.model.ProductImageService;
import com.karshop.productimage.model.ProductImageVO;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductImageService productImageService;

    @GetMapping("/addProduct")
    public String addProduct(ModelMap model){
        ProductVO productVO = new ProductVO();
        model.addAttribute("productVO",productVO);
        return "seller/addProduct";
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(ModelMap model) {
        Integer sellerNo = 101;

        List<ProductVO> productList = productService.getProductsBySellerNo(sellerNo);

        model.addAttribute("productList", productList);

        return "seller/seller_index";
    }

    @Transactional
    @PostMapping("/insert")
    public String insert(ProductVO productVO, BindingResult result, ModelMap model, @RequestParam("upFile") MultipartFile[] upFile)throws IOException {

        if(upFile[0].isEmpty()){
            model.addAttribute("errorMessage", "請上傳商品圖片");
            return  "seller/addProduct";
        }

        productVO.setProductCategoryNo(3);
        productVO.setSellerNo(101);
        productVO.setProdStatus("上架中");
        productVO.setRatingStar(0);  //評分總星數初始為0
        productVO.setRatingAmount(0); //評分總人數初始為0

        productService.addProduct(productVO);

        for (MultipartFile multipartFile : upFile) {
            byte[] pic = multipartFile.getBytes();

            ProductImageVO piVO = new ProductImageVO();
            piVO.setProduct(productVO);
            piVO.setUpFile(pic);

            productImageService.addImages(piVO);
        }

        return "redirect:/product/dashboard";
    }

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


//
//    public List<ProductVO> getProductsByProdStatus(String prodStatus) {
//        return productService.getProductsByProdStatus(prodStatus);
//    }
//
//    public List<ProductVO> getProductsByNameContainingAndProdStatus(String prodName,String prodStatus){
//        return productService.getProductsByNameContainingAndProdStatus(prodName,prodStatus);
//    }

}
