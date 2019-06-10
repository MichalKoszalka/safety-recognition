package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndNeighbourhood;
import com.safety.recognition.processor.CrimeProcessor;
import data.police.uk.model.crime.Crime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CrimesListener {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesListener.class);

    private final CrimeProcessor crimeProcessor;
    private final KafkaTemplate<UUID, String> stringKafkaTemplate;
    private final KafkaTemplate<UUID, StreetAndCategory> streetAndCategoryTemplate;
    private final KafkaTemplate<UUID, NeighbourhoodAndCategory> neighbourhoodAndCategoryTemplate;
    private final KafkaTemplate<UUID, StreetAndNeighbourhood> streetAndNeighbourhoodKafkaTemplate;

    @Autowired
    public CrimesListener(CrimeProcessor crimeProcessor, KafkaTemplate<UUID, String> stringKafkaTemplate, KafkaTemplate<UUID, StreetAndCategory> streetAndCategoryTemplate, KafkaTemplate<UUID, NeighbourhoodAndCategory> neighbourhoodAndCategoryTemplate, KafkaTemplate<UUID, StreetAndNeighbourhood> streetAndNeighbourhoodKafkaTemplate) {
        this.crimeProcessor = crimeProcessor;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.streetAndCategoryTemplate = streetAndCategoryTemplate;
        this.neighbourhoodAndCategoryTemplate = neighbourhoodAndCategoryTemplate;
        this.streetAndNeighbourhoodKafkaTemplate = streetAndNeighbourhoodKafkaTemplate;
    }

    @KafkaListener(topics = "new_crimes", containerFactory = "kafkaCrimesListenerContainerFactory")
    public void crimesListener(Crime crime) {
        LOG.info(String.format("Starting processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
        crimeProcessor.process(crime);
        stringKafkaTemplate.send("calculate_indexes_for_london", UUID.randomUUID(), "london");
        stringKafkaTemplate.send("calculate_indexes_for_london_by_category", UUID.randomUUID(), crime.getCategory());
        streetAndNeighbourhoodKafkaTemplate.send("calculate_indexes_by_street", UUID.randomUUID(), new StreetAndNeighbourhood(crime.getLocation().getStreet().getName(), crime.getNeighbourhood()));
        streetAndCategoryTemplate.send("calculate_indexes_by_street_and_category", UUID.randomUUID(), new StreetAndCategory(new StreetAndNeighbourhood(crime.getLocation().getStreet().getName(), crime.getNeighbourhood()), crime.getCategory()));
        stringKafkaTemplate.send("calculate_indexes_by_neighbourhood", UUID.randomUUID(), crime.getNeighbourhood());
        neighbourhoodAndCategoryTemplate.send("calculate_indexes_by_neighbourhood_and_category", UUID.randomUUID(), new NeighbourhoodAndCategory(crime.getNeighbourhood(), crime.getCategory()));
        LOG.info(String.format("Finished processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
    }

}
