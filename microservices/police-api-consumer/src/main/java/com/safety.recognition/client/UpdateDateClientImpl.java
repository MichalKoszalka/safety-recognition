package com.safety.recognition.client;

import data.police.uk.model.LastUpdateDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Service
public class UpdateDateClientImpl implements UpdateDateClient {

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String UPDATE_DATE = "crime-last-updated";

    @Override
    public LastUpdateDate getUpdateDate() {
        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
                crimeApiUrl + UPDATE_DATE,
                HttpMethod.GET,
                null, new ParameterizedTypeReference<LastUpdateDate>() {
                });
        return response.getBody();
    }
}
