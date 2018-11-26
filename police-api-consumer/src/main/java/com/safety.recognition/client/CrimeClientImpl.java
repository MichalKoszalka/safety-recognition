package com.safety.recognition.client;

import com.safety.recognition.model.Crime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class CrimeClientImpl implements CrimeClient {

    @Value("${userBucket.path}")
    private String crimeApiUrl;

    private static final String CRIME_PATH = "crimes-street/all-crime";

    @Override
    public List<Crime> getCrimes() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<List<Crime>> response = restTemplate.exchange(
            crimeApiUrl + CRIME_PATH + "?lat=52.629729&lng=-1.131592&date=2017-01",
            HttpMethod.GET,
            null, new ParameterizedTypeReference<>() {
            });
        return response.getBody();
    }
}
