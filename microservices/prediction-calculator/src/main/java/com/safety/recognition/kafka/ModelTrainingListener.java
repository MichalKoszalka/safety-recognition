package com.safety.recognition.kafka;

import com.safety.recognition.calculator.CrimeByNeighbourhoodAndCategoryPredictionCalculator;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhoodAndCategory;
import data.police.uk.utils.MonthParser;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ModelTrainingListener {

    private static final Logger LOG = LoggerFactory.getLogger(ModelTrainingListener.class);

    private final CrimeByNeighbourhoodAndCategoryPredictionCalculator crimeByNeighbourhoodAndCategoryPredictionCalculator;

    @Autowired
    public ModelTrainingListener(CrimeByNeighbourhoodAndCategoryPredictionCalculator crimeByNeighbourhoodAndCategoryPredictionCalculator) {
        this.crimeByNeighbourhoodAndCategoryPredictionCalculator = crimeByNeighbourhoodAndCategoryPredictionCalculator;
    }

    @KafkaListener(topics = "train_prediction_model_by_neighbourhood_and_category", containerFactory = "kafkaTrainPredictionModelByNeighbourhoodAndCategoryListenerFactory")
    public void crimesByNeighbourhoodAndCategoryPredictionModelTrainListener(ConsumerRecord<String, CrimeLevelByNeighbourhoodAndCategory> record) {
        LOG.info("Starting training model");
        crimeByNeighbourhoodAndCategoryPredictionCalculator.train(record.value());
        LOG.info("Finished training model");
        crimeByNeighbourhoodAndCategoryPredictionCalculator.calculate(MonthParser.toLocalDate(record.key()).plusMonths(1));
    }

}
