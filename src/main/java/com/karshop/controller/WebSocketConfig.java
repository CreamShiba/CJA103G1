package com.karshop.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
	@Bean
	public ServerEndpointExporter serverEndpointExporter() {
		System.out.println("🔥 WebSocketConfig 正在啟動！有沒有看到我？ 🔥");
		return new ServerEndpointExporter();
	}
}