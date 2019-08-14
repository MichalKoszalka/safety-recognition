package com.safety.recognition.kafka;

import com.safety.recognition.kafka.messages.NeighbourhoodAndCategory;
import com.safety.recognition.kafka.messages.StreetAndCategory;
import com.safety.recognition.kafka.messages.StreetAndNeighbourhood;
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

    @Value("${indexes.calculators_consumer.group.id}")
    private String groupId;

    private <T> ConsumerFactory<String, T> consumerFactory(Deserializer<T> valueDeserializer) {
        Map<String, Object> props = new HashMap<>();
        props.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapAddress);
        props.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                groupId);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaCalculateIndexesStringListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new StringDeserializer()));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, NeighbourhoodAndCategory> kafkaCalculateIndexesNeighbourhoodAndCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, NeighbourhoodAndCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer(NeighbourhoodAndCategory.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StreetAndCategory> kafkaCalculateIndexesStreetAndCategoryListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StreetAndCategory> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer(StreetAndCategory.class)));
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, StreetAndNeighbourhood> kafkaCalculateIndexesStreetAndNeighbourhoodListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, StreetAndNeighbourhood> factory
                = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory(new JsonDeserializer(StreetAndNeighbourhood.class)));
        return factory;
    }

}
