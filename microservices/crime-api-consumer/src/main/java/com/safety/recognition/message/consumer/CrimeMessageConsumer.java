package com.safety.recognition.message.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CrimeMessageConsumer {

    @KafkaListener(topics = "admintome-test")
    public void consume(String message) {
        System.out.println("Consumed message " + message);
    }
}
