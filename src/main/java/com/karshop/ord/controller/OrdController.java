package com.karshop.ord.controller;


import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import com.karshop.product.model.ProductService;
import com.karshop.product.model.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/ord")
public class OrdController {

    @Autowired
    private OrdService ordService;

    @Autowired
    private ProductService productService;

    @GetMapping("/getOneOrder")
    public String getOneOrder(@RequestParam(value = "ordNo") Integer ordNo, ModelMap model) {

        OrdVO ordVO = ordService.getOneOrd(ordNo);

        model.addAttribute("ordVO", ordVO);
        return "back-end/seller/listOneOrder";
    }

    @PostMapping("/ship")
    public String shipOrder(@RequestParam(value = "ordNo") Integer  ordNo,
                            @RequestParam(value = "ordShipNo") String ordShipNo) {
        OrdVO ordVO = ordService.getOneOrd(ordNo);

        if(ordVO.getOrdStatus().equals("待出貨")){
            ordVO.setOrdShipNo(ordShipNo);
            ordVO.setOrdStatus("已出貨");
            ordService.updateOrd(ordVO);
        }

        return "redirect:/product/dashboard?tab=orders";
    }

    @PostMapping("/cancel")
    public String cancelOrder(@RequestParam(value = "ordNo") Integer ordNo,
                              @RequestParam(value = "cancelReason") String cancelReason) {
    OrdVO ordVO = ordService.getOneOrd(ordNo);

    if(ordVO == null){
        return "redirect:/product/dashboard?tab=orders";
    }

    if(!ordVO.getOrdStatus().equals("待出貨")){
        System.out.println("非待出貨狀態無法取消訂單");
        return "redirect:/product/dashboard?tab=orders";
    }

    ordVO.setCancelReason(cancelReason);
    ordVO.setOrdStatus("已取消");
    ordService.updateOrd(ordVO);

        return "redirect:/product/dashboard?tab=orders";
    }

    @GetMapping("/searchOrders")
    public String searchOrders(@RequestParam(value = "keyword", required = false) String keyword,
                               @RequestParam(value = "ordStatus", required = false) String ordStatus,
                               @RequestParam(value = "startDate", required = false) LocalDate startDate,
                               @RequestParam(value = "endDate", required = false) LocalDate endDate, ModelMap model) {

        Integer sellerNo = 101;
        List<OrdVO> searchResult = ordService.searchOrdersForSeller(sellerNo, keyword, ordStatus, startDate, endDate);
        model.addAttribute("ordList", searchResult);
        model.addAttribute("activeTab", "orders");
        model.addAttribute("keyword", keyword);
        model.addAttribute("ordStatus", ordStatus);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "back-end/seller/seller_index";
    }

    @ModelAttribute
    public void populateCommonData(ModelMap model) {
        Integer sellerNo = 101;

        List<OrdVO> ordList = ordService.getOrdBySeller(sellerNo);

        int pendingOrder = 0;
        int allOrder = 0;

        for (OrdVO ordVO : ordList) {
            String status = ordVO.getOrdStatus();
            if (status.equals("待出貨")) {
                pendingOrder++;
            }
            if (status.equals("待出貨") || status.equals("已完成") || status.equals("已出貨")) {
                allOrder++;
            }
        }
        model.addAttribute("pendingOrder", pendingOrder);
        model.addAttribute("allOrder", allOrder);


        List<ProductVO> productList = productService.getProductsBySellerNo(sellerNo);

        int activeProductCount = 0;
        for(ProductVO productVO : productList){
            if(productVO.getProdStatus().equals("上架中")){
                activeProductCount++;
            }
        }
        model.addAttribute("productList", productList);
        model.addAttribute("activeProductCount", activeProductCount);

    }

}
