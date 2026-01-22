package com.karshop.backstage.controller;

import com.karshop.sellertest.model.SellerService;
import com.karshop.sellertest.model.SellerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/sellers")
public class AdminSellerController {

    @Autowired
    private SellerService sellerService;

    @GetMapping
    public String listSeller(@RequestParam(value = "keyword", required = false) String keyword,
                             @RequestParam(value = "status", required = false) String status, ModelMap model){

        List<SellerVO> sellerList = sellerService.searchSellers(keyword, status);
        model.addAttribute("sellerList", sellerList);
        model.addAttribute("keyword", keyword);
        model.addAttribute("status", status);
        return "back-end/admin/seller_management";
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam(value = "sellerNo")  Integer sellerNo,
                               @RequestParam(value = "status") String status){
        sellerService.updateStatus(sellerNo, status);

        return "redirect:/admin/sellers";
    }

}
