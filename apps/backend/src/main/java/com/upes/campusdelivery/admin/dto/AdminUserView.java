package com.upes.campusdelivery.admin.dto;

import java.time.Instant;

public record AdminUserView(
    Long userId,
    String username,
    String fullName,
    String role,
    boolean active,
    String email,
    String phone,
    Instant createdAt
) {}
