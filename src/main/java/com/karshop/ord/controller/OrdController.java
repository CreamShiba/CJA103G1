package com.karshop.ord.controller;


import com.karshop.ord.model.OrdService;
import com.karshop.ord.model.OrdVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/ord")
public class OrdController {

    @Autowired
    private OrdService ordService;

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




//    @GetMapping("/sellerOrder")
//    public String sellerOrder(ModelMap model) {
//        int sellerNo = 101;
//        List<OrdVO> ordList = ordService.getOrdBySeller(sellerNo);
//
//        model.addAttribute("ordList", ordList);
//
//
//        return "seller/seller_order";
//    }

}
