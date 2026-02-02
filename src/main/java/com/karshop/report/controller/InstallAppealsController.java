package com.karshop.report.controller;

import com.karshop.report.model.InstallAppeals;
import com.karshop.report.model.ProductAppeals;
import com.karshop.report.model.InstallAppealImage;
import com.karshop.report.model.ProductAppealImage;
import com.karshop.report.service.InstallAppealsService;
import com.karshop.report.service.ProductAppealsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import java.util.*;
import java.time.LocalDateTime;
import java.io.IOException;

@Controller
@RequestMapping("/appeals")
public class InstallAppealsController {

    @Autowired
    private InstallAppealsService installAppealsService;

    @Autowired
    private ProductAppealsService productAppealsService;

    // 1. 顯示所有申訴清單 (JSON 回傳供測試)
    @GetMapping("/all")
    @ResponseBody
    public List<InstallAppeals> showAll() {
        return installAppealsService.getAllInstallAppeals();
    }

    // 2. 顯示新增申訴的 HTML 頁面
    @GetMapping("/add")
    public String showAddPage() {
        return "templates-report/add-appeal";
    }

    // 3. 處理表單提交 (核心分流邏輯)
    @PostMapping("/submit")
    public String handleForm(@RequestParam("appealType") String appealType,
                             @RequestParam("orderNo") Integer orderNo,
                             @RequestParam(value = "type", required = false) String[] types,
                             @RequestParam("description") String description,
                             @RequestParam(value = "images", required = false) MultipartFile[] images,
                             Model model) {

        String categories = (types != null) ? String.join(",", types) : "";

        if ("install".equals(appealType)) {
            // 處理「安裝申訴」流程
            InstallAppeals appeal = new InstallAppeals();
            appeal.setInstallOrderNo(orderNo);
            appeal.setCategories(categories);
            appeal.setDescription(description);
            setupDefaultValues(appeal);
            installAppealsService.submitInstallAppeal(appeal);
            saveImages(appeal.getAppealsNo(), images);

        } else if ("product".equals(appealType)) {
            // 處理「商品申訴」流程
            ProductAppeals productAppeal = new ProductAppeals();
            productAppeal.setOrdNo(orderNo);
            productAppeal.setCategories(categories);
            productAppeal.setDescription(description);
            productAppeal.setStatus("PENDING");
            productAppeal.setResponse("尚未回覆");
            productAppeal.setPriority("一般");
            productAppeal.setApplyDate(LocalDateTime.now());
            productAppeal.setUpdatedDate(LocalDateTime.now());
            productAppeal.setMemberNo(4);
            productAppeal.setAdmNo(10);
            productAppeal.setTargetMemberNo(999);

            productAppealsService.insert(productAppeal);
            saveProductImages(productAppeal.getAppealsNo(), images);
        }

        // 💡 新增:傳遞申訴類型到成功頁面
        model.addAttribute("orderNo", orderNo);
        model.addAttribute("appealType", appealType);
        return "templates-report/appeal-success";
    }

    // 私有方法:設定安裝申訴的預設值
    private void setupDefaultValues(InstallAppeals appeal) {
        appeal.setResponse("尚未回覆");
        appeal.setStatus("PENDING");
        appeal.setPriority("一般");
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        appeal.setMemberNo(4);
        appeal.setTargetMemberNo(999);
        appeal.setAdmNo(10);
    }

    // 私有方法:處理安裝申訴圖片存檔
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

    // 私有方法:處理商品申訴圖片存檔
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

    // ===== 🔧 緊急修正: 前台申訴紀錄 - 使用 try-catch 防止錯誤 =====
    @GetMapping("/history")
    public String showAppealHistory(Model model) {
        Integer memberNo = 4;
        List<Map<String, Object>> allAppeals = new ArrayList<>();

        try {
            // 取得安裝申訴
            List<InstallAppeals> installList = installAppealsService.getAppealsByMember(memberNo);
            for (InstallAppeals appeal : installList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("type", "install");
                map.put("categories", appeal.getCategories() != null ? appeal.getCategories() : "其他");
                map.put("status", appeal.getStatus());
                map.put("applyDate", appeal.getApplyDate());
                allAppeals.add(map);
            }
        } catch (Exception e) {
            System.err.println("取得安裝申訴失敗: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // 🔧 取得商品申訴 - 加上錯誤處理
            List<ProductAppeals> productList = productAppealsService.getByMemberNo(memberNo);
            for (ProductAppeals appeal : productList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("type", "product");

                // 🔧 安全取得 categories,若欄位不存在則使用預設值
                String displayCategories = "[商品申訴]";
                try {
                    if (appeal.getCategories() != null && !appeal.getCategories().isEmpty()) {
                        displayCategories = appeal.getCategories();
                    }
                } catch (Exception ex) {
                    // categories 欄位不存在或取得失敗,使用預設值
                    System.err.println("取得商品申訴 categories 失敗,使用預設值");
                }

                map.put("categories", displayCategories);
                map.put("status", appeal.getStatus());
                map.put("applyDate", appeal.getApplyDate());
                allAppeals.add(map);
            }
        } catch (Exception e) {
            System.err.println("取得商品申訴失敗: " + e.getMessage());
            e.printStackTrace();
        }

        // 按時間排序
        allAppeals.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("applyDate");
            LocalDateTime dateB = (LocalDateTime) b.get("applyDate");
            return dateB.compareTo(dateA);
        });

        model.addAttribute("appeals", allAppeals);
        return "templates-report/appeal-history";
    }

    // 顯示案件詳細資訊頁面 (前台使用)
    @GetMapping("/history/detail")
    public String showAppealDetail(@RequestParam("id") Integer id,
                                   @RequestParam(value = "type", required = false) String type,
                                   Model model) {
        if ("product".equals(type)) {
            ProductAppeals appeal = productAppealsService.getById(id);
            List<ProductAppealImage> images = productAppealsService.getImagesByAppealsNo(id);
            model.addAttribute("appeal", appeal);
            model.addAttribute("images", images);
            model.addAttribute("type", "product");
        } else {
            InstallAppeals appeal = installAppealsService.getAppealById(id);
            List<InstallAppealImage> images = installAppealsService.getImagesByAppealsNo(id);
            model.addAttribute("appeal", appeal);
            model.addAttribute("images", images);
            model.addAttribute("type", "install");
        }
        return "templates-report/appeal-detail";
    }

    @GetMapping("/admin/list")
    public String showInstallAdminPage() {
        return "templates-report/admin-install-list";
    }

    // ===== 🔧 緊急修正: 後台 API - 使用 try-catch 防止錯誤 =====
    @GetMapping("/api/all")
    @ResponseBody
    public List<Map<String, Object>> showAllAppeals() {
        List<Map<String, Object>> result = new ArrayList<>();

        try {
            // 取得所有安裝申訴
            List<InstallAppeals> installList = installAppealsService.getAllInstallAppeals();
            for (InstallAppeals appeal : installList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("orderNo", appeal.getInstallOrderNo());
                map.put("type", "install");
                map.put("title", appeal.getCategories() != null ? appeal.getCategories() : "其他");
                map.put("status", appeal.getStatus());
                map.put("applyDate", appeal.getApplyDate());
                result.add(map);
            }
        } catch (Exception e) {
            System.err.println("取得安裝申訴清單失敗: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // 🔧 取得所有商品申訴 - 加上錯誤處理
            List<ProductAppeals> productList = productAppealsService.getAll();
            for (ProductAppeals appeal : productList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("orderNo", appeal.getOrdNo());
                map.put("type", "product");

                // 🔧 安全取得 categories
                String displayTitle = "[商品] 單號: " + appeal.getOrdNo();
                try {
                    if (appeal.getCategories() != null && !appeal.getCategories().isEmpty()) {
                        displayTitle = appeal.getCategories();
                    }
                } catch (Exception ex) {
                    // categories 欄位不存在,使用預設值
                    System.err.println("取得商品申訴 categories 失敗,使用訂單編號");
                }

                map.put("title", displayTitle);
                map.put("status", appeal.getStatus());
                map.put("applyDate", appeal.getApplyDate());
                result.add(map);
            }
        } catch (Exception e) {
            System.err.println("取得商品申訴清單失敗: " + e.getMessage());
            e.printStackTrace();
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

    // 處理商品申訴的 API
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

    // 根據類型顯示詳情頁面
    @GetMapping("/admin/handle")
    public String showHandlePage(@RequestParam("id") Integer id,
                                 @RequestParam(value = "type", required = false) String type,
                                 Model model) {

        if ("product".equals(type)) {
            ProductAppeals appeal = productAppealsService.getById(id);
            List<ProductAppealImage> images = productAppealsService.getImagesByAppealsNo(id);
            model.addAttribute("appeal", appeal);
            model.addAttribute("images", images);
            model.addAttribute("type", "product");
            return "templates-report/handle-appeal";
        } else {
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

    // 取得商品申訴的圖片
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