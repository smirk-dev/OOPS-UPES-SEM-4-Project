package com.upes.campusdelivery.vendor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record VendorProductUpsertRequest(
    @NotBlank(message = "Product name is required")
    String name,

    String description,

    @NotBlank(message = "Category is required")
    String category,

    @NotBlank(message = "Vertical is required")
    String vertical,

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.01", message = "MRP must be positive")
    BigDecimal mrp,

    @NotNull(message = "Current price is required")
    @DecimalMin(value = "0.01", message = "Current price must be positive")
    BigDecimal currentPrice,

    @NotBlank(message = "Stock status is required")
    String stockStatus,

    Boolean active
) {}
