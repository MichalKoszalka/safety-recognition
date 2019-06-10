package com.safety.recognition.controller;

import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.cassandra.repository.indexes.CrimesByNeighbourhoodAllTimeIndexRepository;
import com.safety.recognition.geolocation.NeighbourhoodPointLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SafetyController {

    private final NeighbourhoodPointLocator neighbourhoodPointLocator;
    private final CrimesByNeighbourhoodAllTimeIndexRepository crimesByNeighbourhoodAllTimeIndexRepository;

    @Autowired
    public SafetyController(NeighbourhoodPointLocator neighbourhoodPointLocator, CrimesByNeighbourhoodAllTimeIndexRepository crimesByNeighbourhoodAllTimeIndexRepository) {
        this.neighbourhoodPointLocator = neighbourhoodPointLocator;
        this.crimesByNeighbourhoodAllTimeIndexRepository = crimesByNeighbourhoodAllTimeIndexRepository;
    }

    public ResponseEntity<Double> getCrimesAtLocation(@RequestParam double latitude, @RequestParam double longitude) {
        var neighbourhood = neighbourhoodPointLocator.locatePoint(new Point(latitude, longitude));
        if(neighbourhood.isPresent()) {
            var index = crimesByNeighbourhoodAllTimeIndexRepository.findById(neighbourhood.get().getName()).orElseThrow();
        }
        return null;
    }

}
