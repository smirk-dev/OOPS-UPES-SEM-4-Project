package com.upes.campusdelivery.vendor.dto;

import jakarta.validation.constraints.NotBlank;

public record VendorStockUpdateRequest(
    @NotBlank(message = "Stock status is required")
    String stockStatus
) {}
