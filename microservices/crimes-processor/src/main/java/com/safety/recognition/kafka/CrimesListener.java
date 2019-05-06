package com.safety.recognition.kafka;

import com.safety.recognition.processor.CrimeProcessor;
import data.police.uk.model.crime.Crime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class CrimesListener {

    private static final Logger LOG = LoggerFactory.getLogger(CrimeProcessor.class);

    private final CrimeProcessor crimeProcessor;

    @Autowired
    public CrimesListener(CrimeProcessor crimeProcessor) {
        this.crimeProcessor = crimeProcessor;
    }

    @KafkaListener(topics = "new_crimes", containerFactory = "kafkaCrimesListenerContainerFactory")
    public void crimesListener(Crime crime) {
        LOG.info(String.format("Starting processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
        crimeProcessor.process(crime);
        LOG.info(String.format("Finished processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
    }

}
