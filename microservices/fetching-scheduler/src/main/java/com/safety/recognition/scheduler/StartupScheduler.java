package com.safety.recognition.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class StartupScheduler {

    @Value("${kafka.topic.start.fetching.crime.data}")
    private String startFetchingTopic;

    @Value("${kafka.topic.start.fetching.crime.categories}")
    private String startFetchingCrimeCategoriesTopic;

    private final KafkaTemplate<UUID,String> kafkaTemplate;

    @Autowired
    public StartupScheduler(KafkaTemplate<UUID, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    @Scheduled(cron = "0 0 12 1 * ?")
    public void sendFetchStartMessage() {
        kafkaTemplate.send(startFetchingTopic, "start");
    }

    @PostConstruct
    public void sendFetchCrimeCategoriesMessage() {
        kafkaTemplate.send(startFetchingCrimeCategoriesTopic, "start");
    }

}
