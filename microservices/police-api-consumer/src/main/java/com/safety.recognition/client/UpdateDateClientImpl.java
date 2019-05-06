package com.safety.recognition.client;

import data.police.uk.model.LastUpdateDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UpdateDateClientImpl implements UpdateDateClient {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateDateClient.class);

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String UPDATE_DATE = "crime-last-updated";

    @Override
    public LastUpdateDate getUpdateDate() {
        LOG.info("fetching last update date of crime api");
        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
                crimeApiUrl + UPDATE_DATE,
                HttpMethod.GET,
                null, new ParameterizedTypeReference<LastUpdateDate>() {
                });
        return response.getBody();
    }
}
