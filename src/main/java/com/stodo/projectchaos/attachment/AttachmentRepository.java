package com.stodo.projectchaos.attachment;

import com.stodo.projectchaos.model.enums.VectorStatusEnum;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AttachmentRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AttachmentRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AttachmentData> findById(UUID id) {
        String sql = """
                SELECT id, extracted_text, original_name, file_size_in_bytes, task_id, project_id, vector_status
                FROM attachments
                WHERE id = :id
                FOR UPDATE
                """;

        return jdbcTemplate.query(sql,
                        new MapSqlParameterSource("id", id),
                        (rs, rowNum) -> new AttachmentData(
                                rs.getObject("id", UUID.class),
                                rs.getString("extracted_text"),
                                rs.getString("original_name"),
                                rs.getLong("file_size_in_bytes"),
                                rs.getObject("task_id", UUID.class),
                                rs.getObject("project_id", UUID.class),
                                VectorStatusEnum.valueOf(rs.getString("vector_status"))
                        ))
                .stream()
                .findFirst();
    }

    public void updateVectorStatus(UUID id, VectorStatusEnum status) {
        String sql = "UPDATE attachments SET vector_status = :status WHERE id = :id";

        jdbcTemplate.update(sql, new MapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status.name()));
    }
}
