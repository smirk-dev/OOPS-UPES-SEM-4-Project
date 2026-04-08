package com.upes.campusdelivery.auth.dto;

import com.upes.campusdelivery.common.enums.Role;

public record LoginResponse(
    Long userId,
    String username,
    Role role,
    String token,
    long expiresInSeconds
) {}
