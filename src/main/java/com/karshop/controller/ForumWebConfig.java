package com.karshop.controller;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration("forumWebConfig") // 🟢 明確命名，解決 ConflictingBeanDefinitionException
public class ForumWebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 設定圖片讀取路徑
		// 讓 http://localhost:8080/uploads/xxx.jpg 可以讀取 C:/cja103_uploads/xxx.jpg
		registry.addResourceHandler("/uploads/**")
				.addResourceLocations("file:C:/cja103_uploads/");
	}
}