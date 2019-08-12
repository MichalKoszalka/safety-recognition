package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
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
    private final KafkaTemplate<String, CrimeLevel> crimeLevelTrainMessageProducer;
    private final KafkaTemplate<String, CrimeLevelByCategory> crimeLevelByCategoryTrainMessageProducer;
    private final KafkaTemplate<String, CrimeLevelByStreet> crimeLevelByStreetTrainMessageProducer;
    private final KafkaTemplate<String, CrimeLevelByStreetAndCategory> crimeLevelByStreetAndCategoryTrainMessageProducer;
    private final KafkaTemplate<String, CrimeLevelByNeighbourhood> crimeLevelByNeighbourhoodTrainMessageProducer;
    private final KafkaTemplate<String, CrimeLevelByNeighbourhoodAndCategory> crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer;


    public CalculateIndexesListener(CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator, CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator, CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator, CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator, CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator, CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator, KafkaTemplate<String, CrimeLevel> crimeLevelTrainMessageProducer, KafkaTemplate<String, CrimeLevelByCategory> crimeLevelByCategoryTrainMessageProducer, KafkaTemplate<String, CrimeLevelByStreet> crimeLevelByStreetTrainMessageProducer, KafkaTemplate<String, CrimeLevelByStreetAndCategory> crimeLevelByStreetAndCategoryTrainMessageProducer, KafkaTemplate<String, CrimeLevelByNeighbourhood> crimeLevelByNeighbourhoodTrainMessageProducer, KafkaTemplate<String, CrimeLevelByNeighbourhoodAndCategory> crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer) {
        this.crimesByNeighbourhoodAndCategoryIndexesCalculator = crimesByNeighbourhoodAndCategoryIndexesCalculator;
        this.crimesByNeighbourhoodIndexesCalculator = crimesByNeighbourhoodIndexesCalculator;
        this.crimesByStreetAndCategoryIndexesCalculator = crimesByStreetAndCategoryIndexesCalculator;
        this.crimesByStreetIndexesCalculator = crimesByStreetIndexesCalculator;
        this.crimesForLondonByCategoryIndexesCalculator = crimesForLondonByCategoryIndexesCalculator;
        this.crimesForLondonIndexesCalculator = crimesForLondonIndexesCalculator;
        this.crimeLevelTrainMessageProducer = crimeLevelTrainMessageProducer;
        this.crimeLevelByCategoryTrainMessageProducer = crimeLevelByCategoryTrainMessageProducer;
        this.crimeLevelByStreetTrainMessageProducer = crimeLevelByStreetTrainMessageProducer;
        this.crimeLevelByStreetAndCategoryTrainMessageProducer = crimeLevelByStreetAndCategoryTrainMessageProducer;
        this.crimeLevelByNeighbourhoodTrainMessageProducer = crimeLevelByNeighbourhoodTrainMessageProducer;
        this.crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer = crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer;
    }

    @KafkaListener(topics = "calculate_indexes_for_london", containerFactory = "kafkaTrainPredictionModelForLondonListenerFactory")
    public void crimesForLondonIndexesCalculatorListener(String month, String london) {
        LOG.info("Starting calculating indexes for London");
        var lastUpdatedMonth = MonthParser.toLocalDate(month);
        var highestCrimeLevel = crimesForLondonIndexesCalculator.calculate(lastUpdatedMonth);
        if (!highestCrimeLevel.getCrimesByMonth().isEmpty()) {
            crimeLevelTrainMessageProducer.send("train_prediction_model_for_london", month, highestCrimeLevel);
        }
        LOG.info("Finished calculating indexes for London");
    }

    @KafkaListener(topics = "calculate_indexes_for_london_by_category", containerFactory = "kafkaTrainPredictionModelByCategoryistenerFactory")
    public void crimesForLondonByCategoryIndexesCalculatorListener(ConsumerRecord<String, String> categoryByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and category %s", categoryByMonth.value()));
        var lastUpdatedMonth = MonthParser.toLocalDate(categoryByMonth.key());
        var highestCrimeLevel = crimesForLondonByCategoryIndexesCalculator.calculate(lastUpdatedMonth, categoryByMonth.value());
        if (!highestCrimeLevel.getCrimesByMonth().isEmpty()) {
            crimeLevelByCategoryTrainMessageProducer.send("train_prediction_model_for_london_by_category", categoryByMonth.key(), highestCrimeLevel);
        }
        LOG.info(String.format("Finished calculating indexes for London and category %s", categoryByMonth.value()));
    }

    @KafkaListener(topics = "calculate_indexes_by_street", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
    public void crimesByStreetIndexesCalculatorListener(ConsumerRecord<String, StreetAndNeighbourhood> streetAndNeighbourhoodByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhoodByMonth.value().getStreet(), streetAndNeighbourhoodByMonth.value().getNeighbourhood()));
        var lastUpdatedMonth = MonthParser.toLocalDate(streetAndNeighbourhoodByMonth.key());
        var highestCrimeLevel = crimesByStreetIndexesCalculator.calculate(lastUpdatedMonth, streetAndNeighbourhoodByMonth.value());
        if (!highestCrimeLevel.getCrimesByMonth().isEmpty()) {
            crimeLevelByStreetTrainMessageProducer.send("train_prediction_model_by_street", streetAndNeighbourhoodByMonth.key(), highestCrimeLevel);
        }
        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhoodByMonth.value().getStreet(), streetAndNeighbourhoodByMonth.value().getNeighbourhood()));
    }

    @KafkaListener(topics = "calculate_indexes_by_street_and_category", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
    public void crimesByStreetAndCategoryIndexesCalculatorListener(ConsumerRecord<String, StreetAndCategory> streetAndCategoryByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getStreet(), streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategoryByMonth.value().getCategory()));
        var lastUpdatedMonth = MonthParser.toLocalDate(streetAndCategoryByMonth.key());
        var highestCrimeLevel = crimesByStreetAndCategoryIndexesCalculator.calculate(lastUpdatedMonth, streetAndCategoryByMonth.value().getStreetAndNeighbourhood(), streetAndCategoryByMonth.value().getCategory());
        if (!highestCrimeLevel.getCrimesByMonth().isEmpty()) {
            crimeLevelByStreetAndCategoryTrainMessageProducer.send("train_prediction_model_by_street_and_category", streetAndCategoryByMonth.key(), highestCrimeLevel);
        }
        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getStreet(), streetAndCategoryByMonth.value().getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategoryByMonth.value().getCategory()));
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesByNeighbourhoodIndexesCalculatorListener(ConsumerRecord<String, String> neighbourhoodByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and neighbourhood %s", neighbourhoodByMonth.value()));
        var lastUpdatedMonth = MonthParser.toLocalDate(neighbourhoodByMonth.key());
        var highestCrimeLevel = crimesByNeighbourhoodIndexesCalculator.calculate(lastUpdatedMonth, neighbourhoodByMonth.value());
        if (!highestCrimeLevel.getCrimesByMonth().isEmpty()) {
            crimeLevelByNeighbourhoodTrainMessageProducer.send("train_prediction_model_by_neighbourhood", neighbourhoodByMonth.key(), highestCrimeLevel);
        }
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
