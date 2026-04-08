package com.upes.campusdelivery.catalog.dto;

import java.util.List;

public record CatalogListResponse(
    List<CatalogProductSummary> items,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
