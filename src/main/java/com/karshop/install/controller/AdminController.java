package com.karshop.install.controller;

import com.karshop.admins.model.AdminVO; // Updated import
import com.karshop.install.entity.InstallOrder;
import com.karshop.install.entity.Technician;
import com.karshop.install.enums.OrderStatus;
import com.karshop.install.enums.PayoutStatus;
import com.karshop.install.repository.InstallOrderRepository;
import com.karshop.install.service.PayoutService;
import com.karshop.install.service.TechnicianManagementService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller("installAdminController")
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TechnicianManagementService technicianService;
    private final PayoutService payoutService;
    private final InstallOrderRepository orderRepository;

    /**
     * ✅ 管理員儀表板 (Unified Dashboard)
     * 處理所有後台管理分頁的顯示邏輯，包含：技師審核、技師管理、撥款管理、服務項目、場地管理。
     *
     * @param tab     當前選中的分頁 (預設為 'audit')
     * @param session 用於驗證管理員是否登入
     * @param model   用於傳遞資料到前端頁面
     * @return admin-backend.html 模板
     */
    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(defaultValue = "audit") String tab, HttpSession session, Model model) {
        // 1. 權限驗證：檢查 Session 是否有 Admin 物件
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        model.addAttribute("currentTab", tab);

        // 2. 根據 tab 參數載入對應的資料
        if ("audit".equals(tab)) {
            // [技師審核] 取得所有狀態為 PENDING (0) 的技師申請
            List<Technician> pendingTechs = technicianService.getPendingApplications();
            model.addAttribute("pendingTechs", pendingTechs);
        } else if ("technicians".equals(tab)) {
            // [技師管理] 取得所有通過審核的技師 (Active)
            List<Technician> activeTechs = technicianService.getApprovedTechnicians();
            model.addAttribute("activeTechs", activeTechs);
        } else if ("payout".equals(tab)) {
            // [撥款管理]
            // 待撥款 (訂單完成但尚未轉帳)
            List<InstallOrder> pendingPayouts = orderRepository.findPendingPayoutsWithDetails(
                    OrderStatus.COMPLETED.getCode(), PayoutStatus.PENDING.getCode());
            model.addAttribute("pendingPayouts", pendingPayouts);

            // 已撥款 (已完成轉帳)
            List<InstallOrder> completedPayouts = orderRepository.findCompletedPayoutsWithDetails(
                    OrderStatus.COMPLETED.getCode(), PayoutStatus.COMPLETED.getCode());
            model.addAttribute("completedPayouts", completedPayouts);
        } else if ("services".equals(tab)) {
            // [服務項目管理] 取得所有可用的服務項目
            List<com.karshop.install.entity.ServiceItem> services = technicianService.getAllServiceItems();
            model.addAttribute("services", services);
        } else if ("locations".equals(tab)) {
            // [場地管理] 取得所有安裝場地
            List<com.karshop.install.entity.InstallLocation> locations = technicianService.getAllLocations();
            model.addAttribute("locations", locations);
        }

        return "install/admin-backend";
    }

    // --- 舊路徑相容性 (Redirects) ---

    // 將舊的 /admin/audit 請求導向到 dashboard
    @GetMapping("/audit")
    public String auditRedirect() {
        return "redirect:/admin/dashboard?tab=audit";
    }

    // 將舊的 /admin/payout 請求導向到 dashboard
    @GetMapping("/payout")
    public String payoutRedirect() {
        return "redirect:/admin/dashboard?tab=payout";
    }

    // --- 技師審核功能 (Audit Actions) ---

    /**
     * ✅ 通過技師申請
     * 將技師狀態改為 Active 並設為工程師身份
     */
    @PostMapping("/audit/{id}/approve")
    public String approveTechnician(@PathVariable Integer id, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.approveTechnician(id);
        return "redirect:/admin/dashboard?tab=audit";
    }

    /**
     * ❌ 駁回技師申請
     * 刪除該筆申請紀錄
     */
    @PostMapping("/audit/{id}/reject")
    public String rejectTechnician(@PathVariable Integer id, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.rejectTechnician(id);
        return "redirect:/admin/dashboard?tab=audit";
    }

    /**
     * ⛔ 暫時停權技師
     * 將技師狀態退回審核狀態 (0)
     */
    @PostMapping("/technician/{id}/suspend")
    public String suspendTechnician(@PathVariable Integer id, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        try {
            technicianService.suspendTechnician(id);
        } catch (IllegalStateException e) {
            return "redirect:/admin/dashboard?tab=technicians&error="
                    + java.net.URLEncoder.encode(e.getMessage(), java.nio.charset.StandardCharsets.UTF_8);
        }
        return "redirect:/admin/dashboard?tab=technicians";
    }

    // --- 撥款管理功能 (Payout Actions) ---

    /**
     * 💰 執行撥款
     * 確認已轉帳給技師，更新訂單撥款狀態為 COMPLETED
     */
    @PostMapping("/payout/{orderId}")
    public String executePayout(@PathVariable Integer orderId, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        payoutService.executePayout(orderId);
        return "redirect:/admin/dashboard?tab=payout";
    }

    // --- 服務項目管理功能 (Service Item Management) ---

    /**
     * ➕ 新增服務項目
     */
    @PostMapping("/services")
    public String createServiceItem(@RequestParam String serviceName, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        try {
            technicianService.createServiceItem(serviceName, admin);
        } catch (Exception e) {
            // 忽略重複名稱錯誤
        }
        return "redirect:/admin/dashboard?tab=services";
    }

    /**
     * ✏️ 修改服務項目名稱
     */
    @PostMapping("/services/{id}/update")
    public String updateServiceItem(@PathVariable Integer id, @RequestParam String serviceName, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.updateServiceItem(id, serviceName);
        return "redirect:/admin/dashboard?tab=services";
    }

    /**
     * 🗑️ 刪除服務項目
     */
    @PostMapping("/services/{id}/delete")
    public String deleteServiceItem(@PathVariable Integer id, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.deleteServiceItem(id);
        return "redirect:/admin/dashboard?tab=services";
    }

    // --- 場地管理功能 (Location Management) ---

    /**
     * ➕ 新增場地
     */
    @PostMapping("/locations")
    public String createLocation(@RequestParam String locationName, @RequestParam String address,
            @RequestParam Integer pricePer, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.createLocation(locationName, address, pricePer);
        return "redirect:/admin/dashboard?tab=locations";
    }

    /**
     * ✏️ 修改場地資訊
     */
    @PostMapping("/locations/{id}/update")
    public String updateLocation(@PathVariable Integer id, @RequestParam String locationName,
            @RequestParam String address, @RequestParam Integer pricePer, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.updateLocation(id, locationName, address, pricePer);
        return "redirect:/admin/dashboard?tab=locations";
    }

    /**
     * 🗑️ 刪除場地
     */
    @PostMapping("/locations/{id}/delete")
    public String deleteLocation(@PathVariable Integer id, HttpSession session) {
        AdminVO admin = (AdminVO) session.getAttribute("admin");
        if (admin == null)
            return "redirect:/admins/login";

        technicianService.deleteLocation(id);
        return "redirect:/admin/dashboard?tab=locations";
    }
}
