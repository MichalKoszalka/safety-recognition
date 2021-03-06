package com.safety.recognition;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FetchingSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(FetchingSchedulerApplication.class, args);
	}
}
