package com.karshop.system_message;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class system_messagecontroller {

    @Autowired
    private system_messageservice service;

    // ==================== 前台頁面 ====================

    /**
     * 前台 - 會員查看自己的系統通知列表
     * 路徑：GET /member/notifications
     * 注意：實際應用中需要從 Session 或 Security Context 取得當前登入會員的 memberNo
     */
    @GetMapping("/members/notifications")
    public String memberNotificationsPage(Model model,
                                          @SessionAttribute(name = "member_no", required = false) Integer member_no) {
        if (member_no == null) {
            // 如果沒有登入，導向登入頁
            return "redirect:/login";
        }

        // 取得該會員的所有通知
        List<system_message> messages = service.getMessagesByMemberNo(member_no);
        Long unreadCount = service.getUnreadCount(member_no);

        model.addAttribute("messages", messages);
        model.addAttribute("unreadCount", unreadCount);

        return "front-end/notifications";
    }

    /**
     * 前台 - 標記單一通知為已讀
     * 路徑：POST /member/notifications/mark-read
     */
    @PostMapping("/members/notifications/mark-read")
    public String markNotificationAsRead(@RequestParam Integer id) {
        service.markAsRead(id);
        return "redirect:/members/notifications";
    }

    /**
     * 前台 - 標記所有通知為已讀
     * 路徑：POST /member/notifications/mark-all-read
     */
    @PostMapping("/members/notifications/mark-all-read")
    public String markAllNotificationsAsRead(@SessionAttribute(name = "member_no", required = false) Integer member_no) {
        if (member_no != null) {
            service.markAllAsReadByMemberNo(member_no);
        }
        return "redirect:/members/notifications";
    }

    // ==================== 後台頁面 ====================

    /**
     * 後台 - 系統通知管理頁
     * 路徑：GET /admin/system-messages
     */
    @GetMapping("/admins/system-messages")
    public String adminSystemMessagesPage(Model model,
                                          @RequestParam(required = false) Integer editId) {
        // 取得所有系統通知
        List<system_message> messages = service.getAllMessages();
        model.addAttribute("messages", messages);

        // 如果有 editId，表示要編輯
        if (editId != null) {
            system_message editMessage = service.getMessageById(editId);
            model.addAttribute("editMessage", editMessage);
            model.addAttribute("isEdit", true);
        } else {
            // 新增時使用空物件
            model.addAttribute("editMessage", new system_message());
            model.addAttribute("isEdit", false);
        }

        return "back-end/system-messages";
    }

    /**
     * 後台 - 新增系統通知
     * 路徑：POST /admin/system-messages/create
     */
    @PostMapping("/admins/system-messages/create")
    public String createSystemMessage(@ModelAttribute system_message entity,
                                      @SessionAttribute(name = "adm_no", required = false) Integer adm_no) {
        // 從 Session 取得管理員編號
        if (adm_no != null) {
            entity.setAdm_no(adm_no);
        }
        service.createMessage(entity);
        return "redirect:/admins/system-messages";
    }

    /**
     * 後台 - 更新系統通知
     * 路徑：POST /admin/system-messages/update
     */
    @PostMapping("/admins/system-messages/update")
    public String updateSystemMessage(@RequestParam Integer id,
                                      @ModelAttribute system_message entity) {
        service.updateMessage(id, entity);
        return "redirect:/admins/system-messages";
    }

    /**
     * 後台 - 刪除系統通知
     * 路徑：POST /admin/system-messages/delete
     */
    @PostMapping("/admins/system-messages/delete")
    public String deleteSystemMessage(@RequestParam Integer id) {
        service.deleteMessage(id);
        return "redirect:/admins/system-messages";
    }

    /**
     * 後台 - 批量發送通知
     * 路徑：POST /admin/system-messages/bulk-send
     */
    @PostMapping("/admins/system-messages/bulk-send")
    public String bulkSendMessages(@RequestParam List<Integer> member_nos,
                                   @RequestParam String content,
                                   @SessionAttribute(name = "adm_no", required = false) Integer adm_no) {
        if (adm_no != null) {
            service.sendBulkMessages(member_nos, adm_no, content);
        }
        return "redirect:/admins/system-messages";
    }

    // ==================== AJAX API（可選）====================

    /**
     * API - 取得未讀通知數量（用於前台顯示小紅點）
     * 路徑：GET /api/notifications/unread-count
     */
    @GetMapping("/api/notifications/unread-count")
    @ResponseBody
    public Long getUnreadCount(@SessionAttribute(name = "member_no", required = false) Integer member_no) {
        if (member_no == null) {
            return 0L;
        }
        return service.getUnreadCount(member_no);
    }

    /**
     * API - 取得未讀通知列表
     * 路徑：GET /api/notifications/unread
     */
    @GetMapping("/api/notifications/unread")
    @ResponseBody
    public List<system_message> getUnreadMessages(@SessionAttribute(name = "member_no", required = false) Integer member_no) {
        if (member_no == null) {
            return List.of();
        }
        return service.getUnreadMessages(member_no);
    }
}