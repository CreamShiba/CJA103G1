package com.karshop.announcement;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import com.karshop.admins.model.AdminVO;

@Controller
public class announcementcontroller {

    @Autowired
    private announcementservice service;

    // ==================== 前台頁面 ====================

    /**
     * 前台公告列表頁
     * 路徑：GET /announcement
     */
    @GetMapping("/announcement")
    public String announcementListPage(Model model) {
        List<announcement> announcements = service.getAnnouncementsByStatus("發佈");
        model.addAttribute("announcements", announcements);
        return "front-end/announcement-list";
    }

    // ==================== 後台頁面 ====================

    /**
     * 後台公告管理頁
     */
    @GetMapping("/admins/announcement")
    public String adminAnnouncementPage(Model model) {
        List<announcement> announcements = service.getAllAnnouncements();
        model.addAttribute("announcements", announcements);
        return "back-end/announcement-index";
    }
    /**
     * 處理新增公告
     * 路徑：POST /admins/announcement/create
     */
    @PostMapping("/admins/announcement/create")
    public String createAnnouncement(@ModelAttribute announcement entity, HttpSession session) {
        // ✅ 從 session 取得 AdminVO
        AdminVO admin = (AdminVO) session.getAttribute("admin");

        if (admin == null) {
            System.err.println("管理員未登入");
            return "redirect:/admins/login";
        }

        Integer admNo = admin.getAdminNo();

        System.out.println("=== 新增公告 ===");
        System.out.println("管理員編號: " + admNo);
        System.out.println("公告標題: " + entity.getTitle());

        entity.setAdm_no(admNo);
        service.createAnnouncement(entity);

        return "redirect:/admins/announcement";
    }

    /**
     * 處理更新公告
     */
    @PostMapping("/admins/announcement/update")
    public String updateAnnouncement(@RequestParam Integer id, @ModelAttribute announcement entity) {
        Optional<announcement> existing = service.getAnnouncementById(id);
        if (existing.isPresent()) {
            announcement current = existing.get();
            current.setTitle(entity.getTitle());
            current.setContent(entity.getContent());
            current.setStatus(entity.getStatus());
            service.updateAnnouncement(id, current);
        }
        return "redirect:/admins/announcement";
    }

    /**
     * 快速切換公告狀態（發布 ↔ 草稿）
     */
    @PostMapping("/admins/announcement/toggle-status")
    public String toggleAnnouncementStatus(@RequestParam Integer id) {
        Optional<announcement> opt = service.getAnnouncementById(id);
        if (opt.isPresent()) {
            announcement entity = opt.get();
            String newStatus = entity.getStatus().equals("發佈") ? "草稿" : "發佈";
            entity.setStatus(newStatus);
            service.updateAnnouncement(id, entity);
        }
        return "redirect:/admins/announcement";
    }

    /**
     * 處理刪除公告
     */
    @PostMapping("/admins/announcement/delete")
    public String deleteAnnouncement(@RequestParam Integer id) {
        service.deleteAnnouncement(id);
        return "redirect:/admins/announcement";
    }
}