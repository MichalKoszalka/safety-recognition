package com.safety.recognition.calculator;

import com.safety.recognition.kafka.messages.StreetAndNeighbourhood;
import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByStreetAndCategoryRepository;
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
public class CrimesByStreetAndCategoryIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByStreetAndCategoryIndexesCalculator.class);

    private final CrimesByStreetAndCategoryAllTimeIndexRepository crimesByStreetAndCategoryAllTimeIndexRepository;
    private final CrimesByStreetAndCategoryLastYearIndexRepository crimesByStreetAndCategoryLastYearIndexRepository;
    private final CrimesByStreetAndCategoryLast3MonthsIndexRepository crimesByStreetAndCategoryLast3MonthsIndexRepository;
    private final CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository;
    private final HighestCrimeLevelByCategoryRepository highestCrimeLevelByCategoryRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;
    private final CrimeLevelByStreetAndCategoryRepository crimeLevelByStreetAndCategoryRepository;

    @Autowired
    public CrimesByStreetAndCategoryIndexesCalculator(CrimesByStreetAndCategoryAllTimeIndexRepository crimesByStreetAndCategoryAllTimeIndexRepository, CrimesByStreetAndCategoryLastYearIndexRepository crimesByStreetAndCategoryLastYearIndexRepository, CrimesByStreetAndCategoryLast3MonthsIndexRepository crimesByStreetAndCategoryLast3MonthsIndexRepository, CrimeByStreetAndCategoryRepository crimeByStreetAndCategoryRepository, HighestCrimeLevelByCategoryRepository highestCrimeLevelByCategoryRepository, LastUpdateDateRepository lastUpdateDateRepository, CrimeLevelByStreetAndCategoryRepository crimeLevelByStreetAndCategoryRepository) {
        this.crimesByStreetAndCategoryAllTimeIndexRepository = crimesByStreetAndCategoryAllTimeIndexRepository;
        this.crimesByStreetAndCategoryLastYearIndexRepository = crimesByStreetAndCategoryLastYearIndexRepository;
        this.crimesByStreetAndCategoryLast3MonthsIndexRepository = crimesByStreetAndCategoryLast3MonthsIndexRepository;
        this.crimeByStreetAndCategoryRepository = crimeByStreetAndCategoryRepository;
        this.highestCrimeLevelByCategoryRepository = highestCrimeLevelByCategoryRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.crimeLevelByStreetAndCategoryRepository = crimeLevelByStreetAndCategoryRepository;
    }

    public CrimeLevelByStreetAndCategory calculate(LocalDate lastUpdateDate, StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        calculateIndexForLastYear(lastUpdateDate, streetAndNeighbourhood, category);
        calculateIndexForLast3Months(lastUpdateDate, streetAndNeighbourhood, category);
        return calculateAllTimeIndexAndCrimeLevel(streetAndNeighbourhood, category);
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category, yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryLastYearIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetAndCategoryLastYearIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryLast3MonthsIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetAndCategoryLast3MonthsIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private CrimeLevelByStreetAndCategory calculateAllTimeIndexAndCrimeLevel(StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        var lastYearCrimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategory(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth, category);
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryAllTimeIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetAndCategoryAllTimeIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
        return calculateCrimeLevelByStreetAndCategory(crimesByMonth, category, streetAndNeighbourhood.getNeighbourhood(), streetAndNeighbourhood.getStreet());
    }

    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth, String category) {
        var highestCrimeLevel = highestCrimeLevelByCategoryRepository.findById(category);
        if (highestCrimeLevel.isPresent() && !CollectionUtils.isEmpty(highestCrimeLevel.get().getHighestLevelForStreetByMonth())) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForStreetByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForStreetByMonth().forEach((month, level) -> {
                if (crimesByMonth.containsKey(month) && crimesByMonth.get(month) > level) {
                    newHighestCrimeLevelForStreetByMonth.put(month, crimesByMonth.get(month));
                } else {
                    newHighestCrimeLevelForStreetByMonth.put(month, level);
                }
            });
            highestCrimeLevelValue.setHighestLevelForStreetByMonth(newHighestCrimeLevelForStreetByMonth);
            highestCrimeLevelByCategoryRepository.save(highestCrimeLevelValue);
        } else {
            var newHighestCrimeLevel = new HighestCrimeLevelByCategory();
            newHighestCrimeLevel.setCategory(category);
            newHighestCrimeLevel.setHighestLevelForStreetByMonth(crimesByMonth);
            highestCrimeLevelByCategoryRepository.save(newHighestCrimeLevel);
        }
    }

    private CrimeLevelByStreetAndCategory calculateCrimeLevelByStreetAndCategory(Map<LocalDate, Long> crimesByMonth, String category, String neighbourhood, String street) {
        var streetAndCategoryKey = new StreetAndCategoryKey(street, neighbourhood, category);
        return crimeLevelByStreetAndCategoryRepository.save(new CrimeLevelByStreetAndCategory(streetAndCategoryKey, crimesByMonth));

    }
}
