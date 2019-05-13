package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByStreetAndCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByStreetAndCategoryAllTimeIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByStreetAndCategoryLast3MonthsIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByStreetAndCategoryLastYearIndexRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrimesByStreetAndCategoryIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByStreetAndCategoryIndexesCalculator.class);

    private final CrimesByStreetAndCategoryAllTimeIndexRepository crimesByStreetAndCategoryAllTimeIndexRepository;
    private final CrimesByStreetAndCategoryLastYearIndexRepository crimesByStreetAndCategoryLastYearIndexRepository;
    private final CrimesByStreetAndCategoryLast3MonthsIndexRepository crimesByStreetAndCategoryLast3MonthsIndexRepository;
    private final CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;

    @Autowired
    public CrimesByStreetAndCategoryIndexesCalculator(CrimesByStreetAndCategoryAllTimeIndexRepository crimesByStreetAndCategoryAllTimeIndexRepository, CrimesByStreetAndCategoryLastYearIndexRepository crimesByStreetAndCategoryLastYearIndexRepository, CrimesByStreetAndCategoryLast3MonthsIndexRepository crimesByStreetAndCategoryLast3MonthsIndexRepository, CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository, LastUpdateDateRepository lastUpdateDateRepository) {
        this.crimesByStreetAndCategoryAllTimeIndexRepository = crimesByStreetAndCategoryAllTimeIndexRepository;
        this.crimesByStreetAndCategoryLastYearIndexRepository = crimesByStreetAndCategoryLastYearIndexRepository;
        this.crimesByStreetAndCategoryLast3MonthsIndexRepository = crimesByStreetAndCategoryLast3MonthsIndexRepository;
        this.crimeByStreetAndCategoryRepository = crimeByStreetAndCategoryRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }

    public void calculate(String street, String category) {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if(lastUpdateDate.isPresent()) {
            LOG.info(String.format("Calculating indexes for street %s and category %s", street, category));
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate(), street, category);
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate(), street, category);
            calculateAllTimeIndex(street, category);
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String street, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyCategoryAndKeyCrimeDateAfter(street, category, yearBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(street, category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryLastYearIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByStreetAndCategoryLastYearIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String street, String category) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyCategoryAndKeyCrimeDateAfter(street, category, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(street, category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryLast3MonthsIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByStreetAndCategoryLast3MonthsIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex(String street, String category) {
        var lastYearCrimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyCategory(street, category);
        var numberOfCrimes = lastYearCrimes.size();
        var entriesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(street, category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryAllTimeIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByStreetAndCategoryAllTimeIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
    }
}
