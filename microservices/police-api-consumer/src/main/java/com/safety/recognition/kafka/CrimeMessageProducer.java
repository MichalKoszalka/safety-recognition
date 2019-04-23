package com.safety.recognition.kafka;

import data.police.uk.model.crime.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CrimeMessageProducer {

    @Value("${kafka.topic.new.crimes}")
    private String topic;

    private final KafkaTemplate<Long, Crime> kafkaTemplate;

    @Autowired
    public CrimeMessageProducer(KafkaTemplate<Long, Crime> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Crime crime) {
        kafkaTemplate.send(topic, crime.getId(), crime);
    }

}
