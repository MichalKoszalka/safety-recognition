package com.safety.recognition.kafka;

import com.safety.recognition.cassandra.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.cassandra.kafka.messages.StreetAndCategory;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.UUIDDeserializer;
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
import java.util.UUID;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${bootstrap.address}")
    private String bootstrapAddress;

    @Value("${indexes.calculators_consumer.group.id}")
    private String groupId;

    private <T> ConsumerFactory<UUID, T> consumerFactory(Deserializer<T> valueDeserializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        return new DefaultKafkaConsumerFactory<>(props, new UUIDDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, String> kafkaCalculateIndexesStringListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new StringDeserializer()));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, NeighbourhoodAndCategory> kafkaCalculateIndexesNeighbourhoodAndCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, NeighbourhoodAndCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer(NeighbourhoodAndCategory.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<UUID, StreetAndCategory> kafkaCalculateIndexesStreetAndCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<UUID, StreetAndCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer(StreetAndCategory.class)));
        return factory;
    }
}
