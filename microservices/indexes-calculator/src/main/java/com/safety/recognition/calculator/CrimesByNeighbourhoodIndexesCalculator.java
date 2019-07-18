package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByNeighbourhoodRepository;
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
public class CrimesByNeighbourhoodIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByNeighbourhoodIndexesCalculator.class);

    private final CrimesByNeighbourhoodAllTimeIndexRepository crimesByNeighbourhoodAllTimeIndexRepository;
    private final CrimesByNeighbourhoodLastYearIndexRepository crimesByNeighbourhoodLastYearIndexRepository;
    private final CrimesByNeighbourhoodLast3MonthsIndexRepository crimesByNeighbourhoodLast3MonthsIndexRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;
    private final HighestCrimeLevelRepository highestCrimeLevelRepository;
    private final CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository;
    private final CrimeLevelByNeighbourhoodRepository crimeLevelByNeighbourhoodRepository;

    @Autowired
    public CrimesByNeighbourhoodIndexesCalculator(CrimesByNeighbourhoodAllTimeIndexRepository crimesByNeighbourhoodAllTimeIndexRepository, CrimesByNeighbourhoodLastYearIndexRepository crimesByNeighbourhoodLastYearIndexRepository, CrimesByNeighbourhoodLast3MonthsIndexRepository crimesByNeighbourhoodLast3MonthsIndexRepository, LastUpdateDateRepository lastUpdateDateRepository, HighestCrimeLevelRepository highestCrimeLevelRepository, CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository, CrimeLevelByNeighbourhoodRepository crimeLevelByNeighbourhoodRepository) {
        this.crimesByNeighbourhoodAllTimeIndexRepository = crimesByNeighbourhoodAllTimeIndexRepository;
        this.crimesByNeighbourhoodLastYearIndexRepository = crimesByNeighbourhoodLastYearIndexRepository;
        this.crimesByNeighbourhoodLast3MonthsIndexRepository = crimesByNeighbourhoodLast3MonthsIndexRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.highestCrimeLevelRepository = highestCrimeLevelRepository;
        this.crimeByNeighbourhoodRepository = crimeByNeighbourhoodRepository;
        this.crimeLevelByNeighbourhoodRepository = crimeLevelByNeighbourhoodRepository;
    }

    public CrimeLevelByNeighbourhood calculate(LocalDate lastUpdatedMonth, String neighbourhood) {
            calculateIndexForLastYear(lastUpdatedMonth, neighbourhood);
            calculateIndexForLast3Months(lastUpdatedMonth, neighbourhood);
            return calculateAllTimeIndexAndCrimeLevel(neighbourhood);
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String neighbourhood) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByNeighbourhoodRepository.findCrimeByKeyNeighbourhoodAndKeyCrimeDateAfter(neighbourhood, yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByNeighbourhoodLastYearIndex(neighbourhood, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByNeighbourhoodLastYearIndexRepository.deleteById(neighbourhood);
        crimesByNeighbourhoodLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String neighbourhood) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByNeighbourhoodRepository.findCrimeByKeyNeighbourhoodAndKeyCrimeDateAfter(neighbourhood, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByNeighbourhoodLast3MonthsIndex(neighbourhood, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByNeighbourhoodLast3MonthsIndexRepository.deleteById(neighbourhood);
        crimesByNeighbourhoodLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private CrimeLevelByNeighbourhood calculateAllTimeIndexAndCrimeLevel(String neighbourhood) {
        var lastYearCrimes = crimeByNeighbourhoodRepository.findCrimeByKeyNeighbourhood(neighbourhood);
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth);
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAllTimeIndex(neighbourhood, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByNeighbourhoodAllTimeIndexRepository.deleteById(neighbourhood);
        crimesByNeighbourhoodAllTimeIndexRepository.save(lastYearCrimesIndex);
        return calculateCrimeLevelByNeighbourhood(crimesByMonth, neighbourhood);
    }


    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth) {
        var highestCrimeLevel = highestCrimeLevelRepository.findAll().stream().findAny();
        if(highestCrimeLevel.isPresent() && !CollectionUtils.isEmpty(highestCrimeLevel.get().getHighestLevelForNeighbourhoodByMonth())) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForNeighbourhoodByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForNeighbourhoodByMonth().forEach((month, level) -> {
                if(crimesByMonth.containsKey(month) && crimesByMonth.get(month) > level) {
                    newHighestCrimeLevelForNeighbourhoodByMonth.put(month, crimesByMonth.get(month));
                } else {
                    newHighestCrimeLevelForNeighbourhoodByMonth.put(month, level);
                }
            });
            highestCrimeLevelValue.setHighestLevelForNeighbourhoodByMonth(newHighestCrimeLevelForNeighbourhoodByMonth);
            highestCrimeLevelRepository.save(highestCrimeLevelValue);
        } else {
            var newHighestCrimeLevel = new HighestCrimeLevel();
            newHighestCrimeLevel.setId(1L);
            newHighestCrimeLevel.setHighestLevelForNeighbourhoodByMonth(crimesByMonth);
            highestCrimeLevelRepository.save(newHighestCrimeLevel);
        }
    }

    private CrimeLevelByNeighbourhood calculateCrimeLevelByNeighbourhood(Map<LocalDate, Long> crimesByMonth, String neighbourhood) {
        return crimeLevelByNeighbourhoodRepository.save(new CrimeLevelByNeighbourhood(neighbourhood, crimesByMonth));
    }
}
