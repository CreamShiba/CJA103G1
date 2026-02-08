package com.karshop.favoriteStore;

import com.karshop.members.model.MembersVO;
import com.karshop.seller_info.SellerInfo;
import com.karshop.utils.LoginUserHolder;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private LoginUserHolder loginUserHolder;

    @Autowired
    private FavoriteStoreService favoriteStoreService;

    // 1. 頁面跳轉：顯示該會員所有的收藏賣家
//    @GetMapping("/list")
//    public String listAllFavorites(HttpSession session, Model model) {
//        // 從 Session 獲取登入會員編號 (假設 Key 為 memberNo)
//        Integer memberNo = (Integer) session.getAttribute("memberNo");
//
//        if (memberNo == null) {
//            return "redirect:/members/login"; // 未登入導向登入頁面
//        }
//
//        List<FavoriteStore> list = favoriteStoreService.getFavoritesByMember(memberNo);
//        model.addAttribute("favoriteStores", list);
//        return "favorite/listAllFavorite"; // 指向您的 Thymeleaf 頁面
//    }

    // AJAX 新增收藏
    @PostMapping("/addAjax")
    @ResponseBody
    public ResponseEntity<?> addFavorite(@RequestParam Integer sellerNo) {
        // 1. 使用 LoginUserHolder 取得當前會員
        MembersVO member = loginUserHolder.get();

        // 2. 安全檢查
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", "error", "message", "請先登入後再執行收藏！"));
        }

        // 3. 建立關聯並儲存
        SellerInfo sellerInfo = new SellerInfo();
        sellerInfo.setSeller_no(sellerNo);

        FavoriteStore fav = new FavoriteStore();
        fav.setSellerNo(sellerInfo.getSeller_no());  // 根據你的 FavoriteStore 實體屬性名稱調整
        fav.setMemberNo(member.getMemNo());  // 從物件中取出 ID

        favoriteStoreService.insert(fav);
        return ResponseEntity.ok().body(Map.of("status", "success", "message", "成功加入收藏！"));
    }

    //  刪除收藏 (添加 AJAX 刪除端點)
    @PostMapping("/deleteAjax")
    @ResponseBody
    public ResponseEntity<?> deleteFavoriteAjax(@RequestParam Integer sellerNo) {
        // 1. 使用 LoginUserHolder 取得當前會員
        MembersVO member = loginUserHolder.get();

        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "請先登入"));
        }

        // 2. 執行刪除 (傳入從物件中取出的 memNo)
        favoriteStoreService.delete(member.getMemNo(), sellerNo);

        return ResponseEntity.ok().body(Map.of("status", "success", "message", "已取消收藏"));
    }
}
