package com.safety.recognition.scheduling;

import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.kafka.CrimeMessageProducer;
import data.police.uk.model.crime.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrimeFetchScheduledTask {

    private final CrimeClient crimeClient;

    private final CrimeMessageProducer crimeMessageProducer;

    @Autowired
    public CrimeFetchScheduledTask(CrimeClient crimeClient, CrimeMessageProducer crimeMessageProducer) {
        this.crimeClient = crimeClient;
        this.crimeMessageProducer = crimeMessageProducer;
    }

    @Scheduled(fixedRate = 5000)
    public void fetchCrimeData() {
        List<Crime> crimes = crimeClient.getCrimes();
        crimes.forEach(crime -> crimeMessageProducer.sendMessage(crime));
    }



}
