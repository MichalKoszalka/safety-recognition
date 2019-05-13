package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodAllTimeIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodLast3MonthsIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesByNeighbourhoodLastYearIndex;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByNeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodAllTimeIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodLast3MonthsIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodLastYearIndexRepository;
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
public class CrimesByNeighbourhoodIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByNeighbourhoodIndexesCalculator.class);

    private final CrimesByNeighbourhoodAllTimeIndexRepository crimesByNeighbourhoodAllTimeIndexRepository;
    private final CrimesByNeighbourhoodLastYearIndexRepository crimesByNeighbourhoodLastYearIndexRepository;
    private final CrimesByNeighbourhoodLast3MonthsIndexRepository crimesByNeighbourhoodLast3MonthsIndexRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;
    private final CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository;

    @Autowired
    public CrimesByNeighbourhoodIndexesCalculator(CrimesByNeighbourhoodAllTimeIndexRepository crimesByNeighbourhoodAllTimeIndexRepository, CrimesByNeighbourhoodLastYearIndexRepository crimesByNeighbourhoodLastYearIndexRepository, CrimesByNeighbourhoodLast3MonthsIndexRepository crimesByNeighbourhoodLast3MonthsIndexRepository, LastUpdateDateRepository lastUpdateDateRepository, CrimeByNeighbourhoodRepository crimeByNeighbourhoodRepository) {
        this.crimesByNeighbourhoodAllTimeIndexRepository = crimesByNeighbourhoodAllTimeIndexRepository;
        this.crimesByNeighbourhoodLastYearIndexRepository = crimesByNeighbourhoodLastYearIndexRepository;
        this.crimesByNeighbourhoodLast3MonthsIndexRepository = crimesByNeighbourhoodLast3MonthsIndexRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
        this.crimeByNeighbourhoodRepository = crimeByNeighbourhoodRepository;
    }

    public void calculate(String neighbourhood) {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if(lastUpdateDate.isPresent()) {
            LOG.info(String.format("Calculating indexes for neighbourhood %s.", neighbourhood));
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate(), neighbourhood);
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate(), neighbourhood);
            calculateAllTimeIndex(neighbourhood);
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String neighbourhood) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByNeighbourhoodRepository.findCrimeByKeyNeighbourhoodAndKeyCrimeDateAfter(neighbourhood, yearBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByNeighbourhoodLastYearIndex(neighbourhood, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByNeighbourhoodLastYearIndexRepository.deleteById(neighbourhood);
        crimesByNeighbourhoodLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String neighbourhood) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByNeighbourhoodRepository.findCrimeByKeyNeighbourhoodAndKeyCrimeDateAfter(neighbourhood, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByNeighbourhoodLast3MonthsIndex(neighbourhood, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByNeighbourhoodLast3MonthsIndexRepository.deleteById(neighbourhood);
        crimesByNeighbourhoodLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex(String neighbourhood) {
        var lastYearCrimes = crimeByNeighbourhoodRepository.findCrimeByKeyNeighbourhood(neighbourhood);
        var numberOfCrimes = lastYearCrimes.size();
        var entriesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByNeighbourhoodAllTimeIndex(neighbourhood, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByNeighbourhoodAllTimeIndexRepository.deleteById(neighbourhood);
        crimesByNeighbourhoodAllTimeIndexRepository.save(lastYearCrimesIndex);
    }
}
