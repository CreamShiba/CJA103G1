package com.karshop.seller_info;

import com.karshop.members.model.MembersVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class SellerInfoController {

    @Autowired
    private SellerInfoService service;

    /**
     * 從 Session 取得會員 ID (Debug 加強版)
     */
    private Integer getMemberIdFromSession(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");

        if (member == null) {
            System.err.println("❌ 會員未登入");
            return null;
        }

        System.out.println("✅ 會員 ID: " + member.getMemNo());
        return member.getMemNo();
    }

    // ==================== 申請成為賣家 ====================

    @GetMapping("/members/seller/apply")
    public String showApplyForm(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("\n========== 進入申請頁面 ==========");

        Integer memberId = getMemberIdFromSession(session);
        System.out.println("當前會員 ID: " + memberId);

        // 檢查是否已登入
        if (memberId == null) {
            System.out.println("❌ 未登入,重定向到首頁");
            redirectAttributes.addFlashAttribute("error", "請先登入會員");
            return "redirect:/members/login";
        }

        // 檢查是否已經是賣家
        try {
            SellerInfo existingSeller = service.getSellerByMemberId(memberId);
            System.out.println("✅ 找到現有賣家資料: " + existingSeller.getShop_name());

            // ✅ 將賣家資訊放入 session
            session.setAttribute("sellerInfo", existingSeller);

            model.addAttribute("seller", existingSeller);
            model.addAttribute("isAlreadySeller", true);
            model.addAttribute("info", "您已經是賣家,如需修改資料請前往「賣家中心」");
        } catch (RuntimeException e) {
            System.out.println("ℹ️ 尚未申請,顯示空白表單");

            session.removeAttribute("seller");

            model.addAttribute("seller", new SellerInfo());
            model.addAttribute("isAlreadySeller", false);
        }

        System.out.println("========== 顯示申請表單 ==========\n");
        return "front-end/seller-apply";
    }

    @PostMapping("/members/seller/apply")
    public String submitApplication(
            @ModelAttribute SellerInfo seller,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("\n========== 提交賣家申請 ==========");

        Integer memberId = getMemberIdFromSession(session);
        System.out.println("會員 ID: " + memberId);
        System.out.println("店鋪名稱: " + seller.getShop_name());
        System.out.println("賣家姓名: " + seller.getSeller_name());
        System.out.println("聯絡電話: " + seller.getPhone());
        System.out.println("電子郵件: " + seller.getEmail());

        if (memberId == null) {
            System.out.println("❌ 未登入,拒絕申請");
            redirectAttributes.addFlashAttribute("error", "請先登入會員");
            return "redirect:/";
        }

        seller.setMember_no(memberId);

        try {
            // 檢查是否已申請過
            try {
                SellerInfo existingSeller = service.getSellerByMemberId(memberId);
                System.out.println("⚠️ 已申請過,導向編輯頁面");

                session.setAttribute("sellerInfo", existingSeller);

                redirectAttributes.addFlashAttribute("info", "您已經申請過賣家,請到賣家中心編輯資料");
                return "redirect:/members/seller/sellerinfo";

            } catch (RuntimeException e) {
                // 未申請過,新增申請
                System.out.println("ℹ️ 首次申請,建立新賣家資料");

                seller.setStatus("待審核");
                seller.setIsverified(false);

                SellerInfo savedSeller = service.addSeller(seller);
                System.out.println("✅ 賣家資料已儲存,編號: " + savedSeller.getSeller_no());

                session.setAttribute("sellerInfo", savedSeller);

                redirectAttributes.addFlashAttribute("success",
                        "申請已送出!我們將在 3-5 個工作天內完成審核,請留意您的電子郵件。");

                System.out.println("========== 申請成功,導向編輯頁面 ==========\n");
                return "redirect:/members/seller/sellerinfo";
            }

        } catch (Exception e) {
            System.err.println("❌ 申請失敗: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "申請失敗:" + e.getMessage());
            model.addAttribute("seller", seller);
            return "front-end/seller-apply";
        }
    }

// ==================== 編輯賣家資料 ====================

    @GetMapping("/members/seller/sellerinfo")
    public String showSellerInfo(
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("\n========== 進入賣家資料頁面 ==========");

        Integer memberId = getMemberIdFromSession(session);
        System.out.println("會員 ID: " + memberId);

        if (memberId == null) {
            System.out.println("❌ 未登入");
            redirectAttributes.addFlashAttribute("error", "請先登入會員");
            return "redirect:/members/login";
        }

        try {
            SellerInfo seller = service.getSellerByMemberId(memberId);
            System.out.println("✅ 找到賣家資料: " + seller.getShop_name());
            System.out.println("   狀態: " + seller.getStatus());
            System.out.println("   評分: " + seller.getRating_star() + " / " + seller.getRating_amount());

            // ✅ 更新 session
            session.setAttribute("sellerInfo", seller);

            model.addAttribute("seller", seller);

            // 計算平均評分
            if (seller.getRating_amount() != null && seller.getRating_amount() > 0) {
                double avgRating = (double) seller.getRating_star() / seller.getRating_amount();
                model.addAttribute("avgRating", String.format("%.1f", avgRating));
            } else {
                model.addAttribute("avgRating", "0.0");
            }

            System.out.println("========== 顯示賣家資料頁面 ==========\n");
            return "front-end/sellerinfo-list";

        } catch (RuntimeException e) {
            System.out.println("❌ 找不到賣家資料: " + e.getMessage());

            // ✅ 清除 session 中的 seller
            session.removeAttribute("sellerInfo");

            redirectAttributes.addFlashAttribute("error",
                    "您尚未申請成為賣家,請先到「申請成為賣家」頁面提交申請");
            return "redirect:/members/seller/apply";
        }
    }

    @PostMapping("/members/seller/update")
    public String updateSeller(
            @ModelAttribute SellerInfo seller,
            @RequestParam(required = false) MultipartFile file,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        System.out.println("\n========== 更新賣家資料 ==========");

        Integer memberId = getMemberIdFromSession(session);
        System.out.println("會員 ID: " + memberId);

        if (memberId == null) {
            System.out.println("❌ 未登入");
            redirectAttributes.addFlashAttribute("error", "請先登入會員");
            return "redirect:/members/login";
        }

        try {
            SellerInfo existingSeller = service.getSellerByMemberId(memberId);
            System.out.println("✅ 找到現有資料,準備更新");
            System.out.println("   原狀態: " + existingSeller.getStatus());

            seller.setSeller_no(existingSeller.getSeller_no());
            seller.setMember_no(memberId);
            // ===================================================================
            // ✅ 關鍵邏輯：如果原本是「停權」(suspended)，無法變動
            // ===================================================================
            if ("停權".equals(existingSeller.getStatus())) {
                System.out.println("⛔ 帳號已被停權，拒絕更新請求");
                redirectAttributes.addFlashAttribute("error", "您的賣家帳號已被停權，無法修改資料。如有疑問請聯繫管理員。");
                return "redirect:/members/seller/sellerinfo";
            }

            // ===================================================================
            // ✅ 關鍵邏輯：如果原本是「未通過」(rejected)，更新後改為「待審核」(pending)
            // ===================================================================
            if ("未通過".equals(existingSeller.getStatus())) {
                System.out.println("🔄 狀態從「未通過」改為「待審核」，等待管理員重新審核");
                seller.setStatus("待審核");
                seller.setIsverified(false);  // 重新審核時取消驗證狀態
            } else {
                // 其他狀態保持不變
                seller.setStatus(existingSeller.getStatus());
                seller.setIsverified(existingSeller.getIsverified());
            }

            seller.setRating_amount(existingSeller.getRating_amount());
            seller.setRating_star(existingSeller.getRating_star());

            // 處理圖片上傳
            if (file != null && !file.isEmpty()) {
                System.out.println("🖼️ 上傳新圖片: " + file.getOriginalFilename());
                String imagePath = service.uploadImage(file);
                seller.setImage_path(imagePath);
                System.out.println("✅ 圖片已儲存: " + imagePath);
            } else {
                seller.setImage_path(existingSeller.getImage_path());
            }

            SellerInfo updatedSeller = service.updateSeller(seller);
            System.out.println("✅ 資料更新成功");
            System.out.println("   新狀態: " + updatedSeller.getStatus());

            // ✅ 更新 session 中的賣家資訊
            session.setAttribute("sellerInfo", updatedSeller);

            // ===================================================================
            // ✅ 根據狀態顯示不同的提示訊息
            // ===================================================================
            if ("pending".equals(updatedSeller.getStatus())) {
                redirectAttributes.addFlashAttribute("success",
                        "資料更新成功！您的申請已重新送出審核，我們將在 3-5 個工作天內完成審核。");
            } else {
                redirectAttributes.addFlashAttribute("success", "資料更新成功！");
            }

            System.out.println("========== 更新完成 ==========\n");
            return "redirect:/members/seller/sellerinfo";

        } catch (Exception e) {
            System.err.println("❌ 更新失敗: " + e.getMessage());
            e.printStackTrace();

            model.addAttribute("error", "更新失敗:" + e.getMessage());
            model.addAttribute("seller", seller);

            if (seller.getRating_amount() != null && seller.getRating_amount() > 0) {
                double avgRating = (double) seller.getRating_star() / seller.getRating_amount();
                model.addAttribute("avgRating", String.format("%.1f", avgRating));
            } else {
                model.addAttribute("avgRating", "0.0");
            }

            return "front-end/sellerinfo-list";
        }
    }
}