package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.Point;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NeighbourhoodBoundaryClientImpl implements NeighbourhoodBoundaryClient {

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String BOUNDARY = "/boundary";
    private static final String METROPOLITAN = "metropolitan/";

    @Override
    public List<Point> getBoundariesByNeighbourhood(String neighbourhoodId) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Point>> response = restTemplate.exchange(
            crimeApiUrl + METROPOLITAN + neighbourhoodId + BOUNDARY,
            HttpMethod.GET,
            null, new ParameterizedTypeReference<List<Point>>() {
            });
        return response.getBody();    }
}
