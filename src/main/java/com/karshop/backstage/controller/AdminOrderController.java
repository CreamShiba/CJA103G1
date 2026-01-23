package com.karshop.backstage.controller;

import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    OrdService ordService;

    @GetMapping
    public String searchOrders(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "ordStatus", required = false) String ordStatus,
                               @RequestParam(value = "startDate", required = false) LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) LocalDate endDate,
                               @RequestParam(value = "payoutStatus", required = false) String payoutStatus, ModelMap model) {

        List<OrdVO> ordList = ordService.searchOrders(keyword, ordStatus, startDate, endDate, payoutStatus);
        model.addAttribute("ordList", ordList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("ordStatus", ordStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("payoutStatus", payoutStatus);

        return "back-end/admin/order_management";
    }

    @PostMapping("/updatePayout")
    public String updatePayout(@RequestParam(value = "ordNo") Integer ordNo,
                                     @RequestParam(value = "payoutStatus") String payoutStatus, ModelMap model) {
        ordService.updatePayoutStatus(ordNo, payoutStatus);
        return "redirect:/admin/orders";
    }
}
