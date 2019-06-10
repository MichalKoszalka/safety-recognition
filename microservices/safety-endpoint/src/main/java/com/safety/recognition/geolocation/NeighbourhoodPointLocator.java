package com.safety.recognition.geolocation;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.Point;
import com.safety.recognition.cassandra.repository.NeighbourhoodRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NeighbourhoodPointLocator {

    private final NeighbourhoodRepository neighbourhoodRepository;

    @Autowired
    public NeighbourhoodPointLocator(NeighbourhoodRepository neighbourhoodRepository) {
        this.neighbourhoodRepository = neighbourhoodRepository;
    }

    public Optional<Neighbourhood> locatePoint(Point point) {
        return neighbourhoodRepository.findAll().parallelStream().filter(neighbourhood -> contains(point, neighbourhood.getBoundary())).findFirst();
    }

    private boolean contains(Point point, List<Point> points) {
        int i;
        int j;
        boolean result = false;
        for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
            if ((points.get(i).getLatitude() > point.getLatitude()) != (points.get(j).getLatitude() > point.getLatitude()) &&
                    (point.getLongitude() < (points.get(j).getLongitude() - points.get(i).getLongitude()) * (point.getLatitude() - points.get(i).getLatitude()) / (points.get(j).getLatitude()-points.get(i).getLatitude()) + points.get(i).getLongitude())) {
                result = !result;
            }
        }
        return result;
    }

}
