package com.upes.campusdelivery.vendor.dto;

import jakarta.validation.constraints.NotBlank;

public record VendorOrderStatusUpdateRequest(
    @NotBlank(message = "Status is required")
    String status
) {}
