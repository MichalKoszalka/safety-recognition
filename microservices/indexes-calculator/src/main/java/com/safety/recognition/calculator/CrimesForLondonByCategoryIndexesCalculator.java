package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonByCategoryAllTimeIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesForLondonByCategoryLast3MonthsIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesForLondonByCategoryLastYearIndex;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesForLondonByCategoryAllTimeIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesForLondonByCategoryLast3MonthsIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesForLondonByCategoryLastYearIndexRepository;
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
public class CrimesForLondonByCategoryIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesForLondonByCategoryIndexesCalculator.class);

    private final CrimesForLondonByCategoryAllTimeIndexRepository crimesForLondonByCategoryAllTimeIndexRepository;
    private final CrimesForLondonByCategoryLastYearIndexRepository crimesForLondonByCategoryLastYearIndexRepository;
    private final CrimesForLondonByCategoryLast3MonthsIndexRepository crimesForLondonByCategoryLast3MonthsIndexRepository;
    private final CrimeByCategoryRepository crimeByCategoryRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;

    @Autowired
    public CrimesForLondonByCategoryIndexesCalculator(CrimesForLondonByCategoryAllTimeIndexRepository crimesForLondonByCategoryAllTimeIndexRepository, CrimesForLondonByCategoryLastYearIndexRepository crimesForLondonByCategoryLastYearIndexRepository, CrimesForLondonByCategoryLast3MonthsIndexRepository crimesForLondonByCategoryLast3MonthsIndexRepository, CrimeByCategoryRepository crimeByCategoryRepository, LastUpdateDateRepository lastUpdateDateRepository) {
        this.crimesForLondonByCategoryAllTimeIndexRepository = crimesForLondonByCategoryAllTimeIndexRepository;
        this.crimesForLondonByCategoryLastYearIndexRepository = crimesForLondonByCategoryLastYearIndexRepository;
        this.crimesForLondonByCategoryLast3MonthsIndexRepository = crimesForLondonByCategoryLast3MonthsIndexRepository;
        this.crimeByCategoryRepository = crimeByCategoryRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }

    public void calculate(String category) {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if(lastUpdateDate.isPresent()) {
            LOG.info(String.format("Calculating indexes for london and category %s.", category));
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate(), category);
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate(), category);
            calculateAllTimeIndex(category);
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByCategoryRepository.findCrimeByKeyCategoryAndKeyCrimeDateAfter(category, yearBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonByCategoryLastYearIndex(category, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesForLondonByCategoryLastYearIndexRepository.deleteById(category);
        crimesForLondonByCategoryLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String category) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByCategoryRepository.findCrimeByKeyCategoryAndKeyCrimeDateAfter(category, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonByCategoryLast3MonthsIndex(category, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesForLondonByCategoryLast3MonthsIndexRepository.deleteById(category);
        crimesForLondonByCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex(String category) {
        var lastYearCrimes = crimeByCategoryRepository.findCrimeByKeyCategory(category);
        var numberOfCrimes = lastYearCrimes.size();
        var entriesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonByCategoryAllTimeIndex(category, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesForLondonByCategoryAllTimeIndexRepository.deleteById(category);
        crimesForLondonByCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
    }
}
