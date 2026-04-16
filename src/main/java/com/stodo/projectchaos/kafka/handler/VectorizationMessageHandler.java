package com.stodo.projectchaos.kafka.handler;

import com.stodo.projectchaos.embedding.AttachmentEmbeddingService;
import com.stodo.projectchaos.kafka.VectorizationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@KafkaListener(topics = "attachment-vectorization-requested")
public class VectorizationMessageHandler {

    private final AttachmentEmbeddingService attachmentEmbeddingService;

    public VectorizationMessageHandler(AttachmentEmbeddingService attachmentEmbeddingService) {
        this.attachmentEmbeddingService = attachmentEmbeddingService;
    }

    @KafkaHandler
    public void handle(VectorizationMessage message) {
        log.info("Embedding requested for attachment {}", message.getAttachmentId());
        attachmentEmbeddingService.embedAttachment(message.getAttachmentId());
    }
}
