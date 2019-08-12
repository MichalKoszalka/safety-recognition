package com.safety.recognition.scheduler;

import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
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

    @Value("${kafka.topic.start.fetching.neighbourhoods}")
    private String startFetchingNeighbourhoodsTopic;

    private final KafkaTemplate<UUID,String> kafkaTemplate;
    private final CrimeCategoryRepository crimeCategoryRepository;
    private final NeighbourhoodRepository neighbourhoodRepository;

    @Autowired
    public StartupScheduler(KafkaTemplate<UUID, String> kafkaTemplate, CrimeCategoryRepository crimeCategoryRepository, NeighbourhoodRepository neighbourhoodRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.crimeCategoryRepository = crimeCategoryRepository;
        this.neighbourhoodRepository = neighbourhoodRepository;
    }

    @PostConstruct
    public void sendFetchStartMessage() {
        kafkaTemplate.send(startFetchingCrimeCategoriesTopic, "start");
        kafkaTemplate.send(startFetchingNeighbourhoodsTopic, "start");
    }

    @Scheduled(cron = "0 0 12 1 * ?")
    public void sendFetchCrimeCategoriesMessage() {
        kafkaTemplate.send(startFetchingTopic, "start");
    }

}
