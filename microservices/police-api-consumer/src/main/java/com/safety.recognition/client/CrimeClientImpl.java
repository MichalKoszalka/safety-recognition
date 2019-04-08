package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.Point;
import data.police.uk.model.crime.Crime;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CrimeClientImpl implements CrimeClient {

    private static final String CRIME_PATH = "crimes-street/all-crime";

    @Override
    public List<Crime> getCrimes(Neighbourhood neighbourhood, LocalDate updateDate) {
        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
                new RequestPathBuilder().withMethod(CRIME_PATH).withQueryParam("poly", neighbourhood.getBoundary().stream().map(Point::toString).collect(Collectors.joining(","))).withQueryParam("date", updateDate.format(DateTimeFormatter.ofPattern("YYYY-MM"))).build(),
                HttpMethod.GET,
                null, new ParameterizedTypeReference<List<Crime>>() {
                });
        return response.getBody();
    }
}
