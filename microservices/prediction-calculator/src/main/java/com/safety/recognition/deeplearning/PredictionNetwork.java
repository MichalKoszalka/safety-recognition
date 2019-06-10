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
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class PredictionNetwork {

    private static final Logger LOG = LoggerFactory.getLogger(PredictionNetwork.class);

    public PredictionNetwork() {
    }

    private MultiLayerConfiguration createNetworkConfiguration(int inputNeurons) {
        return new NeuralNetConfiguration.Builder()
                .iterations(1000)
                .activation(Activation.TANH)
                .weightInit(WeightInit.XAVIER)
                .learningRate(0.1)
                .regularization(true).l2(0.0001)
                .list()
                .layer(0, new DenseLayer.Builder().nIn(inputNeurons).nOut(inputNeurons).build())
                .layer(1, new DenseLayer.Builder().nIn(inputNeurons).nOut(inputNeurons*4).build())
                .layer(2, new DenseLayer.Builder().nIn(inputNeurons*4).nOut(inputNeurons).build())
                .layer(3, new OutputLayer.Builder(
                        LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nIn(inputNeurons).nOut(1).build())
                .backprop(true).pretrain(true)
                .build();
    }

    public void predict(List<List<Writable>> trainData, String modelPath, List<List<Writable>> predictData) {
        LOG.info(String.format("Starting crime level prediction with model %s"), modelPath);
        var model = loadModel(modelPath, calculateInputNeurons(predictData));
        model = trainAndSaveModel(model, trainData, modelPath);
        var evaluation = model.evaluate(createDataSetIterator(predictData));
        LOG.info("Prediction finished, outcome is %s", evaluation.stats());
    }

    private int calculateInputNeurons(List<List<Writable>> testData) {
        return testData.get(0).size();
    }

    private MultiLayerNetwork createModel(MultiLayerConfiguration configuration) {
        return new MultiLayerNetwork(configuration);
    }

    public MultiLayerNetwork loadModel(String modelPath, int inputNeurons) {
        var resourceFile = new File(this.getClass().getClassLoader().getResource(modelPath).getFile());
        if (resourceFile.exists()) {
            try {
                return ModelSerializer.restoreMultiLayerNetwork(resourceFile);
            } catch (IOException e) {
                LOG.error("Unable to retrieve model, new will be created.", e);
            }
        }
        return createModel(createNetworkConfiguration(inputNeurons));
    }

    private MultiLayerNetwork trainAndSaveModel(MultiLayerNetwork model, List<List<Writable>> trainData, String modelPath) {
        var iterator = new RecordReaderDataSetIterator(createDataReader(trainData), 1);
        LOG.info(String.format("Pretraining model %s", modelPath));
        model.pretrain(iterator);
        saveModel(model, modelPath);
        return model;
    }

    private void saveModel(MultiLayerNetwork model, String modelPath) {
        try {
            ModelSerializer.writeModel(model, modelPath, true);
        } catch (IOException e) {
            LOG.error("Unable to save model to file", modelPath, e);
        }
    }

    private DataSetIterator createDataSetIterator(List<List<Writable>> data) {
        return new RecordReaderDataSetIterator(createDataReader(data), 1);
    }

    private CollectionRecordReader createDataReader(List<List<Writable>> data) {
        return new CollectionRecordReader(data);
    }

}
