package com.upes.campusdelivery.admin.dto;

import java.util.List;

public record AdminAuditListResponse(
    List<AdminAuditView> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
