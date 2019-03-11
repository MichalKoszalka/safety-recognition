package com.safety.recognition.kafka;

import model.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CrimeMessageProducer {

    @Value("${new_crimes.topic}")
    private String topic;

    @Autowired
    private KafkaTemplate<Long, Crime> kafkaTemplate;

    public void sendMessage(Crime crime) {
        kafkaTemplate.send(topic, crime.getId(), crime);
    }

}
