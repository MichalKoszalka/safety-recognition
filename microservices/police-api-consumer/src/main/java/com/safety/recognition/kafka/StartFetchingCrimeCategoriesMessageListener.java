package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.client.CrimeCategoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class StartFetchingCrimeCategoriesMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartFetchingCrimeCategoriesMessageListener.class);

    private final CrimeCategoryClient crimeCategoryClient;
    private final CrimeCategoryRepository crimeCategoryRepository;

    @Autowired
    public StartFetchingCrimeCategoriesMessageListener(CrimeCategoryClient crimeCategoryClient, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeCategoryClient = crimeCategoryClient;
        this.crimeCategoryRepository = crimeCategoryRepository;
    }

    @KafkaListener(topics = "start_fetching_crime_categories", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        if(crimeCategoryRepository.findAll().isEmpty()) {
            LOG.info("starting fetching crime categories");
            crimeCategoryRepository.saveAll(crimeCategoryClient.getCrimeCategories());
            LOG.info("fetching crime categories finished");
        } else {
            LOG.info("skipping fetching crime categories, data already exists");
        }
    }

}
