package com.safety.recognition.calculator;

import com.safety.recognition.cassandra.model.indexes.CrimeLevel;
import com.safety.recognition.cassandra.model.predictions.CrimePrediction;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelRepository;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionRepository;
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
public class CrimeForLondonPredictionCalculator {

    @Value("${neural.network.model.path.crimeForLondon}")
    private String crimeForLondonModelPath;

    private final CrimeLevelRepository crimeLevelRepository;
    private final CrimePredictionRepository crimePredictionRepository;
    private final PredictionNetwork predictionNetwork;

    @Autowired
    public CrimeForLondonPredictionCalculator(CrimeLevelRepository crimeLevelRepository, CrimePredictionRepository crimePredictionRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelRepository = crimeLevelRepository;
        this.crimePredictionRepository = crimePredictionRepository;
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        var crimeLevels = crimeLevelRepository.findAll();
        var testData = crimeLevels.stream().map(crimeLevel -> parseSingleMonthWithoutLabel(nextMonth)).collect(Collectors.toList());
        var predictionResult = predictionNetwork.predict(crimeForLondonModelPath, testData);
        predictionResult.ifPresent(prediction -> savePredictionResult(prediction, testData));
    }

    public void train(CrimeLevel crimeLevel) {
        predictionNetwork.train(parseCrimeData(crimeLevel), crimeForLondonModelPath);
    }


    private List<List<Writable>> parseCrimeData(CrimeLevel crimeLevel) {
        return crimeLevel.getCrimesByMonth().entrySet().stream()
                .map(this::parseSingleMonthWithLabel).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthWithLabel(Map.Entry<LocalDate, Long> crimesNumberForMonth) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
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
        LocalDate month = LocalDate.of(testDataRecord.get(2).toInt(), testDataRecord.get(3).toInt(), 1);
        crimePredictionRepository.findById("London").ifPresentOrElse(crimePredictionForLondon -> {
                    crimePredictionForLondon.getCrimesByMonth().put(month, predictedCrimeLevel);
                    crimePredictionRepository.save(crimePredictionForLondon);
                },
                () -> crimePredictionRepository.save(new CrimePrediction("London", Map.of(month, predictedCrimeLevel))));
    }

    private List<Writable> parseSingleMonthWithoutLabel(LocalDate nextMonth) {
        var writables = new ArrayList<Writable>();
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
