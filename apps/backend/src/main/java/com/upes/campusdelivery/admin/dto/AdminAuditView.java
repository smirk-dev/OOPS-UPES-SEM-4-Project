package com.upes.campusdelivery.admin.dto;

import java.time.Instant;

public record AdminAuditView(
    Long auditId,
    String actorUsername,
    String actorRole,
    String action,
    String entityType,
    Long entityId,
    String traceId,
    String metadataJson,
    Instant createdAt
) {}
