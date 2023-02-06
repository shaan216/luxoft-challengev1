package com.dws.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.dws.challenge.service.EmailNotificationService;
import com.dws.challenge.service.NotificationService;

@SpringBootApplication
public class ChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChallengeApplication.class, args);

	}

	@Bean
	public NotificationService notificationService() {
		return new EmailNotificationService();
	}
}