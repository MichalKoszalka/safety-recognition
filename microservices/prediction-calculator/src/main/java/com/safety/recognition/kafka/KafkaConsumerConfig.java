package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.model.indexes.*;
import data.police.uk.model.crime.Crime;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${bootstrap.address}")
    private String bootstrapAddress;

    @Value("${prediction.calculators_consumer.group.id}")
    private String groupId;

    private <T> ConsumerFactory<String, T> consumerFactory(Deserializer<T> deserializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaCalculatePredictionListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new StringDeserializer()));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CrimeLevel> kafkaTrainPredictionModelForLondonListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrimeLevel> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer<>(CrimeLevel.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByCategory> kafkaTrainPredictionModelByCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer<>(CrimeLevelByCategory.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByNeighbourhood> kafkaTrainPredictionModelByNeighbourhoodListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByNeighbourhood> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer<>(CrimeLevelByNeighbourhood.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByNeighbourhoodAndCategory> kafkaTrainPredictionModelByNeighbourhoodAndCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByNeighbourhoodAndCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer<>(CrimeLevelByNeighbourhoodAndCategory.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByStreet> kafkaTrainPredictionModelByStreetListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByStreet> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer<>(CrimeLevelByStreet.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByStreetAndCategory> kafkaTrainPredictionModelByStreetAndCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CrimeLevelByStreetAndCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer<>(CrimeLevelByStreetAndCategory.class)));
        return factory;
    }

}
