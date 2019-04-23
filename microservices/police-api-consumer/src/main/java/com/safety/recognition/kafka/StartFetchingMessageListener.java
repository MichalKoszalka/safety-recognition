package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.client.UpdateDateClient;
import com.safety.recognition.service.LastUpdateDateService;
import com.safety.recognition.service.NeighbourhoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StartFetchingMessageListener {

    private final NeighbourhoodService neighbourhoodService;
    private final LastUpdateDateService lastUpdateDateService;
    private final CrimeClient crimeClient;
    private final CrimeMessageProducer crimeMessageProducer;
    private final UpdateDateClient updateDateClient;
    private static final LocalDate CRIME_API_FIRST_DATE = LocalDate.of(2017, 1, 1);

    @Autowired
    public StartFetchingMessageListener(NeighbourhoodService neighbourhoodService, LastUpdateDateService lastUpdateDateService, CrimeClient crimeClient, CrimeMessageProducer crimeMessageProducer, UpdateDateClient updateDateClient) {
        this.neighbourhoodService = neighbourhoodService;
        this.lastUpdateDateService = lastUpdateDateService;
        this.crimeClient = crimeClient;
        this.crimeMessageProducer = crimeMessageProducer;
        this.updateDateClient = updateDateClient;
    }

    @KafkaListener(topics = "start_fetching_crime_data", containerFactory = "kafkaStartFetchingListenerFactory")
    public void startFetchingListener() {
        var neighbourhoods = neighbourhoodService.getNeighbourhoodStream();
        var lastUpdateDate = lastUpdateDateService.loadLastUpdateDate();
        var policeApiUpdateDate = updateDateClient.getUpdateDate();
        if(lastUpdateDate.isEmpty()) {
            loadCrimesToKafka(neighbourhoods, CRIME_API_FIRST_DATE, policeApiUpdateDate.getDate());
        } else {
            loadCrimesToKafka(neighbourhoods, lastUpdateDate.get().getPoliceApiLastUpdate(), policeApiUpdateDate.getDate());
        }
    }

    private void loadCrimesToKafka(List<Neighbourhood> neighbourhoods, LocalDate from, LocalDate to) {
        var currentMonth = LocalDate.from(from);
        do {
            LocalDate finalCurrentMonth = currentMonth;
            neighbourhoods.parallelStream().flatMap(neighbourhood -> crimeClient.getCrimes(neighbourhood, finalCurrentMonth).stream()).forEach(crimeMessageProducer::sendMessage);
            currentMonth = currentMonth.plusMonths(1);
        } while (currentMonth.isBefore(to));
        lastUpdateDateService.merge(currentMonth);
    }

}
