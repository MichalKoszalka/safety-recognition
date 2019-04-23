package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.model.PreprocessedCrime;
import data.police.uk.model.crime.Crime;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CrimesListener {

    @KafkaListener(topics = "new_crimes", containerFactory = "kafkaCrimesListenerContainerFactory")
    public void crimesListener(Crime crime) {
        //TODO: send crime with neighbourhood info
        // and for each crime create entry in crime by neighbourhood and crime by date(month) and type
        // and for each new, recalculate statistics ?
        // maybe send message to a different service to recalculate statistics
        //
        PreprocessedCrime preprocessedCrime = new PreprocessedCrime(crime);
    }

}
