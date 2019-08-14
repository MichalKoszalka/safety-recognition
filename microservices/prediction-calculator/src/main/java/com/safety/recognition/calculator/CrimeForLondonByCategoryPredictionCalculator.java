package com.safety.recognition.calculator;

import com.codepoetics.protonpack.maps.MapStream;
import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByCategory;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByCategory;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByCategoryRepository;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionByCategoryRepository;
import com.safety.recognition.deeplearning.PredictionNetwork;
import org.datavec.api.writable.IntWritable;
import org.datavec.api.writable.LongWritable;
import org.datavec.api.writable.Writable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrimeForLondonByCategoryPredictionCalculator {

    @Value("${neural.network.model.path.crimeForLondonByCategory}")
    private String crimeByCategoryModelPath;

    private final CrimeLevelByCategoryRepository crimeLevelByCategoryRepository;
    private final CrimePredictionByCategoryRepository crimePredictionByCategoryRepository;
    private final CrimeCategoryRepository crimeCategoryRepository;
    private final PredictionNetwork predictionNetwork;
    private Map<String, Integer> categoriesNormalised = new HashMap<>();
    private Map<Integer, String> inversedCategoriesNormalised = new HashMap<>();

    @Autowired
    public CrimeForLondonByCategoryPredictionCalculator(CrimeLevelByCategoryRepository crimeLevelByCategoryRepository, CrimePredictionByCategoryRepository crimePredictionByCategoryRepository, PredictionNetwork predictionNetwork, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeLevelByCategoryRepository = crimeLevelByCategoryRepository;
        this.crimePredictionByCategoryRepository = crimePredictionByCategoryRepository;
        this.crimeCategoryRepository = crimeCategoryRepository;
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        loadStreetsAndCategories();
        var crimeLevelsByCategory = crimeLevelByCategoryRepository.findAll();
        var testData = crimeLevelsByCategory.stream().map(crimeLevelByCategory -> parseSingleMonthWithoutLabel(nextMonth, categoriesNormalised.get(crimeLevelByCategory.getCategory()))).collect(Collectors.toList());
        var predictionResult = predictionNetwork.predict(crimeByCategoryModelPath, testData);
        predictionResult.ifPresent(prediction -> savePredictionResult(prediction, testData));
    }

    public void train(CrimeLevelByCategory crimeLevelByCategory) {
        loadStreetsAndCategories();
        predictionNetwork.train(parseCrimeData(crimeLevelByCategory), crimeByCategoryModelPath);
    }

    private void loadStreetsAndCategories() {
        this.categoriesNormalised = crimeCategoryRepository.findAll().stream().collect(Collectors.toMap(CrimeCategory::getUrl, CrimeCategory::getNumericRepresentation));
        this.inversedCategoriesNormalised = MapStream.of(categoriesNormalised).inverseMapping().collect();
    }

    private List<List<Writable>> parseCrimeData(CrimeLevelByCategory crimeLevelByCategory) {
        return crimeLevelByCategory.getCrimesByMonth().entrySet().stream()
                .map(localDateLongEntry -> parseSingleMonthWithLabel(localDateLongEntry, new IntWritable(categoriesNormalised.get(crimeLevelByCategory.getCategory())))).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthWithLabel(Map.Entry<LocalDate, Long> crimesNumberForMonth, IntWritable categoryNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
        writables.add(categoryNormalised);
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getYear()));
        writables.add(new IntWritable(crimesNumberForMonth.getKey().getMonthValue()));
        return writables;
    }

    private void savePredictionResult(INDArray prediction, List<List<Writable>> testData) {
        for (var i = 0; i < prediction.rows(); i++) {
            saveSinglePrediction(prediction.getLong(i), testData.get(i));
        }
    }

    private void saveSinglePrediction(long predictedCrimeLevel, List<Writable> testDataRecord) {
        String category = inversedCategoriesNormalised.get(testDataRecord.get(0).toInt());
        LocalDate month = LocalDate.of(testDataRecord.get(2).toInt(), testDataRecord.get(3).toInt(), 1);
        crimePredictionByCategoryRepository.findById(category).ifPresentOrElse(crimePredictionForLondonByCategory -> {
                    crimePredictionForLondonByCategory.getCrimesByMonth().put(month, predictedCrimeLevel);
                    crimePredictionByCategoryRepository.save(crimePredictionForLondonByCategory);
                },
                () -> crimePredictionByCategoryRepository.save(new CrimePredictionByCategory(category, Map.of(month, predictedCrimeLevel))));
    }

    private List<Writable> parseSingleMonthWithoutLabel(LocalDate nextMonth, Integer categoryNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(categoryNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
