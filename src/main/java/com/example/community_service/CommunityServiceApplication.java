package com.example.community_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CommunityServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunityServiceApplication.class, args);
	}

}
