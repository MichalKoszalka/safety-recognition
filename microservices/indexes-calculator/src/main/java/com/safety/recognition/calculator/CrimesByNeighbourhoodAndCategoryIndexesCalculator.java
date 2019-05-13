package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryAllTimeIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryIndexKey;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryLast3MonthsIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAndCategoryLastYearIndex;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByNeighbourhoodAndCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodAndCategoryAllTimeIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodAndCategoryLastYearIndexRepository;
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
public class CrimesByNeighbourhoodAndCategoryIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByNeighbourhoodAndCategoryIndexesCalculator.class);

    private final CrimesByNeighbourhoodAndCategoryAllTimeIndexRepository crimesByNeighbourhoodAndCategoryAllTimeIndexRepository;
    private final CrimesByNeighbourhoodAndCategoryLastYearIndexRepository crimesByNeighbourhoodAndCategoryLastYearIndexRepository;
    private final CrimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository;
    private final CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;

    @Autowired
    public CrimesByNeighbourhoodAndCategoryIndexesCalculator(CrimesByNeighbourhoodAndCategoryAllTimeIndexRepository crimesByNeighbourhoodAndCategoryAllTimeIndexRepository, CrimesByNeighbourhoodAndCategoryLastYearIndexRepository crimesByNeighbourhoodAndCategoryLastYearIndexRepository, CrimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository, CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository, LastUpdateDateRepository lastUpdateDateRepository) {
        this.crimesByNeighbourhoodAndCategoryAllTimeIndexRepository = crimesByNeighbourhoodAndCategoryAllTimeIndexRepository;
        this.crimesByNeighbourhoodAndCategoryLastYearIndexRepository = crimesByNeighbourhoodAndCategoryLastYearIndexRepository;
        this.crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository = crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository;
        this.crimeByNeighbourhoodAndCategoryRepository = crimeByNeighbourhoodAndCategoryRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }

    public void calculate(String neighbourhood, String category) {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            LOG.info(String.format("Calculating indexes for neighbourhood %s and category %s", neighbourhood, category));
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate(), neighbourhood, category);
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate(), neighbourhood, category);
            calculateAllTimeIndex(neighbourhood, category);
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String neighbourhood, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByNeighbourhoodAndCategoryRepository.findCrimeByKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(neighbourhood, category, yearBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByNeighbourhoodAndCategoryIndexKey(neighbourhood, category);
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAndCategoryLastYearIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByNeighbourhoodAndCategoryLastYearIndexRepository.deleteById(indexKey);
        crimesByNeighbourhoodAndCategoryLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String neighbourhood, String category) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByNeighbourhoodAndCategoryRepository.findCrimeByKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(neighbourhood, category, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
       var indexKey = new CrimesByNeighbourhoodAndCategoryIndexKey(neighbourhood, category);
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAndCategoryLast3MonthsIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository.deleteById(indexKey);
        crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex(String neighbourhood, String category) {
        var lastYearCrimes = crimeByNeighbourhoodAndCategoryRepository.findCrimeByKeyNeighbourhoodAndKeyCategory(neighbourhood, category);
        var numberOfCrimes = lastYearCrimes.size();
        var entriesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var indexKey = new CrimesByNeighbourhoodAndCategoryIndexKey(neighbourhood, category);
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAndCategoryAllTimeIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByNeighbourhoodAndCategoryAllTimeIndexRepository.deleteById(indexKey);
        crimesByNeighbourhoodAndCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
    }
}
