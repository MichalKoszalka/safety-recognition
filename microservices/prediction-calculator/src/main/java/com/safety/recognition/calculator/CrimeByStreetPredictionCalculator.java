package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhood;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreet;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByNeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByStreetRepository;
import com.safety.recognition.deeplearning.PredictionNetwork;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.LongWritable;
import org.datavec.api.writable.Writable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrimeByStreetPredictionCalculator {

    @Value("${neural.network.model.path.crimeByStreet}")
    private String crimeByStreetModelPath;

    private final CrimeLevelByStreetRepository crimeLevelByStreetRepository;
    private final Map<StreetKey, Long> streetsNormalised;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeByStreetPredictionCalculator(CrimeLevelByStreetRepository crimeLevelByStreetRepository, StreetRepository streetRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelByStreetRepository = crimeLevelByStreetRepository;
        this.streetsNormalised = streetRepository.findAll().stream().collect(Collectors.toMap(street1 -> street1.getKey(), Street::getNumericRepresentation));
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevelsByStreet = crimeLevelByStreetRepository.findAll();
        var trainData = parseCrimeData(crimeLevelsByStreet);
        var testData = crimeLevelsByStreet.stream().map(crimeLevelByStreet -> parseSingleMonthForTest(nextMonth, streetsNormalised.get(new StreetKey(crimeLevelByStreet.getKey().getStreet(), crimeLevelByStreet.getKey().getNeighbourhood())))).collect(Collectors.toList());
        predictionNetwork.predict(trainData, crimeByStreetModelPath, testData);
    }

    private List<List<Writable>>  parseCrimeData(List<CrimeLevelByStreet> crimeLevelsByStreetCategory) {
        return crimeLevelsByStreetCategory.stream().map(crimeLevelByStreetAndCategory ->
                crimeLevelByStreetAndCategory.getCrimesByMonth().entrySet().stream()
                        .map(localDateLongEntry -> parseSingleMonthForTraining(localDateLongEntry, new LongWritable(streetsNormalised.get(new StreetKey(crimeLevelByStreetAndCategory.getKey().getStreet(), crimeLevelByStreetAndCategory.getKey().getNeighbourhood())))))).flatMap(listStream -> listStream).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthForTraining(Map.Entry<LocalDate, Long> crimesNumberForMonth, LongWritable neighbourhoodNormalised) {
        var writables =  new ArrayList<Writable>();
        writables.add(neighbourhoodNormalised);
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getYear()));
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getMonthValue()));
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
        return writables;
    }

    private List<Writable> parseSingleMonthForTest(LocalDate nextMonth, Long neighbourhoodNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(neighbourhoodNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
