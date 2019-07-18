package com.safety.recognition.calculator;

import com.codepoetics.protonpack.maps.MapStream;
import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.model.indexes.NeighbourhoodAndCategoryKey;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByNeighbourhoodAndCategory;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByNeighbourhoodAndCategoryRepository;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionByNeighbourhoodAndCategoryRepository;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CrimeByNeighbourhoodAndCategoryPredictionCalculator {

    @Value("${neural.network.model.path.crimeByNeighbourhoodAndCategory}")
    private String crimeByNeighbourhoodAndCategoryModelPath;

    private final CrimeLevelByNeighbourhoodAndCategoryRepository crimeLevelByNeighbourhoodAndCategoryRepository;
    private final CrimePredictionByNeighbourhoodAndCategoryRepository crimePredictionByNeighbourhoodAndCategoryRepository;
    private final Map<String, Long> neighbourhoodsNormalised;
    private final Map<Long, String> inversedNeighbourhoodsNormalised;
    private final Map<String, Long> categoriesNormalised;
    private final Map<Long, String> inversedCategoriesNormalised;

    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeByNeighbourhoodAndCategoryPredictionCalculator(CrimeLevelByNeighbourhoodAndCategoryRepository crimeLevelByNeighbourhoodAndCategoryRepository, CrimePredictionByNeighbourhoodAndCategoryRepository crimePredictionByNeighbourhoodAndCategoryRepository, NeighbourhoodRepository neighbourhoodRepository, PredictionNetwork predictionNetwork, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeLevelByNeighbourhoodAndCategoryRepository = crimeLevelByNeighbourhoodAndCategoryRepository;
        this.crimePredictionByNeighbourhoodAndCategoryRepository = crimePredictionByNeighbourhoodAndCategoryRepository;
        this.neighbourhoodsNormalised = neighbourhoodRepository.findAll().stream().collect(Collectors.toMap(Neighbourhood::getName, Neighbourhood::getNumericRepresentation));
        this.inversedNeighbourhoodsNormalised = MapStream.of(neighbourhoodsNormalised).inverseMapping().collect();
        this.categoriesNormalised = crimeCategoryRepository.findAll().stream().collect(Collectors.toMap(CrimeCategory::getName, CrimeCategory::getNumericRepresentation));
        this.inversedCategoriesNormalised = MapStream.of(categoriesNormalised).inverseMapping().collect();
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevelsByNeighbourhoodCategory = crimeLevelByNeighbourhoodAndCategoryRepository.findAll();
        var testData = crimeLevelsByNeighbourhoodCategory.stream().map(crimeLevelByNeighbourhoodCategory -> parseSingleMonthWithoutLabel(nextMonth, categoriesNormalised.get(crimeLevelByNeighbourhoodCategory.getKey().getCategory()), neighbourhoodsNormalised.get(crimeLevelByNeighbourhoodCategory.getKey().getNeighbourhood()))).collect(Collectors.toList());
        var predictionResult = predictionNetwork.predict(crimeByNeighbourhoodAndCategoryModelPath, testData);
        predictionResult.ifPresent(prediction -> savePredictionResult(prediction, testData));
    }

    public void train(CrimeLevelByNeighbourhoodAndCategory crimeLevelByNeighbourhoodAndCategory) {
        predictionNetwork.train(parseCrimeData(crimeLevelByNeighbourhoodAndCategory), crimeByNeighbourhoodAndCategoryModelPath);
    }

    private List<List<Writable>> parseCrimeData(CrimeLevelByNeighbourhoodAndCategory crimeLevelByNeighbourhoodCategory) {
        return crimeLevelByNeighbourhoodCategory.getCrimesByMonth().entrySet().stream()
                .map(localDateLongEntry -> parseSingleMonthWithLabel(localDateLongEntry, new LongWritable(categoriesNormalised.get(crimeLevelByNeighbourhoodCategory.getKey().getCategory())), new LongWritable(neighbourhoodsNormalised.get(crimeLevelByNeighbourhoodCategory.getKey().getNeighbourhood())))).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthWithLabel(Map.Entry<LocalDate, Long> crimesNumberForMonth, LongWritable categoryNormalised, LongWritable neighbourhoodNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(categoryNormalised);
        writables.add(neighbourhoodNormalised);
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
        String category = inversedCategoriesNormalised.get(testDataRecord.get(0).toLong());
        String neighbourhood = inversedNeighbourhoodsNormalised.get(testDataRecord.get(1).toLong());
        var neighbourhoodAndCategoryKey = new NeighbourhoodAndCategoryKey(neighbourhood, category);
        LocalDate month = LocalDate.of(testDataRecord.get(2).toInt(), testDataRecord.get(3).toInt(), 1);
        crimePredictionByNeighbourhoodAndCategoryRepository.findById(neighbourhoodAndCategoryKey).ifPresentOrElse(crimePredictionByNeighbourhoodAndCategory -> {
                    crimePredictionByNeighbourhoodAndCategory.getCrimesByMonth().put(month, predictedCrimeLevel);
                    crimePredictionByNeighbourhoodAndCategoryRepository.save(crimePredictionByNeighbourhoodAndCategory);
                },
                () -> crimePredictionByNeighbourhoodAndCategoryRepository.save(new CrimePredictionByNeighbourhoodAndCategory(new NeighbourhoodAndCategoryKey(neighbourhood, category), Map.of(month, predictedCrimeLevel))));
    }

    private List<Writable> parseSingleMonthWithoutLabel(LocalDate nextMonth, Long categoryNormalised, Long neighbourhoodNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(categoryNormalised));
        writables.add(new LongWritable(neighbourhoodNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
