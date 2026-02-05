package com.karshop.rating.controller;

import com.karshop.rating.model.RatingService;
import com.karshop.sellertest.model.SellerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@RequestMapping("/rating")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @PostMapping("add")
    public String addRating(@RequestParam("ordNo") Integer ordNo,
                            @RequestParam("memberNo") Integer memberNo,
                            @RequestParam("score") Integer score,
                            @RequestParam("comment") String comment,
                            @SessionAttribute(value = "seller") SellerVO sellerVO){

        Integer sellerNo = sellerVO.getSellerNo();
        ratingService.addRating(ordNo, sellerNo, memberNo, score, comment);
    return "redirect:/product/dashboard?tab=orders";
    }

}
