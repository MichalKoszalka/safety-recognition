package com.safety.recognition.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SchedulerMessagesProducer {

    @Value("${new_crimes.topic}")
    private String topic;

    private final KafkaTemplate<UUID, String> kafkaTemplate;

    @Autowired
    public SchedulerMessagesProducer(KafkaTemplate<UUID, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, UUID.randomUUID(), message);
    }

}
