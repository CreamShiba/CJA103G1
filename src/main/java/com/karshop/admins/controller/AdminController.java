package com.karshop.admins.controller;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageImpl;
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

  // 定義特殊權限 ID
  private static final int SUPER_ADMIN_AUTH_ID = 1; // 超級管理員
  private static final int ADMIN_EDIT_AUTH_ID = 2;  // 管理員編輯權限

  private boolean hasAuth(AdminVO admin, int authId) {
    if (admin == null || admin.getAuths() == null) return false;
    return admin.getAuths().stream()
            .anyMatch(auth -> auth.getAdminAuthList().getAuthNo() == authId);
  }

// 顯示查詢結果 —— 整合搜尋 (支援分頁)
  @GetMapping("/search")
  public String search(@RequestParam(required = false) String keyword,
                       @RequestParam(name = "page", defaultValue = "0") int page,
                       Model model) {

    // 1. 如果關鍵字是空的，直接導回列表頁
    if (keyword == null || keyword.trim().isEmpty()) {
      return "redirect:/admins/listAll";
    }

    // 2. 執行搜尋邏輯 (取得所有符合的結果)
    List<AdminVO> searchResults = new ArrayList<>();

    // 判斷關鍵字是否為數字 (如果是數字，嘗試用 ID 搜尋)
    if (keyword.matches("\\d+")) {
      AdminVO byId = adminService.getById(Integer.valueOf(keyword));
      if (byId != null) {
        searchResults.add(byId);
      }
      // 同時也搜尋名稱中包含該數字的情況 (可選)
      searchResults.addAll(adminService.findByNameLike(keyword));

      // 去除重複 (如果 ID 和 Name 搜尋到同一筆)
      searchResults = searchResults.stream().distinct().toList();
    } else {
      // 非數字，僅搜尋名稱
      searchResults = adminService.findByNameLike(keyword);
    }

    // 3. 手動執行分頁邏輯 (List -> Page)
    // 因為 Service 的搜尋目前回傳 List，需轉為 Page 物件以配合前端 th:if="${adminPage.totalPages > 0}"
    int pageSize = 5;
    Pageable pageable = PageRequest.of(page, pageSize);

    int start = (int) pageable.getOffset();
    int end = Math.min((start + pageable.getPageSize()), searchResults.size());

    Page<AdminVO> adminPage;
    if (start > searchResults.size()) {
      adminPage = new PageImpl<>(Collections.emptyList(), pageable, searchResults.size());
    } else {
      adminPage = new PageImpl<>(searchResults.subList(start, end), pageable, searchResults.size());
    }

    // 4. 將資料放入 Model
    model.addAttribute("adminPage", adminPage); // 用於分頁與表格顯示
    model.addAttribute("keyword", keyword);     // 將關鍵字傳回前端，讓搜尋框保留文字
    model.addAttribute("activePage", "listAll"); // 保持側邊欄選中狀態

    // 5. 回傳與 listAll 相同的視圖
    return "back-end/admin_listAll";
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
    model.addAttribute("activePage", "listAll");
    return "back-end/admin_listAll";
  }

  // 顯示新增表單
  @GetMapping("/add")
  public String showAddForm(Model model) {
    model.addAttribute("form", new AdminDTO());
    model.addAttribute("allFunctions", adminAuthListService.getAll());
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
  public String showEditForm(@RequestParam("adminNo") Integer adminNo, Model model, RedirectAttributes ra, HttpSession session) {

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

    // ★ 權限檢查邏輯
    AdminVO currentLoginAdmin = (AdminVO) session.getAttribute("admin");
    boolean isCurrentUserSuper = hasAuth(currentLoginAdmin, SUPER_ADMIN_AUTH_ID);

    // 2. 把 DTO 和 allFunctions 放入 Model
    model.addAttribute("isCurrentUserSuper", isCurrentUserSuper);
    model.addAttribute("superAdminAuthId", SUPER_ADMIN_AUTH_ID);
    model.addAttribute("adminEditAuthId", ADMIN_EDIT_AUTH_ID);
    model.addAttribute("form", form);
    model.addAttribute("allFunctions", adminAuthListService.getAll());


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
      return "back-end/admin_edit";
    }

    // 2. 先讀出原本的 VO，從 DTO 拿 adminNo
    AdminVO vo = adminService.getById(form.getadminNo());
    AdminVO currentLoginAdmin = (AdminVO) session.getAttribute("admin");
    AdminVO originalTargetAdmin = adminService.getById(form.getadminNo()); // 從 DB 查出原始狀態
    //判斷當前使用者是不是超級管理員
    boolean isCurrentUserSuper = hasAuth(currentLoginAdmin, SUPER_ADMIN_AUTH_ID);

    // ★★★ 權限防護邏輯開始 ★★★

    if (!isCurrentUserSuper) {
      // --- 防護 A: 針對 Auth ID 1 (超級管理員) ---
      // 規則：非超級管理員絕對不能讓表單包含 ID 1
      if (form.getManageFuncIds().contains(SUPER_ADMIN_AUTH_ID)) {
        form.getManageFuncIds().remove(Integer.valueOf(SUPER_ADMIN_AUTH_ID));
      }

      // --- 防護 B: 針對 Auth ID 2 (管理員編輯權限) ---
      // 規則：非超級管理員不能修改此權限，必須維持「資料庫原本的狀態」

      boolean targetHasAuth2Originally = hasAuth(originalTargetAdmin, ADMIN_EDIT_AUTH_ID);
      boolean formHasAuth2 = form.getManageFuncIds().contains(ADMIN_EDIT_AUTH_ID);

      if (targetHasAuth2Originally) {
        // 情況 1: 原本有，但表單被拿掉了 (試圖剝奪權限) -> 強制加回去
        if (!formHasAuth2) {
          form.getManageFuncIds().add(ADMIN_EDIT_AUTH_ID);
        }
      } else {
        // 情況 2: 原本沒有，但表單偷加了 (試圖賦予權限) -> 強制移除
        if (formHasAuth2) {
          form.getManageFuncIds().remove(Integer.valueOf(ADMIN_EDIT_AUTH_ID));
        }
      }
    }
    // ★★★ 權限防護邏輯結束 ★★★

    // 3. 更新基本欄位
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