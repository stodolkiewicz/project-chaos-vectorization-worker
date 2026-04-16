package com.stodo.projectchaos.embedding;

import java.util.Map;
import java.util.UUID;

public record AttachmentEmbeddingData(
        UUID attachmentId,
        UUID projectId,
        UUID taskId,
        int chunkIndex,
        String content,
        Map<String, Object> metadata,
        float[] embedding
) {}
