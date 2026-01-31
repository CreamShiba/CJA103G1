package com.karshop.report.controller;

import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import com.karshop.report.model.InstallAppealImage; //引用圖片model
import com.karshop.report.service.InstallAppealsService; //引用service裡的InstallAppealsService
import org.springframework.beans.factory.annotation.Autowired; //引入自動注入工具
import org.springframework.stereotype.Controller; //標記這是一個「控制器」
import org.springframework.web.bind.annotation.*; //引入網頁標籤工具
import org.springframework.web.multipart.MultipartFile; // 💡 接收上傳檔案必備
import org.springframework.ui.Model; //引入模型對象
import java.util.List; //引入 Java 標準的清單工具
import java.time.LocalDateTime;
import java.io.IOException;

@Controller //控制器,主要任務是「接聽請求」並「回傳網頁」。
@RequestMapping("/appeals") //定義這個控制器的「大門口網址」。
public class InstallAppealsController {

    @Autowired //自動注入
    private InstallAppealsService installAppealsService;

    // 1. 顯示所有申訴清單 (搭配 JSON 回傳供測試，或導向網頁)
    @GetMapping("/all")
    @ResponseBody // 加上這個可以讓它像昨天一樣回傳 JSON 文字
    public List<InstallAppeals> showAll() {
        return installAppealsService.getAllInstallAppeals();
    }

    // 2. 顯示新增申訴的 HTML 頁面
    @GetMapping("/add")
    public String showAddPage() {
        return "templates-report/add-appeal"; // 這會去 templates 資料夾找 add-appeal.html
    }

    // 3. 處理表單提交
    @PostMapping("/submit")
    public String handleForm(@ModelAttribute InstallAppeals appeal,
                             @RequestParam(value = "type", required = false) String[] types,
                             @RequestParam(value = "images", required = false) MultipartFile[] images, // 💡 新增：接收多張圖片
                             Model model) {

        // 補上處理多選類別邏輯
        if (types != null && types.length > 0) {
            appeal.setCategories(String.join(",", types));
        }

        // 設定預設值
        appeal.setResponse("尚未回覆");
        appeal.setStatus("待處理");
        appeal.setPriority("一般");

        // 補上資料庫 NOT NULL 必填欄位
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        appeal.setMemberNo(1);
        appeal.setTargetMemberNo(999);
        appeal.setAdmNo(1);

        // 呼叫 Service 存入資料庫主表
        installAppealsService.submitInstallAppeal(appeal);

        // 💡 處理多張圖片存檔
        if (images != null && images.length > 0) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        installAppealsService.saveAppealImage(appeal.getAppealsNo(), file.getBytes());
                    } catch (IOException e) {
                        System.err.println("圖片讀取失敗: " + e.getMessage());
                    }
                }
            }
        }

        model.addAttribute("orderNo", appeal.getInstallOrderNo());
        return "templates-report/appeal-success";
    }

    // --- 申訴紀錄區域 ---

    // 顯示申訴紀錄列表
    @GetMapping("/history")
    public String showAppealHistory(Model model) {
        Integer memberNo = 1; // 💡 開發階段寫死為 1
        List<InstallAppeals> list = installAppealsService.getAppealsByMember(memberNo);
        model.addAttribute("appeals", list);
        return "templates-report/appeal-history";
    }

    // 💡 顯示案件詳細資訊頁面
    @GetMapping("/history/detail")
    public String showAppealDetail(@RequestParam("id") Integer id, Model model) {
        // 透過 ID 找出該筆申訴的詳細資料
        InstallAppeals appeal = installAppealsService.getAppealById(id);
        // 找出該筆案件關聯的所有圖片清單
        List<InstallAppealImage> images = installAppealsService.getImagesByAppealsNo(id);

        model.addAttribute("appeal", appeal);
        model.addAttribute("images", images);

        return "templates-report/appeal-detail"; // 指向詳細內容頁面
    }

    // --- 管理後台與圖片 API ---

    @GetMapping("/admin/list")
    public String showInstallAdminPage() {
        return "templates-report/admin-install-list";
    }

    @GetMapping("/api/all")
    @ResponseBody
    public List<InstallAppeals> showAllInstallAppeals() {
        return installAppealsService.getAllInstallAppeals();
    }

    @PostMapping("api/handle")
    @ResponseBody
    public String handleInstallAppealByAdmin(@RequestParam Integer id,
                                             @RequestParam String response,
                                             @RequestParam String status,
                                             @RequestParam Integer admNo) {
        try {
            installAppealsService.handleInstallAppeal(id, response, status, admNo);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    @GetMapping("/admin/handle")
    public String showHandlePage(@RequestParam("id") Integer id, Model model) {
        InstallAppeals appeal = installAppealsService.getAppealById(id);
        List<InstallAppealImage> images = installAppealsService.getImagesByAppealsNo(id);
        model.addAttribute("appeal", appeal);
        model.addAttribute("images", images);
        return "templates-report/handle-appeal";
    }

    @GetMapping("/image/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> getAppealImage(@PathVariable Integer id) {
        byte[] imageBytes = installAppealsService.getAppealsImageById(id);
        if (imageBytes != null) {
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}