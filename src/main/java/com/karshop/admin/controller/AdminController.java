package com.karshop.admin.controller;

import com.karshop.admin.model.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import com.karshop.admin.model.AdminVO;
@Controller
@RequestMapping("/admins")
public class AdminController {
  @Autowired
  private AdminService adminService;

  @GetMapping("/search")
  public String search(Model model){
    List<AdminVO> admins = adminService.findAll();
    model.addAttribute("admins", admins);
    return "admins/search";
  }
}
