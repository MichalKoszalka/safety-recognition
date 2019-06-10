package com.safety.recognition.processor;

import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.crime.*;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.crime.*;
import data.police.uk.model.crime.Crime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
public class CrimeProcessor {

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
        processStreet(crime);
        crimeRepository.save(extractCrime(crime));
        crimeByCategoryRepository.save(extractCrimeByCategory(crime));
        crimeByStreetRepository.save(extractCrimeByStreet(crime));
        crimeByStreetAndCategoryRepository.save(extractCrimeByStreetAndCategory(crime));
        crimeByNeighbourhoodRepository.save(extractCrimeByNeighbourhood(crime));
        crimeByNeighbourhoodAndCategoryRepository.save(extractCrimeByNeighbourhoodAndCategory(crime));
    }

    private void processStreet(Crime crime) {
        var streetKey = new StreetKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood());
        if(streetRepository.findById(streetKey).isEmpty()) {
            streetRepository.save(new Street(streetKey, new Random().nextLong()));
        }
    }

    private com.safety.recognition.cassandra.model.crime.Crime extractCrime(Crime crime) {
        return new com.safety.recognition.cassandra.model.crime.Crime(new CrimeKey(crime.getId(), parseDate(crime.getMonth())),new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByCategory extractCrimeByCategory(Crime crime) {
        return new CrimeByCategory(crime.getId(), new CrimeByCategoryKey(crime.getCategory(), parseDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByStreet extractCrimeByStreet(Crime crime) {
        return new CrimeByStreet(crime.getId(), new CrimeByStreetKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood(), parseDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByStreetAndCategory extractCrimeByStreetAndCategory(Crime crime) {
        return new CrimeByStreetAndCategory(crime.getId(), new CrimeByStreetAndCategoryKey(crime.getLocation().getStreet().getName(), crime.getNeighbourhood(), crime.getCategory(), parseDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByNeighbourhood extractCrimeByNeighbourhood(Crime crime) {
        return new CrimeByNeighbourhood(crime.getId(), new CrimeByNeighbourhoodKey(crime.getNeighbourhood(), parseDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private CrimeByNeighbourhoodAndCategory extractCrimeByNeighbourhoodAndCategory(Crime crime) {
        return new CrimeByNeighbourhoodAndCategory(crime.getId(), new CrimeByNeighbourhoodAndCategoryKey(crime.getNeighbourhood(), crime.getCategory(), parseDate(crime.getMonth())), new Point(crime.getLocation().getLatitude().doubleValue(), crime.getLocation().getLongitude().doubleValue()));
    }

    private LocalDate parseDate(String month) {
        return LocalDate.parse(month + "-01", FORMATTER);
    }

}
