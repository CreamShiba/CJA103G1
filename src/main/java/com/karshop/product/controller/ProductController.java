package com.karshop.product.controller;


import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import com.karshop.productimage.model.ProductImageService;
import com.karshop.productimage.model.ProductImageVO;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OrdService ordService;

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/addProduct")
    public String addProduct(ModelMap model){
        ProductVO productVO = new ProductVO();
        model.addAttribute("productVO",productVO);
        return "back-end/seller/addProduct";
    }


    @Transactional
    @PostMapping("/insert")
    public String insert(@Valid ProductVO productVO, BindingResult result, ModelMap model, @RequestParam("upFile") MultipartFile[] upFile)throws IOException {

        if (result.hasErrors() || upFile[0].isEmpty()) {
            if(upFile[0].isEmpty()) {
                model.addAttribute("errorMessage", "請上傳商品圖片");
            }
            return  "back-end/seller/addProduct";
        }


        productVO.setProductCategoryNo(3);
        productVO.setSellerNo(101);
//        productVO.setProdStatus("上架中");
//        productVO.setRatingStar(0);  //評分總星數初始為0
//        productVO.setRatingAmount(0); //評分總人數初始為0

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

    @GetMapping("/getOne_For_Update")
    public String getOne_For_Update(@RequestParam Integer prodNo, ModelMap model){
        ProductVO productVO = productService.getOneProduct(prodNo);
        model.addAttribute("productVO",productVO);
        return "back-end/seller/updateProduct";
    }

    @Transactional
    @PostMapping("/update")
    public String update(@Valid ProductVO productVO, BindingResult result, ModelMap model,
                         @RequestParam(value = "deleteImageNos", required = false) String deleteImageNos,
                         @RequestParam("upFile") MultipartFile[] upFile) throws IOException {

        if (result.hasErrors()) {
            return "back-end/seller/updateProduct";
        }

        // 1. 圖片數量檢查
        ProductVO originalProductVO = productService.getOneProduct(productVO.getProdNo());
        int currentAmount = originalProductVO.getProductImage().size();
        int deleteAmount = (deleteImageNos != null && !deleteImageNos.trim().isEmpty()) ? deleteImageNos.split(",").length : 0;
        int newAmount = (upFile != null && !upFile[0].isEmpty()) ? upFile.length : 0;

        if (currentAmount - deleteAmount + newAmount == 0) {
            model.addAttribute("errorMessage", "請至少保留一張圖片");
            model.addAttribute("productVO", originalProductVO);
            return "back-end/seller/updateProduct";
        }

        // 2. 更新商品文字資料
        productService.updateProduct(productVO);

        // 3. 處理刪除舊圖
        if (deleteAmount > 0) {
            String[] ids = deleteImageNos.split(",");
            for (String id : ids) {
                productImageService.deleteImage(Integer.valueOf(id));
            }
        }

        // 4. 處理新圖
        if (newAmount > 0) {
            // 【關鍵點】確保 productVO 內部的 List 已經被 new 出來
            List<ProductImageVO> list = productVO.getProductImage();
            if (list == null) {
                list = new ArrayList<>();
                productVO.setProductImage(list); // 重新塞回 VO 確保兩邊同步
            }

            for (MultipartFile multipartFile : upFile) {
                byte[] pic = multipartFile.getBytes();

                ProductImageVO piVO = new ProductImageVO();
                piVO.setProduct(productVO);
                piVO.setUpFile(pic);

                list.add(piVO);
                productImageService.addImages(piVO);
            }
        } else {
            // 【額外保險】如果沒有新圖，也幫它初始化一個空清單，防止 size() 報錯
            if (productVO.getProductImage() == null) {
                productVO.setProductImage(new ArrayList<>());
            }
        }

        entityManager.flush(); // 強制讓剛剛的刪除和新增生效
        entityManager.clear(); // 清空快取，強迫 JPA 重新去資料庫撈資料


        ProductVO finalProductVO = productService.getOneProduct(productVO.getProdNo());

        // 這裡印出來看看，數量應該會等於 (舊圖 - 刪圖 + 新圖)
        if (finalProductVO != null) {
            System.out.println("最終顯示圖片張數: " + finalProductVO.getProductImage().size());
        }

        model.addAttribute("productVO", finalProductVO);
        return "back-end/seller/listOneProduct";
    }

    @GetMapping("/updateStatus")
    public String updateStatus(@RequestParam(value = "prodNo") Integer prodNo,
                               @RequestParam(value = "prodStatus") String prodStatus){

        ProductVO productVO = productService.getOneProduct(prodNo);
        productVO.setProdStatus(prodStatus);
        productService.updateProduct(productVO);

        return "redirect:/product/dashboard?tab=products";
    }


    @PostMapping("/searchForSeller")
    public String searchForSeller(@RequestParam (value = "keyword")  String keyword, ModelMap model){

        Integer sellerNo = 101;
        List<ProductVO> searchResult;
        if (keyword == null || keyword.trim().isEmpty()) {
            searchResult = productService.getProductsBySellerNo(sellerNo);
        }else {
            searchResult = productService.getProductBySearchForSeller(sellerNo, keyword);
        }

        model.addAttribute("activeTab", "products");
        model.addAttribute("productList", searchResult);
        return "back-end/seller/seller_index";
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(@RequestParam(value = "tab", defaultValue = "dashboard") String tab,
                                  @RequestParam(value = "keyword", required = false) String keyword  ,ModelMap model) {
        Integer sellerNo = 101;

        List<OrdVO> ordList ;
        if (keyword != null && !keyword.trim().isEmpty()) {
            ordList = ordService.searchOrdersForSeller(sellerNo, keyword);
        }else{
            ordList = ordService.getOrdBySeller(sellerNo);
        }

        List<ProductVO> productList = productService.getProductsBySellerNo(sellerNo);

        int activeProductCount = 0;
        for(ProductVO productVO : productList){
            if(productVO.getProdStatus().equals("上架中")){
                activeProductCount++;
            }
        }

        model.addAttribute("activeProductCount", activeProductCount);
        model.addAttribute("ordList", ordList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("productList", productList);
        model.addAttribute("activeTab", tab);

        return "back-end/seller/seller_index";
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
