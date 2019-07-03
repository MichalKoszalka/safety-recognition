package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreetAndCategory;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByStreetAndCategoryRepository;
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
public class CrimeByStreetAndCategoryPredictionCalculator {

    @Value("${neural.network.model.path.crimeByStreetAndCategory}")
    private String crimeByStreetAndCategoryModelPath;

    private final CrimeLevelByStreetAndCategoryRepository crimeLevelByStreetAndCategoryRepository;
    private final Map<StreetKey, Long> streetsNormalised;
    private final Map<String, Long> categoriesNormalised;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeByStreetAndCategoryPredictionCalculator(CrimeLevelByStreetAndCategoryRepository crimeLevelByStreetAndCategoryRepository, StreetRepository streetRepository, PredictionNetwork predictionNetwork, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeLevelByStreetAndCategoryRepository = crimeLevelByStreetAndCategoryRepository;
        this.streetsNormalised = streetRepository.findAll().stream().collect(Collectors.toMap(Street::getKey, Street::getNumericRepresentation));
        this.categoriesNormalised = crimeCategoryRepository.findAll().stream().collect(Collectors.toMap(CrimeCategory::getName, CrimeCategory::getNumericRepresentation));
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevelsByStreetCategory = crimeLevelByStreetAndCategoryRepository.findAll();
        var testData = crimeLevelsByStreetCategory.stream().map(crimeLevelByStreetCategory -> parseSingleMonthForTest(nextMonth, categoriesNormalised.get(crimeLevelByStreetCategory.getKey().getCategory()), streetsNormalised.get(new StreetKey(crimeLevelByStreetCategory.getKey().getStreet(), crimeLevelByStreetCategory.getKey().getNeighbourhood())))).collect(Collectors.toList());
        predictionNetwork.predict(crimeByStreetAndCategoryModelPath, testData);
    }

    private List<List<Writable>>  parseCrimeData(List<CrimeLevelByStreetAndCategory> crimeLevelsByStreetCategory) {
        return crimeLevelsByStreetCategory.stream().map(crimeLevelByStreetAndCategory ->
                crimeLevelByStreetAndCategory.getCrimesByMonth().entrySet().stream()
                        .map(localDateLongEntry -> parseSingleMonthForTraining(localDateLongEntry, new LongWritable(categoriesNormalised.get(crimeLevelByStreetAndCategory.getKey().getCategory())), new LongWritable(streetsNormalised.get(new StreetKey(crimeLevelByStreetAndCategory.getKey().getStreet(), crimeLevelByStreetAndCategory.getKey().getNeighbourhood())))))).flatMap(listStream -> listStream).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthForTraining(Map.Entry<LocalDate, Long> crimesNumberForMonth, LongWritable categoryNormalised, LongWritable streetNormalised) {
        var writables =  new ArrayList<Writable>();
        writables.add(categoryNormalised);
        writables.add(streetNormalised);
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getYear()));
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getMonthValue()));
        return writables;
    }

    private List<Writable> parseSingleMonthForTest(LocalDate nextMonth, Long categoryNormalised, Long streetNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(categoryNormalised));
        writables.add(new LongWritable(streetNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }
}
