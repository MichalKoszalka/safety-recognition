package com.safety.recognition.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NeighbourhoodsFetchedListener {

    @Value("${kafka.topic.start.fetching.crime.data}")
    private String startFetchingTopic;

    private final KafkaTemplate<UUID,String> kafkaTemplate;

    @Autowired
    public NeighbourhoodsFetchedListener(KafkaTemplate<UUID, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "neighbourhoods_fetched", containerFactory = "kafkaNeighbourhoodsFetchedListenerFactory")
    public void crimesByNeighbourhoodAndCategoryIndexesCalculatorListener() {
        kafkaTemplate.send(startFetchingTopic, UUID.randomUUID(), "start");

    }

}
