package com.safety.recognition.client;

import com.safety.recognition.cassandra.model.Neighbourhood;
import com.safety.recognition.cassandra.model.Point;
import data.police.uk.model.crime.Crime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private static final Logger LOG = LoggerFactory.getLogger(CrimeClient.class);

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private static final String CRIME_PATH = "crimes-street/all-crime";

    @Autowired
    public CrimeClientImpl() {
    }

    @Override
    public List<Crime> getCrimes(Neighbourhood neighbourhood, LocalDate updateDate) {
            LOG.info(String.format("fetching crimes for neighbourhood: %s:%d and date: %s. Number of boundary points: %s", neighbourhood.getName(), neighbourhood.getNumericRepresentation(), updateDate, neighbourhood.getBoundary().size()));
            var restTemplate = new RestTemplate();
            var response = restTemplate.exchange(
                    new RequestPathBuilder(crimeApiUrl).withMethod(CRIME_PATH).withQueryParam("poly", neighbourhood.getBoundary().stream().map(Point::toString).collect(Collectors.joining(":"))).withQueryParam("date", updateDate.format(DateTimeFormatter.ofPattern("YYYY-MM"))).build(),
                    HttpMethod.GET,
                    null, new ParameterizedTypeReference<List<Crime>>() {
                    });
            return enrichWithNeighbourhood(response.getBody(), neighbourhood.getName());
    }

    private List<Crime> enrichWithNeighbourhood(List<Crime> crimes, String neighbourhood) {
        crimes.forEach(crime -> crime.setNeighbourhood(neighbourhood));
        return crimes;
    }
}
