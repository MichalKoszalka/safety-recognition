package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.crime.CrimeByCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrimesForLondonByCategoryIndexesCalculator {


    private final CrimesForLondonByCategoryAllTimeIndexRepository crimesForLondonByCategoryAllTimeIndexRepository;
    private final CrimesForLondonByCategoryLastYearIndexRepository crimesForLondonByCategoryLastYearIndexRepository;
    private final CrimesForLondonByCategoryLast3MonthsIndexRepository crimesForLondonByCategoryLast3MonthsIndexRepository;
    private final CrimeByCategoryRepository crimeByCategoryRepository;
    private final HighestCrimeLevelByCategoryRepository highestCrimeLevelByCategoryRepository;
    private final CrimeLevelByCategoryRepository crimeLevelByCategoryRepository;

    @Autowired
    public CrimesForLondonByCategoryIndexesCalculator(CrimesForLondonByCategoryAllTimeIndexRepository crimesForLondonByCategoryAllTimeIndexRepository, CrimesForLondonByCategoryLastYearIndexRepository crimesForLondonByCategoryLastYearIndexRepository, CrimesForLondonByCategoryLast3MonthsIndexRepository crimesForLondonByCategoryLast3MonthsIndexRepository, CrimeByCategoryRepository crimeByCategoryRepository, HighestCrimeLevelByCategoryRepository highestCrimeLevelByCategoryRepository, CrimeLevelByCategoryRepository crimeLevelByCategoryRepository) {
        this.crimesForLondonByCategoryAllTimeIndexRepository = crimesForLondonByCategoryAllTimeIndexRepository;
        this.crimesForLondonByCategoryLastYearIndexRepository = crimesForLondonByCategoryLastYearIndexRepository;
        this.crimesForLondonByCategoryLast3MonthsIndexRepository = crimesForLondonByCategoryLast3MonthsIndexRepository;
        this.crimeByCategoryRepository = crimeByCategoryRepository;
        this.highestCrimeLevelByCategoryRepository = highestCrimeLevelByCategoryRepository;
        this.crimeLevelByCategoryRepository = crimeLevelByCategoryRepository;
    }

    public void calculate(LocalDate lastUpdateDate, String category) {
            calculateIndexForLastYear(lastUpdateDate, category);
            calculateIndexForLast3Months(lastUpdateDate, category);
            calculateAllTimeIndex(category);
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByCategoryRepository.findCrimeByKeyCategoryAndKeyCrimeDateAfter(category, yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonByCategoryLastYearIndex(category, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesForLondonByCategoryLastYearIndexRepository.deleteById(category);
        crimesForLondonByCategoryLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String category) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByCategoryRepository.findCrimeByKeyCategoryAndKeyCrimeDateAfter(category, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonByCategoryLast3MonthsIndex(category, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesForLondonByCategoryLast3MonthsIndexRepository.deleteById(category);
        crimesForLondonByCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex(String category) {
        var lastYearCrimes = crimeByCategoryRepository.findCrimeByKeyCategory(category);
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth, category);
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonByCategoryAllTimeIndex(category, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesForLondonByCategoryAllTimeIndexRepository.deleteById(category);
        crimesForLondonByCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
        calculateCrimeLevelByCategory(crimesByMonth, category);
    }

    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth, String category) {
        var highestCrimeLevel = highestCrimeLevelByCategoryRepository.findById(category);
        if(highestCrimeLevel.isPresent() && !CollectionUtils.isEmpty(highestCrimeLevel.get().getHighestLevelForLondonByMonth())) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForLondonByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForLondonByMonth().forEach((month, level) -> {
                if(crimesByMonth.containsKey(month) && crimesByMonth.get(month) > level) {
                    newHighestCrimeLevelForLondonByMonth.put(month, crimesByMonth.get(month));
                } else {
                    newHighestCrimeLevelForLondonByMonth.put(month, level);
                }
            });
            highestCrimeLevelValue.setHighestLevelForLondonByMonth(newHighestCrimeLevelForLondonByMonth);
            highestCrimeLevelByCategoryRepository.save(highestCrimeLevelValue);
        } else {
            var newHighestCrimeLevel = new HighestCrimeLevelByCategory();
            newHighestCrimeLevel.setCategory(category);
            newHighestCrimeLevel.setHighestLevelForLondonByMonth(crimesByMonth);
            highestCrimeLevelByCategoryRepository.save(newHighestCrimeLevel);
        }
    }

    private void calculateCrimeLevelByCategory(Map<LocalDate, Long> crimesByMonth, String category) {
        crimeLevelByCategoryRepository.save(new CrimeLevelByCategory(category, crimesByMonth));

    }
}
