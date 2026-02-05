package com.karshop.report.controller;

import com.karshop.report.model.Reports;
import com.karshop.report.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

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
            RedirectAttributes redirectAttributes,           // ← 用於傳遞訊息
            Model model) {

        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理"); // ✅ 統一使用「待處理」
        report.setMemberNo(1);
        report.setAdmNo(1);

        // ✅ 核心修改：prodNo 現在會自動透過 @ModelAttribute 綁定到 report.prodNo
        // 所以不需要額外的 @RequestParam 接收！
        // 表單的 name="prodNo" 會自動對應到 Reports 物件的 setProdNo() 方法

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
            return "redirect:/product/" + report.getProdNo();
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
                                      @RequestParam String response){
        try{
            // ✅ status 應該是「待處理」或「已處理」
            reportsService.handleReport(id, status, admNo, response);
            System.out.println("✅ 檢舉 #" + id + " 已更新為：" + status);
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

        model.addAttribute("reports", list);
        return "templates-report/report-history";
    }

    // 顯示檢舉紀錄的詳細資訊頁面
    @GetMapping("/history/detail")
    public String showReportHistoryDetail(@RequestParam("id") Integer id, Model model) {
        Reports report = reportsService.getReportById(id);
        if (report != null) {
            model.addAttribute("report", report);
            return "templates-report/report-detail";
        } else {
            return "redirect:/reports/history";
        }
    }

    /**
     * ✅ 核心修改：後台管理列表 (支援 Thymeleaf 與 分頁)
     * 存取路徑範例: /reports/admin/list?status=待處理&page=0
     */
    @GetMapping("/admin/list")
    public String showReportAdminPage(
            @RequestParam(value = "status", defaultValue = "待處理") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        System.out.println("💡 檢舉後台清單 - 查詢狀態：「" + status + "」，頁碼：" + page);

        // 強制每頁顯示 6 筆
        int pageSize = 6;

        // ✅ 呼叫 Service 取得分頁資料
        Page<Reports> reportPage = reportsService.getReportsByStatusWithPagination(status, page, pageSize);

        System.out.println("💡 查詢到 " + reportPage.getTotalElements() + " 筆資料，當前頁有 " + reportPage.getNumberOfElements() + " 筆");

        // 將資料與當前狀態傳回前端
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", page);

        // 指向 templates/templates-report/ 目錄下的 admin-report-list.html
        return "templates-report/admin-report-list";
    }
}