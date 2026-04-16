package com.stodo.projectchaos.embedding;

import com.stodo.projectchaos.attachment.AttachmentData;
import com.stodo.projectchaos.attachment.AttachmentRepository;
import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class AttachmentEmbeddingService {

    private final AttachmentRepository attachmentRepository;
    private final AttachmentEmbeddingRepository attachmentEmbeddingRepository;
    private final EmbeddingModel embeddingModel;
    private final AppTokenTextSplitter tokenTextSplitter;

    public AttachmentEmbeddingService(AttachmentRepository attachmentRepository, AttachmentEmbeddingRepository attachmentEmbeddingRepository, EmbeddingModel embeddingModel, AppTokenTextSplitter tokenTextSplitter) {
        this.attachmentRepository = attachmentRepository;
        this.attachmentEmbeddingRepository = attachmentEmbeddingRepository;
        this.embeddingModel = embeddingModel;
        this.tokenTextSplitter = tokenTextSplitter;
    }

    @Transactional
    public void embedAttachment(UUID attachmentId) {
        try {
            AttachmentData attachment = attachmentRepository.findById(attachmentId);
            Document document = new Document(attachment.extractedText());

            List<String> chunks = tokenTextSplitter.split(List.of(document))
                    .stream()
                    .map(Document::getText)
                    .map(text -> text.replaceAll("\\s{2,}", " ").trim())
                    .collect(toList());

            List<float[]> vectors = embeddingModel.embed(chunks);

            List<AttachmentEmbeddingData> embeddings = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                HashMap<String, Object> metadata = new HashMap<>();
                metadata.put("projectId", attachment.projectId());
                metadata.put("fileSizeInBytes", attachment.fileSizeInBytes());
                metadata.put("filename", attachment.originalName());
                if (attachment.taskId() != null) {
                    metadata.put("taskId", attachment.taskId());
                }

                embeddings.add(new AttachmentEmbeddingData(
                        attachmentId,
                        attachment.projectId(),
                        attachment.taskId(),
                        i,
                        chunks.get(i),
                        metadata,
                        vectors.get(i)
                ));
            }

            attachmentEmbeddingRepository.saveAll(embeddings);
            attachmentRepository.updateVectorStatus(attachmentId, VectorStatusEnum.PROCESSED);
            log.info("Embedding created for attachmentId: {}", attachmentId);
        } catch (Exception e) {
            log.error("Failed to embed attachment {}", attachmentId, e);
            markAsFailed(attachmentId);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsFailed(UUID attachmentId) {
        attachmentRepository.updateVectorStatus(attachmentId, VectorStatusEnum.FAILED);
    }
}
