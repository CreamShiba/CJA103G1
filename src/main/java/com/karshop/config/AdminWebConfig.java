package com.karshop.config;

import com.karshop.admins.interceptor.AdminInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdminWebConfig implements WebMvcConfigurer {

    @Autowired
    private AdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 註冊攔截器
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/coupon/**", "/memberInfo/**")    // 攔截管理路徑
                .excludePathPatterns("/admins/login", "/css/**", "/js/**", "/images/**"); // 排除登入頁與靜態資源
    }
}
