package com.upes.campusdelivery.vendor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record VendorFlashDiscountRequest(
    @NotNull(message = "Flash discount percent is required")
    @DecimalMin(value = "0.0", message = "Flash discount percent must be non-negative")
    BigDecimal flashDiscountPercent
) {}
