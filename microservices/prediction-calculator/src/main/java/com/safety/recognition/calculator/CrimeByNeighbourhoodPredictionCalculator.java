package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhood;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByNeighbourhoodRepository;
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
public class CrimeByNeighbourhoodPredictionCalculator {

    @Value("${neural.network.model.path.crimeByNeighbourhood}")
    private String crimeByNeighbourhoodModelPath;

    private final CrimeLevelByNeighbourhoodRepository crimeLevelByNeighbourhoodRepository;
    private final Map<String, Long> neighbourhoodsNormalised;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeByNeighbourhoodPredictionCalculator(CrimeLevelByNeighbourhoodRepository crimeLevelByNeighbourhoodRepository, NeighbourhoodRepository neighbourhoodRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelByNeighbourhoodRepository = crimeLevelByNeighbourhoodRepository;
        this.neighbourhoodsNormalised = neighbourhoodRepository.findAll().stream().collect(Collectors.toMap(Neighbourhood::getName, Neighbourhood::getNumericRepresentation));
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevelsByNeighbourhoodCategory = crimeLevelByNeighbourhoodRepository.findAll();
        var trainData = parseCrimeData(crimeLevelsByNeighbourhoodCategory);
        var testData = crimeLevelsByNeighbourhoodCategory.stream().map(crimeLevelByNeighbourhoodCategory -> parseSingleMonthForTest(nextMonth, neighbourhoodsNormalised.get(crimeLevelByNeighbourhoodCategory.getNeighbourhood()))).collect(Collectors.toList());
        predictionNetwork.predict(trainData, crimeByNeighbourhoodModelPath, testData);
    }

    private List<List<Writable>>  parseCrimeData(List<CrimeLevelByNeighbourhood> crimeLevelsByNeighbourhoodCategory) {
        return crimeLevelsByNeighbourhoodCategory.stream().map(crimeLevelByNeighbourhoodAndCategory ->
                crimeLevelByNeighbourhoodAndCategory.getCrimesByMonth().entrySet().stream()
                        .map(localDateLongEntry -> parseSingleMonthForTraining(localDateLongEntry, new LongWritable(neighbourhoodsNormalised.get(crimeLevelByNeighbourhoodAndCategory.getNeighbourhood()))))).flatMap(listStream -> listStream).collect(Collectors.toList());
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
