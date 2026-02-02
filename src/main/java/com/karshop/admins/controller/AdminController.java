package com.karshop.admins.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.karshop.admins.model.AdminDTO;
import com.karshop.admins.model.AdminService;
import com.karshop.admins.model.AdminVO;
import com.karshop.adminauthmanage.model.AdminAuthListService;
import com.karshop.adminauthmanage.model.AdminAuthListVO;

@Controller
@RequestMapping("/admins")
public class AdminController {

  @Autowired
  private AdminService adminService;

  @Autowired
  private AdminAuthListService adminAuthListService;

  // 管理員首頁
  @GetMapping("/home")
  public String showIndex() {
    return "back-end/admin_index";
  }

  // 搜尋頁面
  @GetMapping("/selectPage")
  public String showSelectPage(Model model) {
    model.addAttribute("currentPage", "accounts");
    model.addAttribute("currentPage2", "adminSelect");
    return "back-end/admin_selectPage";
  }

  // 顯示查詢結果（多筆）—— 單一條件
  @GetMapping("/search")
  public String search(@RequestParam(required = false) String adminNo,
                       @RequestParam(required = false) String adminName, Model model) {

    List<AdminVO> results = new ArrayList<>();

    if (adminNo != null && !adminNo.isBlank()) {
      // 依 ID 精確查一筆
      if (adminNo.matches("\\d+")) {
        AdminVO a = adminService.getById(Integer.valueOf(adminNo));
        if (a != null) {
          results.add(a);
        }
      } else {
        // 非數字就跳回查詢頁並帶錯誤訊息
        model.addAttribute("errorMsg", "管理員編號必須是數字");
        return "back-end/admin_selectPage"; // 查詢表單頁面
      }

    } else if (adminName != null && !adminName.isBlank()) {
      // 依名稱模糊查詢
      results = adminService.findByNameLike(adminName);
    }

    model.addAttribute("admins", results);
    model.addAttribute("currentPage", "accounts");
    return "back-end/admin_search";
  }

  @GetMapping("/listAll")
  public String listAll(
          @RequestParam(name = "page", defaultValue = "0") int page,
          Model model) {
    if (page < 0) {
      page = 0;
    }
    // 設定每頁顯示 5 筆
    int pageSize = 5;
    Pageable pageable = PageRequest.of(page, pageSize);

    Page<AdminVO> adminPage = adminService.getAll(pageable);

    // 防呆：如果請求的頁數超過總頁數，且總頁數大於 0
    // adminPage.getTotalPages() 會回傳總頁數
    if (page >= adminPage.getTotalPages() && adminPage.getTotalPages() > 0) {
      // 重新導向到「最後一頁」 (索引值 = 總頁數 - 1)
      return "redirect:/admins/listAll?page=" + (adminPage.getTotalPages() - 1);
    }

    // 取出所有
    List<AdminVO> admins = adminService.getAll();

    model.addAttribute("adminPage", adminPage);
    model.addAttribute("admins", admins);
    model.addAttribute("currentPage", "accounts");
    model.addAttribute("currentPage2", "adminList");
    return "back-end/admin_listAll";
  }

  // 顯示新增表單
  @GetMapping("/add")
  public String showAddForm(Model model) {
    model.addAttribute("form", new AdminDTO());
    model.addAttribute("allFunctions", adminAuthListService.getAll());
    model.addAttribute("currentPage", "accounts");
    model.addAttribute("currentPage2", "adminAdd");
    return "back-end/admin_add";
  }

  @PostMapping("/add")
  public String addAdministrant(@Valid @ModelAttribute("form") AdminDTO form, BindingResult result,
                                Model model, RedirectAttributes ra) {

    // 1. 帳號唯一性檢查
    if (adminService.existsByAdminAcc(form.getAdminAcc())) {
      result.rejectValue("adminAcc", "error.form", "帳號已存在，請使用其他帳號");
    }

    // 2. 驗證失敗 → 回到表單，並補入 allFunctions
    if (result.hasErrors()) {
      List<String> errorMsgs = result.getFieldErrors().stream().map(FieldError::getDefaultMessage).toList();
      model.addAttribute("errorMsgs", errorMsgs);
      model.addAttribute("allFunctions", adminAuthListService.getAll());
      model.addAttribute("currentPage", "accounts");
      model.addAttribute("currentPage2", "adminAdd");
      return "back-end/admin_add";
    }

    // 3. DTO → VO
    AdminVO vo = new AdminVO();
    vo.setAdminAcc(form.getAdminAcc());
    vo.setAdminPwd(form.getAdminPwd());
    vo.setAdminName(form.getAdminName());
    vo.setAdminEmail(form.getAdminEmail());
    vo.setAdminCreatedAt(new Timestamp(System.currentTimeMillis()));
    vo.setAdminUpdatedAt(new Timestamp(System.currentTimeMillis()));
    // 4. 處理功能分配：依 form.getManageFuncIds() 建中介實體集合
    for (Integer funcId : form.getManageFuncIds()) {
      com.karshop.adminauth.model.AdminAuthVO auth = new com.karshop.adminauth.model.AdminAuthVO();
      auth.setAdmin(vo);
      AdminAuthListVO func = adminAuthListService.findById(funcId)
              .orElseThrow(() -> new IllegalArgumentException("功能不存在：" + funcId));
      auth.setAdmAuthList(func);
      vo.getAuths().add(auth);
    }

    // 5. 呼叫 Service 寫入
    adminService.create(vo);
    ra.addFlashAttribute("successMsg", "新增成功！");
    return "redirect:/admins/listAll";
  }

  // 顯示編輯頁面
  @GetMapping("/edit")
  public String showEditForm(@RequestParam("adminNo") Integer adminNo, Model model, RedirectAttributes ra) {

    // 防呆：若無 adminNo，就導回查詢頁並顯示錯誤
    if (adminNo == null) {
      ra.addFlashAttribute("errorMsg", "必須指定管理員 ID");
      return "redirect:/admins/listAll";
    }

    // 讀取資料，若找不到也跳回查詢頁
    AdminVO vo = adminService.getById(adminNo);
    if (vo == null) {
      ra.addFlashAttribute("errorMsg", "找不到指定的管理員");
      return "redirect:/admins/listAll";
    }

    // ----- 下面開始改成用 DTO -----
    // 1. 建立 DTO 並把 VO 的資料搬過去
    AdminDTO form = new AdminDTO();
    form.setadminNo(vo.getAdminNo());
    form.setAdminAcc(vo.getAdminAcc());
    form.setAdminPwd(vo.getAdminPwd());
    form.setAdminName(vo.getAdminName());
    form.setAdminEmail(vo.getAdminEmail());
    form.setAdminStatus(vo.getAdminStatus());
    // 把原本的功能授權 ID 全搬到 DTO 的清單
    List<Integer> selected = new ArrayList<>();
    for (com.karshop.adminauth.model.AdminAuthVO auth : vo.getAuths()) {
      selected.add(auth.getAdminAuthList().getAuthNo());
    }
    form.setManageFuncIds(selected);

    // 2. 把 DTO 和 allFunctions 放入 Model
    model.addAttribute("form", form);
    model.addAttribute("allFunctions", adminAuthListService.getAll());
    model.addAttribute("currentPage", "accounts");

    return "back-end/admin_edit";
  }

  // 處理「編輯管理員」提交

  @PostMapping("/edit")
  public String updateAdmin(
          @Valid @ModelAttribute("form") AdminDTO form,
          BindingResult result,
          HttpSession session,
          Model model,
          RedirectAttributes ra) {

    // 1. 驗證失敗 → 回到表單並補 allFunctions
    if (result.hasErrors()) {
      List<String> errorMsgs = result.getFieldErrors().stream()
              .map(FieldError::getDefaultMessage)
              .toList();
      model.addAttribute("errorMsgs", errorMsgs);
      model.addAttribute("allFunctions", adminAuthListService.getAll());
      model.addAttribute("currentPage", "accounts");
      return "back-end/admin_edit";
    }

    // 2. 先讀出原本的 VO，從 DTO 拿 adminNo
    AdminVO vo = adminService.getById(form.getadminNo());

    // 3. 更新基本欄位
    vo.setAdminAcc(form.getAdminAcc());
    vo.setAdminPwd(form.getAdminPwd());
    vo.setAdminName(form.getAdminName());
    vo.setAdminStatus(form.getAdminStatus());
    vo.setAdminUpdatedAt(new Timestamp(System.currentTimeMillis()));

    // 4. 重建中介表關聯
    vo.getAuths().clear();
    for (Integer funcId : form.getManageFuncIds()) {
      com.karshop.adminauth.model.AdminAuthVO auth = new com.karshop.adminauth.model.AdminAuthVO();
      auth.setAdmin(vo);
      AdminAuthListVO func = adminAuthListService.findById(funcId)
              .orElseThrow(() -> new IllegalArgumentException("功能不存在：" + funcId));
      auth.setAdmAuthList(func);
      vo.getAuths().add(auth);
    }

    // 5. 呼叫 Service 更新
    adminService.update(vo);
    ra.addFlashAttribute("successMsg", "更新成功！");
   // 只有在修改「自己」時，才更新 Session ★★★
    AdminVO currentLoginAdmin = (AdminVO) session.getAttribute("admin");

    // 進行比對 (防止空指針例外，先檢查 currentLoginAdmin 是否存在)
    if (currentLoginAdmin != null &&
            currentLoginAdmin.getAdminNo().equals(vo.getAdminNo())) {

      // 情況 A: 如果你 Session 裡存的是整個物件
      currentLoginAdmin.setAdminName(vo.getAdminName());
      // 如果有改頭像或其他欄位，也要在這裡 set 進去
      // currentLoginAdmin.setAdminAvatar(vo.getAdminAvatar());
      session.setAttribute("admin", currentLoginAdmin); // 寫回 Session

      // 情況 B: 如果你 Session 裡只存了名字字串 (Key 是 "adminName")
      session.setAttribute("adminName", vo.getAdminName());
    }
    return "redirect:/admins/listAll";
  }

}