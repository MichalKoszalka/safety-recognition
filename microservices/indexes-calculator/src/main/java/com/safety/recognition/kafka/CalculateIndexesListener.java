package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.cassandra.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndNeighbourhood;
import com.safety.recognition.cassandra.model.LastUpdateDate;
import com.safety.recognition.cassandra.model.indexes.IndexType;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class CalculateIndexesListener {

    private static final Logger LOG = LoggerFactory.getLogger(CalculateIndexesListener.class);

    private final CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator;
    private final CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator;
    private final CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator;
    private final CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator;
    private final CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator;
    private final CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator;
    private final KafkaTemplate<String, String> predictMessageProducer;
    private final LastUpdateDateRepository lastUpdateDateRepository;

    public CalculateIndexesListener(CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator, CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator, CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator, CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator, CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator, CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator, KafkaTemplate<String, String> predictMessageProducer, LastUpdateDateRepository lastUpdateDateRepository) {
        this.crimesByNeighbourhoodAndCategoryIndexesCalculator = crimesByNeighbourhoodAndCategoryIndexesCalculator;
        this.crimesByNeighbourhoodIndexesCalculator = crimesByNeighbourhoodIndexesCalculator;
        this.crimesByStreetAndCategoryIndexesCalculator = crimesByStreetAndCategoryIndexesCalculator;
        this.crimesByStreetIndexesCalculator = crimesByStreetIndexesCalculator;
        this.crimesForLondonByCategoryIndexesCalculator = crimesForLondonByCategoryIndexesCalculator;
        this.crimesForLondonIndexesCalculator = crimesForLondonIndexesCalculator;
        this.predictMessageProducer = predictMessageProducer;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }

    @KafkaListener(topics = "calculate_indexes_for_london", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesForLondonIndexesCalculatorListener() {
        LOG.info("Starting calculating indexes for London");
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            crimesForLondonIndexesCalculator.calculate(lastUpdateDate.get());
            sendPredictionMessage(IndexType.LONDON, lastUpdateDate.get().getSafetyRecognitionLastUpdate());
            LOG.info("Finished calculating indexes for London");
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    @KafkaListener(topics = "calculate_indexes_for_london_by_category", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesForLondonByCategoryIndexesCalculatorListener(String category) {
        LOG.info(String.format("Starting calculating indexes for London and category %s", category));
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            crimesForLondonByCategoryIndexesCalculator.calculate(lastUpdateDate.get(), category);
            sendPredictionMessage(IndexType.LONDON_AND_CATEGORY, lastUpdateDate.get().getSafetyRecognitionLastUpdate());
            LOG.info(String.format("Finished calculating indexes for London and category %s", category));
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    @KafkaListener(topics = "calculate_indexes_by_street", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
    public void crimesByStreetIndexesCalculatorListener(StreetAndNeighbourhood streetAndNeighbourhood) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            crimesByStreetIndexesCalculator.calculate(lastUpdateDate.get(), streetAndNeighbourhood);
            sendPredictionMessage(IndexType.STREET, lastUpdateDate.get().getSafetyRecognitionLastUpdate());
            LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    @KafkaListener(topics = "calculate_indexes_by_street_and_category", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
    public void crimesByStreetAndCategoryIndexesCalculatorListener(StreetAndCategory streetAndCategory) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategory.getStreetAndNeighbourhood().getStreet(), streetAndCategory.getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategory.getCategory()));
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            crimesByStreetAndCategoryIndexesCalculator.calculate(lastUpdateDate.get(), streetAndCategory.getStreetAndNeighbourhood(), streetAndCategory.getCategory());
            sendPredictionMessage(IndexType.STREET_AND_CATEGORY, lastUpdateDate.get().getSafetyRecognitionLastUpdate());
            LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategory.getStreetAndNeighbourhood().getStreet(), streetAndCategory.getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategory.getCategory()));
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesByNeighbourhoodIndexesCalculatorListener(String neighbourhood) {
        LOG.info(String.format("Starting calculating indexes for London and neighbourhood %s", neighbourhood));
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            crimesByNeighbourhoodIndexesCalculator.calculate(lastUpdateDate.get(), neighbourhood);
            sendPredictionMessage(IndexType.NEIGHBOURHOOD, lastUpdateDate.get().getSafetyRecognitionLastUpdate());
            LOG.info(String.format("Finished calculating indexes for London and neighbourhood %s", neighbourhood));
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood_and_category", containerFactory = "kafkaCalculateIndexesNeighbourhoodAndCategoryListenerFactory")
    public void crimesByNeighbourhoodAndCategoryIndexesCalculatorListener(NeighbourhoodAndCategory neighbourhoodAndCategory) {
        LOG.info(String.format("Starting calculating indexes for London and street %s and category %s", neighbourhoodAndCategory.getNeighbourhood(), neighbourhoodAndCategory.getCategory()));
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            crimesByNeighbourhoodAndCategoryIndexesCalculator.calculate(lastUpdateDate.get(), neighbourhoodAndCategory.getNeighbourhood(), neighbourhoodAndCategory.getCategory());
            sendPredictionMessage(IndexType.NEIGHBOURHOOD_AND_CATEGORY, lastUpdateDate.get().getSafetyRecognitionLastUpdate());
            LOG.info(String.format("Finished calculating indexes for London and street %s and category %s", neighbourhoodAndCategory.getNeighbourhood(), neighbourhoodAndCategory.getCategory()));
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void sendPredictionMessage(IndexType indexType, LocalDate currentMonth) {
        predictMessageProducer.send("calculate_prediction", indexType.getName(), currentMonth.format(DateTimeFormatter.ISO_DATE));
    }
}
