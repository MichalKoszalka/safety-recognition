package com.safety.recognition.calculator;

import com.safety.recognition.kafka.messages.StreetAndNeighbourhood;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByStreetRepository;
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
public class CrimesByStreetIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByStreetIndexesCalculator.class);

    private final CrimesByStreetAllTimeIndexRepository crimesByStreetAllTimeIndexRepository;
    private final CrimesByStreetLastYearIndexRepository crimesByStreetLastYearIndexRepository;
    private final CrimesByStreetLast3MonthsIndexRepository crimesByStreetLast3MonthsIndexRepository;
    private final CrimeByStreetRepository crimeByStreetRepository;
    private final HighestCrimeLevelRepository highestCrimeLevelRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;
    private final CrimeLevelByStreetRepository crimeLevelByStreetRepository;

    @Autowired
    public CrimesByStreetIndexesCalculator(CrimesByStreetAllTimeIndexRepository crimesByStreetAllTimeIndexRepository, CrimesByStreetLastYearIndexRepository crimesByStreetLastYearIndexRepository, CrimesByStreetLast3MonthsIndexRepository crimesByStreetLast3MonthsIndexRepository, CrimeByStreetRepository crimeByStreetRepository, HighestCrimeLevelRepository highestCrimeLevelRepository, LastUpdateDateRepository lastUpdateDateRepository, CrimeLevelByStreetRepository crimeLevelByStreetRepository) {
        this.crimesByStreetAllTimeIndexRepository = crimesByStreetAllTimeIndexRepository;
        this.crimesByStreetLastYearIndexRepository = crimesByStreetLastYearIndexRepository;
        this.crimesByStreetLast3MonthsIndexRepository = crimesByStreetLast3MonthsIndexRepository;
        this.crimeByStreetRepository = crimeByStreetRepository;
        this.highestCrimeLevelRepository = highestCrimeLevelRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.crimeLevelByStreetRepository = crimeLevelByStreetRepository;
    }


    public CrimeLevelByStreet calculate(LocalDate lastUpdateDate, StreetAndNeighbourhood streetAndNeighbourhood) {
            calculateIndexForLastYear(lastUpdateDate, streetAndNeighbourhood);
            calculateIndexForLast3Months(lastUpdateDate, streetAndNeighbourhood);
            return calculateAllTimeIndexAndCrimeLevel(streetAndNeighbourhood);
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, StreetAndNeighbourhood streetAndNeighbourhood) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByStreetRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCrimeDateAfter(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), yearBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByStreetLastYearIndex(new StreetKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()), numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetLastYearIndexRepository.deleteById(new StreetKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
        crimesByStreetLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, StreetAndNeighbourhood streetAndNeighbourhood) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByStreetRepository.findCrimeByKeyStreetAndKeyNeighbourhoodAndKeyCrimeDateAfter(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood(), threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var crimesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByStreetLast3MonthsIndex(new StreetKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()), numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetLast3MonthsIndexRepository.deleteById(new StreetKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
        crimesByStreetLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private CrimeLevelByStreet calculateAllTimeIndexAndCrimeLevel(StreetAndNeighbourhood streetAndNeighbourhood) {
        var lastYearCrimes = crimeByStreetRepository.findCrimeByKeyStreetAndKeyNeighbourhood(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood());
        var numberOfCrimes = lastYearCrimes.size();
        var crimesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()));
        calculateHighestCrimeLevel(crimesByMonth);
        var entriesByMonth = crimesByMonth.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByStreetAllTimeIndex(new StreetKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()), numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay, crimesByMonth);
        crimesByStreetAllTimeIndexRepository.deleteById(new StreetKey(streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood()));
        crimesByStreetAllTimeIndexRepository.save(lastYearCrimesIndex);
        return calculateCrimeLevelByStreet(crimesByMonth, streetAndNeighbourhood.getStreet(), streetAndNeighbourhood.getNeighbourhood());
    }

    private void calculateHighestCrimeLevel(Map<LocalDate, Long> crimesByMonth) {
        var highestCrimeLevel = highestCrimeLevelRepository.findAll().stream().findAny();
        if(highestCrimeLevel.isPresent() && !CollectionUtils.isEmpty(highestCrimeLevel.get().getHighestLevelForStreetByMonth())) {
            var highestCrimeLevelValue = highestCrimeLevel.get();
            Map<LocalDate, Long> newHighestCrimeLevelForStreetByMonth = new HashMap<>();
            highestCrimeLevelValue.getHighestLevelForStreetByMonth().forEach((month, level) -> {
                if(crimesByMonth.containsKey(month) && crimesByMonth.get(month) > level) {
                    newHighestCrimeLevelForStreetByMonth.put(month, crimesByMonth.get(month));
                } else {
                    newHighestCrimeLevelForStreetByMonth.put(month, level);
                }
            });
            highestCrimeLevelValue.setHighestLevelForStreetByMonth(newHighestCrimeLevelForStreetByMonth);
            highestCrimeLevelRepository.save(highestCrimeLevelValue);
        } else {
            var newHighestCrimeLevel = new HighestCrimeLevel();
            newHighestCrimeLevel.setId(1L);
            newHighestCrimeLevel.setHighestLevelForStreetByMonth(crimesByMonth);
            highestCrimeLevelRepository.save(newHighestCrimeLevel);
        }
    }

    private CrimeLevelByStreet calculateCrimeLevelByStreet(Map<LocalDate, Long> crimesByMonth, String street, String neighbourhood) {
        return crimeLevelByStreetRepository.save(new CrimeLevelByStreet(new StreetKey(street, neighbourhood), crimesByMonth));
    }
}
