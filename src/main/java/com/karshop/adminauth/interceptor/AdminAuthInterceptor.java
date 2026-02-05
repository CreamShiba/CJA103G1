package com.karshop.adminauth.interceptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import static java.util.Map.entry;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {
  private static final Integer SUPER_ADMIN_ID = 1;
  // URL → 功能 ID 對應表
  private static final Map<String, Integer> URL_TO_FUNC = Map.ofEntries(
          entry("/admins/listAll",2),  // 2對應的是權限編號
          entry("/admins/selectPage",2),
          entry("/admins/search", 2),
          entry("/admins/add",    2),
          entry("/admins/edit",      2),
          entry("/admins/carcate/edit", 4),
          entry("/admins/carcate/add", 4),
          entry("/admins/carcate/insert", 4),
          entry("/memberInfo/manage", 5),
          entry("/coupon/admin", 5)

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
      // 1. 設定編碼，非常重要！否則中文會變成亂碼
      response.setContentType("text/html; charset=UTF-8");
      response.setCharacterEncoding("UTF-8");

      try (java.io.PrintWriter out = response.getWriter()) {
        // 2. 寫入 JavaScript 腳本
        out.println("<script>");
        out.println("alert('沒有操作此功能的權限');");

        // 3. 建議加上這行：按下確定後回到上一頁，不然使用者會停在白畫面
        out.println("window.history.back();");

        // 或者也可以導向特定頁面：
        // out.println("window.location.href='/index';");

        out.println("</script>");
        out.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }

      return false; // 攔截請求，不繼續執行
    }

    // 權限足夠 → 放行
    return true;
  }
}