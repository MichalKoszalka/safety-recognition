package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.CrimeCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class CrimeCategoryClientImpl implements CrimeCategoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(CrimeClient.class);

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String CRIME_CATEGORIES_PATH = "crime-categories";

    @Autowired
    public CrimeCategoryClientImpl() {
    }

    @Override
    public List<CrimeCategory> getCrimeCategories() {
        LOG.info(String.format("fetching crimes categories"));
        var restTemplate = new RestTemplate();
        var response = restTemplate.exchange(
                new RequestPathBuilder(crimeApiUrl).withMethod(CRIME_CATEGORIES_PATH).build(),
                HttpMethod.GET,
                null, new ParameterizedTypeReference<List<CrimeCategory>>() {
                });
        return response.getBody();
    }
}
