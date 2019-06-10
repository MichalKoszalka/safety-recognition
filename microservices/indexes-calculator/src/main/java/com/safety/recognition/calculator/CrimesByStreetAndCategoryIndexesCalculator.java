package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.kafka.messages.StreetAndNeighbourhood;
import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByStreetAndCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public void calculate(StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if(lastUpdateDate.isPresent()) {
            LOG.info(String.format("Calculating indexes for street %s in neighbourhood %s and category %s", streetAndNeighbourhood, category));
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate(), streetAndNeighbourhood, category);
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate(), streetAndNeighbourhood, category);
            calculateAllTimeIndexAndCrimeLevel(streetAndNeighbourhood, category);
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategoryAndKeyCrimeDateAfter(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category, yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
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
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryLast3MonthsIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetAndCategoryLast3MonthsIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndexAndCrimeLevel(StreetAndNeighbourhood streetAndNeighbourhood, String category) {
        var lastYearCrimes = crimeByStreetAndCategoryRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCategory(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth, category);
        calculateCrimeLevelByStreetAndCategory(crimesByMonth, category, streetAndNeighbourhood.getNeighbourhood(), streetAndNeighbourhood.getStreet());
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var indexKey = new CrimesByStreetAndCategoryIndexKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), category);
        var lastYearCrimesIndex = new CrimesByStreetAndCategoryAllTimeIndex(indexKey, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetAndCategoryAllTimeIndexRepository.deleteById(indexKey);
        crimesByStreetAndCategoryAllTimeIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth, String category) {
        var highestCrimeLevel = highestCrimeLevelByCategoryRepository.findById(category);
        if(highestCrimeLevel.isPresent()) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForStreetByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForStreetByMonth().forEach((moth, level) -> {
                if(crimesByMonth.containsKey(moth) && crimesByMonth.get(moth) > level) {
                    newHighestCrimeLevelForStreetByMonth.put(moth, crimesByMonth.get(moth));
                } else {
                    newHighestCrimeLevelForStreetByMonth.put(moth, level);
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

    private void calculateCrimeLevelByStreetAndCategory(Map<LocalDate, Long> crimesByMonth, String category, String neighbourhood, String street) {
        var streetAndCategoryKey = new StreetAndCategoryKey(street, neighbourhood, category);
        crimeLevelByStreetAndCategoryRepository.save(new CrimeLevelByStreetAndCategory(streetAndCategoryKey, crimesByMonth));

    }
}
