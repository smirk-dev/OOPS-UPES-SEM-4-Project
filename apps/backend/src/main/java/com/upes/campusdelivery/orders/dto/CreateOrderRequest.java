package com.upes.campusdelivery.orders.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "Zone id is required")
    Long zoneId,

    @NotEmpty(message = "At least one item is required")
    @Valid
    List<CreateOrderItemRequest> items
) {}
