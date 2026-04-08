package com.upes.campusdelivery.admin.dto;

import java.util.List;

public record AdminUserListResponse(
    List<AdminUserView> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
