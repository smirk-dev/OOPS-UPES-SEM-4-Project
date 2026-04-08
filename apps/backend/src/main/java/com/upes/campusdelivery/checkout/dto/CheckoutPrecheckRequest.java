package com.upes.campusdelivery.checkout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CheckoutPrecheckRequest(
    @NotNull(message = "Zone id is required")
    Long zoneId,

    @NotEmpty(message = "At least one cart item is required")
    @Valid
    List<CheckoutItemRequest> items
) {}
