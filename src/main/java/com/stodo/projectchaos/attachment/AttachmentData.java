package com.stodo.projectchaos.attachment;

import java.util.UUID;

public record AttachmentData(
        UUID id,
        String extractedText,
        String originalName,
        long fileSizeInBytes,
        UUID taskId,
        UUID projectId
) {}
