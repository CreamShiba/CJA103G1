package com.karshop.config;

import com.karshop.product.SellerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SellerInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private SellerInterceptor sellerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sellerInterceptor)
                .addPathPatterns("/product/**")
                .excludePathPatterns("/product/detail")
                .excludePathPatterns("/product/displayMain")
                .excludePathPatterns("/product/displayDetail");

    }
}
