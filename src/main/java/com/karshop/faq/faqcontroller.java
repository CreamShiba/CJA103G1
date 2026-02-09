package com.karshop.faq;

import java.util.List;

import com.karshop.admins.model.AdminVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
public class faqcontroller {

    @Autowired
    private faqservice service;

    // ==================== 前台頁面 ====================

    /**
     * 前台 FAQ 列表頁
     * 路徑：GET /faq
     */
    @GetMapping("/faq")
    public String faqListPage(Model model) {
        List<faq> faqs = service.getPublishedFaqs();
        model.addAttribute("faqs", faqs);
        return "front-end/faq-list";
    }

    // ==================== 後台頁面 ====================

    /**
     * 後台 FAQ 管理頁（唯一的後台頁面）
     * 路徑：GET /admin/faq
     */
    @GetMapping("/admins/faq")
    public String adminFaqPage(Model model,
                               @RequestParam(required = false) Integer editId) {
        // 取得所有 FAQ
        List<faq> faqs = service.getAllFaqs();
        model.addAttribute("faqs", faqs);

        // 如果有 editId，表示要編輯，取出該 FAQ 資料
        if (editId != null) {
            faq editFaq = service.getFaqById(editId);
            model.addAttribute("editFaq", editFaq);
            model.addAttribute("isEdit", true);
        } else {
            // 新增時使用空物件
            model.addAttribute("editFaq", new faq());
            model.addAttribute("isEdit", false);
        }

        return "back-end/faq-index";
    }

    /**
     * 處理新增 FAQ
     * 路徑：POST /admin/faq/create
     */
    @PostMapping("/admins/faq/create")
    public String createFaq(@ModelAttribute faq entity, HttpSession session) {

        AdminVO admin = (AdminVO) session.getAttribute("admin");

        if (admin == null) {
            System.err.println("管理員未登入");
            return "redirect:/admins/login";
        }


        Integer admNo = admin.getAdminNo();

        System.out.println("=== 新增 FAQ ===");
        System.out.println("管理員編號: " + admNo);
        System.out.println("問題: " + entity.getQuestion());
        System.out.println("狀態: " + entity.getStatus());

        entity.setAdm_no(admNo);
        service.createFaq(entity);

        return "redirect:/admins/faq";
    }

    /**
     * 處理更新 FAQ
     * 路徑：POST /admin/faq/update
     */
    @PostMapping("/admins/faq/update")
    public String updateFaq(@RequestParam Integer id, @ModelAttribute faq entity) {
        service.updateFaq(id, entity);
        return "redirect:/admins/faq";
    }

    /**
     * 處理刪除 FAQ
     * 路徑：POST /admin/faq/delete
     */
    @PostMapping("/admins/faq/delete")
    public String deleteFaq(@RequestParam Integer id) {
        service.deleteFaq(id);
        return "redirect:/admins/faq";
    }
    /**
     * 快速切換 FAQ 狀態（發布 ↔ 草稿）
     * 路徑：POST /admin/faq/toggle-status
     */
    @PostMapping("/admins/faq/toggle-status")
    public String toggleFaqStatus(@RequestParam Integer id) {
        faq entity = service.getFaqById(id);
        if (entity != null) {
            // 切換狀態：發布 → 草稿，草稿 → 發布
            String newStatus = entity.getStatus().equals("已發佈") ? "草稿" : "已發佈";
            entity.setStatus(newStatus);
            service.updateFaq(id, entity);
        }
        return "redirect:/admins/faq";
    }
}