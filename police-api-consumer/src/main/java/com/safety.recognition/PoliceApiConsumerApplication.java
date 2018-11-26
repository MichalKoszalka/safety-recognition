package com.safety.recognition;

import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.model.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class PoliceApiConsumerApplication {

    @Autowired
    private CrimeClient crimeClient;

	public static void main(String[] args) {
		SpringApplication.run(PoliceApiConsumerApplication.class, args);

	}
}
