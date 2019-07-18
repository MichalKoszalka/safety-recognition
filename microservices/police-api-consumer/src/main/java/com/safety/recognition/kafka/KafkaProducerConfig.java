package com.safety.recognition.kafka;

import com.safety.recognition.kafka.messages.Crimes;
import data.police.uk.model.crime.Crime;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${bootstrap.address}")
    private String bootstrapAddress;

    private <V, T> ProducerFactory<V, T> producerFactory(Class<? extends Serializer> keySerializer, Class<? extends Serializer> valueSerializer) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        configProps.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        configProps.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                valueSerializer);
        configProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Crimes> crimesFetchedMessageProducer() {
        return new KafkaTemplate<>(producerFactory(StringSerializer.class, JsonSerializer.class));
    }

    @Bean
    public KafkaTemplate<String, String> predictMessageProducer() {
        return new KafkaTemplate<>(producerFactory(StringSerializer.class, StringSerializer.class));
    }

}
