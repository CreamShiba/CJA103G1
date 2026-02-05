package com.karshop.report.controller;

import com.karshop.report.model.*;
import com.karshop.report.service.InstallAppealsService;
import com.karshop.report.service.ProductAppealsService;
import com.karshop.report.repository.OrdForReportRepository; // 💡 注入妳剛創的 Repository
import com.karshop.report.repository.InstallOrderForReportRepository; // 💡 注入妳剛創的 Repository
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import java.util.*;
import java.time.LocalDateTime;
import java.io.IOException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/appeals")
public class InstallAppealsController {

    @Autowired
    private InstallAppealsService installAppealsService;

    @Autowired
    private ProductAppealsService productAppealsService;

    // 💡 注入妳自己創立的讀取 Repository，不影響組員進度
    @Autowired
    private OrdForReportRepository ordForReportRepository;

    @Autowired
    private InstallOrderForReportRepository installOrderForReportRepository;

    // 💡 修改後的顯示新增申訴頁面：自動抓取會員的訂單
    @GetMapping("/add")
    public String showAddPage(HttpSession session, Model model) {
        // 1. 從 Session 取得當前登入會員編號
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        // 2. 測試模式：如果沒登入，預設使用 memberNo = 8 (對應 SQL 假資料)
        if (memberNo == null) {
            System.out.println("⚠️ 頁面載入測試：未偵測到 Session，自動載入會員 8 的訂單資料");
            memberNo = 8;
        }

        // 3. 自動抓取該會員所有的商品訂單與安裝訂單
        List<OrdForReport> productOrders = ordForReportRepository.findByMemberNo(memberNo);
        List<InstallOrderForReport> installOrders = installOrderForReportRepository.findByMemberNo(memberNo);

        // 4. 將訂單資料傳往前端 HTML
        model.addAttribute("productOrders", productOrders);
        model.addAttribute("installOrders", installOrders);

        return "templates-report/add-appeal";
    }

    // 處理表單提交
    @PostMapping("/submit")
    public String handleForm(@RequestParam("appealType") String appealType,
                             @RequestParam("orderNo") Integer orderNo,
                             @RequestParam(value = "type", required = false) String[] types,
                             @RequestParam("description") String description,
                             @RequestParam(value = "images", required = false) MultipartFile[] images,
                             HttpSession session,
                             Model model) {

        String categories = (types != null) ? String.join(",", types) : "";

        // 💡 從 Session 取得當前登入會員編號
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        // 💡 測試模式：如果沒登入，使用假資料 memberNo = 4
        if (memberNo == null) {
            System.out.println("⚠️ 測試模式：未從 Session 取得會員編號，使用假資料 memberNo = 4");
            memberNo = 4;
        } else {
            System.out.println("✅ 從 Session 取得會員編號：" + memberNo);
        }

        if ("install".equals(appealType)) {
            InstallAppeals appeal = new InstallAppeals();
            appeal.setInstallOrderNo(orderNo);
            appeal.setCategories(categories);
            appeal.setDescription(description);
            appeal.setMemberNo(memberNo);
            appeal.setResponse("尚未回覆");
            appeal.setStatus("待處理"); // ✅ 統一使用「待處理」
            appeal.setPriority("一般");
            appeal.setApplyDate(LocalDateTime.now());
            appeal.setUpdatedDate(LocalDateTime.now());
            appeal.setTargetMemberNo(999);
            appeal.setAdmNo(10);

            installAppealsService.submitInstallAppeal(appeal);
            saveImages(appeal.getAppealsNo(), images);

            System.out.println("✅ 新增安裝申訴成功！編號：" + appeal.getAppealsNo() + "，會員：" + memberNo + "，狀態：" + appeal.getStatus());

        } else if ("product".equals(appealType)) {
            // ✅ 加入 try-catch 處理訂單驗證錯誤
            try {
                ProductAppeals productAppeal = new ProductAppeals();
                productAppeal.setOrdNo(orderNo);
                productAppeal.setCategories(categories);
                productAppeal.setDescription(description);
                productAppeal.setStatus("待處理"); // ✅ 統一使用「待處理」
                productAppeal.setResponse("尚未回覆");
                productAppeal.setPriority("一般");
                productAppeal.setApplyDate(LocalDateTime.now());
                productAppeal.setUpdatedDate(LocalDateTime.now());
                productAppeal.setMemberNo(memberNo);
                productAppeal.setAdmNo(10);
                productAppeal.setTargetMemberNo(999);

                // ✅ 這裡會進行訂單驗證（訂單存在 + 申訴人是買家）
                productAppealsService.insert(productAppeal);
                saveProductImages(productAppeal.getAppealsNo(), images);

                System.out.println("✅ 新增商品申訴成功！編號：" + productAppeal.getAppealsNo() + "，會員：" + memberNo + "，狀態：" + productAppeal.getStatus());

            } catch (RuntimeException e) {
                // ❌ 驗證失敗的錯誤處理
                System.err.println("❌ 商品申訴提交失敗: " + e.getMessage());
                model.addAttribute("error", e.getMessage());
                model.addAttribute("orderNo", orderNo);
                model.addAttribute("appealType", appealType);
                return "templates-report/appeal-error"; // 顯示錯誤頁面
            }
        }

        model.addAttribute("orderNo", orderNo);
        model.addAttribute("appealType", appealType);
        return "templates-report/appeal-success";
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

    // ===== 前台申訴紀錄 =====
    @GetMapping("/history")
    public String showAppealHistory(HttpSession session, Model model) {
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        // 測試模式：如果沒登入，顯示所有假資料
        boolean testMode = (memberNo == null);
        if (testMode) {
            System.out.println("⚠️ 測試模式：未從 Session 取得會員編號，顯示所有申訴紀錄");
        } else {
            System.out.println("✅ 會員模式：顯示會員 " + memberNo + " 的申訴紀錄");
        }

        List<Map<String, Object>> allAppeals = new ArrayList<>();

        try {
            List<InstallAppeals> installList;
            if (testMode) {
                installList = installAppealsService.getAllInstallAppeals();
            } else {
                installList = installAppealsService.getAppealsByMember(memberNo);
            }

            for (InstallAppeals appeal : installList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("type", "install");
                map.put("categories", appeal.getCategories() != null ? appeal.getCategories() : "其他");
                map.put("status", appeal.getStatus()); // ✅ 直接使用資料庫的狀態
                map.put("applyDate", appeal.getApplyDate());
                allAppeals.add(map);
            }
        } catch (Exception e) {
            System.err.println("取得安裝申訴失敗: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            List<ProductAppeals> productList;
            if (testMode) {
                productList = productAppealsService.getAll();
            } else {
                productList = productAppealsService.getByMemberNo(memberNo);
            }

            for (ProductAppeals appeal : productList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("type", "product");
                map.put("categories", appeal.getCategories() != null && !appeal.getCategories().isEmpty()
                        ? appeal.getCategories() : "[商品申訴]");
                map.put("status", appeal.getStatus()); // ✅ 直接使用資料庫的狀態
                map.put("applyDate", appeal.getApplyDate());
                allAppeals.add(map);
            }
        } catch (Exception e) {
            System.err.println("取得商品申訴失敗: " + e.getMessage());
            e.printStackTrace();
        }

        // 按時間排序（最新的在前）
        allAppeals.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("applyDate");
            LocalDateTime dateB = (LocalDateTime) b.get("applyDate");
            if (dateA == null) return 1;
            if (dateB == null) return -1;
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

    // ===== 後台申訴清單頁面：修正幽靈頁面問題 =====
    @GetMapping("/admin/list")
    public String showInstallAdminPage(
            @RequestParam(value = "status", required = false, defaultValue = "待處理") String status,
            Model model) {

        // 💡 狀態校準器：只接受「待處理」與「已處理」，對齊字串防止幽靈頁面
        final String finalStatus = (status.contains("處理") && !status.equals("待處理")) ? "已處理" : status;
        System.out.println("💡 後台申訴清單 - 查詢狀態校準為：「" + finalStatus + "」");

        List<Map<String, Object>> allAppeals = new ArrayList<>();

        try {
            // 取得所有安裝申訴
            List<InstallAppeals> installList = installAppealsService.getAllInstallAppeals();
            for (InstallAppeals appeal : installList) {
                // 向後兼容以前改過的狀態字串
                String currentStatus = appeal.getStatus();
                if ("已結案".equals(currentStatus)) currentStatus = "已處理";

                if (currentStatus.equals(finalStatus)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appealsNo", appeal.getAppealsNo());
                    map.put("orderNo", appeal.getInstallOrderNo());
                    map.put("type", "install");
                    map.put("title", appeal.getCategories() != null ? appeal.getCategories() : "其他");
                    map.put("status", currentStatus);
                    map.put("applyDate", appeal.getApplyDate());
                    // 💡 把管理員手寫的回覆內容也塞進去
                    map.put("adminResponse", appeal.getResponse() != null ? appeal.getResponse() : "尚未回覆");
                    allAppeals.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        try {
            // 取得所有商品申訴
            List<ProductAppeals> productList = productAppealsService.getAll();
            for (ProductAppeals appeal : productList) {
                String currentStatus = appeal.getStatus();
                if ("已結案".equals(currentStatus)) currentStatus = "已處理";

                if (currentStatus.equals(finalStatus)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appealsNo", appeal.getAppealsNo());
                    map.put("orderNo", appeal.getOrdNo());
                    map.put("type", "product");
                    map.put("title", appeal.getCategories() != null && !appeal.getCategories().isEmpty()
                            ? appeal.getCategories()
                            : "[商品] 單號: " + appeal.getOrdNo());
                    map.put("status", currentStatus);
                    map.put("applyDate", appeal.getApplyDate());
                    // 💡 把商品申訴的管理員回覆也塞進去
                    map.put("adminResponse", appeal.getResponse() != null ? appeal.getResponse() : "尚未回覆");
                    allAppeals.add(map);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        // 按時間排序
        allAppeals.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("applyDate");
            LocalDateTime dateB = (LocalDateTime) b.get("applyDate");
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateB.compareTo(dateA);
        });

        // 傳遞資料到 Thymeleaf
        model.addAttribute("appeals", allAppeals);
        model.addAttribute("currentStatus", finalStatus); // 💡 重要：傳回校準後的狀態

        return "templates-report/admin-install-list";
    }

    // ===== 後台 API =====
    @GetMapping("/api/all")
    @ResponseBody
    public List<Map<String, Object>> showAllAppeals() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
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
            List<ProductAppeals> productList = productAppealsService.getAll();
            for (ProductAppeals appeal : productList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("orderNo", appeal.getOrdNo());
                map.put("type", "product");
                map.put("title", appeal.getCategories() != null && !appeal.getCategories().isEmpty() ? appeal.getCategories() : "[商品] 單號: " + appeal.getOrdNo());
                map.put("status", appeal.getStatus());
                map.put("applyDate", appeal.getApplyDate());
                result.add(map);
            }
        } catch (Exception e) { e.printStackTrace(); }
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
            // 💡 強制校準：處理完統一存為「已處理」
            String finalStatus = "已處理";
            installAppealsService.handleInstallAppeal(id, response, finalStatus, admNo);
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
            // 💡 強制校準：處理完統一存為「已處理」
            String finalStatus = "已處理";
            productAppealsService.handleProductAppeal(id, response, finalStatus, admNo);
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