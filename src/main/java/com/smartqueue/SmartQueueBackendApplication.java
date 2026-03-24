package com.smartqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SmartQueueBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartQueueBackendApplication.class, args);
	}

}
