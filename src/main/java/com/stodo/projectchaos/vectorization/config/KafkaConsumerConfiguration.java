package com.stodo.projectchaos.vectorization.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;

@Configuration
public class KafkaConsumerConfiguration {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.key-deserializer}")
    private String keyDeserializer;

    @Value("${spring.kafka.consumer.value-deserializer}")
    private String valueDeserializer;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        HashMap<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put("schema.registry.url", schemaRegistryUrl);


        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Object>>
    kafkaListenerContainerFactory(KafkaTemplate<String, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
//        factory.setConcurrency(3);
//        factory.getContainerProperties().setPollTimeout(3000);
//
//        // will send the message to the same topic it got it from but ending with .DLT - product-created-events-topic.DLT
//        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
//                new DeadLetterPublishingRecoverer(kafkaTemplate),
//                new FixedBackOff(5000, 3)
//        );
//        errorHandler.addRetryableExceptions(RetryableException.class);
//        errorHandler.addNotRetryableExceptions(NotRetryableException.class);
//
//        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

}