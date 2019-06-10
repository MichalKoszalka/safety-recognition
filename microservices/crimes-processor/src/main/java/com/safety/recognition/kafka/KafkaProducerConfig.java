package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndNeighbourhood;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaProducerConfig {

    @Value("${bootstrap.address}")
    private String bootstrapAddress;

    private <T> ProducerFactory<UUID, T> producerFactory(Class<? extends Serializer> serializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
            bootstrapAddress);
        configProps.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        configProps.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            serializer);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<UUID, String> stringKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(StringSerializer.class));
    }

    @Bean
    public KafkaTemplate<UUID, StreetAndNeighbourhood> streetAndNeighbourhoodKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(JsonSerializer.class));
    }

    @Bean
    public KafkaTemplate<UUID, StreetAndCategory> streetAndCategoryKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(JsonSerializer.class));
    }

    @Bean
    public KafkaTemplate<UUID, NeighbourhoodAndCategory> neighbourhoodAndCategoryKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(JsonSerializer.class));
    }
}