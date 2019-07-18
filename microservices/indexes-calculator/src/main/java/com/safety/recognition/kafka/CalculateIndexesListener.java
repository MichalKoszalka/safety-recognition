package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
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
    private final LastUpdateDateRepository lastUpdateDateRepository;
    private final KafkaTemplate<String, String> predictMessageProducer;
    private final KafkaTemplate<String, CrimeLevelByNeighbourhoodAndCategory> crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer;


    public CalculateIndexesListener(CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator, CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator, CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator, CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator, CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator, CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator, LastUpdateDateRepository lastUpdateDateRepository, KafkaTemplate<String, String> predictMessageProducer, KafkaTemplate<String, CrimeLevelByNeighbourhoodAndCategory> crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer) {
        this.crimesByNeighbourhoodAndCategoryIndexesCalculator = crimesByNeighbourhoodAndCategoryIndexesCalculator;
        this.crimesByNeighbourhoodIndexesCalculator = crimesByNeighbourhoodIndexesCalculator;
        this.crimesByStreetAndCategoryIndexesCalculator = crimesByStreetAndCategoryIndexesCalculator;
        this.crimesByStreetIndexesCalculator = crimesByStreetIndexesCalculator;
        this.crimesForLondonByCategoryIndexesCalculator = crimesForLondonByCategoryIndexesCalculator;
        this.crimesForLondonIndexesCalculator = crimesForLondonIndexesCalculator;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.predictMessageProducer = predictMessageProducer;
        this.crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer = crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer;
    }

//    @KafkaListener(topics = "calculate_indexes_for_london", containerFactory = "kafkaTrainPredictionModelForLondonListenerFactory")
//    public void crimesForLondonIndexesCalculatorListener(String month, String london) {
//        LOG.info("Starting calculating indexes for London");
//        var lastUpdatedMonth = MonthParser.toLocalDate(month);
//        var highestCrimeLevel = crimesForLondonIndexesCalculator.calculate(lastUpdatedMonth);
//        sendPredictionMessage(IndexType.LONDON, lastUpdatedMonth);
//        LOG.info("Finished calculating indexes for London");
//    }
//
//    @KafkaListener(topics = "calculate_indexes_for_london_by_category", containerFactory = "kafkaTrainPredictionModelByCategoryistenerFactory")
//    public void crimesForLondonByCategoryIndexesCalculatorListener(String month, String category) {
//        LOG.info(String.format("Starting calculating indexes for London and category %s", category));
//        var lastUpdatedMonth = MonthParser.toLocalDate(month);
//        var highestCrimeLevel = crimesForLondonByCategoryIndexesCalculator.calculate(lastUpdatedMonth, category);
//        sendPredictionMessage(IndexType.LONDON_AND_CATEGORY, lastUpdatedMonth);
//        LOG.info(String.format("Finished calculating indexes for London and category %s", category));
//    }
//
//    @KafkaListener(topics = "calculate_indexes_by_street", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
//    public void crimesByStreetIndexesCalculatorListener(String month, StreetAndNeighbourhood streetAndNeighbourhood) {
//        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
//        var lastUpdatedMonth = MonthParser.toLocalDate(month);
//        var highestCrimeLevel = crimesByStreetIndexesCalculator.calculate(lastUpdatedMonth, streetAndNeighbourhood);
//        sendPredictionMessage(IndexType.STREET, lastUpdatedMonth);
//        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
//    }
//
//    @KafkaListener(topics = "calculate_indexes_by_street_and_category", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
//    public void crimesByStreetAndCategoryIndexesCalculatorListener(String month, StreetAndCategory streetAndCategory) {
//        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategory.getStreetAndNeighbourhood().getStreet(), streetAndCategory.getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategory.getCategory()));
//        var lastUpdatedMonth = MonthParser.toLocalDate(month);
//        var highestCrimeLevel = crimesByStreetAndCategoryIndexesCalculator.calculate(lastUpdatedMonth, streetAndCategory.getStreetAndNeighbourhood(), streetAndCategory.getCategory());
//        sendPredictionMessage(IndexType.STREET_AND_CATEGORY, lastUpdatedMonth);
//        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategory.getStreetAndNeighbourhood().getStreet(), streetAndCategory.getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategory.getCategory()));
//    }
//
//    @KafkaListener(topics = "calculate_indexes_by_neighbourhood", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
//    public void crimesByNeighbourhoodIndexesCalculatorListener(String month, String neighbourhood) {
//        LOG.info(String.format("Starting calculating indexes for London and neighbourhood %s", neighbourhood));
//        var lastUpdatedMonth = MonthParser.toLocalDate(month);
//        var highestCrimeLevel = crimesByNeighbourhoodIndexesCalculator.calculate(lastUpdatedMonth, neighbourhood);
//        sendPredictionMessage(IndexType.NEIGHBOURHOOD, lastUpdatedMonth);
//        LOG.info(String.format("Finished calculating indexes for London and neighbourhood %s", neighbourhood));
//    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood_and_category", containerFactory = "kafkaCalculateIndexesNeighbourhoodAndCategoryListenerFactory")
    public void crimesByNeighbourhoodAndCategoryIndexesCalculatorListener(ConsumerRecord<String, NeighbourhoodAndCategory> neighbourhoodAndCategoryByMonth) {
        LOG.info(String.format("Starting calculating indexes for London and street %s and category %s", neighbourhoodAndCategoryByMonth.value().getNeighbourhood(), neighbourhoodAndCategoryByMonth.value().getCategory()));
        var lastUpdatedMonth = MonthParser.toLocalDate(neighbourhoodAndCategoryByMonth.key());
        var highestCrimeLevel = crimesByNeighbourhoodAndCategoryIndexesCalculator.calculate(lastUpdatedMonth, neighbourhoodAndCategoryByMonth.value().getNeighbourhood(), neighbourhoodAndCategoryByMonth.value().getCategory());
        crimeLevelByNeighbourhoodAndCategoryTrainMessageProducer.send("train_prediction_model_by_neighbourhood_and_category", neighbourhoodAndCategoryByMonth.key(), highestCrimeLevel);
        LOG.info(String.format("Finished calculating indexes for London and street %s and category %s", neighbourhoodAndCategoryByMonth.value().getNeighbourhood(), neighbourhoodAndCategoryByMonth.value().getCategory()));
    }

}
