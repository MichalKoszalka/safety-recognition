package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByCategory;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByCategoryRepository;
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
public class CrimeForLondonByCategoryPredictionCalculator {

    @Value("${neural.network.model.path.crimeForLondonByCategory}")
    private String crimeByCategoryModelPath;

    private final CrimeLevelByCategoryRepository crimeLevelByCategoryRepository;
    private final Map<String, Long> categoriesNormalised;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeForLondonByCategoryPredictionCalculator(CrimeLevelByCategoryRepository crimeLevelByCategoryRepository, CrimeCategoryRepository crimeCategoryRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelByCategoryRepository = crimeLevelByCategoryRepository;
        this.categoriesNormalised = crimeCategoryRepository.findAll().stream().collect(Collectors.toMap(CrimeCategory::getName, CrimeCategory::getNumericRepresentation));
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var testData = categoriesNormalised.values().stream().map(categoryNormalised -> parseSingleMonthForTest(nextMonth, categoryNormalised)).collect(Collectors.toList());
        predictionNetwork.predict(crimeByCategoryModelPath, testData);
    }

    private List<List<Writable>> parseCrimeData(List<CrimeLevelByCategory> crimeLevelsByCategory) {
        return crimeLevelsByCategory.stream().map(crimeLevelByCategory ->
        crimeLevelByCategory.getCrimesByMonth().entrySet().stream()
                .map(localDateLongEntry -> parseSingleMonthForTraining(localDateLongEntry, new LongWritable(categoriesNormalised.get(crimeLevelByCategory.getCategory()))))).flatMap(listStream -> listStream).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthForTraining(Map.Entry<LocalDate, Long> crimesNumberForMonth, LongWritable categoryNormalised) {
        var writables =  new ArrayList<Writable>();
        writables.add(categoryNormalised);
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getYear()));
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getMonthValue()));
        return writables;
    }

    private List<Writable> parseSingleMonthForTest(LocalDate nextMonth, Long categoryNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(categoryNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
