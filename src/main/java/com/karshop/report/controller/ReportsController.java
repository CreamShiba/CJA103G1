package com.karshop.report.controller;

import com.karshop.report.model.Reports;
import com.karshop.report.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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

    // 處理前台檢舉提交表單
    @PostMapping("/submit")
    public String handleReport(@ModelAttribute Reports report, Model model) {
        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理");
        report.setMemberNo(1);
        report.setAdmNo(1);
        reportsService.submitReport(report);
        model.addAttribute("target", report.getReportsTarget());
        return "templates-report/report-success";
    }

    // 提供 JSON 格式的所有檢舉資料 (保留原本 API 功能，供其他需求使用)
    @GetMapping("/api/all")
    @ResponseBody
    public List<Reports> getAllReportsForAdmin(){
        return reportsService.getAllReports();
    }

    // 處理管理員後台的審核結案動作
    @PostMapping("/api/handle")
    @ResponseBody
    public String handleReportByAdmin(@RequestParam Integer id,
                                      @RequestParam String status,
                                      @RequestParam Integer admNo,
                                      @RequestParam String response){
        try{
            reportsService.handleReport(id, status, admNo, response);
            return "success";
        }catch (Exception e){
            return "error" + e.getMessage();
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

    @GetMapping("/history")
    public String showReportHistory(Model model) {
        Integer memberNo = 1;
        List<Reports> list = reportsService.getReportsByMember(memberNo);
        model.addAttribute("reports", list);
        return "templates-report/report-history";
    }

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
     * 💡 核心修改：後台管理列表 (支援 Thymeleaf 與 分頁)
     * 存取路徑範例: /reports/admin/list?status=待處理&page=0
     */
    @GetMapping("/admin/list")
    public String showReportAdminPage(
            @RequestParam(value = "status", defaultValue = "待處理") String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        // 強制每頁顯示 6 筆
        int pageSize = 6;

        // 呼叫 Service 取得分頁資料
        Page<Reports> reportPage = reportsService.getReportsByStatusWithPagination(status, page, pageSize);

        // 將資料與當前狀態傳回前端
        model.addAttribute("reportPage", reportPage);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentPage", page);

        // 指向 templates/templates-report/ 目錄下的 admin-report-list.html
        return "templates-report/admin-report-list";
    }
}