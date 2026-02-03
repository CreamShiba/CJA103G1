package com.karshop.config;

import com.karshop.admins.interceptor.AdminInterceptor;
import com.karshop.adminauth.interceptor.AdminAuthInterceptor;

import com.karshop.members.interceptor.MembersInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LoginInterceptorConfig implements WebMvcConfigurer {

  @Autowired
  private MembersInterceptor membersInterceptor;

  @Autowired
  private AdminInterceptor adminInterceptor;

  @Autowired
  private AdminAuthInterceptor adminAuthInterceptor;

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    // 管理員攔截
    registry.addInterceptor(adminInterceptor)
            .addPathPatterns("/admins/**"
            )
            .excludePathPatterns(
                    "/admins/login",
                    "/admins/logout",
                    "/css/**",
                    "/js/**",
                    "/images/**"
            );


    //權限攔截器
		registry.addInterceptor(adminAuthInterceptor)
        .addPathPatterns(
            "/admins/listAll",
            "/admins/selectPage",
            "/admins/search",
            "/admins/add",
            "/admins/edit",
            "/admins/carcate/*"
            // … 對應 Map 裡的所有 key
        )
        .excludePathPatterns(
                "/css/**",
                "/js/**",
                "/images/**"
        );

      registry.addInterceptor(membersInterceptor)
              // 要攔截的路徑模式：
              .addPathPatterns(
                  "/members/update", //會員修改
                  "/members/view"//會員個人資料檢視

              )

              // 排除靜態資源，以及登入與註冊頁面
              .excludePathPatterns(
                  "/css/**", // *只會攔截一層 **可攔截多層
                  "/js/**",
                  "/images/**"
              );
  }
}