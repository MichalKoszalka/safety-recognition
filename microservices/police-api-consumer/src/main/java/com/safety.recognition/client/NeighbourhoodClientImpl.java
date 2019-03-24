package com.safety.recognition.client;

import data.police.uk.model.crime.Crime;
import data.police.uk.model.neighbourhood.Neighbourhood;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NeighbourhoodClientImpl implements NeighbourhoodClient {

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String NEIGHBOURHOODS = "metropolitan/neighbourhoods";

    @Override
    public List<Neighbourhood> getNeigbourhoods() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Neighbourhood>> response = restTemplate.exchange(
            crimeApiUrl + NEIGHBOURHOODS,
            HttpMethod.GET,
            null, new ParameterizedTypeReference<List<Neighbourhood>>() {
            });
        return response.getBody();
    }
}
