package com.karshop.memberInfo;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;



@Controller
@RequestMapping("/memberInfo")
public class MemberInfoController {

    @Autowired
    private MemberInfoService memberInfoService;

    @GetMapping("/manage")
    public String showManagePage(Model model) {
        List<MemberInfo> list = memberInfoService.getAll();
        model.addAttribute("memberList", list);
        return "/memberInfo/adminMemberInfo";
    }

    // 進入編輯頁面
    @GetMapping("/edit/{no}")
    public String editMember(@PathVariable Integer no, Model model) {
        try {
            MemberInfo memberInfo = memberInfoService.findByMemberNo(no);
            if (memberInfo == null) {
                return "redirect:/memberInfo/adminMemberInfo";
            }

            // 計算平均評分
            double averageRating = memberInfo.getRatingAmount() > 0
                    ? (double) memberInfo.getRatingStar() / memberInfo.getRatingAmount()
                    : 0.0;

            model.addAttribute("member", memberInfo);
            model.addAttribute("averageRating", String.format("%.2f", averageRating));

            return "/memberInfo/updateMemberInfo";
        } catch (Exception e) {
            return "redirect:/memberInfo/adminMemberInfo";
        }
    }

    // 處理更新
    @PostMapping("/update")
    public String updateMember(@Valid @ModelAttribute("member") MemberInfo memberInfo,
                               BindingResult result,
                               @RequestParam(value = "imageFile", required = false) MultipartFile file,
                               Model model,
                               RedirectAttributes ra) {
        // 檢查格式是否有誤
        if (result.hasErrors()) {
            // 格式不符停留在編輯頁面
            result.getFieldErrors().forEach(error ->
                    System.out.println("錯誤欄位：" + error.getField() + " | 錯誤原因：" + error.getDefaultMessage())
            );
            Integer amount = memberInfo.getRatingAmount();
            Integer star = memberInfo.getRatingStar();
            double averageRating = (amount != null && amount > 0) ? (double) star / amount : 0.0;
            model.addAttribute("averageRating", String.format("%.2f", averageRating));
            return "memberInfo/updateMemberInfo";
        }
        try {
            memberInfoService.updateMemberInfo(memberInfo, file);

            // 關鍵：將訊息放入 FlashAttribute，重定向後依然存在
            ra.addFlashAttribute("successMessage", "會員資料更新成功！");

            return "redirect:/memberInfo/manage";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "更新失敗：" + e.getMessage());
            return "memberInfo/updateMemberInfo";
        }
    }

    @GetMapping("/image/{no}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> getMemberImage(@PathVariable Integer no) {
        MemberInfo memberInfo = memberInfoService.findByMemberNo(no);
        if (memberInfo != null && memberInfo.getMemberImage() != null) {
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG) // 根據需求可調整
                    .body(memberInfo.getMemberImage());
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }

    //查詢
    @GetMapping("/api/search")
    @ResponseBody // 回傳 JSON 格式數據
    public List<MemberInfo> searchMembers(
            @RequestParam(required = false) Integer memberNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isSeller,
            @RequestParam(required = false) Integer isEngineer) {

        // 處理前端傳來的 -1 (代表「全部」)，轉換為 null 供 Repository 判斷
        Integer searchStatus = (status != null && status == -1) ? null : status;
        Integer searchSeller = (isSeller != null && isSeller == -1) ? null : isSeller;
        Integer searchEngineer = (isEngineer != null && isEngineer == -1) ? null : isEngineer;
        String searchKw = (keyword != null && keyword.trim().isEmpty()) ? null : keyword;

        //  Repository 的複合查詢方法
        return memberInfoService.findByCompositeQuery(memberNo, searchKw, searchStatus, searchSeller, searchEngineer);
    }
}