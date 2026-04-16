package com.stodo.projectchaos.embedding;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class AttachmentEmbeddingRepository {

    private static final String INSERT_SQL = """
            INSERT INTO attachment_embeddings (id, attachment_id, task_id, project_id, chunk_index, content, metadata, embedding, created_date)
            VALUES (:id, :attachmentId, :taskId, :projectId, :chunkIndex, :content, :metadata::jsonb, :embedding::vector, NOW())
            """;

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AttachmentEmbeddingRepository(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void saveAll(List<AttachmentEmbeddingData> embeddings) {
        SqlParameterSource[] params = embeddings.stream()
                .map(this::toParams)
                .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(INSERT_SQL, params);
    }

    private SqlParameterSource toParams(AttachmentEmbeddingData data) {
        try {
            return new MapSqlParameterSource()
                    .addValue("id", UUID.randomUUID())
                    .addValue("attachmentId", data.attachmentId())
                    .addValue("taskId", data.taskId())
                    .addValue("projectId", data.projectId())
                    .addValue("chunkIndex", data.chunkIndex())
                    .addValue("content", data.content())
                    .addValue("metadata", objectMapper.writeValueAsString(data.metadata()))
                    .addValue("embedding", toVectorString(data.embedding()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to map embedding params for attachment " + data.attachmentId(), e);
        }
    }

    private String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
