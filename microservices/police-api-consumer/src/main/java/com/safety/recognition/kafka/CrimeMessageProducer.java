package com.safety.recognition.kafka;

import com.safety.recognition.kafka.messages.Crimes;
import data.police.uk.model.crime.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CrimeMessageProducer {

    @Value("${kafka.topic.new.crimes}")
    private String topic;

    private final KafkaTemplate<String, Crimes> kafkaTemplate;

    @Autowired
    public CrimeMessageProducer(KafkaTemplate<String, Crimes> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String neighbourhood, Crimes crimes) {
        kafkaTemplate.send(topic, neighbourhood, crimes);
    }

}
