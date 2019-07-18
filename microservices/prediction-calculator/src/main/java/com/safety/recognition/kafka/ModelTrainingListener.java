package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.indexes.IndexType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ModelTrainingListener {

    private static final Logger LOG = LoggerFactory.getLogger(ModelTrainingListener.class);

    private final CrimeByNeighbourhoodPredictionCalculator crimeByNeighbourhoodPredictionCalculator;
    private final CrimeByNeighbourhoodAndCategoryPredictionCalculator crimeByNeighbourhoodAndCategoryPredictionCalculator;
    private final CrimeByStreetPredictionCalculator crimeByStreetPredictionCalculator;
    private final CrimeByStreetAndCategoryPredictionCalculator crimeByStreetAndCategoryPredictionCalculator;
    private final CrimeForLondonPredictionCalculator crimeForLondonPredictionCalculator;
    private final CrimeForLondonByCategoryPredictionCalculator crimeForLondonByCategoryPredictionCalculator;
    private final KafkaTemplate<String, String> predictionMessageProducer;

    @Autowired
    public ModelTrainingListener(CrimeByNeighbourhoodPredictionCalculator crimeByNeighbourhoodPredictionCalculator, CrimeByNeighbourhoodAndCategoryPredictionCalculator crimeByNeighbourhoodAndCategoryPredictionCalculator, CrimeByStreetPredictionCalculator crimeByStreetPredictionCalculator, CrimeByStreetAndCategoryPredictionCalculator crimeByStreetAndCategoryPredictionCalculator, CrimeForLondonPredictionCalculator crimeForLondonPredictionCalculator, CrimeForLondonByCategoryPredictionCalculator crimeForLondonByCategoryPredictionCalculator, KafkaTemplate<String, String> predictionMessageProducer) {
        this.crimeByNeighbourhoodPredictionCalculator = crimeByNeighbourhoodPredictionCalculator;
        this.crimeByNeighbourhoodAndCategoryPredictionCalculator = crimeByNeighbourhoodAndCategoryPredictionCalculator;
        this.crimeByStreetPredictionCalculator = crimeByStreetPredictionCalculator;
        this.crimeByStreetAndCategoryPredictionCalculator = crimeByStreetAndCategoryPredictionCalculator;
        this.crimeForLondonPredictionCalculator = crimeForLondonPredictionCalculator;
        this.crimeForLondonByCategoryPredictionCalculator = crimeForLondonByCategoryPredictionCalculator;
        this.predictionMessageProducer = predictionMessageProducer;
    }

//    @KafkaListener(topics = "train_prediction_model_for_london", containerFactory = "kafkaCalculatePredictionListenerFactory")
//    public void crimesForLondonPredictionModelTrainListener(ConsumerRecord<String, CrimeLevel> record) {
//        LOG.info("Starting training prediction");
//        trainModelBasedOnIndexType(record.key(), LocalDate.parse(record.value(), DateTimeFormatter.ISO_DATE));
//        LOG.info("Finished calculating prediction");
//    }
//
//    @KafkaListener(topics = "train_prediction_model_by_category", containerFactory = "kafkaCalculatePredictionListenerFactory")
//    public void crimesForLondonPredictionModelTrainListener(ConsumerRecord<String, String> record) {
//        LOG.info("Starting training prediction");
//        trainModelBasedOnIndexType(record.key(), LocalDate.parse(record.value(), DateTimeFormatter.ISO_DATE));
//        LOG.info("Finished calculating prediction");
//    }
//
//    @KafkaListener(topics = "train_prediction_model_by_neighbourhood", containerFactory = "kafkaCalculatePredictionListenerFactory")
//    public void crimesForLondonPredictionModelTrainListener(ConsumerRecord<String, String> record) {
//        LOG.info("Starting training prediction");
//        trainModelBasedOnIndexType(record.key(), LocalDate.parse(record.value(), DateTimeFormatter.ISO_DATE));
//        LOG.info("Finished calculating prediction");
//    }
    @KafkaListener(topics = "train_prediction_model_by_neighbourhood_and_category", containerFactory = "kafkaTrainPredictionModelByNeighbourhoodAndCategoryListenerFactory")
    public void crimesByNeighbourhoodAndCategoryPredictionModelTrainListener(ConsumerRecord<String, CrimeLevelByNeighbourhoodAndCategory> record) {
        LOG.info("Starting training model");
        crimeByNeighbourhoodAndCategoryPredictionCalculator.train(record.value());
        LOG.info("Finished training model");
        predictionMessageProducer.send("calculate_prediction", IndexType.NEIGHBOURHOOD_AND_CATEGORY.getName(), record.key());

    }
//    @KafkaListener(topics = "train_prediction_model_by_street", containerFactory = "kafkaCalculatePredictionListenerFactory")
//    public void crimesForLondonPredictionModelTrainListener(ConsumerRecord<String, String> record) {
//        LOG.info("Starting training prediction");
//        trainModelBasedOnIndexType(record.key(), LocalDate.parse(record.value(), DateTimeFormatter.ISO_DATE));
//        LOG.info("Finished calculating prediction");
//    }
//    @KafkaListener(topics = "train_prediction_model_by_street_and_category", containerFactory = "kafkaCalculatePredictionListenerFactory")
//    public void crimesForLondonPredictionModelTrainListener(ConsumerRecord<String, String> record) {
//        LOG.info("Starting training prediction");
//        trainModelBasedOnIndexType(record.key(), LocalDate.parse(record.value(), DateTimeFormatter.ISO_DATE));
//        LOG.info("Finished calculating prediction");
//    }

}
