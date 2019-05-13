package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.*;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeByStreetRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByStreetAllTimeIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByStreetLast3MonthsIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesByStreetLastYearIndexRepository;
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
public class CrimesByStreetIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesByStreetIndexesCalculator.class);

    private final CrimesByStreetAllTimeIndexRepository crimesByStreetAllTimeIndexRepository;
    private final CrimesByStreetLastYearIndexRepository crimesByStreetLastYearIndexRepository;
    private final CrimesByStreetLast3MonthsIndexRepository crimesByStreetLast3MonthsIndexRepository;
    private final CrimeByStreetRepository crimeByStreetRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;

    @Autowired
    public CrimesByStreetIndexesCalculator(CrimesByStreetAllTimeIndexRepository crimesByStreetAllTimeIndexRepository, CrimesByStreetLastYearIndexRepository crimesByStreetLastYearIndexRepository, CrimesByStreetLast3MonthsIndexRepository crimesByStreetLast3MonthsIndexRepository, CrimeByStreetRepository crimeByStreetRepository, LastUpdateDateRepository lastUpdateDateRepository) {
        this.crimesByStreetAllTimeIndexRepository = crimesByStreetAllTimeIndexRepository;
        this.crimesByStreetLastYearIndexRepository = crimesByStreetLastYearIndexRepository;
        this.crimesByStreetLast3MonthsIndexRepository = crimesByStreetLast3MonthsIndexRepository;
        this.crimeByStreetRepository = crimeByStreetRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }


    public void calculate(String street) {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if (lastUpdateDate.isPresent()) {
            LOG.info(String.format("Calculating indexes for street %s.", street));
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate(), street);
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate(), street);
            calculateAllTimeIndex(street);
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate, String street) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeByStreetRepository.findCrimeByKeyStreetAndKeyCrimeDateAfter(street, yearBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByStreetLastYearIndex(street, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByStreetLastYearIndexRepository.deleteById(street);
        crimesByStreetLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate, String street) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeByStreetRepository.findCrimeByKeyStreetAndKeyCrimeDateAfter(street, threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesByStreetLast3MonthsIndex(street, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByStreetLast3MonthsIndexRepository.deleteById(street);
        crimesByStreetLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex(String street) {
        var lastYearCrimes = crimeByStreetRepository.findCrimeByKeyStreet(street);
        var numberOfCrimes = lastYearCrimes.size();
        var entriesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size() - 1) / 2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
       var lastYearCrimesIndex = new CrimesByStreetAllTimeIndex(street, numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesByStreetAllTimeIndexRepository.deleteById(street);
        crimesByStreetAllTimeIndexRepository.save(lastYearCrimesIndex);
    }
}
