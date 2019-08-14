package com.safety.recognition.deeplearning;

import com.safety.recognition.ui.NeuralNetworkUI;
import org.apache.commons.io.FileUtils;
import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerStandardize;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PredictionNetwork {

    private static final Logger LOG = LoggerFactory.getLogger(PredictionNetwork.class);

    private final NeuralNetworkUI neuralNetworkUI;

    @Inject
    public PredictionNetwork(NeuralNetworkUI neuralNetworkUI) {
        this.neuralNetworkUI = neuralNetworkUI;
    }

    private MultiLayerConfiguration createNetworkConfiguration(int inputNeurons) {
        return new NeuralNetConfiguration.Builder()
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputNeurons).nOut(inputNeurons * 25).weightInit(WeightInit.XAVIER).activation(Activation.IDENTITY).dropOut(0.25).build())
                .layer(1, new DenseLayer.Builder().nIn(inputNeurons * 25).nOut(inputNeurons * 200).weightInit(WeightInit.XAVIER).activation(Activation.RELU).dropOut(0.1).build())
                .layer(2, new OutputLayer.Builder(
                        LossFunctions.LossFunction.MSE)
                        .weightInit(WeightInit.XAVIER)
                        .activation(Activation.IDENTITY)
                        .updater(new AdaGrad(0.001))
                        .nIn(inputNeurons * 200).nOut(1).build()).backpropType(BackpropType.Standard).build();
    }

    public Optional<INDArray> predict(String modelPath, List<List<Writable>> predictData) {
        LOG.info(String.format("Starting level prediction with model %s", modelPath));
        return loadModel(modelPath).map(model -> {
                    var predictDataIterator = normalizeData(createDataSetIteratorWithoutLabel(predictData));
                    var output = model.output(predictDataIterator);
                    System.out.println(String.format("Prediction finished for model %s, outcome is \n: %s", modelPath, output));
                    return Optional.of(output);
                }).orElse(Optional.empty());
    }

    public void train(List<List<Writable>> trainData, String modelPath) {
        LOG.info(String.format("Starting training model %s", modelPath));
        var model = getOrCreateModel(modelPath, calculateInputNeurons(trainData));
        var iterator = normalizeData(new RecordReaderDataSetIterator.Builder(createDataReader(trainData), 10).regression(0).build());
        model.fit(iterator, 20);
        var evaluation = model.evaluateRegression(normalizeData(new RecordReaderDataSetIterator.Builder(createDataReader(trainData), 10).regression(0).build()));
        System.out.println(String.format("Evaluation finished, outcome is \n: %s", evaluation.stats()));
        saveModel(model, modelPath);
    }

    private MultiLayerNetwork getOrCreateModel(String modelPath, int inputNeurons) {
        return loadModel(modelPath).orElseGet(() -> createModel(createNetworkConfiguration(inputNeurons)));
    }

    private int calculateInputNeurons(List<List<Writable>> testData) {
        return testData.get(0).size() - 1;
    }

    private MultiLayerNetwork createModel(MultiLayerConfiguration configuration) {
        LOG.info("Creating new model");
        var neuralNetwork = new MultiLayerNetwork(configuration);
        neuralNetwork.setListeners(new StatsListener(neuralNetworkUI.getStatsStorage()));
        return neuralNetwork;
    }

    private Optional<MultiLayerNetwork> loadModel(String modelPath) {
        var resourceFile = getFileIfExists(this.getClass().getClassLoader().getResource("").getPath()+modelPath);
        if (resourceFile.isPresent()) {
            try {
                return Optional.of(ModelSerializer.restoreMultiLayerNetwork(resourceFile.get()));
            } catch (IOException e) {
                LOG.error("Unable to retrieve model", e);
            }
        }
        LOG.info(String.format("No existing model found for path: %s", modelPath));
        return Optional.empty();
    }

    private Optional<File> getFileIfExists(String modelPath) {
        var file = FileUtils.getFile(modelPath);
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    private void saveModel(MultiLayerNetwork model, String modelPath) {
        try {
            var pathInResources = this.getClass().getClassLoader().getResource("").getPath()+modelPath;
            var file = new File(pathInResources);
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            ModelSerializer.writeModel(model, pathInResources, true);
        } catch (IOException e) {
            LOG.error("Unable to save model to file", modelPath, e);
        }
    }

    private DataSetIterator createDataSetIteratorWithoutLabel(List<List<Writable>> data) {
        return new RecordReaderDataSetIterator(createDataReader(data), 10);
    }

    private CollectionRecordReader createDataReader(List<List<Writable>> data) {
        return new CollectionRecordReader(data);
    }

    private DataSetIterator normalizeData(DataSetIterator dataSetIterator) {
        var scaler = new NormalizerStandardize();
        scaler.fit(dataSetIterator);
        dataSetIterator.setPreProcessor(scaler);
        return dataSetIterator;
    }


}
