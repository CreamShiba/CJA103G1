package com.karshop.system_message;

import com.karshop.admins.model.AdminVO;
import com.karshop.members.model.MembersRepository;
import com.karshop.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class system_messagecontroller {

    @Autowired
    private system_messageservice service;

    @Autowired
    private MembersRepository membersRepository;

    // ================= 前台會員功能 =================

    /**
     * 前台 - 顯示會員的所有通知
     */
    @GetMapping("/members/notifications")
    public String listMemberMessages(HttpSession session, Model model) {
        MembersVO member = (MembersVO) session.getAttribute("member");

        if (member == null) {
            return "redirect:/members/login";
        }

        Integer memberId = member.getMemNo();

        // 1. 取得所有通知清單
        List<system_message> myMessages = service.getMessagesByMember(memberId);
        model.addAttribute("notifications", myMessages);

        // 2. 計算未讀數量
        long unreadCount = myMessages.stream()
                .filter(m -> !m.getMessage_status())
                .count();
        model.addAttribute("unreadCount", unreadCount);

        return "front-end/notifications";
    }

    /**
     * 前台 - 標記單一通知為已讀
     * 🚩 新增這個方法
     */
    @PostMapping("/members/notifications/mark-read")
    public String markAsRead(@RequestParam Integer id) {
        service.markAsRead(id);
        return "redirect:/members/notifications";
    }

    /**
     * 前台 - 標記所有通知為已讀
     * 🚩 新增這個方法
     */
    @PostMapping("/members/notifications/mark-all-read")
    public String markAllAsRead(HttpSession session) {
        MembersVO member = (MembersVO) session.getAttribute("member");
        if (member != null) {
            service.markAllAsReadByMember(member.getMemNo());
        }
        return "redirect:/members/notifications";
    }

    // ================= 後台管理功能 =================

    /**
     * 後台 - 系統通知管理頁面
     */
    @GetMapping("/admins/system-message")
    public String adminSystemMessagesPage(@RequestParam(required = false) Integer editId, Model model) {
        model.addAttribute("messages", service.getAllMessages());
        model.addAttribute("members", membersRepository.findAll());

        if (editId != null) {
            model.addAttribute("editMessage", service.getOneMessage(editId));
            model.addAttribute("isEdit", true);
        } else {
            model.addAttribute("editMessage", new system_message());
            model.addAttribute("isEdit", false);
        }
        return "back-end/system-message";
    }

    /**
     * 後台 - 新增通知
     */
    @PostMapping("/admins/system-message/create")
    public String create(@ModelAttribute system_message entity, HttpSession session) {
        AdminVO loginAdmin = (AdminVO) session.getAttribute("admin");

        if (loginAdmin == null) {
            return "redirect:/admins/login";
        }

        Integer currentAdmNo = loginAdmin.getAdminNo();
        entity.setAdm_no(currentAdmNo);

        try {
            // 如果選擇「全體會員」(member_no = 0)
            if (entity.getMember_no() != null && entity.getMember_no() == 0) {
                service.sendToAllMembers(currentAdmNo, entity.getMessage_content());
            } else {
                service.createMessage(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/admins/system-message?error=db";
        }
        return "redirect:/admins/system-message";
    }

    /**
     * 後台 - 更新通知
     */
    @PostMapping("/admins/system-message/update")
    public String update(@RequestParam Integer id, @ModelAttribute system_message entity) {
        service.updateMessage(id, entity);
        return "redirect:/admins/system-message";
    }

    /**
     * 後台 - 刪除通知
     */
    @PostMapping("/admins/system-message/delete")
    public String delete(@RequestParam Integer id) {
        service.deleteMessage(id);
        return "redirect:/admins/system-message";
    }
}