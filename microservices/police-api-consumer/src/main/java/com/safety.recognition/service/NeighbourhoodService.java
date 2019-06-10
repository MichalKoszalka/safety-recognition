package com.safety.recognition.service;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.client.NeighbourhoodBoundaryClient;
import com.safety.recognition.client.NeighbourhoodClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class NeighbourhoodService {

    private static final Logger LOG = LoggerFactory.getLogger(NeighbourhoodService.class);


    private final NeighbourhoodClient neighbourhoodClient;

    private final NeighbourhoodBoundaryClient neighbourhoodBoundaryClient;

    private final CassandraRepository<Neighbourhood, String> neighbourhoodCassandraRepository;

    @Autowired
    public NeighbourhoodService(NeighbourhoodClient neighbourhoodClient, NeighbourhoodBoundaryClient neighbourhoodBoundaryClient, CassandraRepository<Neighbourhood, String> neighbourhoodCassandraRepository) {
        this.neighbourhoodClient = neighbourhoodClient;
        this.neighbourhoodBoundaryClient = neighbourhoodBoundaryClient;
        this.neighbourhoodCassandraRepository = neighbourhoodCassandraRepository;
    }

    public List<Neighbourhood> getNeighbourhoods() {
        var neighbourhoods = neighbourhoodCassandraRepository.findAll();
        if (neighbourhoods.isEmpty()) {
            neighbourhoods = neighbourhoodClient.getNeighbourhoods()
                    .parallelStream()
                    .map(this::createNeighbourhood)
                    .map(neighbourhoodCassandraRepository::save).collect(Collectors.toList());

        }
        return neighbourhoods;
    }

    private Neighbourhood createNeighbourhood(data.police.uk.model.neighbourhood.Neighbourhood policeNeighbourhood) {
        var neighbourhood = new Neighbourhood();
        neighbourhood.setName(policeNeighbourhood.getName());
        neighbourhood.setNumericRepresentation(new Random().nextLong());
        neighbourhood.setBoundary(reducePolygonComplexity(policeNeighbourhood));
        return neighbourhood;
    }

    private List<Point> reducePolygonComplexity(data.police.uk.model.neighbourhood.Neighbourhood policeNeighbourhood) {
        var points = neighbourhoodBoundaryClient.getBoundariesByNeighbourhood(policeNeighbourhood.getId());
        int reducedPercentage = 0;
        var previousSize = points.size();
        while (points.size() > 50) {
            if (points.size() == previousSize && reducedPercentage > 0) {
                reducedPercentage = reducedPercentage + 5;
            } else {
                reducedPercentage = 5;
            }
            previousSize = points.size();
            LOG.info(String.format("Starting reducing neighbourhood boundary polygon complexity for neighbourhood: %s:%s. Current number of points: %d. Reduced percentage: %d", policeNeighbourhood.getName(), policeNeighbourhood.getId(), points.size(), reducedPercentage));
            points = reducePercent(points, reducedPercentage);
        }
        var simpleBoundary = duplicateFirstPointAtTheEnd(points);
        LOG.info(String.format("Reducing neighbourhood boundary polygon complexity finished for neighbourhood: %s:%s. Current number of points: %d", policeNeighbourhood.getName(), policeNeighbourhood.getId(), simpleBoundary.size()));
        return simpleBoundary;
    }

    private List<Pair<Double, Point>> calculateWeights(List<Point> points) {
        List<Pair<Double, Point>> pointsWithWeights = new ArrayList<>();
        for (var i = 0; i < points.size(); i++) {
            double latLongDifferenceSum;
            if (i == 0) {
                latLongDifferenceSum = calculateLatLongDifferenceSum(points.get(points.size() - 1), points.get(i), points.get(i + 1));
            } else if (i == points.size() - 1) {
                latLongDifferenceSum = calculateLatLongDifferenceSum(points.get(i - 1), points.get(i), points.get(0));
            } else {
                latLongDifferenceSum = calculateLatLongDifferenceSum(points.get(i - 1), points.get(i), points.get(i + 1));
            }
            pointsWithWeights.add(Pair.of(latLongDifferenceSum, points.get(i)));
        }
        return pointsWithWeights;
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

    private List<Point> reducePercent(List<Point> points, int percent) {
        var pointsWithWeights = calculateWeights(points);
        Double finalMaxDifferenceSum = pointsWithWeights.stream().sorted((o1, o2) -> o2.getFirst().compareTo(o1.getFirst())).findFirst().map(Pair::getFirst).get();
        return pointsWithWeights.stream().map(pair -> Pair.of(Double.valueOf(pair.getFirst() / finalMaxDifferenceSum * 100).intValue(), pair.getSecond())).filter(pair -> pair.getFirst() > percent).map(Pair::getSecond).collect(Collectors.toList());
    }


}
