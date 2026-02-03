package com.karshop.adminauth.interceptor;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
  private static final Integer SUPER_ADMIN_ID = 10;
  // URL → 功能 ID 對應表
  private static final Map<String, Integer> URL_TO_FUNC = Map.of(
          "/admins/listAll",2,  // 20對應的是權限編號
          "/admins/selectPage",2,
          "/admins/search", 2,
          "/admins/add",    2,
          "/admins/edit",      2,
          "/admins/carcate/list", 4,
          "/admins/carcate/edit", 4,
          "/admins/carcate/add", 4,
          "/admins/carcate/insert", 4
          // 如有更多需要授權的路徑，繼續加在這裡
  );

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler) throws Exception {

    // 取 session（不要自動建）
    HttpSession session = request.getSession(false);
    if (session == null) {
      response.sendRedirect(request.getContextPath() + "/admins/login");
      return false;
    }

    // 從 session 拿使用者的權限清單（登入時要放進 PERMISSIONS）
    @SuppressWarnings("unchecked")
    List<Integer> perms = (List<Integer>) session.getAttribute("adminFuncIds");
    if (perms == null) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "尚未授權");
      return false;
    }
    if (perms.contains(SUPER_ADMIN_ID)) {
      return true;
    }
    // 取得請求的相對路徑（去掉 contextPath）
    String path = request.getRequestURI()
            .substring(request.getContextPath().length());
    Integer funcId = URL_TO_FUNC.get(path);

    // 如果這條路徑不需授權檢查，直接放行
    if (funcId == null) {
      return true;
    }

    // 檢查該使用者是否擁有這個功能
    if (!perms.contains(funcId)) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "沒有操作此功能的權限");
      return false;
    }

    // 權限足夠 → 放行
    return true;
  }
}