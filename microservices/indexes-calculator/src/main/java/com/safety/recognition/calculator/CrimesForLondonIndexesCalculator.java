package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.CrimesForLondonAllTimeIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesForLondonLast3MonthsIndex;
import com.safety.recognition.cassandra.model.indexes.CrimesForLondonLastYearIndex;
import com.safety.recognition.cassandra.repository.LastUpdateDateRepository;
import com.safety.recognition.cassandra.repository.crime.CrimeRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesForLondonAllTimeIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesForLondonLast3MonthsIndexRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimesForLondonLastYearIndexRepository;
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
public class CrimesForLondonIndexesCalculator {

    private static final Logger LOG = LoggerFactory.getLogger(CrimesForLondonIndexesCalculator.class);

    private final CrimesForLondonAllTimeIndexRepository crimesForLondonAllTimeIndexRepository;
    private final CrimesForLondonLastYearIndexRepository crimesForLondonLastYearIndexRepository;
    private final CrimesForLondonLast3MonthsIndexRepository crimesForLondonLast3MonthsIndexRepository;
    private final CrimeRepository crimeRepository;
    private final LastUpdateDateRepository lastUpdateDateRepository;

    @Autowired
    public CrimesForLondonIndexesCalculator(CrimesForLondonAllTimeIndexRepository crimesForLondonAllTimeIndexRepository, CrimesForLondonLastYearIndexRepository crimesForLondonLastYearIndexRepository, CrimesForLondonLast3MonthsIndexRepository crimesForLondonLast3MonthsIndexRepository, CrimeRepository crimeRepository, LastUpdateDateRepository lastUpdateDateRepository) {
        this.crimesForLondonAllTimeIndexRepository = crimesForLondonAllTimeIndexRepository;
        this.crimesForLondonLastYearIndexRepository = crimesForLondonLastYearIndexRepository;
        this.crimesForLondonLast3MonthsIndexRepository = crimesForLondonLast3MonthsIndexRepository;
        this.crimeRepository = crimeRepository;
        this.lastUpdateDateRepository = lastUpdateDateRepository;
    }

    public void calculate() {
        var lastUpdateDate = lastUpdateDateRepository.findAll().stream().findFirst();
        if(lastUpdateDate.isPresent()) {
            calculateIndexForLastYear(lastUpdateDate.get().getPoliceApiLastUpdate());
            calculateIndexForLast3Months(lastUpdateDate.get().getPoliceApiLastUpdate());
            calculateAllTimeIndex();
        } else {
            LOG.info("No data for calculation, skipping this time.");
        }
    }

    private void calculateIndexForLastYear(LocalDate policeApiLastUpdate) {
        var yearBefore = policeApiLastUpdate.minusYears(1);
        var crimes = crimeRepository.findCrimesByKeyCrimeDateAfter(yearBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(yearBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(yearBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(yearBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonLastYearIndex("London", numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesForLondonLastYearIndexRepository.deleteById(lastYearCrimesIndex.getCity());
        crimesForLondonLastYearIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateIndexForLast3Months(LocalDate policeApiLastUpdate) {
        var threeMonthsBefore = policeApiLastUpdate.minusMonths(3);
        var crimes = crimeRepository.findCrimesByKeyCrimeDateAfter(threeMonthsBefore);
        var numberOfCrimes = crimes.size();
        var entriesByMonth = crimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(threeMonthsBefore, LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(threeMonthsBefore, LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonLast3MonthsIndex("London", numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesForLondonLast3MonthsIndexRepository.deleteById(lastYearCrimesIndex.getCity());
        crimesForLondonLast3MonthsIndexRepository.save(lastYearCrimesIndex);
    }

    private void calculateAllTimeIndex() {
        var lastYearCrimes = crimeRepository.findAll();
        var numberOfCrimes = lastYearCrimes.size();
        var entriesByMonth = lastYearCrimes.stream().collect(Collectors.groupingBy(crime -> crime.getKey().getCrimeDate(), Collectors.counting()))
                .entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).collect(Collectors.toList());
        var medianByMonth = entriesByMonth.get((entriesByMonth.size()-1)/2).getValue().intValue();
        var meanByMonth = Long.valueOf(numberOfCrimes / (ChronoUnit.MONTHS.between(entriesByMonth.get(0).getKey(), LocalDate.now()))).intValue();
        var meanByWeek = Long.valueOf(numberOfCrimes / ChronoUnit.WEEKS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var meanByDay = Long.valueOf(numberOfCrimes / ChronoUnit.DAYS.between(entriesByMonth.get(0).getKey(), LocalDate.now())).intValue();
        var lastYearCrimesIndex = new CrimesForLondonAllTimeIndex("London", numberOfCrimes, medianByMonth, meanByMonth, meanByWeek, meanByDay);
        crimesForLondonAllTimeIndexRepository.deleteById(lastYearCrimesIndex.getCity());
        crimesForLondonAllTimeIndexRepository.save(lastYearCrimesIndex);
    }


}
