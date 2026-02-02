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
        if (attr == null) return null;

        HttpSession session = attr.getRequest().getSession(false);
        if (session == null) return null;

        // 對齊 MembersLoginController 存入的 Key: "loginMembers"
        return (MembersVO) session.getAttribute("member");
    }
}
