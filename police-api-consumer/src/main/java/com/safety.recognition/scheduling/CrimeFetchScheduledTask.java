package com.safety.recognition.scheduling;

import com.safety.recognition.client.CrimeClient;
import com.safety.recognition.model.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CrimeFetchScheduledTask {

    @Autowired
    private CrimeClient crimeClient;

    @Scheduled(fixedRate = 5000)
    public void fetchCrimeData() {
        List<Crime> crimes = crimeClient.getCrimes();
    }



}
