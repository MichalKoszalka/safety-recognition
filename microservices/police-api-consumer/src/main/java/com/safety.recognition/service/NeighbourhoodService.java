package com.safety.recognition.service;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.client.NeighbourhoodBoundaryClient;
import com.safety.recognition.client.NeighbourhoodClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NeighbourhoodService {

    private final NeighbourhoodClient neighbourhoodClient;

    private final NeighbourhoodBoundaryClient neighbourhoodBoundaryClient;

    private final CassandraRepository<Neighbourhood, String> neighbourhoodCassandraRepository;

    @Autowired
    public NeighbourhoodService(NeighbourhoodClient neighbourhoodClient, NeighbourhoodBoundaryClient neighbourhoodBoundaryClient, CassandraRepository<Neighbourhood, String> neighbourhoodCassandraRepository) {
        this.neighbourhoodClient = neighbourhoodClient;
        this.neighbourhoodBoundaryClient = neighbourhoodBoundaryClient;
        this.neighbourhoodCassandraRepository = neighbourhoodCassandraRepository;
    }

    public List<Neighbourhood> getNeighbourhoodStream() {
        var neighbourhoods = neighbourhoodCassandraRepository.findAll();
        if (neighbourhoods.isEmpty()) {
            neighbourhoods = neighbourhoodClient.getNeigbourhoods()
                    .parallelStream()
                    .map(this::createNeighbourhood)
                    .map(neighbourhoodCassandraRepository::save).collect(Collectors.toList());

        }
        return neighbourhoods;
    }

    private Neighbourhood createNeighbourhood(data.police.uk.model.neighbourhood.Neighbourhood policeNeighbourhood) {
        var neighbourhood = new Neighbourhood();
        neighbourhood.setId(policeNeighbourhood.getId());
        neighbourhood.setName(policeNeighbourhood.getName());
        neighbourhood.setBoundary(reducePolygonComplexity(neighbourhoodBoundaryClient.getBoundariesByNeighbourhood(policeNeighbourhood.getId())));
        return neighbourhood;
    }

    private List<Point> reducePolygonComplexity(List<Point> points) {
        List<Pair<Double, Point>> pointsWithWeights = new ArrayList<>();
        Double maxDifferenceSum = null;
        for (var i = 0; i < points.size(); i++) {
            double latLongDifferenceSum;
            if (i == 0) {
                latLongDifferenceSum = calculateLatLongDifferenceSum(points.get(points.size()-1), points.get(i), points.get(i+1));
            } else if (i == points.size() - 1) {
                latLongDifferenceSum = calculateLatLongDifferenceSum(points.get(i-1), points.get(i), points.get(0));
            } else {
                latLongDifferenceSum = calculateLatLongDifferenceSum(points.get(i-1), points.get(i), points.get(i+1));
            }
            if(maxDifferenceSum == null || maxDifferenceSum < latLongDifferenceSum) {
                maxDifferenceSum = latLongDifferenceSum;
            }
            pointsWithWeights.add(Pair.of(latLongDifferenceSum, points.get(i)));
        }
        Double finalMaxDifferenceSum = maxDifferenceSum;
        return duplicateFirstPointAtTheEnd(pointsWithWeights.stream().map(pair -> Pair.of(Double.valueOf(pair.getFirst()/finalMaxDifferenceSum*100).intValue(), pair.getSecond())).filter(pair -> pair.getFirst() > 80).map(Pair::getSecond).collect(Collectors.toList()));
    }

    private double calculateLatLongDifferenceSum(Point previous, Point current, Point next) {
        var longDifference = Math.abs(previous.getLatitude() - current.getLatitude()) + Math.abs(next.getLatitude() - current.getLatitude());
        var lattDifference = Math.abs(previous.getLongitude() - current.getLongitude()) + Math.abs(next.getLongitude() - current.getLongitude());
        return longDifference + lattDifference;
    }

    private List<Point> duplicateFirstPointAtTheEnd(List<Point> points) {
        points.add(points.stream().findFirst().orElseThrow(IllegalArgumentException::new));
        return points;
    }


}
