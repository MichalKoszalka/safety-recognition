package com.safety.recognition.client;


import data.police.uk.model.neighbourhood.Neighbourhood;
import org.springframework.stereotype.Service;

import java.util.List;

public interface NeighbourhoodClient {

    List<Neighbourhood> getNeigbourhoods();

}
