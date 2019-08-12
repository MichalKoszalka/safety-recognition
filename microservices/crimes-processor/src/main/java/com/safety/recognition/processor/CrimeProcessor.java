package com.safety.recognition.processor;

import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.crime.*;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.crime.*;
import data.police.uk.model.crime.Crime;
import data.police.uk.utils.MonthParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class CrimeProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(CrimeProcessor.class);


    private final CrimeByCategoryRepository crimeByCategoryRepository;
    private final CrimeByStreetRepository crimeByStreetRepository;
    private final CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository;
    private final CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository;
    private final CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository;
    private final CrimeRepository crimeRepository;
    private final StreetRepository streetRepository;
    private final CrimeCategoryRepository crimeCategoryRepository;

    @Autowired
    public CrimeProcessor(CrimeByCategoryRepository crimeByCategoryRepository, CrimeByStreetRepository crimeByStreetRepository, CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository, CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository, CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository, CrimeRepository crimeRepository, StreetRepository streetRepository, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeByCategoryRepository = crimeByCategoryRepository;
        this.crimeByStreetRepository = crimeByStreetRepository;
        this.crimeByStreetAndCategoryRepository = crimeByStreetAndCategoryRepository;
        this.crimeByNeighbourhoodRepository = crimeByNeighbourhoodRepository;
        this.crimeByNeighbourhoodAndCategoryRepository = crimeByNeighbourhoodAndCategoryRepository;
        this.crimeRepository = crimeRepository;
        this.streetRepository = streetRepository;
        this.crimeCategoryRepository = crimeCategoryRepository;
    }

    public void process(Crime crime) {
        LOG.info(String.format("Starting processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
        String category = reduceCategoryUpperCasing(crime);
        createCategoryIfNotExists(crime, category);
        processStreet(crime);
        crimeRepository.save(extractCrime(crime));
        crimeByCategoryRepository.save(extractCrimeByCategory(crime));
        crimeByStreetRepository.save(extractCrimeByStreet(crime));
        crimeByStreetAndCategoryRepository.save(extractCrimeByStreetAndCategory(crime));
        crimeByNeighbourhoodRepository.save(extractCrimeByNeighbourhood(crime));
        crimeByNeighbourhoodAndCategoryRepository.save(extractCrimeByNeighbourhoodAndCategory(crime));
        LOG.info(String.format("Finished processing crime with id: %s, street: %s, category: %s and neighbourhood: %s", crime.getId(), crime.getLocation().getStreet().getName(), crime.getCategory(), crime.getNeighbourhood()));
    }

    private void createCategoryIfNotExists(Crime crime, String category) {
        crimeCategoryRepository.findById(category).ifPresentOrElse((existingCategory) -> LOG.info(String.format("Crime category exists %s", existingCategory.getUrl())), () -> crimeCategoryRepository.save(new CrimeCategory(category, category.hashCode(), crime.getCategory())));
    }

    private String reduceCategoryUpperCasing(Crime crime) {
        return crime.getCategory().toLowerCase();
    }

    private void processStreet(Crime crime) {
        var streetKey = new StreetKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood());
        if(streetRepository.findById(streetKey).isEmpty()) {
            streetRepository.save(new Street(streetKey, (streetKey.getNeighbourhood()+streetKey.getStreet()).hashCode()));
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
