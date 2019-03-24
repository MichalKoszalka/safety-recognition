package com.safety.recognition.client;


import com.safety.recognition.cassandra.model.Point;

import java.util.List;

public interface NeighbourhoodBoundaryClient {

    List<Point> getBoundariesByNeighbourhood(String neighbourhoodId);

}
