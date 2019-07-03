package com.safety.recognition.deeplearning;

import org.datavec.api.records.reader.impl.collection.CollectionRecordReader;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.NormalizerMinMaxScaler;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PredictionNetwork {

    private static final Logger LOG = LoggerFactory.getLogger(PredictionNetwork.class);

    public PredictionNetwork() {
    }

    private MultiLayerConfiguration createNetworkConfiguration(int inputNeurons) {
        return new NeuralNetConfiguration.Builder()
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputNeurons).nOut(inputNeurons * 4).build())
                .layer(1, new DenseLayer.Builder().nIn(inputNeurons * 4).nOut(inputNeurons * 50).build())
                .layer(2, new DenseLayer.Builder().nIn(inputNeurons * 50).nOut(inputNeurons * 50).build())
                .layer(3, new OutputLayer.Builder(
                        LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(inputNeurons * 50).nOut(1).build()).build();
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
        var iterator = normalizeData(new RecordReaderDataSetIterator.Builder(createDataReader(trainData), 1).regression(0).build());
        model.fit(iterator, 1000);
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
        return new MultiLayerNetwork(configuration);
    }

    private Optional<MultiLayerNetwork> loadModel(String modelPath) {
        var resourceFile = getFileIfExists(modelPath);
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
        var resource = this.getClass().getClassLoader().getResource(modelPath);
        if (resource != null) {
            var file = new File(resource.getPath());
            return file.exists() ? Optional.of(file) : Optional.empty();
        }
        return Optional.empty();
    }

    private void saveModel(MultiLayerNetwork model, String modelPath) {
        try {
            ModelSerializer.writeModel(model, modelPath, true);
        } catch (IOException e) {
            LOG.error("Unable to save model to file", modelPath, e);
        }
    }

    private DataSetIterator createDataSetIteratorWithLabel(List<List<Writable>> data) {
        return new RecordReaderDataSetIterator.Builder(createDataReader(data), 1).regression(0).build();
    }

    private DataSetIterator createDataSetIteratorWithoutLabel(List<List<Writable>> data) {
        return new RecordReaderDataSetIterator(createDataReader(data), 1);
    }

    private CollectionRecordReader createDataReader(List<List<Writable>> data) {
        return new CollectionRecordReader(data);
    }

    private DataSetIterator normalizeData(DataSetIterator dataSetIterator) {
        var scaler = new NormalizerMinMaxScaler(0, 1);
        scaler.fit(dataSetIterator);
        dataSetIterator.setPreProcessor(scaler);
        return dataSetIterator;
    }


}
