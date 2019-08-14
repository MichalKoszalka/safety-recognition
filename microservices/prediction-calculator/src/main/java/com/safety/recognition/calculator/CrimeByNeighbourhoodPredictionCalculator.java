package com.safety.recognition.calculator;

import com.codepoetics.protonpack.maps.MapStream;
import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.indexes.CrimeLevelByNeighbourhood;
import com.safety.recognition.cassandra.model.predictions.CrimePredictionByNeighbourhood;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.indexes.CrimeLevelByNeighbourhoodRepository;
import com.safety.recognition.cassandra.repository.predictions.CrimePredictionByNeighbourhoodRepository;
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
public class CrimeByNeighbourhoodPredictionCalculator {

    @Value("${neural.network.model.path.crimeByNeighbourhood}")
    private String crimeByNeighbourhoodModelPath;

    private final CrimeLevelByNeighbourhoodRepository crimeLevelByNeighbourhoodRepository;
    private final CrimePredictionByNeighbourhoodRepository crimePredictionByNeighbourhoodRepository;
    private final NeighbourhoodRepository neighbourhoodRepository;
    private final PredictionNetwork predictionNetwork;
    private Map<String, Integer> neighbourhoodsNormalised = new HashMap<>();
    private Map<Integer, String> inversedNeighbourhoodsNormalised = new HashMap<>();

    @Autowired
    public CrimeByNeighbourhoodPredictionCalculator(CrimeLevelByNeighbourhoodRepository crimeLevelByNeighbourhoodRepository, CrimePredictionByNeighbourhoodRepository crimePredictionByNeighbourhoodRepository, NeighbourhoodRepository neighbourhoodRepository, PredictionNetwork predictionNetwork) {
        this.crimeLevelByNeighbourhoodRepository = crimeLevelByNeighbourhoodRepository;
        this.crimePredictionByNeighbourhoodRepository = crimePredictionByNeighbourhoodRepository;
        this.neighbourhoodRepository = neighbourhoodRepository;
        this.predictionNetwork = predictionNetwork;
    }

    public void calculate(LocalDate nextMonth) {
        loadNeighbourhoodsAndCategories();
        var crimeLevelsByNeighbourhood = crimeLevelByNeighbourhoodRepository.findAll();
        var testData = crimeLevelsByNeighbourhood.stream().map(crimeLevelByNeighbourhood -> parseSingleMonthWithoutLabel(nextMonth, neighbourhoodsNormalised.get(crimeLevelByNeighbourhood.getNeighbourhood()))).collect(Collectors.toList());
        var predictionResult = predictionNetwork.predict(crimeByNeighbourhoodModelPath, testData);
        predictionResult.ifPresent(prediction -> savePredictionResult(prediction, testData));
    }

    public void train(CrimeLevelByNeighbourhood crimeLevelByNeighbourhood) {
        loadNeighbourhoodsAndCategories();
        predictionNetwork.train(parseCrimeData(crimeLevelByNeighbourhood), crimeByNeighbourhoodModelPath);
    }

    private void loadNeighbourhoodsAndCategories() {
        this.neighbourhoodsNormalised = neighbourhoodRepository.findAll().stream().collect(Collectors.toMap(Neighbourhood::getName, Neighbourhood::getNumericRepresentation));
        this.inversedNeighbourhoodsNormalised = MapStream.of(neighbourhoodsNormalised).inverseMapping().collect();
    }

    private List<List<Writable>> parseCrimeData(CrimeLevelByNeighbourhood crimeLevelByNeighbourhood) {
        return crimeLevelByNeighbourhood.getCrimesByMonth().entrySet().stream()
                .map(localDateLongEntry -> parseSingleMonthWithLabel(localDateLongEntry, new IntWritable(neighbourhoodsNormalised.get(crimeLevelByNeighbourhood.getNeighbourhood())))).collect(Collectors.toList());
    }

    private List<Writable> parseSingleMonthWithLabel(Map.Entry<LocalDate, Long> crimesNumberForMonth, IntWritable neighbourhoodNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(crimesNumberForMonth.getValue()));
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
        String neighbourhood = inversedNeighbourhoodsNormalised.get(testDataRecord.get(1).toInt());
        LocalDate month = LocalDate.of(testDataRecord.get(2).toInt(), testDataRecord.get(3).toInt(), 1);
        crimePredictionByNeighbourhoodRepository.findById(neighbourhood).ifPresentOrElse(crimePredictionByNeighbourhood -> {
                    crimePredictionByNeighbourhood.getCrimesByMonth().put(month, predictedCrimeLevel);
                    crimePredictionByNeighbourhoodRepository.save(crimePredictionByNeighbourhood);
                },
                () -> crimePredictionByNeighbourhoodRepository.save(new CrimePredictionByNeighbourhood(neighbourhood, Map.of(month, predictedCrimeLevel))));
    }

    private List<Writable> parseSingleMonthWithoutLabel(LocalDate nextMonth, Integer neighbourhoodNormalised) {
        var writables = new ArrayList<Writable>();
        writables.add(new LongWritable(neighbourhoodNormalised));
        writables.add(new IntWritable(nextMonth.getYear()));
        writables.add(new IntWritable(nextMonth.getMonthValue()));
        return writables;
    }

}
