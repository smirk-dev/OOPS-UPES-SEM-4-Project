package com.upes.campusdelivery.audit;

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

    public AuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        if (metadata == null || metadata.isEmpty()) {
            return "{}";
        }

        StringBuilder builder = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ?> entry : metadata.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"').append(escape(entry.getKey())).append('"').append(':');
            Object value = entry.getValue();
            if (value == null) {
                builder.append("null");
            } else if (value instanceof Number || value instanceof Boolean) {
                builder.append(value);
            } else {
                builder.append('"').append(escape(String.valueOf(value))).append('"');
            }
        }
        builder.append('}');
        return builder.toString();
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
