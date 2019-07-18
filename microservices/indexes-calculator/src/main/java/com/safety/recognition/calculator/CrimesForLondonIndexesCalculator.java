package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeRepository;
import com.safety.recognition.cassandra.repository.indexes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class CrimesForLondonIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesForLondonIndexesCalculator.class);

    private final CrimesForLondonAllTimeIndexRepository crimesForLondonAllTimeIndexRepository;
    private final CrimesForLondonLastYearIndexRepository crimesForLondonLastYearIndexRepository;
    private final CrimesForLondonLast3MonthsIndexRepository crimesForLondonLast3MonthsIndexRepository;
    private final CrimeRepository crimeRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;
    private final HighestCrimeLevelRepository highestCrimeLevelRepository;
    private final CrimeLevelRepository crimeLevelRepository;

    @Autowired
    public CrimesForLondonIndexesCalculator(CrimesForLondonAllTimeIndexRepository crimesForLondonAllTimeIndexRepository, CrimesForLondonLastYearIndexRepository crimesForLondonLastYearIndexRepository, CrimesForLondonLast3MonthsIndexRepository crimesForLondonLast3MonthsIndexRepository, CrimeRepository crimeRepository, LastUpdateDateRepository lastUpdateDateRepository, HighestCrimeLevelRepository highestCrimeLevelRepository, CrimeLevelRepository crimeLevelRepository) {
        this.crimesForLondonAllTimeIndexRepository = crimesForLondonAllTimeIndexRepository;
        this.crimesForLondonLastYearIndexRepository = crimesForLondonLastYearIndexRepository;
        this.crimesForLondonLast3MonthsIndexRepository = crimesForLondonLast3MonthsIndexRepository;
        this.crimeRepository = crimeRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.highestCrimeLevelRepository = highestCrimeLevelRepository;
        this.crimeLevelRepository = crimeLevelRepository;
    }

    public CrimeLevel calculate(LocalDate lastUpdatedMonth) {
        calculateIndexForLastYear(lastUpdatedMonth);
        calculateIndexForLast3Months(lastUpdatedMonth);
        return calculateAllTimeIndex();
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeRepository.findCrimesByKeyCrimeDateAfter(yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonLastYearIndex("London", numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesForLondonLastYearIndexRepository.deleteById(lastYearCrimesIndex.getCity());
        crimesForLondonLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeRepository.findCrimesByKeyCrimeDateAfter(threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonLast3MonthsIndex("London", numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesForLondonLast3MonthsIndexRepository.deleteById(lastYearCrimesIndex.getCity());
        crimesForLondonLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private CrimeLevel calculateAllTimeIndex() {
        var lastYearCrimes = crimeRepository.findAll();
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth);
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonAllTimeIndex("London", numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesForLondonAllTimeIndexRepository.deleteById(lastYearCrimesIndex.getCity());
        crimesForLondonAllTimeIndexRepository.save(lastYearCrimesIndex);
        return calculateCrimeLevel(crimesByMonth);
    }

    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth) {
        var highestCrimeLevel = highestCrimeLevelRepository.findAll().stream().findAny();
        if (highestCrimeLevel.isPresent() && !CollectionUtils.isEmpty(highestCrimeLevel.get().getHighestLevelForLondonByMonth())) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForLondonByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForLondonByMonth().forEach((month, level) -> {
                if (crimesByMonth.containsKey(month) && crimesByMonth.get(month) > level) {
                    newHighestCrimeLevelForLondonByMonth.put(month, crimesByMonth.get(month));
                } else {
                    newHighestCrimeLevelForLondonByMonth.put(month, level);
                }
            });
            highestCrimeLevelValue.setHighestLevelForLondonByMonth(newHighestCrimeLevelForLondonByMonth);
            highestCrimeLevelRepository.save(highestCrimeLevelValue);
        } else {
            var newHighestCrimeLevel = new HighestCrimeLevel();
            newHighestCrimeLevel.setId(1L);
            newHighestCrimeLevel.setHighestLevelForLondonByMonth(crimesByMonth);
            highestCrimeLevelRepository.save(newHighestCrimeLevel);
        }
    }

    private CrimeLevel calculateCrimeLevel(Map<LocalDate, Long> crimesByMonth) {
        return crimeLevelRepository.save(new CrimeLevel("London", crimesByMonth));
    }


}
