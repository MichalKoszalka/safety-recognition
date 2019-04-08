package com.safety.recognition.kafka;

import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.service.LastUpdateDateLoader;
import com.safety.recognition.service.NeighbourhoodLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StartFetchingMessageListener {

    private final NeighbourhoodLoader neighbourhoodLoader;
    private final LastUpdateDateLoader lastUpdateDateLoader;
    private final CrimeClient crimeClient;

    @Autowired
    public StartFetchingMessageListener(NeighbourhoodLoader neighbourhoodLoader, LastUpdateDateLoader lastUpdateDateLoader, CrimeClient crimeClient) {
        this.neighbourhoodLoader = neighbourhoodLoader;
        this.lastUpdateDateLoader = lastUpdateDateLoader;
        this.crimeClient = crimeClient;
    }

    @KafkaListener(topics = "start_fetching_crime_data", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        var neighbourhoods = neighbourhoodLoader.loadNeighbourhoods();
        var lastUpdateDate = lastUpdateDateLoader.loadLastUpdateDate();
        //if never updated then start from scratch
        if(lastUpdateDate.getSafetyRecognitionLastUpdate().isEqual(LocalDate.now())) {
//            crimeClient.getCrimes()
        } else {
            // else get from lastApiUpdate to lastSafetyUpdate

        }

        //TODO: fetch last update date from api and cassandra and calculate which time window needs to be fetched
        //TODO: then for each boundary fetch crimes and send for processing
    }

    private void fetchCrimes() {

    }



}
