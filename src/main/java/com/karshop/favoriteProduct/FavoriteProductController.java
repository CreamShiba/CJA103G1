package com.karshop.favoriteProduct;


import com.karshop.members.model.MembersVO;
import com.karshop.productProd.ProductProd;
import com.karshop.utils.LoginUserHolder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/favorite")
public class FavoriteProductController {

    @Autowired
    private FavoriteProductService favoriteProductService;

    @Autowired
    private com.karshop.productProd.ProductImgService productImgService;

    @Autowired
    private LoginUserHolder loginUserHolder;

    //  查看個人收藏清單
    @GetMapping("/list-page")
    public String listPage(Model model, HttpSession session) {

        // 1. 統一從 Session 取得名為 "member" 的物件
        MembersVO member = (MembersVO) session.getAttribute("member");

        // 2. 安全檢查：如果沒登入就踢回登入頁，並帶上 redirect 參數
        if (member == null) {
            return "redirect:/members/login?redirect=/favorite/list-page";
        }

        // 3. 從物件中取出編號
        Integer memberNo = member.getMemId();

        // 4. 抓取資料並回傳
        model.addAttribute("favorites", favoriteProductService.getFavoritesByMember(memberNo));

        return "favorite/listAllFavorite";
    }

    @PostMapping("/addAjax")
    @ResponseBody
    public ResponseEntity<?> addAjax(@RequestParam Integer prodNo) {
        // 1. 使用 LoginUserHolder 取得當前會員
        MembersVO member = loginUserHolder.get();

        // 2. 安全檢查
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "請先登入後再執行收藏！"));
        }

        // 3. 建立關聯並儲存
        ProductProd productProd = new ProductProd();
        productProd.setProdNo(prodNo);

        FavoriteProduct fav = new FavoriteProduct();
        fav.setProductProd(productProd);
        fav.setMemberNo(member.getMemId()); // 從物件中取出 ID

        favoriteProductService.insert(fav);
        return ResponseEntity.ok().body(Map.of("status", "success", "message", "成功加入收藏！"));
    }




    //  刪除收藏
    @PostMapping("/delete")
    public String delete(@RequestParam("memberNo") Integer memberNo,
                         @RequestParam("prodNo") Integer prodNo) {
        favoriteProductService.delete(memberNo, prodNo);
        return "redirect:/favorite/list-page";
    }

    @PostMapping("/deleteAjax")
    @ResponseBody
    public ResponseEntity<?> deleteAjax(@RequestParam Integer prodNo) {
        // 1. 使用 LoginUserHolder 取得當前會員
        MembersVO member = loginUserHolder.get();

        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請先登入"));
        }

        // 2. 執行刪除 (傳入從物件中取出的 memId)
        favoriteProductService.delete(member.getMemId(), prodNo);

        return ResponseEntity.ok().body(Map.of("status", "success", "message", "已取消收藏"));
    }

    @GetMapping("/showImage")
    @ResponseBody
    public ResponseEntity<byte[]> showImage(@RequestParam("prodNo") Integer prodNo) {
        // 1. 根據商品編號取得該商品的所有圖片
        List<com.karshop.productProd.ProductImg> list = productImgService.getByProdNo(prodNo);

        // 2. 檢查是否有圖片資料
        if (list != null && !list.isEmpty()) {
            // 取得第一張圖片作為代表圖
            byte[] imageBytes = list.get(0).getUpFile();

            // 3. 回傳圖片內容與正確的 Content-Type
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG) // 根據你圖片存儲的格式調整，或使用 MediaType.IMAGE_PNG
                    .body(imageBytes);
        }

        // 4. 若無圖片，可回傳 404 或一張預設的圖片
        return ResponseEntity.notFound().build();
    }

    //對應productDetail.html 寫法
    //HTML 指向 /fav/toggle，Controller 類別註解應改為 @RequestMapping("/fav")，或者將 HTML 的路徑改為 /favorite/toggle
    //需重新整理畫面才會顯示收藏變動
    @PostMapping("/toggle")
    public String toggleFavorite(@RequestParam("prodNo") Integer prodNo,
                                 HttpSession session,
                                 jakarta.servlet.http.HttpServletRequest request) {

        // 1. 檢查登入狀態
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null) {
            // 沒登入就導向登入頁
            return "redirect:/members/login";
        }

        Integer memberNo = member.getMemId();

        // 2. 判斷目前是否已收藏
        FavoriteProduct existing = favoriteProductService.getOne(memberNo, prodNo);

        if (existing != null) {
            // 已有資料 -> 移除收藏
            favoriteProductService.delete(memberNo, prodNo);
        } else {
            // 無資料 -> 新增收藏
            FavoriteProduct fav = new FavoriteProduct();
            fav.setMemberNo(memberNo);
            fav.setProdNo(prodNo);

            // 為了符合 JPA 關聯，若實體內有 productProd 欄位可不設或設為空，
            // 因為 insertable = false。但 memberNo 與 prodNo 必須設定。
            favoriteProductService.insert(fav);
        }

        // 3. 取得來源網址，讓使用者回到原商品頁，而不是空頁面
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/product/all");
    }


    //對應productDetail.html 寫法
    //即時渲染畫面，需搭配html、js變動
    @PostMapping("/toggleAjax")
    @ResponseBody
    public ResponseEntity<?> toggleFavoriteAjax(@RequestParam("prodNo") Integer prodNo,
                                                HttpSession session) {

        // 1. 取得登入會員資訊
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
        }

        Integer memberNo = member.getMemId();
        // 2. 檢查資料庫中是否已有該收藏紀錄
        FavoriteProduct existing = favoriteProductService.getOne(memberNo, prodNo);

        boolean isFavoriteNow; // 修正後的變數名

        if (existing != null) {
            // 原本已收藏 -> 執行刪除
            favoriteProductService.delete(memberNo, prodNo);
            isFavoriteNow = false;
        } else {
            // 原本未收藏 -> 執行新增
            FavoriteProduct fav = new FavoriteProduct();
            fav.setMemberNo(memberNo);
            fav.setProdNo(prodNo);
            favoriteProductService.insert(fav);
            isFavoriteNow = true;
        }

        // 3. 回傳最新的狀態與訊息給前端
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "isFavorite", isFavoriteNow,
                "message", isFavoriteNow ? "已加入收藏" : "已取消收藏"
        ));
    }

}