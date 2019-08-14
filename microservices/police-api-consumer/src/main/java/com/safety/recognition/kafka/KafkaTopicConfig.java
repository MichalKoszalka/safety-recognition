package com.safety.recognition.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    @Value(value = "${bootstrap.address}")
    private String bootstrapAddress;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        return new KafkaAdmin(configs);
    }

    @Bean
    public NewTopic neighbourhoodsFetched() {
        return new NewTopic("neighbourhoods_fetched", 1, (short) 3);
    }

    @Bean
    public NewTopic startFetchingCrimeData() {
        return new NewTopic("start_fetching_crime_data", 1, (short) 3);
    }

    @Bean
    public NewTopic fetchCrimes() {
        return new NewTopic("fetch_crimes", 1, (short) 3);
    }

    @Bean
    public NewTopic calculateIndexesForLondon() {
        return new NewTopic("calculate_indexes_for_london", 5, (short) 3);
    }

    @Bean
    public NewTopic calculateIndexesForLondonByCategory() {
        return new NewTopic("calculate_indexes_for_london_by_category", 5, (short) 3);
    }

    @Bean
    public NewTopic calculateIndexesByStreet() {
        return new NewTopic("calculate_indexes_by_street", 5, (short) 3);
    }

    @Bean
    public NewTopic calculateIndexesByStreetAndCategory() {
        return new NewTopic("calculate_indexes_by_street_and_category", 5, (short) 3);
    }

    @Bean
    public NewTopic calculateIndexesByNeighbourhood() {
        return new NewTopic("calculate_indexes_by_neighbourhood", 5, (short) 3);
    }

    @Bean
    public NewTopic calculateIndexesByNeighbourhoodAndCategory() {
        return new NewTopic("calculate_indexes_by_neighbourhood_and_category", 5, (short) 3);
    }

    @Bean
    public NewTopic calculatePrediction() {
        return new NewTopic("calculate_prediction", 5, (short) 3);
    }

    @Bean
    public NewTopic newCrimes() {
        return new NewTopic("new_crimes", 2, (short) 3);
    }
}