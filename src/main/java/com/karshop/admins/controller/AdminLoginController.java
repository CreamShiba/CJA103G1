package com.karshop.admins.controller;

import java.util.List;
import java.util.Optional;

import com.karshop.admins.model.AdminService;
import com.karshop.admins.model.AdminVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admins")
public class AdminLoginController {

  @Autowired
  private AdminService adminService;

  // 顯示管理員登入頁面
  @GetMapping("/login")
  public String showLoginPage(HttpSession session) {
    // 如果 session 裡已經有 admin，就直接導到index（或你想去的頁面）
    if (session.getAttribute("admin") != null) {
      return "redirect:/admins/home";
    }
    return "back-end/admin_login";
  }

  // 處理登入
  @PostMapping("/login")
  public String processLogin(@RequestParam String adminAcc, @RequestParam String adminPwd, HttpSession session,
                             Model model) {

    // 1. 判斷帳號是否存在
    Optional<AdminVO> opt = adminService.findByAdminAcc(adminAcc);
    if (opt.isEmpty()) {
      model.addAttribute("errorMsgs", List.of("管理員帳號錯誤"));
      model.addAttribute("adminAcc", adminAcc);
      return "back-end/admin_login";
    }
    AdminVO admin = opt.get();

    // 2. 密碼比對
    if (!admin.getAdminPwd().equals(adminPwd)) {
      model.addAttribute("errorMsgs", List.of("管理員密碼錯誤"));
      model.addAttribute("adminAcc", adminAcc);
      return "back-end/admin_login";
    }

    // 3. 帳號狀態檢查：0 = 停權；1 = 啟用
    if (admin.getAdminStatus() == 0) {
      model.addAttribute("errorMsgs", List.of("帳號已被停權，如有問題請聯絡最高管理員"));
      model.addAttribute("adminAcc", adminAcc);
      return "back-end/admin_login";
    }

    // 4. 登入成功：放入 Session
    session.setAttribute("admin", admin);

    // 把功能授權也存進 session
    List<Integer> funcIds = admin.getAuths().stream()
            .map(auth -> auth.getAdminAuthList().getAuthNo())
            .toList();
    session.setAttribute("adminFuncIds", funcIds);

    // 5. 回跳原始頁面或導到管理員列表
    String location = (String) session.getAttribute("location");
    if (location != null) {
      session.removeAttribute("location");
      return "redirect:" + location;
    }
    return "redirect:/admins/home";
  }

  // 處理登出
  @PostMapping("/logout")
  public String logout(HttpSession session) {
    // 1. 銷毀 Session (這一步會清除所有存在 Session 中的使用者資訊與權限)
    session.invalidate();
    // 2. 重導向回登入頁面
    return "redirect:/admins/login";
  }

}