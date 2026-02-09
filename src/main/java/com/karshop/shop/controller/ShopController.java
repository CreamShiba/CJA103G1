package com.karshop.shop.controller;

import com.karshop.favoriteProduct.FavoriteProduct;
import com.karshop.favoriteProduct.FavoriteProductService;
import com.karshop.favoriteStore.FavoriteStoreService;
import com.karshop.membercar.model.MemberCarService;
import com.karshop.membercar.model.MemberCarVO;
import com.karshop.members.model.MembersVO;
import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import com.karshop.productcategorytest.model.ProductCategoryService;
import com.karshop.productcategorytest.model.ProductCategoryVO;
import com.karshop.sellertest.model.SellerService;
import com.karshop.sellertest.model.SellerVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ShopController {
    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private SellerService sellerService;

    @Autowired
    private MemberCarService memberCarService;

    @Autowired
    private FavoriteProductService favoriteProductService; // 收藏 Service

    @Autowired
    private FavoriteStoreService favoriteStoreService; // 收藏 Service

    //  商城首頁
    @GetMapping("/shop")
    public String getAllForBuyer(@RequestParam(defaultValue = "1") int page ,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Integer productCategoryNo,
                                 @RequestParam(required = false) Integer carCategoryNo,
                                 @RequestParam(required = false) Integer sellerNo,
                                 @RequestParam(required = false) Integer minPrice,
                                 @RequestParam(required = false) Integer maxPrice, ModelMap model,
                                 HttpSession session) {

        Page<ProductVO> productPage = productService.getAllForBuyer(page, keyword, productCategoryNo, carCategoryNo, sellerNo, minPrice, maxPrice);

        model.addAttribute("productPage", productPage);
        model.addAttribute("currentPage", page);

        if (sellerNo != null) {
            SellerVO currentSeller = sellerService.getOneSeller(sellerNo);
            model.addAttribute("currentSeller", currentSeller);
        }

//      搜尋條件存回去
        model.addAttribute("keyword", keyword);
        model.addAttribute("productCategoryNo", productCategoryNo);
        model.addAttribute("carCategoryNo", carCategoryNo);
        model.addAttribute("sellerNo", sellerNo);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        List<ProductCategoryVO> categoryList = productCategoryService.getAll();
        model.addAttribute("categoryList", categoryList);

         MembersVO member = (MembersVO) session.getAttribute("member");
         Integer memberNo = (member != null) ? member.getMemberNo() : null;

        if (memberNo != null) {
             List<MemberCarVO> myCars = memberCarService.getCarsByMemberId(memberNo);
             model.addAttribute("myCars", myCars);
        }

        // --- 收藏賣場判斷邏輯開始 ---
        boolean isFavoriteStore = false; // 預設為未收藏

        if (sellerNo != null) {
            SellerVO currentSeller = sellerService.getOneSeller(sellerNo);
            model.addAttribute("currentSeller", currentSeller);

            // 如果會員已登入，檢查是否已收藏此賣場
            if (member != null) {
                isFavoriteStore = favoriteStoreService.isExists(member.getMemberNo(), sellerNo);
            }
        }
        // 將收藏狀態傳給 index2.html
        model.addAttribute("isFavorite", isFavoriteStore);
        // --- 收藏賣場判斷邏輯結束 ---

        return "front-end/index2";
    }

    //    商品詳情頁
    @GetMapping("/product/detail")
    public String getOneProduct(@RequestParam(value = "prodNo")  Integer prodNo, ModelMap model, HttpSession session) {
        ProductVO productVO = productService.getOneProduct(prodNo);
        model.addAttribute("productVO", productVO);

        List<ProductCategoryVO> categoryList = productCategoryService.getAll();
        model.addAttribute("categoryList", categoryList);

        // 判斷收藏狀態
        boolean isFavorite = false;

        // 從 Session 取得會員資料
        MembersVO member = (MembersVO) session.getAttribute("member");

        if (member != null) {
            // 如果已登入，去資料庫查詢是否有收藏紀錄
            FavoriteProduct existing = favoriteProductService.getOne(member.getMemNo(), prodNo);
            isFavorite = (existing != null);
            List<MemberCarVO> myCars = memberCarService.getCarsByMemberId(member.getMemNo());
            model.addAttribute("myCars", myCars);
        }

        // 將狀態傳遞給前端 Thymeleaf
        model.addAttribute("isFavorite", isFavorite);

        return "front-end/product/productDetail";
    }
}
