package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.cassandra.kafka.messages.MonthDate;
import com.safety.recognition.cassandra.model.indexes.IndexType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class PredictionListener {

    private static final Logger LOG = LoggerFactory.getLogger(PredictionListener.class);

    private final CrimeByNeighbourhoodPredictionCalculator crimeByNeighbourhoodPredictionCalculator;
    private final CrimeByNeighbourhoodAndCategoryPredictionCalculator crimeByNeighbourhoodAndCategoryPredictionCalculator;
    private final CrimeByStreetPredictionCalculator crimeByStreetPredictionCalculator;
    private final CrimeByStreetAndCategoryPredictionCalculator crimeByStreetAndCategoryPredictionCalculator;
    private final CrimeForLondonPredictionCalculator crimeForLondonPredictionCalculator;
    private final CrimeForLondonByCategoryPredictionCalculator crimeForLondonByCategoryPredictionCalculator;

    @Autowired
    public PredictionListener(CrimeByNeighbourhoodPredictionCalculator crimeByNeighbourhoodPredictionCalculator, CrimeByNeighbourhoodAndCategoryPredictionCalculator crimeByNeighbourhoodAndCategoryPredictionCalculator, CrimeByStreetPredictionCalculator crimeByStreetPredictionCalculator, CrimeByStreetAndCategoryPredictionCalculator crimeByStreetAndCategoryPredictionCalculator, CrimeForLondonPredictionCalculator crimeForLondonPredictionCalculator, CrimeForLondonByCategoryPredictionCalculator crimeForLondonByCategoryPredictionCalculator) {
        this.crimeByNeighbourhoodPredictionCalculator = crimeByNeighbourhoodPredictionCalculator;
        this.crimeByNeighbourhoodAndCategoryPredictionCalculator = crimeByNeighbourhoodAndCategoryPredictionCalculator;
        this.crimeByStreetPredictionCalculator = crimeByStreetPredictionCalculator;
        this.crimeByStreetAndCategoryPredictionCalculator = crimeByStreetAndCategoryPredictionCalculator;
        this.crimeForLondonPredictionCalculator = crimeForLondonPredictionCalculator;
        this.crimeForLondonByCategoryPredictionCalculator = crimeForLondonByCategoryPredictionCalculator;
    }

    @KafkaListener(topics = "calculate_prediction", containerFactory = "kafkaCalculatePredictionListenerFactory")
    public void crimesForLondonPredictionCalculatorListener(String indexType, MonthDate monthDate) {
        LOG.info("Starting calculating prediction");
            calculatePredictionBasedOnIndexType(indexType, monthDate.getDate());
        LOG.info("Finished calculating prediction");
    }

    private void calculatePredictionBasedOnIndexType(String indexType, LocalDate nextMonth) {
        switch(IndexType.valueOf(indexType)) {
            case LONDON:
                crimeForLondonPredictionCalculator.calculate(nextMonth);
            case LONDON_AND_CATEGORY:
                crimeForLondonByCategoryPredictionCalculator.calculate(nextMonth);
            case NEIGHBOURHOOD:
                crimeByNeighbourhoodPredictionCalculator.calculate(nextMonth);
                break;
            case NEIGHBOURHOOD_AND_CATEGORY:
                crimeByNeighbourhoodAndCategoryPredictionCalculator.calculate(nextMonth);
            case STREET:
                crimeByStreetPredictionCalculator.calculate(nextMonth);
            case STREET_AND_CATEGORY:
                crimeByStreetAndCategoryPredictionCalculator.calculate(nextMonth);
                default:
                    break;
        }

    }


}
