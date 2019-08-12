package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.client.UpdateDateClient;
import com.safety.recognition.kafka.messages.Crimes;
import com.safety.recognition.service.LastUpdateDateService;
import com.safety.recognition.utils.ParallelRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

@Service
public class StartFetchingCrimesMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartFetchingCrimesMessageListener.class);

    private final NeighbourhoodRepository neighbourhoodRepository;
    private final LastUpdateDateService lastUpdateDateService;
    private final CrimeClient crimeClient;
    private final CrimeMessageProducer crimeMessageProducer;
    private final UpdateDateClient updateDateClient;
    private static final LocalDate CRIME_API_FIRST_DATE = LocalDate.of(2017, 1, 1);

    @Autowired
    public StartFetchingCrimesMessageListener(NeighbourhoodRepository neighbourhoodRepository, LastUpdateDateService lastUpdateDateService, CrimeClient crimeClient, CrimeMessageProducer crimeMessageProducer, UpdateDateClient updateDateClient) {
        this.neighbourhoodRepository = neighbourhoodRepository;
        this.lastUpdateDateService = lastUpdateDateService;
        this.crimeClient = crimeClient;
        this.crimeMessageProducer = crimeMessageProducer;
        this.updateDateClient = updateDateClient;
    }

    @KafkaListener(topics = "start_fetching_crime_data", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        LOG.info("starting fetching crime data");
        var neighbourhoods = neighbourhoodRepository.findAll();
        var lastUpdateDate = lastUpdateDateService.loadLastUpdateDate();
        var policeApiUpdateDate = updateDateClient.getUpdateDate();
        if (lastUpdateDate.isEmpty()) {
            LOG.info("fetching for the first time");
            loadCrimesToKafka(neighbourhoods, CRIME_API_FIRST_DATE, policeApiUpdateDate.getDate());
        } else {
            LOG.info("fetching again");
            loadCrimesToKafka(neighbourhoods, lastUpdateDate.get().getPoliceApiLastUpdate(), policeApiUpdateDate.getDate());
        }
        LOG.info("fetching crime data finished");
    }

    private void loadCrimesToKafka(List<Neighbourhood> neighbourhoods, LocalDate from, LocalDate to) {
        var currentMonth = LocalDate.from(from);
        do {
            LocalDate finalCurrentMonth = currentMonth;
            ParallelRunner parallelRunner = new ParallelRunner(5, Duration.ofSeconds(1L));
            parallelRunner.submit(neighbourhoods, (neighbourhood) -> getAndSendCrimes(neighbourhood, finalCurrentMonth));
            lastUpdateDateService.merge(currentMonth);
            currentMonth = currentMonth.plusMonths(1);
        } while (currentMonth.isBefore(to));
    }

    private void getAndSendCrimes(Neighbourhood neighbourhood, LocalDate finalCurrentMonth) {
        crimeMessageProducer.sendMessage(neighbourhood.getName(), new Crimes(crimeClient.getCrimes(neighbourhood, finalCurrentMonth)));
    }

}
