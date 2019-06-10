package com.safety.recognition.kafka;

import com.safety.recognition.calculator.*;
import com.safety.recognition.cassandra.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndNeighbourhood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
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

    public CalculateIndexesListener(CrimesByNeighbourhoodAndCategoryIndexesCalculator crimesByNeighbourhoodAndCategoryIndexesCalculator, CrimesByNeighbourhoodIndexesCalculator crimesByNeighbourhoodIndexesCalculator, CrimesByStreetAndCategoryIndexesCalculator crimesByStreetAndCategoryIndexesCalculator, CrimesByStreetIndexesCalculator crimesByStreetIndexesCalculator, CrimesForLondonByCategoryIndexesCalculator crimesForLondonByCategoryIndexesCalculator, CrimesForLondonIndexesCalculator crimesForLondonIndexesCalculator) {
        this.crimesByNeighbourhoodAndCategoryIndexesCalculator = crimesByNeighbourhoodAndCategoryIndexesCalculator;
        this.crimesByNeighbourhoodIndexesCalculator = crimesByNeighbourhoodIndexesCalculator;
        this.crimesByStreetAndCategoryIndexesCalculator = crimesByStreetAndCategoryIndexesCalculator;
        this.crimesByStreetIndexesCalculator = crimesByStreetIndexesCalculator;
        this.crimesForLondonByCategoryIndexesCalculator = crimesForLondonByCategoryIndexesCalculator;
        this.crimesForLondonIndexesCalculator = crimesForLondonIndexesCalculator;
    }

    @KafkaListener(topics = "calculate_indexes_for_london", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesForLondonIndexesCalculatorListener() {
        LOG.info("Starting calculating indexes for London");
        crimesForLondonIndexesCalculator.calculate();
        LOG.info("Finished calculating indexes for London");
    }

    @KafkaListener(topics = "calculate_indexes_for_london_by_category", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesForLondonByCategoryIndexesCalculatorListener(String category) {
        LOG.info(String.format("Starting calculating indexes for London and category %s", category));
        crimesForLondonByCategoryIndexesCalculator.calculate(category);
        LOG.info(String.format("Finished calculating indexes for London and category %s", category));
    }

    @KafkaListener(topics = "calculate_indexes_by_street", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesByStreetIndexesCalculatorListener(StreetAndNeighbourhood streetAndNeighbourhood) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
        crimesByStreetIndexesCalculator.calculate(streetAndNeighbourhood);
        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s", streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
    }

    @KafkaListener(topics = "calculate_indexes_by_street_and_category", containerFactory = "kafkaCalculateIndexesStreetAndCategoryListenerFactory")
    public void crimesByStreetAndCategoryIndexesCalculatorListener(StreetAndCategory streetAndCategory) {
        LOG.info(String.format("Starting calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategory.getStreetAndNeighbourhood().getStreet(), streetAndCategory.getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategory.getCategory()));
        crimesByStreetAndCategoryIndexesCalculator.calculate(streetAndCategory.getStreetAndNeighbourhood(), streetAndCategory.getCategory());
        LOG.info(String.format("Finished calculating indexes for London and street %s in neighbourhood %s and category %s", streetAndCategory.getStreetAndNeighbourhood().getStreet(), streetAndCategory.getStreetAndNeighbourhood().getNeighbourhood(), streetAndCategory.getCategory()));
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood", containerFactory = "kafkaCalculateIndexesStringListenerFactory")
    public void crimesByNeighbourhoodIndexesCalculatorListener(String neighbourhood) {
        LOG.info(String.format("Starting calculating indexes for London and neighbourhood %s", neighbourhood));
        crimesByNeighbourhoodIndexesCalculator.calculate(neighbourhood);
        LOG.info(String.format("Finished calculating indexes for London and neighbourhood %s", neighbourhood));
    }

    @KafkaListener(topics = "calculate_indexes_by_neighbourhood_and_category", containerFactory = "kafkaCalculateIndexesNeighbourhoodAndCategoryListenerFactory")
    public void crimesByNeighbourhoodAndCategoryIndexesCalculatorListener(NeighbourhoodAndCategory neighbourhoodAndCategory) {
        LOG.info(String.format("Starting calculating indexes for London and street %s and category %s", neighbourhoodAndCategory.getNeighbourhood(), neighbourhoodAndCategory.getCategory()));
        crimesByNeighbourhoodAndCategoryIndexesCalculator.calculate(neighbourhoodAndCategory.getNeighbourhood(), neighbourhoodAndCategory.getCategory());
        LOG.info(String.format("Finished calculating indexes for London and street %s and category %s", neighbourhoodAndCategory.getNeighbourhood(), neighbourhoodAndCategory.getCategory()));
    }
}
