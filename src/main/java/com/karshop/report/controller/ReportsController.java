package com.karshop.report.controller;

import com.karshop.report.model.Reports;
import com.karshop.report.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
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
        // 設定存檔預設值
        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理");
        report.setMemberNo(1); // 開發階段預設目前登入者為 1
        report.setAdmNo(1);

        // 呼叫 Service 執行存入資料庫動作
        reportsService.submitReport(report);

        // 將檢舉對象傳給成功頁面顯示
        model.addAttribute("target", report.getReportsTarget());

        return "templates-report/report-success";
    }

    // 提供 JSON 格式的所有檢舉資料 (供管理後台使用)
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

    // 顯示該會員的所有檢舉紀錄列表
    @GetMapping("/history")
    public String showReportHistory(Model model) {
        Integer memberNo = 1; // 開發階段寫死會員編號為 1
        List<Reports> list = reportsService.getReportsByMember(memberNo);
        model.addAttribute("reports", list);
        return "templates-report/report-history";
    }

    // 💡 新增：顯示檢舉紀錄的詳細資訊頁面
    @GetMapping("/history/detail")
    public String showReportHistoryDetail(@RequestParam("id") Integer id, Model model) {
        // 透過 ID 取得該筆檢舉的詳細資料
        Reports report = reportsService.getReportById(id);

        if (report != null) {
            model.addAttribute("report", report);
            return "templates-report/report-detail"; // 指向詳細內容頁面
        } else {
            return "redirect:/reports/history"; // 找不到資料則導回列表
        }
    }
}