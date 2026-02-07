package com.karshop.favoriteStore;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/favoriteStore")
public class FavoriteStoreController {

    @Autowired
    private FavoriteStoreService favoriteStoreService;

    // 1. 頁面跳轉：顯示該會員所有的收藏賣家
    @GetMapping("/list")
    public String listAllFavorites(HttpSession session, Model model) {
        // 從 Session 獲取登入會員編號 (假設 Key 為 memberNo)
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        if (memberNo == null) {
            return "redirect:/member/login"; // 未登入導向登入頁面
        }

        List<FavoriteStore> list = favoriteStoreService.getFavoritesByMember(memberNo);
        model.addAttribute("favoriteStores", list);
        return "backend/favorite/listStore"; // 指向您的 Thymeleaf 頁面
    }

    // 2. AJAX 新增收藏 (給前端按鈕使用)
    @PostMapping("/addAjax")
    @ResponseBody
    public ResponseEntity<?> addFavorite(@RequestParam Integer sellerNo, HttpSession session) {
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        if (memberNo == null) {
            return ResponseEntity.status(401).body(Map.of("message", "請先登入"));
        }

        if (favoriteStoreService.isExists(memberNo, sellerNo)) {
            return ResponseEntity.badRequest().body(Map.of("message", "已在收藏清單中"));
        }

        favoriteStoreService.addFavorite(memberNo, sellerNo);
        return ResponseEntity.ok(Map.of("status", "success", "message", "成功加入收藏"));
    }

    // 3. 刪除收藏 (傳統 Form 提交或簡單跳轉)
    @PostMapping("/delete")
    public String deleteFavorite(@RequestParam Integer sellerNo, HttpSession session) {
        Integer memberNo = (Integer) session.getAttribute("memberNo");
        if (memberNo != null) {
            favoriteStoreService.deleteFavorite(memberNo, sellerNo);
        }
        return "redirect:/favoriteStore/list";
    }
}
