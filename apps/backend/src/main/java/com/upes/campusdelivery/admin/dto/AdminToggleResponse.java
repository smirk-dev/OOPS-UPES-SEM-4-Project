package com.upes.campusdelivery.admin.dto;

import java.time.Instant;

public record AdminToggleResponse(
    String entityType,
    Long entityId,
    boolean active,
    String reason,
    Instant updatedAt
) {}
