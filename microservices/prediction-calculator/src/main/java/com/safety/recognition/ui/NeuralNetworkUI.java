package com.safety.recognition.ui;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NeuralNetworkUI {

    private static StatsStorage statsStorage;

    @PostConstruct
    public void bootStrapUI() {
        //Initialize the user interface backend
        UIServer uiServer = UIServer.getInstance();

        //Configure where the network information (gradients, score vs. time etc) is to be stored. Here: store in memory.
        statsStorage = new InMemoryStatsStorage();         //Alternative: new FileStatsStorage(File), for saving and loading later

        //Attach the StatsStorage instance to the UI: this allows the contents of the StatsStorage to be visualized
        uiServer.attach(statsStorage);
    }

    public StatsStorage getStatsStorage() {
        return statsStorage;
    }
}
