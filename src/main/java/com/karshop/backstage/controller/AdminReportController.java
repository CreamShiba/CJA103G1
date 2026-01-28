package com.karshop.backstage.controller;

import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import com.karshop.reporttest.model.ReportService;
import com.karshop.reporttest.model.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String listReports(@RequestParam(value = "status", defaultValue = "all") String status, ModelMap model) {
        List<ReportVO> reportList = reportService.getReportsByStatus(status);
        model.addAttribute("reportList", reportList);

        // 讓前端知道現在選的是哪個，以便「選單維持選中狀態」
        model.addAttribute("currentStatus", status);

        // 指定 activePage 讓側邊欄亮起來
        model.addAttribute("activePage", "reports");
        return "back-end/admin/report_management";
    }

    @PostMapping("/approve")
    public String approveReport(@RequestParam("reportNo") Integer reportNo,
                                @RequestParam("prodNo") Integer prodNo,
                                RedirectAttributes redirectAttributes) {
        Integer admNo = 1;
        reportService.processReport(reportNo, "已處理", admNo);

        ProductVO productVO = productService.getOneProduct(prodNo);
        productVO.setProdStatus("違規下架");
        productService.updateProduct(productVO);

        redirectAttributes.addAttribute("status", "已處理");
        return "redirect:/admin/reports";
    }

    @PostMapping("/reject")
    public String rejectReport(@RequestParam("reportNo") Integer reportNo, RedirectAttributes redirectAttributes) {
        Integer admNo = 1;
        reportService.processReport(reportNo, "駁回", admNo);
        redirectAttributes.addAttribute("status", "駁回");
        return "redirect:/admin/reports";
    }

}
