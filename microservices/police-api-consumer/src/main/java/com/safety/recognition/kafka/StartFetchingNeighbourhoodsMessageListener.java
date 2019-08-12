package com.safety.recognition.kafka;

import com.safety.recognition.service.NeighbourhoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class StartFetchingNeighbourhoodsMessageListener {

    @Value("${kafka.topic.neighbourhoods.fetched}")
    private String neighbourhoodsFetchedTopic;

    private static final Logger LOG = LoggerFactory.getLogger(StartFetchingNeighbourhoodsMessageListener.class);

    private final NeighbourhoodService neighbourhoodService;
    private final KafkaTemplate<UUID, String> fetchedMessageProducer;

    @Autowired
    public StartFetchingNeighbourhoodsMessageListener(NeighbourhoodService neighbourhoodService, KafkaTemplate<UUID, String> fetchedMessageProducer) {
        this.neighbourhoodService = neighbourhoodService;
        this.fetchedMessageProducer = fetchedMessageProducer;
    }

    @KafkaListener(topics = "start_fetching_neighbourhoods", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        LOG.info("starting fetching neighbourhoods data");
        neighbourhoodService.loadNeighbourhoods();
        fetchedMessageProducer.send(neighbourhoodsFetchedTopic, UUID.randomUUID(), "fetched");
        LOG.info("finished fetching neighbourhoods data");
    }

}
