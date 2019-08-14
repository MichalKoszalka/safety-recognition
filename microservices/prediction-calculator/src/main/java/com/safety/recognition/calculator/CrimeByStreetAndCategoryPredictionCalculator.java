package com.safety.recognition.calculator;

import com.codepoetics.protonpack.maps.MapStream;
import com.safety.recognition.cassandra.model.CrimeCategory;
import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreetAndCategory;
import com.safety.recognition.cassandra.model.indexes.StreetAndCategoryKey;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByStreetAndCategory;
import com.safety.recognition.cassandra.repository.CrimeCategoryRepository;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByStreetAndCategoryRepository;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionByStreetAndCategoryRepository;
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
public class CrimeByStreetAndCategoryPredictionCalculator {

    @Value("${neural.network.model.path.crimeByStreetAndCategory}")
    private String crimeByStreetAndCategoryModelPath;

    private final CrimeLevelByStreetAndCategoryRepository crimeLevelByStreetAndCategoryRepository;
    private final CrimePredictionByStreetAndCategoryRepository crimePredictionByStreetAndCategoryRepository;
    private final CrimeCategoryRepository crimeCategoryRepository;
    private final StreetRepository streetRepository;
    private final PredictionNetwork predictionNetwork;
    private Map<StreetKey, Integer> streetsNormalised = new HashMap<>();
    private Map<Integer, StreetKey> inversedStreetsNormalised = new HashMap<>();
    private Map<String, Integer> categoriesNormalised = new HashMap<>();
    private Map<Integer, String> inversedCategoriesNormalised = new HashMap<>();

    @Autowired
    public CrimeByStreetAndCategoryPredictionCalculator(CrimeLevelByStreetAndCategoryRepository crimeLevelByStreetAndCategoryRepository, CrimePredictionByStreetAndCategoryRepository crimePredictionByStreetAndCategoryRepository, StreetRepository streetRepository, PredictionNetwork predictionNetwork, CrimeCategoryRepository crimeCategoryRepository) {
        this.crimeLevelByStreetAndCategoryRepository = crimeLevelByStreetAndCategoryRepository;
        this.crimePredictionByStreetAndCategoryRepository = crimePredictionByStreetAndCategoryRepository;
        this.streetRepository = streetRepository;
        this.crimeCategoryRepository = crimeCategoryRepository;
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        loadStreetsAndCategories();
        var crimeLevelsByStreetCategory = crimeLevelByStreetAndCategoryRepository.findAll();
        var testData = crimeLevelsByStreetCategory.stream().map(crimeLevelByStreetCategory -> parseSingleMonthWithoutLabel(nextMonth, categoriesNormalised.get(crimeLevelByStreetCategory.getKey().getCategory()), streetsNormalised.get(crimeLevelByStreetCategory.getKey().getStreet()))).collect(Collectors.toList());
        var predictionResult = predictionNetwork.predict(crimeByStreetAndCategoryModelPath, testData);
        predictionResult.ifPresent(prediction -> savePredictionResult(prediction, testData));
    }

    public void train(CrimeLevelByStreetAndCategory crimeLevelByStreetAndCategory) {
        loadStreetsAndCategories();
        predictionNetwork.train(parseCrimeData(crimeLevelByStreetAndCategory), crimeByStreetAndCategoryModelPath);
    }

    private void loadStreetsAndCategories() {
        this.streetsNormalised = streetRepository.findAll().stream().collect(Collectors.toMap(Street::getKey, Street::getNumericRepresentation));
        this.inversedStreetsNormalised = MapStream.of(streetsNormalised).inverseMapping().collect();
        this.categoriesNormalised = crimeCategoryRepository.findAll().stream().collect(Collectors.toMap(CrimeCategory::getUrl, CrimeCategory::getNumericRepresentation));
        this.inversedCategoriesNormalised = MapStream.of(categoriesNormalised).inverseMapping().collect();
    }

    private List<List<Writable>> parseCrimeData(CrimeLevelByStreetAndCategory crimeLevelByStreetCategory) {
        return crimeLevelByStreetCategory.getCrimesByMonth().entrySet().stream()
                .map(localDateLongEntry -> parseSingleMonthWithLabel(localDateLongEntry, new IntWritable(categoriesNormalised.get(crimeLevelByStreetCategory.getKey().getCategory())), new IntWritable(streetsNormalised.get(crimeLevelByStreetCategory.getKey().getStreet())))).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthWithLabel(Map.Entry<LocalDate, Long> crimesNumberForMonth, IntWritable categoryNormalised, IntWritable streetNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
        writables.add(categoryNormalised);
        writables.add(streetNormalised);
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
        StreetKey street = inversedStreetsNormalised.get(testDataRecord.get(1).toInt());
        var streetAndCategoryKey = new StreetAndCategoryKey(street.getStreet(), street.getNeighbourhood(), category);
        LocalDate month = LocalDate.of(testDataRecord.get(2).toInt(), testDataRecord.get(3).toInt(), 1);
        crimePredictionByStreetAndCategoryRepository.findById(streetAndCategoryKey).ifPresentOrElse(crimePredictionByStreetAndCategory -> {
                    crimePredictionByStreetAndCategory.getCrimesByMonth().put(month, predictedCrimeLevel);
                    crimePredictionByStreetAndCategoryRepository.save(crimePredictionByStreetAndCategory);
                },
                () -> crimePredictionByStreetAndCategoryRepository.save(new CrimePredictionByStreetAndCategory(new StreetAndCategoryKey(street.getStreet(),street.getNeighbourhood(), category), Map.of(month, predictedCrimeLevel))));
    }

    private List<Writable> parseSingleMonthWithoutLabel(LocalDate nextMonth, Integer categoryNormalised, Integer streetNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(categoryNormalised));
        writables.add(new LongWritable(streetNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
