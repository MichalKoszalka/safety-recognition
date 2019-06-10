package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.CrimeLevel;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelRepository;
import com.safety.recognition.deeplearning.PredictionNetwork;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.LongWritable;
import org.datavec.api.writable.Writable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrimeForLondonPredictionCalculator {

    @Value("${neural.network.model.path.crimeForLondon}")
    private String crimeForLondonModelPath;

    private final CrimeLevelRepository crimeLevelRepository;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeForLondonPredictionCalculator(CrimeLevelRepository crimeLevelRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelRepository = crimeLevelRepository;
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevelsByCategory = crimeLevelRepository.findAll();
        var trainData = parseCrimeData(crimeLevelsByCategory);
        var testData = Collections.singletonList(parseSingleMonthForTest(nextMonth));
        predictionNetwork.predict(trainData, crimeForLondonModelPath, testData);
    }

    private List<List<Writable>> parseCrimeData(List<CrimeLevel> crimeLevels) {
        return crimeLevels.stream().map(crimeLevel ->
                crimeLevel.getCrimesByMonth().entrySet().stream()
                        .map(this::parseSingleMonthForTraining)).flatMap(listStream -> listStream).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthForTraining(Map.Entry<LocalDate, Long> crimesNumberForMonth) {
        var writables =  new ArrayList<Writable>();
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getYear()));
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getMonthValue()));
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
        return writables;
    }

    private List<Writable> parseSingleMonthForTest(LocalDate nextMonth) {
        var writables = new ArrayList<Writable>();
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
