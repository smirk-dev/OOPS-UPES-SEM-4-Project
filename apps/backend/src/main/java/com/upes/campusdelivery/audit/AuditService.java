package com.upes.campusdelivery.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuditService(JdbcTemplate jdbcTemplate) {
        this(jdbcTemplate, new ObjectMapper());
    }

    public AuditService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void record(String actorUsername, String actorRole, String action, String entityType, Long entityId, String traceId, Map<String, ?> metadata) {
        String metadataJson = toJson(metadata);

        try {
            jdbcTemplate.update(
                """
                INSERT INTO audit_logs (actor_username, actor_role, action, entity_type, entity_id, trace_id, metadata_json)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                actorUsername,
                actorRole,
                action,
                entityType,
                entityId,
                traceId,
                metadataJson
            );
        } catch (DataAccessException exception) {
            log.warn("audit-write-failed action={} entityType={} entityId={} traceId={}", action, entityType, entityId, traceId, exception);
        }
    }

    public void record(String actorUsername, String actorRole, String action, String entityType, Long entityId, String traceId) {
        record(actorUsername, actorRole, action, entityType, entityId, traceId, Map.of());
    }

    private String toJson(Map<String, ?> metadata) {
        Map<String, ?> safeMetadata = metadata == null ? Map.of() : metadata;
        try {
            return objectMapper.writeValueAsString(safeMetadata);
        } catch (JsonProcessingException exception) {
            log.warn("audit-metadata-serialize-failed", exception);
            return "{}";
        }
    }
}
