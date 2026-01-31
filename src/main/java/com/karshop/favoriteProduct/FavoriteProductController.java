package com.karshop.favoriteProduct;


import com.karshop.productProd.ProductProd;
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

    //  查看個人收藏清單
    @GetMapping("/list-page")
    public String listPage(Model model, HttpSession session) {
        // 1. 模擬登入 (僅用於開發測試)
        if (session.getAttribute("memberNo") == null) {
            session.setAttribute("memberNo", 1);
        }

        // 2. 安全檢查
        Integer memberNo = (Integer) session.getAttribute("memberNo");
        if (memberNo == null) {
            return "redirect:/member/login";
        }

        // 3. 同時抓取兩份資料
        model.addAttribute("favorites", favoriteProductService.getFavoritesByMember(memberNo));

        return "favorite/listAllFavorite";
    }

    // AJAX 新增 API
    @PostMapping("/addAjax")
    @ResponseBody
    public ResponseEntity<?> addAjax(@RequestParam Integer prodNo, HttpSession session) {
        // 1. 從 Session 中取得登入的會員資訊

        Integer memberNo = (Integer) session.getAttribute("memberNo");

        // 2. 安全檢查：如果 Session 中沒有編號，代表未登入
        if (memberNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "請先登入後再執行收藏！"));
        }

        // 3. 建立關聯並儲存
        ProductProd productProd = new ProductProd();
        productProd.setProdNo(prodNo);

        FavoriteProduct fav = new FavoriteProduct();
        fav.setProductProd(productProd);
        fav.setMemberNo(memberNo);

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
    public ResponseEntity<?> deleteAjax(@RequestParam Integer prodNo, HttpSession session) {
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        if (memberNo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "請先登入"));
        }

        // 執行刪除
        favoriteProductService.delete(memberNo, prodNo);

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

}