package com.stodo.projectchaos.vectorization.handler;

import com.stodo.projectchaos.kafka.VectorizationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@KafkaListener(topics = "attachment.vectorization.requested")
public class VectorizationMessageHandler {

    @KafkaHandler
    public void handle(VectorizationMessage vectorizationMessage) {
        log.info("Message received! {}", vectorizationMessage);
    }
}
