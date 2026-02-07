package com.karshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling //coupon排程器使用
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
