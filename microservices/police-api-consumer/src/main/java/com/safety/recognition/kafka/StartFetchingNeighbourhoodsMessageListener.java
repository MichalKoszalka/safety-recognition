package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.repository.FetchingStatusRepository;
import com.safety.recognition.service.NeighbourhoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StartFetchingNeighbourhoodsMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartFetchingNeighbourhoodsMessageListener.class);

    private final NeighbourhoodService neighbourhoodService;
    private final KafkaTemplate<UUID, String> fetchedMessageProducer;
    private final FetchingStatusRepository fetchingStatusRepository;

    @Autowired
    public StartFetchingNeighbourhoodsMessageListener(NeighbourhoodService neighbourhoodService, KafkaTemplate<UUID, String> fetchedMessageProducer, FetchingStatusRepository fetchingStatusRepository) {
        this.neighbourhoodService = neighbourhoodService;
        this.fetchedMessageProducer = fetchedMessageProducer;
        this.fetchingStatusRepository = fetchingStatusRepository;
    }

    @KafkaListener(topics = "start_fetching_neighbourhoods", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        LOG.info("starting fetching neighbourhoods data");
        neighbourhoodService.loadNeighbourhoods();
        fetchingStatusRepository.findById(1L).ifPresent((status) -> {
            status.setNeighbourhoodsFetched(true);
            fetchingStatusRepository.save(status);
        });
        LOG.info("finished fetching neighbourhoods data");
    }

}
