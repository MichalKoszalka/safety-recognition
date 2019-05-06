package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NeighbourhoodBoundaryClientImpl implements NeighbourhoodBoundaryClient {

    private static final Logger LOG = LoggerFactory.getLogger(NeighbourhoodBoundaryClient.class);

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String BOUNDARY = "/boundary";
    private static final String METROPOLITAN = "metropolitan/";

    @Override
    public List<Point> getBoundariesByNeighbourhood(String neighbourhoodId) {
        LOG.info(String.format("fetching boundary for neighbourhood: %s", neighbourhoodId));
        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
            crimeApiUrl + METROPOLITAN + neighbourhoodId + BOUNDARY,
            HttpMethod.GET,
            null, new ParameterizedTypeReference<List<Point>>() {
            });
        return response.getBody();
    }
}
