package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.FetchingStatusRepository;
import com.safety.recognition.client.CrimeCategoryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
public class StartFetchingCrimeCategoriesMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartFetchingCrimeCategoriesMessageListener.class);

    private final CrimeCategoryClient crimeCategoryClient;
    private final CrimeCategoryRepository crimeCategoryRepository;
    private final FetchingStatusRepository fetchingStatusRepository;

    @Autowired
    public StartFetchingCrimeCategoriesMessageListener(CrimeCategoryClient crimeCategoryClient, CrimeCategoryRepository crimeCategoryRepository, FetchingStatusRepository fetchingStatusRepository) {
        this.crimeCategoryClient = crimeCategoryClient;
        this.crimeCategoryRepository = crimeCategoryRepository;
        this.fetchingStatusRepository = fetchingStatusRepository;
    }

    @KafkaListener(topics = "start_fetching_crime_categories", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        if (crimeCategoryRepository.findAll().isEmpty()) {
            LOG.info("starting fetching crime categories");
            var crimeCategories = crimeCategoryClient.getCrimeCategories();
            AtomicInteger numericRepresentation = new AtomicInteger();
            crimeCategories.forEach(crimeCategory -> {
                crimeCategory.setName(crimeCategory.getName().toLowerCase().replace("-", " ").replace(" and ", " "));
                crimeCategory.setUrl(crimeCategory.getUrl().toLowerCase().replace("-", " ").replace(" and ", " "));
                crimeCategory.setNumericRepresentation(numericRepresentation.getAndIncrement());
            });
            crimeCategoryRepository.saveAll(crimeCategories);
            fetchingStatusRepository.findById(1L).ifPresent((status) -> {
                status.setCategoriesFetched(true);
                fetchingStatusRepository.save(status);
            });
            LOG.info("fetching crime categories finished");
        } else {
            LOG.info("skipping fetching crime categories, data already exists");
        }
    }

}
