package com.safety.recognition.processor;

import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.crime.*;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.crime.*;
import data.police.uk.model.crime.Crime;
import data.police.uk.utils.MonthParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class CrimeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CrimeProcessor.class);


    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final CrimeByCategoryRepository crimeByCategoryRepository;
    private final CrimeByStreetRepository crimeByStreetRepository;
    private final CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository;
    private final CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository;
    private final CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository;
    private final CrimeRepository crimeRepository;
    private final StreetRepository streetRepository;

    @Autowired
    public CrimeProcessor(CrimeByCategoryRepository crimeByCategoryRepository, CrimeByStreetRepository crimeByStreetRepository, CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository, CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository, CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository, CrimeRepository crimeRepository, StreetRepository streetRepository) {
        this.crimeByCategoryRepository = crimeByCategoryRepository;
        this.crimeByStreetRepository = crimeByStreetRepository;
        this.crimeByStreetAndCategoryRepository = crimeByStreetAndCategoryRepository;
        this.crimeByNeighbourhoodRepository = crimeByNeighbourhoodRepository;
        this.crimeByNeighbourhoodAndCategoryRepository = crimeByNeighbourhoodAndCategoryRepository;
        this.crimeRepository = crimeRepository;
        this.streetRepository = streetRepository;
    }

    public void process(Crime crime) {
        LOG.info(String.format("Starting processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
        crime = reduceCategoryUpperCasing(crime);
        processStreet(crime);
        crimeRepository.save(extractCrime(crime));
        crimeByCategoryRepository.save(extractCrimeByCategory(crime));
        crimeByStreetRepository.save(extractCrimeByStreet(crime));
        crimeByStreetAndCategoryRepository.save(extractCrimeByStreetAndCategory(crime));
        crimeByNeighbourhoodRepository.save(extractCrimeByNeighbourhood(crime));
        crimeByNeighbourhoodAndCategoryRepository.save(extractCrimeByNeighbourhoodAndCategory(crime));
        LOG.info(String.format("Finished processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
    }

    private Crime reduceCategoryUpperCasing(Crime crime) {
        crime.setCategory(crime.getCategory().toLowerCase());
        return crime;
    }

    private void processStreet(Crime crime) {
        var streetKey = new StreetKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood());
        if(streetRepository.findById(streetKey).isEmpty()) {
            streetRepository.save(new Street(streetKey, new Random().nextLong()));
        }
    }

    private com.safety.recognition.cassandra.model.crime.Crime extractCrime(Crime crime) {
        return new com.safety.recognition.cassandra.model.crime.Crime(new CrimeKey(crime.getId(), MonthParser.toLocalDate(crime.getMonth())),new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByCategory extractCrimeByCategory(Crime crime) {
        return new CrimeByCategory(crime.getId(), new CrimeByCategoryKey(crime.getCategory(), MonthParser.toLocalDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByStreet extractCrimeByStreet(Crime crime) {
        return new CrimeByStreet(crime.getId(), new CrimeByStreetKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood(), MonthParser.toLocalDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByStreetAndCategory extractCrimeByStreetAndCategory(Crime crime) {
        return new CrimeByStreetAndCategory(crime.getId(), new CrimeByStreetAndCategoryKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood(), crime.getCategory(), MonthParser.toLocalDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByNeighbourhood extractCrimeByNeighbourhood(Crime crime) {
        return new CrimeByNeighbourhood(crime.getId(), new CrimeByNeighbourhoodKey(crime.getNeighbourhood(), MonthParser.toLocalDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByNeighbourhoodAndCategory extractCrimeByNeighbourhoodAndCategory(Crime crime) {
        return new CrimeByNeighbourhoodAndCategory(crime.getId(), new CrimeByNeighbourhoodAndCategoryKey(crime.getNeighbourhood(), crime.getCategory(), MonthParser.toLocalDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

}
