package com.karshop.seller_info;

import com.karshop.members.model.MembersVO;
import com.karshop.sellertest.model.SellerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/members/seller")
public class SellerInfoController {

    @Autowired
    private SellerInfoService service;

    private Integer getMemberIdFromSession(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        return (member != null) ? member.getMemNo() : null;
    }

    // 1. 顯示申請頁面
    @GetMapping("/apply")
    public String showApplyForm(HttpSession session, Model model) {
        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        try {
            SellerInfo existing = service.getSellerByMemberId(memberId);

            // 已開通則同步狀態並去後台
            if ("已開通".equals(existing.getStatus())) {
                syncLegacySeller(session, existing);
                return "redirect:/product/dashboard";
            }

            if ("待審核".equals(existing.getStatus())) {
                return "front-end/apply-pending";
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

    // 2. 提交申請
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

    // 3. 賣家資料頁 (修正：移除自動導向 Dashboard 以免死循環)
    @GetMapping("/sellerinfo")
    public String showSellerInfo(HttpSession session, Model model) {
        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        try {
            SellerInfo seller = service.getSellerByMemberId(memberId);

            // 重要：進來這頁時，如果是已開通，我們只同步 Session，不強迫跳轉
            if ("已開通".equals(seller.getStatus())) {
                syncLegacySeller(session, seller);
            }

            if ("待審核".equals(seller.getStatus())) {
                return "front-end/apply-pending";
            }

            model.addAttribute("seller", seller);
            return "front-end/sellerinfo-list";
        } catch (RuntimeException e) {
            return "redirect:/members/seller/apply";
        }
    }

    // 4. 更新資料
    @PostMapping("/update")
    public String updateSeller(
            @ModelAttribute SellerInfo seller,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        Integer memberId = getMemberIdFromSession(session);
        if (memberId == null) return "redirect:/members/login";

        try {
            SellerInfo existing = service.getSellerByMemberId(memberId);
            seller.setSeller_no(existing.getSeller_no());
            seller.setMember_no(memberId);

            if ("未通過".equals(existing.getStatus())) {
                seller.setStatus("待審核");
            } else {
                seller.setStatus(existing.getStatus());
            }

            if (file != null && !file.isEmpty()) {
                seller.setImage_path(service.uploadImage(file));
            } else {
                seller.setImage_path(existing.getImage_path());
            }

            SellerInfo updated = service.updateSeller(seller);

            // 同步 Session
            syncLegacySeller(session, updated);
            session.setAttribute("sellerInfo", updated);

            // 更新完後的跳轉邏輯
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

    private void syncLegacySeller(HttpSession session, SellerInfo source) {
        // 更新 Interceptor 使用的 "seller" Key
        SellerVO legacyVO = (SellerVO) session.getAttribute("seller");
        if (legacyVO == null) {
            legacyVO = new SellerVO();
        }
        legacyVO.setSellerNo(source.getSeller_no());
        legacyVO.setSellerStatus(source.getStatus());
        legacyVO.setMember((MembersVO) session.getAttribute("member"));

        session.setAttribute("seller", legacyVO);
    }

    //    @GetMapping("/uploads/seller-images/{filename:.+}")
//    @ResponseBody
//    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
//        try {
//            // 設定圖片存放的實體路徑 (必須與 Service 的 UPLOAD_DIR 一致)
//            Path file = Paths.get(System.getProperty("user.dir"), "src/main/resources/static/uploads/seller-images/").resolve(filename);
//            Resource resource = new UrlResource(file.toUri());
//
//            if (resource.exists() || resource.isReadable()) {
//
//                String contentType = filename.toLowerCase().endsWith(".png") ? "image/png" : "image/jpeg";
//
//                return ResponseEntity.ok()
//                        .contentType(MediaType.IMAGE_PNG) // 或根據副檔名動態判斷
//                        .body(resource);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (Exception e) {
//            return ResponseEntity.internalServerError().build();
//        }
//    }
    // 💡 修改 Mapping，加上 /images
    // 💡 路徑加上 /file
    // 💡 攔截帶有 /file 的請求
    @GetMapping("/uploads/seller-images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            // 實體路徑指向專案根目錄下的 uploads
            Path filePath = Paths.get(System.getProperty("user.dir"), "uploads", "seller-images").resolve(filename).normalize();

            System.out.println("DEBUG >>> 讀取圖片路徑: " + filePath.toAbsolutePath());

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                String contentType = Files.probeContentType(filePath);
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, contentType != null ? contentType : "image/jpeg")
                        .body(resource);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * 臨時端點:修正所有圖片路徑
     * 使用後可以刪除此方法
     */
    @GetMapping("/fix-image-paths")
    @ResponseBody
    public String fixImagePaths() {
        try {
            int count = service.fixAllImagePaths();
            return "成功修正 " + count + " 筆圖片路徑!<br><br>請重新整理賣家資料頁面查看結果。";
        } catch (Exception e) {
            e.printStackTrace();
            return "修正失敗: " + e.getMessage();
        }
    }
}