package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.kafka.messages.MonthDate;
import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.indexes.IndexType;
import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.client.UpdateDateClient;
import com.safety.recognition.service.LastUpdateDateService;
import com.safety.recognition.service.NeighbourhoodService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
public class StartFetchingCrimesMessageListener {

    private static final Logger LOG = LoggerFactory.getLogger(StartFetchingCrimesMessageListener.class);

    private final NeighbourhoodService neighbourhoodService;
    private final LastUpdateDateService lastUpdateDateService;
    private final CrimeClient crimeClient;
    private final CrimeMessageProducer crimeMessageProducer;
    private final UpdateDateClient updateDateClient;
    private final KafkaTemplate<String, MonthDate> kafkaTemplate;
    private static final LocalDate CRIME_API_FIRST_DATE = LocalDate.of(2018, 12, 1);

    @Autowired
    public StartFetchingCrimesMessageListener(NeighbourhoodService neighbourhoodService, LastUpdateDateService lastUpdateDateService, CrimeClient crimeClient, CrimeMessageProducer crimeMessageProducer, UpdateDateClient updateDateClient, KafkaTemplate<String, MonthDate> kafkaTemplate) {
        this.neighbourhoodService = neighbourhoodService;
        this.lastUpdateDateService = lastUpdateDateService;
        this.crimeClient = crimeClient;
        this.crimeMessageProducer = crimeMessageProducer;
        this.updateDateClient = updateDateClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "start_fetching_crime_data", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        LOG.info("starting fetching crime data");
        var neighbourhoods = neighbourhoodService.getNeighbourhoods();
        var lastUpdateDate = lastUpdateDateService.loadLastUpdateDate();
        var policeApiUpdateDate = updateDateClient.getUpdateDate();
        if(lastUpdateDate.isEmpty()) {
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
            neighbourhoods.parallelStream().flatMap(neighbourhood -> crimeClient.getCrimes(neighbourhood, finalCurrentMonth).stream()).forEach(crimeMessageProducer::sendMessage);
            lastUpdateDateService.merge(currentMonth);
            sendPredictionMessages(currentMonth);
            currentMonth = currentMonth.plusMonths(1);
        } while (currentMonth.isBefore(to));
    }

    private void sendPredictionMessages(LocalDate currentMonth) {
        Arrays.stream(IndexType.values()).forEach(value -> kafkaTemplate.send("calculate_prediction", value.getName(), new MonthDate(currentMonth)));
    }

}
