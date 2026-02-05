package com.karshop.utils;

import com.karshop.members.model.MembersVO;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Component
public class LoginUserHolder {

    public MembersVO get() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attr == null) {
            System.out.println("❌ RequestAttributes 為 null");
            return null;
        }

        HttpSession session = attr.getRequest().getSession(false);
        if (session == null) {
            System.out.println("❌ Session 為 null");
            return null;
        }

        // ✅ 嘗試多個可能的 Session Key
        MembersVO member = null;

        // 嘗試 "loginMember" (最常見)
        member = (MembersVO) session.getAttribute("loginMember");
        if (member != null) {
            System.out.println("✅ 找到會員 (loginMember)，編號: " + member.getMemNo());
            return member;
        }

        // 嘗試 "member"
        member = (MembersVO) session.getAttribute("member");
        if (member != null) {
            System.out.println("✅ 找到會員 (member)，編號: " + member.getMemNo());
            return member;
        }

        // 嘗試 "loginMembers"
        member = (MembersVO) session.getAttribute("loginMembers");
        if (member != null) {
            System.out.println("✅ 找到會員 (loginMembers)，編號: " + member.getMemNo());
            return member;
        }

        // 除錯：列出所有 Session 屬性
        System.out.println("❌ 找不到會員，Session 屬性列表:");
        java.util.Enumeration<String> names = session.getAttributeNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            System.out.println("  - " + name);
        }

        return null;
    }
}
