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
@RequestMapping("/members/seller") // 統一抽取父路徑，減少出錯
public class SellerInfoController {

    @Autowired
    private SellerInfoService service;

    private Integer getMemberIdFromSession(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        return (member != null) ? member.getMemNo() : null;
    }

    // 1. 顯示申請頁面: GET /members/seller/apply
    @GetMapping("/apply")
    public String showApplyForm(HttpSession session, Model model) {
        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        try {
            SellerInfo existing = service.getSellerByMemberId(memberId);
            if ("待審核".equals(existing.getStatus())) {
                return "front-end/apply-pending"; // 找 templates/front-end/apply-pending.html
            }
            model.addAttribute("seller", existing);
            model.addAttribute("isAlreadySeller", true);
        } catch (RuntimeException e) {
            SellerInfo newSeller = new SellerInfo();
            newSeller.setMember_no(memberId);
            model.addAttribute("seller", newSeller);
            model.addAttribute("isAlreadySeller", false);
        }
        return "front-end/seller-apply";
    }

    // 2. 提交申請: POST /members/seller/apply
    @PostMapping("/apply")
    public String submitApplication(@ModelAttribute SellerInfo seller, HttpSession session) {
        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        seller.setMember_no(memberId);
        seller.setStatus("待審核");
        seller.setIsverified(false);
        service.addSeller(seller);

        return "front-end/apply-pending";
    }

    // 3. 賣家資料頁: GET /members/seller/sellerinfo
    @GetMapping("/sellerinfo")
    public String showSellerInfo(HttpSession session, Model model) {
        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        try {
            SellerInfo seller = service.getSellerByMemberId(memberId);
            if ("待審核".equals(seller.getStatus())) {
                return "front-end/apply-pending";
            }
            model.addAttribute("seller", seller);
            return "front-end/sellerinfo-list";
        } catch (RuntimeException e) {
            return "redirect:/members/seller/apply";
        }
    }

//    // 4. 更新資料: POST /members/seller/update
//    @PostMapping("/update")
//    public String updateSeller(
//            @ModelAttribute SellerInfo seller,
//            @RequestParam(value = "file", required = false) MultipartFile file,
//            HttpSession session,
//            RedirectAttributes redirectAttributes,
//            Model model) {
//
//        Integer memberId = getMemberIdFromSession(session);
//        if (memberId == null) return "redirect:/members/login";
//
//        try {
//            // 1. 取得舊資料
//            SellerInfo existing = service.getSellerByMemberId(memberId);
//            seller.setSeller_no(existing.getSeller_no());
//            seller.setMember_no(memberId);
//
//            // 2. 處理圖片上傳
//            if (file != null && !file.isEmpty()) {
//                String imagePath = service.uploadImage(file);
//                seller.setImage_path(imagePath); // 更新新路徑
//                System.out.println("✅ 圖片上傳成功，新路徑為: " + imagePath);
//            } else {
//                // 若沒上傳新檔案，保留原本的圖片路徑
//                seller.setImage_path(existing.getImage_path());
//            }
//
//            // 3. 處理狀態 (只有未通過者更新後才需要重送審核)
//            boolean needToReapply = "未通過".equals(existing.getStatus());
//            if (needToReapply) {
//                seller.setStatus("待審核");
//            } else {
//                seller.setStatus(existing.getStatus()); // 保持原有狀態(如：已開通)
//            }
//
//            // 4. 執行存檔
//            SellerInfo updated = service.updateSeller(seller);
//            session.setAttribute("sellerInfo", updated);
//
//            // 5. 決定導向
//            if (needToReapply) {
//                return "front-end/apply-pending"; // 只有「重新送審」才去 Pending 頁
//            }
//
//            // 一般資料修改，留在原頁面並帶上成功訊息
//            redirectAttributes.addFlashAttribute("success", "資料與頭像更新成功！");
//            return "redirect:/members/seller/sellerinfo";
//
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("error", "更新失敗：" + e.getMessage());
//            return "redirect:/members/seller/sellerinfo";
//        }
//    }

    @PostMapping("/update")
    public String updateSeller(
            @ModelAttribute SellerInfo seller,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        try {
            // 1. 撈出資料庫目前的舊資料
            SellerInfo existing = service.getSellerByMemberId(memberId);

            // 2. 補齊必要 ID 與關聯，確保 JPA 執行的是 Update 而不是 Insert
            seller.setSeller_no(existing.getSeller_no());
            seller.setMember_no(memberId);

            // 3. 處理狀態 (這是你最在意的部分)
            // 只要原本是「未通過」，不管前端傳什麼過來，我們在後端強制設定為「待審核」
            if ("未通過".equals(existing.getStatus())) {
                seller.setStatus("待審核");
                System.out.println("DEBUG: 狀態由 [未通過] 修改為 -> [待審核]");
            } else {
                // 如果原本是 [已開通] 或 [待審核]，則維持原狀
                seller.setStatus(existing.getStatus());
            }

            // 4. 處理圖片路徑
            if (file != null && !file.isEmpty()) {
                seller.setImage_path(service.uploadImage(file));
            } else {
                // 沒傳新圖就抓舊圖路徑
                seller.setImage_path(existing.getImage_path());
            }

            // 5. 執行存檔 (全案只呼叫這一次 Service)
            SellerInfo updated = service.updateSeller(seller);
            System.out.println("DEBUG: 資料庫最終存檔狀態 -> " + updated.getStatus());

            // 6. 同步更新 Session
            session.setAttribute("sellerInfo", updated);

            // 7. 跳轉邏輯
            if ("待審核".equals(updated.getStatus())) {
                return "front-end/apply-pending";
            }

            redirectAttributes.addFlashAttribute("success", "資料更新成功！");
            return "redirect:/members/seller/sellerinfo";

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "更新失敗：" + e.getMessage());
            return "redirect:/members/seller/sellerinfo";
        }
    }
}