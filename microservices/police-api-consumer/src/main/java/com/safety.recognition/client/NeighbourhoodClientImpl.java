package com.safety.recognition.client;

import data.police.uk.model.neighbourhood.Neighbourhood;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class NeighbourhoodClientImpl implements NeighbourhoodClient {

    private static final Logger LOG = LoggerFactory.getLogger(NeighbourhoodClient.class);

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String NEIGHBOURHOODS = "metropolitan/neighbourhoods";

    @Override
    public List<Neighbourhood> getNeighbourhoods() {
        LOG.info("fetching neighbourhoods for metropolitan force");
        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
            crimeApiUrl + NEIGHBOURHOODS,
            HttpMethod.GET,
            null, new ParameterizedTypeReference<List<Neighbourhood>>() {
            });
        return response.getBody();
    }
}
