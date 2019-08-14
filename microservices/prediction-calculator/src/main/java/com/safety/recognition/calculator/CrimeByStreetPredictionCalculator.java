package com.safety.recognition.calculator;

import com.codepoetics.protonpack.maps.MapStream;
import com.safety.recognition.cassandra.model.Street;
import com.safety.recognition.cassandra.model.StreetKey;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByStreet;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByStreet;
import com.safety.recognition.cassandra.repository.StreetRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByStreetRepository;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionByStreetRepository;
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
public class CrimeByStreetPredictionCalculator {

    @Value("${neural.network.model.path.crimeByStreet}")
    private String crimeByStreetModelPath;

    private final CrimeLevelByStreetRepository crimeLevelByStreetRepository;
    private final CrimePredictionByStreetRepository crimePredictionByStreetRepository;
    private final StreetRepository streetRepository;
    private final PredictionNetwork predictionNetwork;
    private Map<StreetKey, Integer> streetsNormalised = new HashMap<>();
    private Map<Integer, StreetKey> inversedStreetsNormalised = new HashMap<>();

    @Autowired
    public CrimeByStreetPredictionCalculator(CrimeLevelByStreetRepository crimeLevelByStreetRepository, CrimePredictionByStreetRepository crimePredictionByStreetRepository, StreetRepository streetRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelByStreetRepository = crimeLevelByStreetRepository;
        this.crimePredictionByStreetRepository = crimePredictionByStreetRepository;
        this.streetRepository = streetRepository;
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        loadStreetsAndCategories();
        var crimeLevelsByStreet = crimeLevelByStreetRepository.findAll();
        var testData = crimeLevelsByStreet.stream().map(crimeLevelByStreet -> parseSingleMonthWithoutLabel(nextMonth, streetsNormalised.get(crimeLevelByStreet.getKey()))).collect(Collectors.toList());
        var predictionResult = predictionNetwork.predict(crimeByStreetModelPath, testData);
        predictionResult.ifPresent(prediction -> savePredictionResult(prediction, testData));
    }

    public void train(CrimeLevelByStreet crimeLevelByStreet) {
        loadStreetsAndCategories();
        predictionNetwork.train(parseCrimeData(crimeLevelByStreet), crimeByStreetModelPath);
    }

    private void loadStreetsAndCategories() {
        this.streetsNormalised = streetRepository.findAll().stream().collect(Collectors.toMap(Street::getKey, Street::getNumericRepresentation));
        this.inversedStreetsNormalised = MapStream.of(streetsNormalised).inverseMapping().collect();
    }

    private List<List<Writable>> parseCrimeData(CrimeLevelByStreet crimeLevelByStreet) {
        return crimeLevelByStreet.getCrimesByMonth().entrySet().stream()
                .map(localDateLongEntry -> parseSingleMonthWithLabel(localDateLongEntry, new IntWritable(streetsNormalised.get(crimeLevelByStreet.getKey())))).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthWithLabel(Map.Entry<LocalDate, Long> crimesNumberForMonth, IntWritable streetNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
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
        var streetKey = inversedStreetsNormalised.get(testDataRecord.get(1).toInt());
        LocalDate month = LocalDate.of(testDataRecord.get(2).toInt(), testDataRecord.get(3).toInt(), 1);
        crimePredictionByStreetRepository.findById(streetKey).ifPresentOrElse(crimePredictionByStreet -> {
                    crimePredictionByStreet.getCrimesByMonth().put(month, predictedCrimeLevel);
                    crimePredictionByStreetRepository.save(crimePredictionByStreet);
                },
                () -> crimePredictionByStreetRepository.save(new CrimePredictionByStreet(new StreetKey(streetKey.getStreet(), streetKey.getNeighbourhood()), Map.of(month, predictedCrimeLevel))));
    }

    private List<Writable> parseSingleMonthWithoutLabel(LocalDate nextMonth, Integer streetNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(streetNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
