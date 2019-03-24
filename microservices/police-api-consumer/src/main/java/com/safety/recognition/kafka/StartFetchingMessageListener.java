package com.safety.recognition.kafka;

import com.safety.recognition.service.NeighbourhoodLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StartFetchingMessageListener {

    private final NeighbourhoodLoader neighbourhoodLoader;

    @Autowired
    public StartFetchingMessageListener(NeighbourhoodLoader neighbourhoodLoader) {
        this.neighbourhoodLoader = neighbourhoodLoader;
    }

    @KafkaListener(topics = "start_fetching_crime_data", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        //TODO: if no neighbourhoods in Cassandra - fetch new.
        neighbourhoodLoader.loadNeighbourhoods();
        //TODO: fetch last update date from api and cassandra and calculate which time window needs to be fetched
        //TODO: then for each boundary fetch crimes and send for processing
    }

}
