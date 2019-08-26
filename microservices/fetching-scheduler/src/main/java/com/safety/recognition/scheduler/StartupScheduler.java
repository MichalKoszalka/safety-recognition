package com.safety.recognition.scheduler;

import com.safety.recognition.cassandra.model.CrimesFetchingStatus;
import com.safety.recognition.cassandra.model.FetchingStatus;
import com.safety.recognition.cassandra.repository.FetchingStatusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class StartupScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(StartupScheduler.class);

    @Value("${kafka.topic.start.fetching.crime.data}")
    private String startFetchingTopic;

    @Value("${kafka.topic.start.fetching.crime.categories}")
    private String startFetchingCrimeCategoriesTopic;

    @Value("${kafka.topic.start.fetching.neighbourhoods}")
    private String startFetchingNeighbourhoodsTopic;

    private final KafkaTemplate<UUID,String> kafkaTemplate;
    private final FetchingStatusRepository fetchingStatusRepository;

    @Autowired
    public StartupScheduler(KafkaTemplate<UUID, String> kafkaTemplate, FetchingStatusRepository fetchingStatusRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.fetchingStatusRepository = fetchingStatusRepository;
    }

    @PostConstruct
    public void sendFetchStartMessage() {
        fetchingStatusRepository.findById(1L).ifPresentOrElse((status) -> LOG.info(String.format("Status already exists: %s", status.toString())), () -> fetchingStatusRepository.save(new FetchingStatus(1L, false, false, CrimesFetchingStatus.NOT_FETCHED)));
        kafkaTemplate.send(startFetchingCrimeCategoriesTopic, "start");
        kafkaTemplate.send(startFetchingNeighbourhoodsTopic, "start");
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void startFetching() {
        var status = fetchingStatusRepository.findById(1L);
        status.ifPresent((statusValue) -> {
            if(statusValue.isReadyForInitialFetching()) {
                kafkaTemplate.send(startFetchingTopic, "start");
            }
        });
    }

    @Scheduled(cron = "0 0 12 1 * ?")
    public void sendFetchingMonthly() {
        var status = fetchingStatusRepository.findById(1L);
        status.ifPresent((statusValue) -> {
            if(statusValue.isReadyForMonthlyFetching()) {
                kafkaTemplate.send(startFetchingTopic, "start");
            }
        });
    }
}
