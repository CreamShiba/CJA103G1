package com.karshop.product.controller;


import com.karshop.carcategory.model.CarCategoryService;
import com.karshop.carcategory.model.CarCategoryVO;
import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import com.karshop.productcategorytest.model.ProductCategoryService;
import com.karshop.productcategorytest.model.ProductCategoryVO;
import com.karshop.productimage.model.ProductImageService;
import com.karshop.productimage.model.ProductImageVO;
import com.karshop.reporttest.model.ReportRepository;
import com.karshop.reporttest.model.ReportVO;
import com.karshop.sellertest.model.SellerVO;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
    private ProductCategoryService productCategoryService;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private RedisTemplate<String, byte[]> imageRedisTemplate;

    @Autowired
    private CarCategoryService carCategoryService;

    @Autowired
    private EntityManager entityManager;

    @GetMapping("/addProduct")
    public String addProduct(ModelMap model,
                             @SessionAttribute(value = "seller", required = false) SellerVO sellerVO){

        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }

        ProductVO productVO = new ProductVO();
        model.addAttribute("productVO",productVO);

        return "back-end/seller/addProduct";
    }


    @Transactional
    @PostMapping("/insert")
    public String insert(@Valid ProductVO productVO, BindingResult result, ModelMap model,
                         @RequestParam("upFile") MultipartFile[] upFile,
                         @SessionAttribute(value = "seller", required = false) SellerVO sellerVO )throws IOException {

        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }

        if (result.hasErrors() || upFile[0].isEmpty()) {
            if(upFile[0].isEmpty()) {
                model.addAttribute("errorMessage", "請上傳商品圖片");
            }
            return  "back-end/seller/addProduct";
        }

        productVO.setSeller(sellerVO);

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
    public String getOne_For_Update(@RequestParam Integer prodNo, ModelMap model,
                                    @SessionAttribute(value = "seller", required = false) SellerVO sellerVO){
        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }

        ProductVO productVO = productService.getOneProduct(prodNo);

//      檢查是否為該賣家的商品
        if(!productVO.getSeller().getSellerNo().equals(sellerVO.getSellerNo())){
            return "redirect:/product/dashboard";
        }

        model.addAttribute("productVO",productVO);

        if("違規下架".equals(productVO.getProdStatus())){
            ReportVO violationReport = reportRepository.findTopByProductProdNoOrderByReportTimeDesc(prodNo);
            if(violationReport != null){
                model.addAttribute("violationReport",violationReport);
            }
        }
        return "back-end/seller/updateProduct";
    }

    @Transactional
    @PostMapping("/update")
    public String update(@Valid ProductVO productVO, BindingResult result, ModelMap model,
                         @RequestParam(value = "deleteImageNos", required = false) String deleteImageNos,
                         @RequestParam("upFile") MultipartFile[] upFile,
                         @SessionAttribute(value = "seller", required = false) SellerVO sellerVO) throws IOException {

        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }
        productVO.setSeller(sellerVO);

        // 檢查是否為該賣家的商品
        ProductVO checkOwner = productService.getOneProduct(productVO.getProdNo());
        if (!checkOwner.getSeller().getSellerNo().equals(sellerVO.getSellerNo())) {
            return "redirect:/product/dashboard";
        }

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

        if("違規下架".equals(productVO.getProdStatus())){
            productVO.setProdStatus("審核中");
        }

        // 2. 更新商品文字資料
        productService.updateProduct(productVO);

        // 3. 處理刪除舊圖
        if (deleteAmount > 0) {
            String[] ids = deleteImageNos.split(",");
            for (String id : ids) {
                productImageService.deleteImage(Integer.valueOf(id));

                String redisKey = "product:img:" + id;
                imageRedisTemplate.delete(redisKey);
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

        if(deleteAmount > 0 || newAmount > 0){
            String redisKey = "product:main:" + productVO.getProdNo();
            imageRedisTemplate.delete(redisKey);
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
                               @RequestParam(value = "prodStatus") String prodStatus,
                               @SessionAttribute(value = "seller", required = false) SellerVO sellerVO){
        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }

        ProductVO productVO = productService.getOneProduct(prodNo);

        if (!productVO.getSeller().getSellerNo().equals(sellerVO.getSellerNo())) {
            System.out.println("賣家 " + sellerVO.getSellerNo() + " 試圖操作別人的商品 " + prodNo);
            return "redirect:/product/dashboard";
        }

        productVO.setProdStatus(prodStatus);
        productService.updateProduct(productVO);

        return "redirect:/product/dashboard?tab=products";
    }


    @GetMapping("/searchForSeller")
    public String searchForSeller(@RequestParam(value = "searchName", required = false)  String searchName,
                                  @RequestParam(value = "minPrice", required = false) Integer minPrice,
                                  @RequestParam(value = "maxPrice", required = false) Integer maxPrice,
                                  @RequestParam(value = "searchStatus", required = false) String searchStatus,
                                  ModelMap model,
                                  @SessionAttribute(value = "seller", required = false) SellerVO sellerVO){
        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }
        Integer sellerNo = sellerVO.getSellerNo();
        prepareSellerDashboardData(model, sellerNo);
        List<ProductVO> searchResult = productService.getProductBySearchForSeller(sellerNo, searchName, minPrice, maxPrice, searchStatus);

        model.addAttribute("activeTab", "products");
        model.addAttribute("productList", searchResult);
//      保留輸入傳回前端
        model.addAttribute("searchName", searchName);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("searchStatus", searchStatus);
        model.addAttribute("activeTab", "products");

        return "back-end/seller/seller_index";
    }

    @GetMapping("/dashboard")
    public String sellerDashboard(@RequestParam(value = "tab", defaultValue = "dashboard") String tab, ModelMap model,
                                  @SessionAttribute(value = "seller", required = false) SellerVO sellerVO) {
        if(sellerVO == null){
            return "redirect:/members/seller/apply";
        }
        if(!"已開通".equals(sellerVO.getSellerStatus())){
            return "redirect:/members/seller/sellerinfo";
        }
        prepareSellerDashboardData(model, sellerVO.getSellerNo());
        model.addAttribute("activeTab", tab);

        return "back-end/seller/seller_index";
    }

    private void prepareSellerDashboardData(ModelMap model, Integer sellerNo) {
        model.addAttribute("sellerNo", sellerNo);
        List<OrdVO> ordList = ordService.getOrdBySeller(sellerNo);
        int pendingOrder = 0;
        int allOrder = 0;

        for (OrdVO ordVO : ordList) {
            String status = ordVO.getOrdStatus();
            if (status.equals("待出貨")) {
                pendingOrder++;
            }
            if (status.equals("待出貨") || status.equals("已完成") || status.equals("已出貨")) {
                allOrder++;
            }
        }
        model.addAttribute("pendingOrder", pendingOrder);
        model.addAttribute("allOrder", allOrder);

        model.addAttribute("ordList", ordList);

        List<ProductVO> productList = productService.getProductsBySellerNo(sellerNo);
        model.addAttribute("productList", productList);
        int activeProductCount = 0;
        for(ProductVO productVO : productList){
            if(productVO.getProdStatus().equals("上架中")){
                activeProductCount++;
            }
        }
        model.addAttribute("activeProductCount", activeProductCount);
    }

    @ModelAttribute("categoryList")
    public List<ProductCategoryVO> populateProductCategories() {
        //   側邊攔、下拉選單
        return productCategoryService.getAll();
    }

    @ModelAttribute("carCategoryList")
    public List<CarCategoryVO> populateCarCategories() {
        return carCategoryService.getAllCarCategories();
    }

}
