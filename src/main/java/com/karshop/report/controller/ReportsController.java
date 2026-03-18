package com.karshop.report.controller; // 路徑

import com.karshop.report.model.Reports; // 引入Reports VO
import com.karshop.report.service.ReportsService; // 引入ReportsService
import com.karshop.members.model.MembersVO; // 引入會員 VO
import com.karshop.members.model.MembersService; // 導入會員 Service
import org.springframework.beans.factory.annotation.Autowired; // 用來實現依賴注入,自動裝配 Spring Bean
import org.springframework.data.domain.Page; //引入分頁與排序工具
import org.springframework.jdbc.core.JdbcTemplate; // 導入 JdbcTemplate 用於快速抓取商品名稱
import org.springframework.stereotype.Controller; // 讓 Spring Boot 知道這個類別用來處理網頁瀏覽器的請求
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; //官方術語：重定向屬性,用於顯示操作成功,顯示完後跳轉頁面
import java.time.LocalDateTime; // 提供操作陣列的靜態方法（例如將陣列轉為 List）。
import java.util.Arrays; // 提供操作陣列的靜態方法（如將陣列轉為 List）
import java.util.List; // 定義列表集合，用於儲存多筆資料
import jakarta.servlet.http.HttpSession; // 💡 導入 Session

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @Autowired
    private MembersService membersService; // 注入會員服務

    @Autowired
    private JdbcTemplate jdbcTemplate; // 注入 JDBC 工具

    // 顯示前台新增檢舉頁面
    @GetMapping("/add")
    public String showReportPage() {
        return "templates-report/add-report";
    }

    // ✅ 處理前台檢舉提交表單（支援從商品頁檢舉後返回原頁面）
    @PostMapping("/submit")
    public String handleReport(
            @ModelAttribute Reports report,
            @RequestParam(required = false) String source,  // ← 接收來源標記
            HttpSession session,                            // ← 接收 Session 抓真實會員
            RedirectAttributes redirectAttributes,           // ← 用於傳遞訊息
            Model model) {

        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理"); // ✅ 統一使用「待處理」

        // 💡 修正點：不再寫死。從 Session 抓取真實會員物件
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member != null) {
            report.setMemberNo(member.getMemNo()); // 設定為當前登入者 ID
            report.setEmail(member.getMemEmail()); // 💡 同步存入該會員的信箱
            System.out.println("✅ 會員檢舉 - 檢舉人：" + member.getMemName() + "，信箱：" + member.getMemEmail());
        } else {
            // 💡 訪客檢舉：將 memberNo 設為 null
            // 提醒：SQL 中的 member_no 必須拿掉 NOT NULL 否則存檔會失敗
            report.setMemberNo(null);
            System.out.println("⚠️ 訪客檢舉 - memberNo 設為空值");
        }

        report.setAdmNo(1);

        // ✅ 儲存檢舉資料（包含商品編號）
        reportsService.submitReport(report);

        System.out.println("✅ 檢舉已儲存 - 商品編號：" + report.getProdNo() +
                "，賣家編號：" + report.getSellerNo());

        // ✅ 設定成功提示訊息
        redirectAttributes.addFlashAttribute("message", "檢舉已送出，我們會盡快審核！");

        // ✅ 關鍵邏輯：判斷要跳轉去哪裡
        if ("productDetail".equals(source) && report.getProdNo() != null) {
            // 如果是從商品頁來的，就導回該商品的詳情頁
            System.out.println("✅ 從商品頁檢舉，返回商品編號：" + report.getProdNo());
            return "redirect:/product/detail?prodNo=" + report.getProdNo();
        }

        // 預設行為：顯示檢舉成功頁面（原本的邏輯）
        model.addAttribute("target", report.getReportsTarget());
        return "templates-report/report-success";
    }

    // 提供 JSON 格式的所有檢舉資料 (保留原本 API 功能，供其他需求使用)
    @GetMapping("/api/all")
    @ResponseBody
    public List<Reports> getAllReportsForAdmin(){
        return reportsService.getAllReports();
    }

    // ✅ 處理管理員後台的審核結案動作
    @PostMapping("/api/handle")
    @ResponseBody
    public String handleReportByAdmin(@RequestParam Integer id,
                                      @RequestParam String status,
                                      @RequestParam Integer admNo,
                                      @RequestParam(required = false) String response){
        try{
            // 💡 抓取該案件當前狀態
            Reports currentReport = reportsService.getReportById(id);

            // 💡 預設回覆文字
            String finalResponse = (response == null || response.trim().isEmpty()) ? "案件已處理完畢。" : response;
            String finalStatus = "已處理"; // 預設存為已處理

            // 💡 核心修改邏輯：
            // 1. 如果是「駁回」，資料庫狀態存「駁回」，回覆文字加上 [案件駁回]
            // 2. 如果是「下架」，資料庫狀態存「已處理」(避開組員的標籤)，回覆文字加上 [商品下架]
            if (status.equals("駁回") || (response != null && response.contains("駁回"))) {
                finalStatus = "駁回";
                finalResponse = "[案件駁回] " + finalResponse;
                System.out.println("💡 案件 #" + id + " 處理動作：案件駁回，DB狀態設為：駁回");
            } else if (status.equals("已下架") || status.contains("下架") || (response != null && (response.contains("下架") || response.contains("違規")))) {
                finalStatus = "已處理"; // 💡 存成已處理，組員那邊就不會看到下架標籤
                finalResponse = "[商品下架] " + finalResponse;
                System.out.println("💡 案件 #" + id + " 處理動作：商品下架，DB狀態設為：已處理");
            } else {
                finalResponse = "[處理完成] " + finalResponse;
            }

            reportsService.handleReport(id, finalStatus, admNo, finalResponse);
            System.out.println("✅ 檢舉 #" + id + " 結案存檔成功，最終狀態：" + finalStatus + "，回覆文字：" + finalResponse);
            return "success";
        }catch (Exception e){
            return "error: " + e.getMessage();
        }
    }

    // 顯示管理員後台的「處理案件」詳細頁面
    @GetMapping("/admin/handle")
    public String showHandlePage(@RequestParam("id") Integer id, Model model) {
        Reports report = reportsService.getReportById(id);
        if (report != null) {
            model.addAttribute("report", report);

            // 💡 1. 抓取檢舉人姓名 (人性化優化)
            if (report.getMemberNo() != null && report.getMemberNo() > 0) {
                MembersVO m = membersService.getOneMember(report.getMemberNo());
                // 顯示：匿名 (ID)
                model.addAttribute("reporterDisplay", (m != null ? m.getMemUsername() : "不明會員") + " (ID: " + report.getMemberNo() + ")");
            } else {
                model.addAttribute("reporterDisplay", "訪客");
            }

            // 💡 2. 核心共識：抓取被檢舉的商品名稱
            if ("商品".equals(report.getReportsType()) && report.getProdNo() != null) {
                try {
                    String sql = "SELECT prod_name FROM product WHERE prod_no = ?";
                    String prodName = jdbcTemplate.queryForObject(sql, String.class, report.getProdNo());
                    model.addAttribute("targetDisplay", prodName + " (編號: " + report.getProdNo() + ")");
                } catch (Exception e) {
                    model.addAttribute("targetDisplay", "未知商品 (編號: " + report.getProdNo() + ")");
                }
            } else {
                model.addAttribute("targetDisplay", report.getReportsTarget());
            }

            // 💡 3. 傳遞狀態讓側邊欄 active 正常顯示
            // 修正：只有「待處理」狀態的會在高亮區
            String s = report.getStatus();
            String currentStatus = ("待處理".equals(s)) ? "待處理" : "已處理";
            model.addAttribute("currentStatus", currentStatus);

            return "templates-report/handle-report";
        } else {
            return "redirect:/reports/admin/list";
        }
    }

    // --- 會員中心功能 ---

    /**
     * ✅ 前台顯示檢舉紀錄列表
     */
    @GetMapping("/history")
    public String showReportHistory(Model model) {
        // 為了整合期測試，直接抓取資料庫所有檢舉，讓假資料全部現身
        List<Reports> list = reportsService.getAllReports();

        // 💡 轉換檢舉對象名稱：如果是商品類型就轉換名稱
        for (Reports report : list) {
            if ("商品".equals(report.getReportsType()) && report.getProdNo() != null) {
                try {
                    String sql = "SELECT prod_name FROM product WHERE prod_no = ?";
                    String prodName = jdbcTemplate.queryForObject(sql, String.class, report.getProdNo());
                    report.setReportsTarget(prodName); // 暫存到 Target 欄位供前端顯示
                } catch (Exception e) {
                    report.setReportsTarget("商品編號: " + report.getProdNo());
                }
            }
        }

        model.addAttribute("reports", list);
        return "templates-report/report-history";
    }

    // 顯示檢舉紀錄的詳細資訊頁面
    @GetMapping("/history/detail")
    public String showReportHistoryDetail(@RequestParam("id") Integer id, Model model) {
        Reports report = reportsService.getReportById(id);
        if (report != null) {
            // 💡 轉換詳情頁的對象名稱
            String targetDisplay = report.getReportsTarget();
            if ("商品".equals(report.getReportsType()) && report.getProdNo() != null) {
                try {
                    String sql = "SELECT prod_name FROM product WHERE prod_no = ?";
                    targetDisplay = jdbcTemplate.queryForObject(sql, String.class, report.getProdNo());
                } catch (Exception e) {
                    targetDisplay = "商品編號: " + report.getProdNo();
                }
            }

            model.addAttribute("report", report);
            model.addAttribute("targetDisplay", targetDisplay); // 💡 傳遞名稱到詳情頁
            return "templates-report/report-detail";
        } else {
            return "redirect:/reports/history";
        }
    }

    /**
     * ✅ 核心修改：後台管理列表 (支援 Thymeleaf 與 分頁)
     */
    @GetMapping("/admin/list")
    public String showReportAdminPage(
            @RequestParam(value = "status", defaultValue = "待處理") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // 💡 狀態校準
        String finalStatus = status;
        if (status.equals("PENDING") || status.equals("尚未處理")) {
            finalStatus = "待處理";
        } else if (status.equals("已結案")) {
            finalStatus = "已處理";
        }

        System.out.println("💡 檢舉後台清單 - 查詢狀態校準為：「" + finalStatus + "」，頁碼：「" + page + "」");

        // 強制每頁顯示 6 筆
        int pageSize = 6;
        Page<Reports> reportPage;

        // ✅ 核心調整：
        // 1. 待處理清單：依照組員需求，目前僅顯示真正「待處理」的案件。
        // 2. 已處理清單：顯示所有「已處理」及「駁回」的結案案件。
        if ("待處理".equals(finalStatus)) {
            reportPage = reportsService.getReportsByStatusWithPagination("待處理", page, pageSize);
        } else {
            // ✅ 已處理區抓取結案狀態
            reportPage = reportsService.getReportsByStatusWithPagination("已處理", page, pageSize);
        }

        System.out.println("💡 查詢到 " + reportPage.getTotalElements() + " 筆資料，當前頁有 " + reportPage.getNumberOfElements() + " 筆");

        // 將資料與當前狀態傳回前端
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("currentStatus", finalStatus);
        model.addAttribute("currentPage", page);

        return "templates-report/admin-report-list";
    }
}