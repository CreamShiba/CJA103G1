package com.karshop.favoriteProduct;

import com.product.Product;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/favorite")
public class FavoriteProductController {

    @Autowired
    private FavoriteProductService favoriteProductService;

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
        Product product = new Product();
        product.setProdNo(prodNo);

        FavoriteProduct fav = new FavoriteProduct();
        fav.setProduct(product);
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
}