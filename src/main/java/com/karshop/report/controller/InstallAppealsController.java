package com.karshop.report.controller;

import com.karshop.report.model.InstallAppeals; //引用model裡的InstallAppeals
import com.karshop.report.model.ProductAppeals; //引用model裡的ProductAppeals
import com.karshop.report.model.InstallAppealImage; //引用圖片model
import com.karshop.report.model.ProductAppealImage; //引用商品圖片Model
import com.karshop.report.service.InstallAppealsService; //引用service裡的InstallAppealsService
import com.karshop.report.service.ProductAppealsService; //引用service裡的ProductAppealsService
import org.springframework.beans.factory.annotation.Autowired; //引入自動注入工具
import org.springframework.stereotype.Controller; //標記這是一個「控制器」
import org.springframework.web.bind.annotation.*; //引入網頁標籤工具
import org.springframework.web.multipart.MultipartFile; // 💡 接收上傳檔案必備
import org.springframework.ui.Model; //引入模型對象
import java.util.*;
import java.time.LocalDateTime;
import java.io.IOException;

@Controller //控制器,主要任務是「接聽請求」並「回傳網頁」。
@RequestMapping("/appeals") //定義這個控制器的「大門口網址」。
public class InstallAppealsController {

    @Autowired //自動注入安裝申訴服務
    private InstallAppealsService installAppealsService;

    @Autowired //自動注入商品申訴服務 (💡 解決分流問題)
    private ProductAppealsService productAppealsService;

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

    // 3. 處理表單提交 (核心分流邏輯)
    @PostMapping("/submit")
    public String handleForm(@RequestParam("appealType") String appealType, // 💡 接收申訴類型
                             @RequestParam("orderNo") Integer orderNo, // 💡 接收訂單編號
                             @RequestParam(value = "type", required = false) String[] types,
                             @RequestParam("description") String description,
                             @RequestParam(value = "images", required = false) MultipartFile[] images,
                             Model model) {

        String categories = (types != null) ? String.join(",", types) : "";

        if ("install".equals(appealType)) {
            // 💡 處理「安裝申訴」流程
            InstallAppeals appeal = new InstallAppeals();
            appeal.setInstallOrderNo(orderNo);
            appeal.setCategories(categories);
            appeal.setDescription(description);
            setupDefaultValues(appeal); // 設定預設欄位
            installAppealsService.submitInstallAppeal(appeal); // 存入安裝申訴表

            // 處理安裝圖片
            saveImages(appeal.getAppealsNo(), images);

        } else if ("product".equals(appealType)) {
            // 💡 處理「商品申訴」流程 (這會解決存錯表的問題)
            ProductAppeals productAppeal = new ProductAppeals();
            productAppeal.setOrdNo(orderNo); // 存入商品訂單編號欄位
            productAppeal.setDescription(description);

            // 設定商品申訴預設值 (對接資料庫 NOT NULL 必填欄位)
            productAppeal.setStatus("PENDING");
            productAppeal.setResponse("尚未回覆");
            productAppeal.setPriority("一般");
            productAppeal.setApplyDate(LocalDateTime.now());
            productAppeal.setUpdatedDate(LocalDateTime.now());
            productAppeal.setMemberNo(4); // 💡 使用您目前的會員 ID 4
            productAppeal.setAdmNo(10); // 💡 使用管理員 ID 10
            productAppeal.setTargetMemberNo(999);

            productAppealsService.insert(productAppeal); // 💡 呼叫商品申訴的 Service 存入正確表格

            // 💡 處理商品申訴的多張圖片
            saveProductImages(productAppeal.getAppealsNo(), images);
        }

        model.addAttribute("orderNo", orderNo);
        return "templates-report/appeal-success";
    }

    // 私有方法：設定安裝申訴的預設值
    private void setupDefaultValues(InstallAppeals appeal) {
        appeal.setResponse("尚未回覆");
        appeal.setStatus("PENDING");
        appeal.setPriority("一般");
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        appeal.setMemberNo(4); // 💡 使用會員 ID 4
        appeal.setTargetMemberNo(999);
        appeal.setAdmNo(10); // 💡 使用管理員 ID 10
    }

    // 私有方法：處理安裝申訴圖片存檔
    private void saveImages(Integer appealsNo, MultipartFile[] images) {
        if (images != null && images.length > 0) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        installAppealsService.saveAppealImage(appealsNo, file.getBytes());
                    } catch (IOException e) {
                        System.err.println("安裝申訴圖片讀取失敗: " + e.getMessage());
                    }
                }
            }
        }
    }

    // 💡 新增私有方法：處理商品申訴圖片存檔
    private void saveProductImages(Integer appealsNo, MultipartFile[] images) {
        if (images != null && images.length > 0) {
            for (MultipartFile file : images) {
                if (!file.isEmpty()) {
                    try {
                        productAppealsService.saveAppealImage(appealsNo, file.getBytes());
                    } catch (IOException e) {
                        System.err.println("商品申訴圖片讀取失敗: " + e.getMessage());
                    }
                }
            }
        }
    }

    // 顯示申訴紀錄列表
    @GetMapping("/history")
    public String showAppealHistory(Model model) {
        Integer memberNo = 4; // 💡 修改為會員 ID 4
        List<InstallAppeals> list = installAppealsService.getAppealsByMember(memberNo);
        model.addAttribute("appeals", list);
        return "templates-report/appeal-history";
    }

    // 💡 顯示案件詳細資訊頁面 (前台使用)
    @GetMapping("/history/detail")
    public String showAppealDetail(@RequestParam("id") Integer id, Model model) {
        InstallAppeals appeal = installAppealsService.getAppealById(id);
        List<InstallAppealImage> images = installAppealsService.getImagesByAppealsNo(id);
        model.addAttribute("appeal", appeal);
        model.addAttribute("images", images);
        return "templates-report/appeal-detail";
    }

    @GetMapping("/admin/list")
    public String showInstallAdminPage() {
        return "templates-report/admin-install-list";
    }

    // 💡 整合 API：同時回傳安裝申訴和商品申訴的清單給管理後台
    @GetMapping("/api/all")
    @ResponseBody
    public List<Map<String, Object>> showAllAppeals() {
        List<Map<String, Object>> result = new ArrayList<>();

        // 取得所有安裝申訴
        List<InstallAppeals> installList = installAppealsService.getAllInstallAppeals();
        for (InstallAppeals appeal : installList) {
            Map<String, Object> map = new HashMap<>();
            map.put("appealsNo", appeal.getAppealsNo());
            map.put("orderNo", appeal.getInstallOrderNo());
            map.put("type", "install"); // 💡 標記類型
            map.put("title", appeal.getCategories() != null ? appeal.getCategories() : "其他");
            map.put("status", appeal.getStatus());
            map.put("applyDate", appeal.getApplyDate());
            result.add(map);
        }

        // 取得所有商品申訴
        List<ProductAppeals> productList = productAppealsService.getAll();
        for (ProductAppeals appeal : productList) {
            Map<String, Object> map = new HashMap<>();
            map.put("appealsNo", appeal.getAppealsNo());
            map.put("orderNo", appeal.getOrdNo());
            map.put("type", "product"); // 💡 標記類型
            map.put("title", "[商品] 單號: " + appeal.getOrdNo());
            map.put("status", appeal.getStatus());
            map.put("applyDate", appeal.getApplyDate());
            result.add(map);
        }

        return result;
    }

    // 管理員處理安裝申訴
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

    // 💡 新增：處理商品申訴的 API
    @PostMapping("api/product/handle")
    @ResponseBody
    public String handleProductAppealByAdmin(@RequestParam Integer id,
                                             @RequestParam String response,
                                             @RequestParam String status,
                                             @RequestParam Integer admNo) {
        try {
            productAppealsService.handleProductAppeal(id, response, status, admNo);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }

    // 💡 新增：根據類型顯示詳情頁面 (解決查看內容錯誤問題)
    @GetMapping("/admin/handle")
    public String showHandlePage(@RequestParam("id") Integer id,
                                 @RequestParam(value = "type", required = false) String type,
                                 Model model) {

        if ("product".equals(type)) {
            // 💡 顯示商品申訴內容與圖片
            ProductAppeals appeal = productAppealsService.getById(id);
            List<ProductAppealImage> images = productAppealsService.getImagesByAppealsNo(id);
            model.addAttribute("appeal", appeal);
            model.addAttribute("images", images);
            model.addAttribute("type", "product");
            return "templates-report/handle-appeal";
        } else {
            // 💡 顯示安裝申訴內容與圖片
            InstallAppeals appeal = installAppealsService.getAppealById(id);
            List<InstallAppealImage> images = installAppealsService.getImagesByAppealsNo(id);
            model.addAttribute("appeal", appeal);
            model.addAttribute("images", images);
            model.addAttribute("type", "install");
            return "templates-report/handle-appeal";
        }
    }

    // 安裝申訴圖片顯示讀取器
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

    // 💡 新增：取得商品申訴的圖片 (從 product_appeal_images 表)
    @GetMapping("/product/image/{id}")
    @ResponseBody
    public org.springframework.http.ResponseEntity<byte[]> getProductAppealImage(@PathVariable Integer id) {
        byte[] imageBytes = productAppealsService.getAppealImageById(id);
        if (imageBytes != null) {
            return org.springframework.http.ResponseEntity.ok()
                    .contentType(org.springframework.http.MediaType.IMAGE_JPEG)
                    .body(imageBytes);
        }
        return org.springframework.http.ResponseEntity.notFound().build();
    }
}