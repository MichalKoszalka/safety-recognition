package com.safety.recognition.scheduling;

import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.message.producer.CrimeMessageProducer;
import com.safety.recognition.model.Crime;
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
        System.out.println("before fetching");
        List<Crime> crimes = crimeClient.getCrimes();
        System.out.println("after fetching");
        System.out.println("before sending");
        crimeMessageProducer.sendMessage(crimes.toString());
        System.out.println("after sending");
    }



}
