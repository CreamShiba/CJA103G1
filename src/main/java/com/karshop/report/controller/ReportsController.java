package com.karshop.report.controller;

import com.karshop.report.model.Reports;
import com.karshop.report.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reports")
public class ReportsController {

    @Autowired
    private ReportsService reportsService;

    @GetMapping("/add")
    public String showAddPage(){
        return "add-report";
    }

    @PostMapping("/submit")
    public String handleForm(@ModelAttribute Reports report, Model model){
        reportsService.submitReport(report);

        model.addAttribute("reportNo", report.getReportsNo());
        return "report-success";
    }
}
