package com.karshop.membercar.controller;

import com.karshop.members.model.MembersVO;
import com.karshop.membercar.model.MemberCarVO;
import com.karshop.membercar.model.MemberCarService;
import com.karshop.carcategory.model.CarCategoryVO;
import com.karshop.carcategory.model.CarCategoryService; // 假設您有這個Service

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/members/cars")
public class MemberCarController {

  @Autowired
  private MemberCarService memberCarService;

  @Autowired
  private CarCategoryService carCategoryService; // 用於下拉選單顯示所有車型

  // 頁面：我的車庫
  @GetMapping
  public String myCars(HttpSession session, Model model) {
    // 1. 檢查登入狀態
    MembersVO member = (MembersVO) session.getAttribute("member");
    if (member == null) {
      return "redirect:/members/login";
    }

    // 2. 取得該會員的車輛列表
    List<MemberCarVO> myCars = memberCarService.getCarsByMemberId(member.getMemNo());
    model.addAttribute("myCars", myCars);

    // 3. 取得所有車型 (供新增車輛的下拉選單使用)
    List<CarCategoryVO> carCategories = carCategoryService.getAllCarCategories();
    model.addAttribute("carCategories", carCategories);

    // 4. 準備一個空的物件供 Form 表單綁定
    model.addAttribute("newCar", new MemberCarVO());

    return "front-end/members/my_cars"; // 對應 templates/front-end/members/my-cars.html
  }

  // 動作：新增車輛
  @PostMapping("/add")
  public String addCar(@ModelAttribute("newCar") MemberCarVO memberCar, HttpSession session) {
    MembersVO member = (MembersVO) session.getAttribute("member");
    if (member != null) {
      memberCar.setMember(member); // 強制設定為當前登入者
      memberCarService.addCar(memberCar);
    }
    return "redirect:/members/cars";
  }

  // 動作：刪除車輛
  @PostMapping("/delete")
  public String deleteCar(@RequestParam("memberCarNo") Integer memberCarNo) {
    memberCarService.deleteCar(memberCarNo);
    return "redirect:/members/cars";
  }
}