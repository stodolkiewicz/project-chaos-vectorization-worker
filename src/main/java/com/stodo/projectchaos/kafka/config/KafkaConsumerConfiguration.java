package com.stodo.projectchaos.kafka.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class KafkaConsumerConfiguration {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String consumerGroupId;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.consumer.properties.specific.avro.reader}")
    private boolean ifSpecificAvroReader;

    @Bean
    public ConsumerFactory<Void, Object> consumerFactory() {
        HashMap<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put("schema.registry.url", schemaRegistryUrl);
        config.put("specific.avro.reader", ifSpecificAvroReader);

        ErrorHandlingDeserializer<Void> keyDeserializer = new ErrorHandlingDeserializer<>(new VoidDeserializer());
        ErrorHandlingDeserializer<Object> valueDeserializer = new ErrorHandlingDeserializer<>(new KafkaAvroDeserializer());

        return new DefaultKafkaConsumerFactory<>(config, keyDeserializer, valueDeserializer);
    }

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Void, Object>>
    kafkaListenerContainerFactory(KafkaTemplate<Object, Object> kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<Void, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        // creates 3 consumer threads, each reading from a different partition
        factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(5000);

        // will send the message to the same topic it got it from but ending with -dlt
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> {
                    log.error("Record failed after retries, sending to DLT. topic={}", record.topic(), exception);
                    recoverer.accept(record, exception);
                },
                new FixedBackOff(5000, 5)
        );

        factory.setCommonErrorHandler(errorHandler);

        return factory;
    }

    @Bean
    ProducerFactory<Object, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<Object, Object> dltRawTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

}