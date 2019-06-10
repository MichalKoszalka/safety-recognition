package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreetAndCategory;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByNeighbourhoodAndCategoryRepository;
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
public class CrimeByNeighbourhoodAndCategoryPredictionCalculator {

    @Value("${neural.network.model.path.crimeByNeighbourhoodAndCategory}")
    private String crimeByNeighbourhoodAndCategoryModelPath;

    private final CrimeLevelByNeighbourhoodAndCategoryRepository crimeLevelByNeighbourhoodAndCategoryRepository;
    private final Map<String, Long> neighbourhoodsNormalised;
    private final Map<String, Long> categoriesNormalised;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeByNeighbourhoodAndCategoryPredictionCalculator(CrimeLevelByNeighbourhoodAndCategoryRepository crimeLevelByNeighbourhoodAndCategoryRepository, NeighbourhoodRepository neighbourhoodRepository, PredictionNetwork predictionNetwork, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeLevelByNeighbourhoodAndCategoryRepository = crimeLevelByNeighbourhoodAndCategoryRepository;
        this.neighbourhoodsNormalised = neighbourhoodRepository.findAll().stream().collect(Collectors.toMap(Neighbourhood::getName, Neighbourhood::getNumericRepresentation));
        this.categoriesNormalised = crimeCategoryRepository.findAll().stream().collect(Collectors.toMap(CrimeCategory::getName, CrimeCategory::getNumericRepresentation));
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevelsByNeighbourhoodCategory = crimeLevelByNeighbourhoodAndCategoryRepository.findAll();
        var trainData = parseCrimeData(crimeLevelsByNeighbourhoodCategory);
        var testData = crimeLevelsByNeighbourhoodCategory.stream().map(crimeLevelByNeighbourhoodCategory -> parseSingleMonthForTest(nextMonth, categoriesNormalised.get(crimeLevelByNeighbourhoodCategory.getKey().getCategory()), neighbourhoodsNormalised.get(crimeLevelByNeighbourhoodCategory.getKey().getNeighbourhood()))).collect(Collectors.toList());
        predictionNetwork.predict(trainData, crimeByNeighbourhoodAndCategoryModelPath, testData);
    }

    private List<List<Writable>>  parseCrimeData(List<CrimeLevelByNeighbourhoodAndCategory> crimeLevelsByNeighbourhoodCategory) {
        return crimeLevelsByNeighbourhoodCategory.stream().map(crimeLevelByNeighbourhoodAndCategory ->
                crimeLevelByNeighbourhoodAndCategory.getCrimesByMonth().entrySet().stream()
                        .map(localDateLongEntry -> parseSingleMonthForTraining(localDateLongEntry, new LongWritable(categoriesNormalised.get(crimeLevelByNeighbourhoodAndCategory.getKey().getCategory())), new LongWritable(neighbourhoodsNormalised.get(crimeLevelByNeighbourhoodAndCategory.getKey().getNeighbourhood()))))).flatMap(listStream -> listStream).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthForTraining(Map.Entry<LocalDate, Long> crimesNumberForMonth, LongWritable categoryNormalised, LongWritable neighbourhoodNormalised) {
        var writables =  new ArrayList<Writable>();
        writables.add(categoryNormalised);
        writables.add(neighbourhoodNormalised);
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getYear()));
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getMonthValue()));
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
        return writables;
    }

    private List<Writable> parseSingleMonthForTest(LocalDate nextMonth, Long categoryNormalised, Long neighbourhoodNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(categoryNormalised));
        writables.add(new LongWritable(neighbourhoodNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
