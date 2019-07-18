package com.safety.recognition.kafka;

import com.safety.recognition.kafka.messages.Crimes;
import com.safety.recognition.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.kafka.messages.StreetAndCategory;
import com.safety.recognition.kafka.messages.StreetAndNeighbourhood;
import com.safety.recognition.processor.CrimeProcessor;
import data.police.uk.model.crime.Crime;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrimesListener {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesListener.class);

    private final CrimeProcessor crimeProcessor;
    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, StreetAndCategory> streetAndCategoryTemplate;
    private final KafkaTemplate<String, NeighbourhoodAndCategory> neighbourhoodAndCategoryTemplate;
    private final KafkaTemplate<String, StreetAndNeighbourhood> streetAndNeighbourhoodKafkaTemplate;

    @Autowired
    public CrimesListener(CrimeProcessor crimeProcessor, KafkaTemplate<String, String> stringKafkaTemplate, KafkaTemplate<String, StreetAndCategory> streetAndCategoryTemplate, KafkaTemplate<String, NeighbourhoodAndCategory> neighbourhoodAndCategoryTemplate, KafkaTemplate<String, StreetAndNeighbourhood> streetAndNeighbourhoodKafkaTemplate) {
        this.crimeProcessor = crimeProcessor;
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.streetAndCategoryTemplate = streetAndCategoryTemplate;
        this.neighbourhoodAndCategoryTemplate = neighbourhoodAndCategoryTemplate;
        this.streetAndNeighbourhoodKafkaTemplate = streetAndNeighbourhoodKafkaTemplate;
    }

    @KafkaListener(topics = "new_crimes", containerFactory = "kafkaCrimesListenerContainerFactory")
    public void crimesListener(ConsumerRecord<String, Crimes> crimesForNeighbourhood) {
        String month = crimesForNeighbourhood.value().getCrimeList().stream().findAny().orElseThrow(() -> new IllegalArgumentException("No crime data found!")).getMonth();
        LOG.info(String.format("Starting processing crimes with neighbourhood: %s and date: %s", crimesForNeighbourhood.key(), month));
        crimesForNeighbourhood.value().getCrimeList().parallelStream().forEach(crimeProcessor::process);
        stringKafkaTemplate.send("calculate_indexes_for_london", month, "london");
        extractDistinctCategories(crimesForNeighbourhood.value()).forEach(category -> stringKafkaTemplate.send("calculate_indexes_for_london_by_category", month, category));
        extractDistinctStreets(crimesForNeighbourhood.value()).forEach(street-> streetAndNeighbourhoodKafkaTemplate.send("calculate_indexes_by_street", month, new StreetAndNeighbourhood(street, crimesForNeighbourhood.key())));
        extractDistinctStreetsAndCategories(crimesForNeighbourhood.key(), crimesForNeighbourhood.value()).forEach(streetAndCategory -> streetAndCategoryTemplate.send("calculate_indexes_by_street_and_category", month, streetAndCategory));
        stringKafkaTemplate.send("calculate_indexes_by_neighbourhood", month, crimesForNeighbourhood.key());
        extractDistinctCategories(crimesForNeighbourhood.value()).forEach(category -> neighbourhoodAndCategoryTemplate.send("calculate_indexes_by_neighbourhood_and_category", month, new NeighbourhoodAndCategory(crimesForNeighbourhood.key(), category)));
        LOG.info(String.format("Finished processing crimes with neighbourhood: %s and date: %s", crimesForNeighbourhood.key(), month));
    }

    private List<String> extractDistinctCategories(Crimes crimes) {
        return crimes.getCrimeList().stream().map(crime -> crime.getCategory().replace("-", " ").replace(" and ", " ")).distinct().collect(Collectors.toList());
    }

    private List<String> extractDistinctStreets(Crimes crimes) {
        return crimes.getCrimeList().stream().map(crime -> crime.getLocation().getStreet().getName()).distinct().collect(Collectors.toList());
    }

    private List<StreetAndCategory> extractDistinctStreetsAndCategories(String neighbourhood, Crimes crimes) {
        return crimes.getCrimeList().stream().map(crime -> new StreetAndCategory(new StreetAndNeighbourhood(crime.getLocation().getStreet().getName(), neighbourhood), crime.getCategory())).distinct().collect(Collectors.toList());
    }

}
