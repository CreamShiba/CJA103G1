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
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/appeals")
public class InstallAppealsController {

    @Autowired
    private InstallAppealsService installAppealsService;

    @Autowired
    private ProductAppealsService productAppealsService;

    // 顯示新增申訴的 HTML 頁面
    @GetMapping("/add")
    public String showAddPage() {
        return "templates-report/add-appeal";
    }

    // 處理表單提交
    @PostMapping("/submit")
    public String handleForm(@RequestParam("appealType") String appealType,
                             @RequestParam("orderNo") Integer orderNo,
                             @RequestParam(value = "type", required = false) String[] types,
                             @RequestParam("description") String description,
                             @RequestParam(value = "images", required = false) MultipartFile[] images,
                             HttpSession session, // 💡 加入 Session 參數
                             Model model) {

        String categories = (types != null) ? String.join(",", types) : "";

        // 💡 從 Session 取得當前登入會員編號
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        // 💡 如果沒登入，使用測試用會員編號 4（測試模式）
        if (memberNo == null) {
            System.out.println("⚠️ 警告：未從 Session 取得會員編號，使用測試模式 memberNo = 4");
            memberNo = 4;
        } else {
            System.out.println("✅ 從 Session 取得會員編號：" + memberNo);
        }

        if ("install".equals(appealType)) {
            InstallAppeals appeal = new InstallAppeals();
            appeal.setInstallOrderNo(orderNo);
            appeal.setCategories(categories);
            appeal.setDescription(description);
            appeal.setMemberNo(memberNo); // 💡 使用從 Session 取得的會員編號
            setupDefaultValues(appeal);
            installAppealsService.submitInstallAppeal(appeal);
            saveImages(appeal.getAppealsNo(), images);

            System.out.println("✅ 新增安裝申訴成功！編號：" + appeal.getAppealsNo() + "，會員：" + memberNo + "，狀態：" + appeal.getStatus());

        } else if ("product".equals(appealType)) {
            ProductAppeals productAppeal = new ProductAppeals();
            productAppeal.setOrdNo(orderNo);
            productAppeal.setCategories(categories);
            productAppeal.setDescription(description);
            productAppeal.setStatus("待處理");
            productAppeal.setResponse("尚未回覆");
            productAppeal.setPriority("一般");
            productAppeal.setApplyDate(LocalDateTime.now());
            productAppeal.setUpdatedDate(LocalDateTime.now());
            productAppeal.setMemberNo(memberNo); // 💡 使用從 Session 取得的會員編號
            productAppeal.setAdmNo(10);
            productAppeal.setTargetMemberNo(999);

            productAppealsService.insert(productAppeal);
            saveProductImages(productAppeal.getAppealsNo(), images);

            System.out.println("✅ 新增商品申訴成功！編號：" + productAppeal.getAppealsNo() + "，會員：" + memberNo + "，狀態：" + productAppeal.getStatus());
        }

        model.addAttribute("orderNo", orderNo);
        model.addAttribute("appealType", appealType);
        return "templates-report/appeal-success";
    }

    // 私有方法:設定安裝申訴的預設值
    private void setupDefaultValues(InstallAppeals appeal) {
        appeal.setResponse("尚未回覆");
        appeal.setStatus("待處理");
        appeal.setPriority("一般");
        appeal.setApplyDate(LocalDateTime.now());
        appeal.setUpdatedDate(LocalDateTime.now());
        // 💡 移除這行：appeal.setMemberNo(4); 因為已經在上面設定了
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

    // ===== 🔧 修正: 前台申訴紀錄 - 從 Session 取得會員編號 =====
    @GetMapping("/history")
    public String showAppealHistory(HttpSession session, Model model) {
        // 💡 從 Session 取得當前登入會員編號
        Integer memberNo = (Integer) session.getAttribute("memberNo");

        // 💡 測試模式：如果沒登入，顯示所有假資料（方便測試）
        boolean testMode = (memberNo == null);
        if (testMode) {
            System.out.println("⚠️ 測試模式：未從 Session 取得會員編號，顯示所有申訴紀錄");
        } else {
            System.out.println("✅ 會員模式：顯示會員 " + memberNo + " 的申訴紀錄");
        }

        List<Map<String, Object>> allAppeals = new ArrayList<>();

        try {
            List<InstallAppeals> installList;

            // 💡 根據是否登入，決定要顯示全部或只顯示該會員的申訴
            if (testMode) {
                installList = installAppealsService.getAllInstallAppeals(); // 測試模式：顯示全部
            } else {
                installList = installAppealsService.getAppealsByMember(memberNo); // 會員模式：只顯示自己的
            }

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
            List<ProductAppeals> productList;

            // 💡 根據是否登入，決定要顯示全部或只顯示該會員的申訴
            if (testMode) {
                productList = productAppealsService.getAll(); // 測試模式：顯示全部
            } else {
                productList = productAppealsService.getByMemberNo(memberNo); // 會員模式：只顯示自己的
            }

            for (ProductAppeals appeal : productList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("type", "product");

                String displayCategories = "[商品申訴]";
                try {
                    if (appeal.getCategories() != null && !appeal.getCategories().isEmpty()) {
                        displayCategories = appeal.getCategories();
                    }
                } catch (Exception ex) {
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

        // 💡 修正排序邏輯：加入 Null 檢查防止錯誤
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

    // ===== 🔧 修正: 後台申訴清單頁面（Thymeleaf 渲染）+ Debug Log =====
    @GetMapping("/admin/list")
    public String showInstallAdminPage(@RequestParam(value = "status", required = false, defaultValue = "待處理") String status,
                                       Model model) {
        List<Map<String, Object>> allAppeals = new ArrayList<>();

        try {
            // 取得所有安裝申訴
            List<InstallAppeals> installList = installAppealsService.getAllInstallAppeals();
            System.out.println("💡 總共查到 " + installList.size() + " 筆安裝申訴");

            for (InstallAppeals appeal : installList) {
                System.out.println("💡 安裝申訴 #" + appeal.getAppealsNo() + " 狀態：「" + appeal.getStatus() + "」");

                // 💡 只加入符合狀態的申訴
                if (appeal.getStatus().equals(status)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appealsNo", appeal.getAppealsNo());
                    map.put("orderNo", appeal.getInstallOrderNo());
                    map.put("type", "install");
                    map.put("title", appeal.getCategories() != null ? appeal.getCategories() : "其他");
                    map.put("status", appeal.getStatus());
                    map.put("applyDate", appeal.getApplyDate());
                    allAppeals.add(map);
                }
            }
        } catch (Exception e) {
            System.err.println("取得安裝申訴清單失敗: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            // 取得所有商品申訴
            List<ProductAppeals> productList = productAppealsService.getAll();
            System.out.println("💡 總共查到 " + productList.size() + " 筆商品申訴");

            for (ProductAppeals appeal : productList) {
                System.out.println("💡 商品申訴 #" + appeal.getAppealsNo() + " 狀態：「" + appeal.getStatus() + "」");

                // 💡 只加入符合狀態的申訴
                if (appeal.getStatus().equals(status)) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("appealsNo", appeal.getAppealsNo());
                    map.put("orderNo", appeal.getOrdNo());
                    map.put("type", "product");

                    String displayTitle = appeal.getCategories() != null && !appeal.getCategories().isEmpty()
                            ? appeal.getCategories()
                            : "[商品] 單號: " + appeal.getOrdNo();

                    map.put("title", displayTitle);
                    map.put("status", appeal.getStatus());
                    map.put("applyDate", appeal.getApplyDate());
                    allAppeals.add(map);
                }
            }
        } catch (Exception e) {
            System.err.println("取得商品申訴清單失敗: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("💡 過濾狀態「" + status + "」後剩下 " + allAppeals.size() + " 筆");

        // 💡 按時間排序（最新的在前）+ Null 檢查
        allAppeals.sort((a, b) -> {
            LocalDateTime dateA = (LocalDateTime) a.get("applyDate");
            LocalDateTime dateB = (LocalDateTime) b.get("applyDate");
            if (dateA == null) return 1;
            if (dateB == null) return -1;
            return dateB.compareTo(dateA);
        });

        // 💡 傳遞資料到 Thymeleaf
        model.addAttribute("appeals", allAppeals);
        model.addAttribute("currentStatus", status);

        return "templates-report/admin-install-list";
    }

    // ===== 後台 API - 使用 try-catch 防止錯誤 =====
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
        } catch (Exception e) {
            System.err.println("取得安裝申訴清單失敗: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            List<ProductAppeals> productList = productAppealsService.getAll();
            for (ProductAppeals appeal : productList) {
                Map<String, Object> map = new HashMap<>();
                map.put("appealsNo", appeal.getAppealsNo());
                map.put("orderNo", appeal.getOrdNo());
                map.put("type", "product");

                String displayTitle = "[商品] 單號: " + appeal.getOrdNo();
                try {
                    if (appeal.getCategories() != null && !appeal.getCategories().isEmpty()) {
                        displayTitle = appeal.getCategories();
                    }
                } catch (Exception ex) {
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