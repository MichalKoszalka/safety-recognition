package com.safety.recognition.scheduling;

import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.kafka.CrimeMessageProducer;
import model.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrimeFetchScheduledTask {

    @Autowired
    private CrimeClient crimeClient;

    @Autowired
    private CrimeMessageProducer crimeMessageProducer;

    @Scheduled(fixedRate = 5000)
    public void fetchCrimeData() {
        List<Crime> crimes = crimeClient.getCrimes();
        crimes.forEach(crime -> crimeMessageProducer.sendMessage(crime));
    }



}
