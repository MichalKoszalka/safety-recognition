package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.kafka.messages.StreetAndCategory;
import com.safety.recognition.kafka.messages.StreetAndNeighbourhood;
import data.police.uk.utils.MonthParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class CalculateIndexesListener {

    private static final Logger LOG = LoggerFactory.getLogger(CalculateIndexesListener.class);

    private final CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator;
    private final CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator;
    private final CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator;
    private final CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator;
    private final CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator;
    private final CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator;
    private final KafkaTemplate<String, CrimeLevelByNeighbourhoodAndCategory> crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer;


    public CalculateIndexesListener(CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator, CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator, CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator, CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator, CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator, CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator, KafkaTemplate<String, CrimeLevelByNeighbourhoodAndCategory> crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer) {
        this.crimesByNeighbourhoodAndCategoryIndexesCalculator = crimesByNeighbourhoodAndCategoryIndexesCalculator;
        this.crimesByNeighbourhoodIndexesCalculator = crimesByNeighbourhoodIndexesCalculator;
        this.crimesByStreetAndCategoryIndexesCalculator = crimesByStreetAndCategoryIndexesCalculator;
        this.crimesByStreetIndexesCalculator = crimesByStreetIndexesCalculator;
        this.crimesForLondonByCategoryIndexesCalculator = crimesForLondonByCategoryIndexesCalculator;
        this.crimesForLondonIndexesCalculator = crimesForLondonIndexesCalculator;
        this.crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer = crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer;
    }

    @KafkaListener(topics = "calculate_indexes_for_london", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesForLondonIndexesCalculatorListener(ConsumerRecord<String, String> cityByMonth) {
        LOG.info("Starting calculating indexes for London");
        var lastUpdatedMonth = MonthParser.toLocalDate(cityByMonth.key());
        crimesForLondonIndexesCalculator.calculate(lastUpdatedMonth);
        LOG.info("Finished calculating indexes for London");
    }

        @KafkaListener(topics = "calculate_indexes_for_london_by_category", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesForLondonByCategoryIndexesCalculatorListener(ConsumerRecord<String, String> categoryByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and category %s", categoryByMonth.value()));
        var lastUpdatedMonth = MonthParser.toLocalDate(categoryByMonth.key());
        crimesForLondonByCategoryIndexesCalculator.calculate(lastUpdatedMonth, categoryByMonth.value());
        LOG.info(String.format("Finished calculating indexes for London and category %s", categoryByMonth.value()));
    }

    @KafkaListener(topics = "calculate_indexes_by_street", containerFactory = "kafkaCalculateIndexesStreetAndNeighbourhoodListenerFactory")
    public void crimesByStreetIndexesCalculatorListener(ConsumerRecord<String, StreetAndNeighbourhood> streetAndNeighbourhoodByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhoodByMonth.value().getStreet(), streetAndNeighbourhoodByMonth.value().getNeighbourhood()));
        var lastUpdatedMonth = MonthParser.toLocalDate(streetAndNeighbourhoodByMonth.key());
        crimesByStreetIndexesCalculator.calculate(lastUpdatedMonth, streetAndNeighbourhoodByMonth.value());
        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhoodByMonth.value().getStreet(), streetAndNeighbourhoodByMonth.value().getNeighbourhood()));
    }

    @KafkaListener(topics = "calculate_indexes_by_street_and_category", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
    public void crimesByStreetAndCategoryIndexesCalculatorListener(ConsumerRecord<String, StreetAndCategory> streetAndCategoryByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getStreet(), streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategoryByMonth.value().getCategory()));
        var lastUpdatedMonth = MonthParser.toLocalDate(streetAndCategoryByMonth.key());
        crimesByStreetAndCategoryIndexesCalculator.calculate(lastUpdatedMonth, streetAndCategoryByMonth.value().getStreetAndNeighbourhood(), streetAndCategoryByMonth.value().getCategory());
        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getStreet(), streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategoryByMonth.value().getCategory()));
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesByNeighbourhoodIndexesCalculatorListener(ConsumerRecord<String, String> neighbourhoodByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and neighbourhood %s", neighbourhoodByMonth.value()));
        var lastUpdatedMonth = MonthParser.toLocalDate(neighbourhoodByMonth.key());
        crimesByNeighbourhoodIndexesCalculator.calculate(lastUpdatedMonth, neighbourhoodByMonth.value());
        LOG.info(String.format("Finished calculating indexes for London and neighbourhood %s", neighbourhoodByMonth.value()));
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood_and_category", containerFactory = "kafkaCalculateIndexesNeighbourhoodAndCategoryListenerFactory")
    public void crimesByNeighbourhoodAndCategoryIndexesCalculatorListener(ConsumerRecord<String, NeighbourhoodAndCategory> neighbourhoodAndCategoryByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and street %s and category %s", neighbourhoodAndCategoryByMonth.value().getNeighbourhood(), neighbourhoodAndCategoryByMonth.value().getCategory()));
        var lastUpdatedMonth = MonthParser.toLocalDate(neighbourhoodAndCategoryByMonth.key());
        var highestCrimeLevel = crimesByNeighbourhoodAndCategoryIndexesCalculator.calculate(lastUpdatedMonth, neighbourhoodAndCategoryByMonth.value().getNeighbourhood(), neighbourhoodAndCategoryByMonth.value().getCategory());
        if (!highestCrimeLevel.getCrimesByMonth().isEmpty()) {
            crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer.send("train_prediction_model_by_neighbourhood_and_category", neighbourhoodAndCategoryByMonth.key(), highestCrimeLevel);
        }
        LOG.info(String.format("Finished calculating indexes for London and street %s and category %s", neighbourhoodAndCategoryByMonth.value().getNeighbourhood(), neighbourhoodAndCategoryByMonth.value().getCategory()));
    }

}
