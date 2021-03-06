package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByNeighbourhoodAndCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.*;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionByNeighbourhoodAndCategoryRepository;
import io.micrometer.core.instrument.MeterRegistry;
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
public class CrimesByNeighbourhoodAndCategoryIndexesCalculator {

    private final MeterRegistry meterRegistry;
    private final CrimesByNeighbourhoodAndCategoryAllTimeIndexRepository crimesByNeighbourhoodAndCategoryAllTimeIndexRepository;
    private final CrimesByNeighbourhoodAndCategoryLastYearIndexRepository crimesByNeighbourhoodAndCategoryLastYearIndexRepository;
    private final CrimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository;
    private final CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository;
    private final HighestCrimeLevelByCategoryRepository highestCrimeLevelByCategoryRepository;
    private final CrimeLevelByNeighbourhoodAndCategoryRepository crimeLevelByNeighbourhoodAndCategoryRepository;
    private final CrimePredictionByNeighbourhoodAndCategoryRepository crimePredictionByNeighbourhoodAndCategoryRepository;

    @Autowired
    public CrimesByNeighbourhoodAndCategoryIndexesCalculator(MeterRegistry meterRegistry, CrimesByNeighbourhoodAndCategoryAllTimeIndexRepository crimesByNeighbourhoodAndCategoryAllTimeIndexRepository, CrimesByNeighbourhoodAndCategoryLastYearIndexRepository crimesByNeighbourhoodAndCategoryLastYearIndexRepository, CrimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository, CrimeByNeighbourhoodAndCategoryRepository crimeByNeighbourhoodAndCategoryRepository, HighestCrimeLevelByCategoryRepository highestCrimeLevelByCategoryRepository, LastUpdateDateRepository lastUpdateDateRepository, CrimeLevelByNeighbourhoodAndCategoryRepository crimeLevelByNeighbourhoodAndCategoryRepository, CrimePredictionByNeighbourhoodAndCategoryRepository crimePredictionByNeighbourhoodAndCategoryRepository) {
        this.meterRegistry = meterRegistry;
        this.crimesByNeighbourhoodAndCategoryAllTimeIndexRepository = crimesByNeighbourhoodAndCategoryAllTimeIndexRepository;
        this.crimesByNeighbourhoodAndCategoryLastYearIndexRepository = crimesByNeighbourhoodAndCategoryLastYearIndexRepository;
        this.crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository = crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository;
        this.crimeByNeighbourhoodAndCategoryRepository = crimeByNeighbourhoodAndCategoryRepository;
        this.highestCrimeLevelByCategoryRepository = highestCrimeLevelByCategoryRepository;
        this.crimeLevelByNeighbourhoodAndCategoryRepository = crimeLevelByNeighbourhoodAndCategoryRepository;
        this.crimePredictionByNeighbourhoodAndCategoryRepository = crimePredictionByNeighbourhoodAndCategoryRepository;
    }

    public CrimeLevelByNeighbourhoodAndCategory calculate(LocalDate lastUpdatedMonth, String neighbourhood, String category) {
        calculateIndexForLastYear(lastUpdatedMonth, neighbourhood, category);
        calculateIndexForLast3Months(lastUpdatedMonth, neighbourhood, category);
        return calculateAllTimeIndexAndCrimeLevel(neighbourhood, category);
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String neighbourhood, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByNeighbourhoodAndCategoryRepository.findCrimeByKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(neighbourhood, category, yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.size() > 0 ? entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue() : 0;
        var meanByMonth = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByNeighbourhoodAndCategoryIndexKey(neighbourhood, category);
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAndCategoryLastYearIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByNeighbourhoodAndCategoryLastYearIndexRepository.deleteById(indexKey);
        crimesByNeighbourhoodAndCategoryLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String neighbourhood, String category) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByNeighbourhoodAndCategoryRepository.findCrimeByKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(neighbourhood, category, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.size() > 0 ? entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue() : 0;
        var meanByMonth = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByNeighbourhoodAndCategoryIndexKey(neighbourhood, category);
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAndCategoryLast3MonthsIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository.deleteById(indexKey);
        crimesByNeighbourhoodAndCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private CrimeLevelByNeighbourhoodAndCategory calculateAllTimeIndexAndCrimeLevel(String neighbourhood, String category) {
        var lastYearCrimes = crimeByNeighbourhoodAndCategoryRepository.findCrimeByKeyNeighbourhoodAndKeyCategory(neighbourhood, category);
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth, category);
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.size() > 0 ? entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue() : 0;
        var meanByMonth = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = entriesByMonth.size() == 0 ? 0 : Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var indexKey = new CrimesByNeighbourhoodAndCategoryIndexKey(neighbourhood, category);
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAndCategoryAllTimeIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByNeighbourhoodAndCategoryAllTimeIndexRepository.deleteById(indexKey);
        crimesByNeighbourhoodAndCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
        return calculateCrimeLevelByNeighbourhoodAndCategory(crimesByMonth, category, neighbourhood);
    }

    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth, String category) {
        var highestCrimeLevel = highestCrimeLevelByCategoryRepository.findById(category);
        if (highestCrimeLevel.isPresent() && !CollectionUtils.isEmpty(highestCrimeLevel.get().getHighestLevelForNeighbourhoodByMonth())) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForNeighbourhoodByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForNeighbourhoodByMonth().forEach((month, level) -> {
                if (crimesByMonth.containsKey(month) && crimesByMonth.get(month) > level) {
                    newHighestCrimeLevelForNeighbourhoodByMonth.put(month, crimesByMonth.get(month));
                } else {
                    newHighestCrimeLevelForNeighbourhoodByMonth.put(month, level);
                }
            });
            highestCrimeLevelValue.setHighestLevelForNeighbourhoodByMonth(newHighestCrimeLevelForNeighbourhoodByMonth);
            highestCrimeLevelByCategoryRepository.save(highestCrimeLevelValue);
        } else {
            var newHighestCrimeLevel = new HighestCrimeLevelByCategory();
            newHighestCrimeLevel.setCategory(category);
            newHighestCrimeLevel.setHighestLevelForNeighbourhoodByMonth(crimesByMonth);
            highestCrimeLevelByCategoryRepository.save(newHighestCrimeLevel);
        }
    }

    private CrimeLevelByNeighbourhoodAndCategory calculateCrimeLevelByNeighbourhoodAndCategory(Map<LocalDate, Long> crimesByMonth, String category, String neighbourhood) {
        var neighbourhoodAndCategoryKey = new NeighbourhoodAndCategoryKey(neighbourhood, category);
        var crimeLevel = crimeLevelByNeighbourhoodAndCategoryRepository.save(new CrimeLevelByNeighbourhoodAndCategory(neighbourhoodAndCategoryKey, crimesByMonth));
        updateMetrics(crimeLevel);
        return crimeLevel;
    }

    private void updateMetrics(CrimeLevelByNeighbourhoodAndCategory crimeLevelByNeighbourhoodAndCategory) {
        var predictions = crimePredictionByNeighbourhoodAndCategoryRepository.findById(crimeLevelByNeighbourhoodAndCategory.getKey());
        predictions.ifPresent(crimePredictionByNeighbourhoodAndCategory -> crimePredictionByNeighbourhoodAndCategory.getCrimesByMonth().forEach((key, value) -> meterRegistry.gauge("prediction.deviation", crimeLevelByNeighbourhoodAndCategory.getCrimesByMonth().getOrDefault(key, 0L) - value)));


    }
}
