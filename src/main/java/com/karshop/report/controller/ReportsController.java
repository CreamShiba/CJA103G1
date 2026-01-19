package com.karshop.report.controller;

import com.karshop.report.model.Reports;
import com.karshop.report.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    // 顯示檢舉頁面
    @GetMapping("/add")
    public String showReportPage() {
        return "templates-report/add-report"; // 請確認檔名是否為 add-report.html
    }

    // 處理檢舉提交
    @PostMapping("/submit")
    public String handleReport(@ModelAttribute Reports report, Model model) {

        // 設定預設值
        report.setReportsTimestamp(LocalDateTime.now());
        report.setStatus("待處理");
        report.setMemberNo(1); // 預設目前登入者
        report.setAdmNo(1);

        // 存入資料庫
        reportsService.submitReport(report);

        // 將檢舉對象或編號傳給成功頁面
        model.addAttribute("target", report.getReportsTarget());

        return "templates-report/report-success";
    }
}