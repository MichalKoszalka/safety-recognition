package com.safety.recognition;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

@SpringBootApplication(exclude = KafkaAutoConfiguration.class)
public class IndexesCalculatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IndexesCalculatorApplication.class, args);
    }

}
