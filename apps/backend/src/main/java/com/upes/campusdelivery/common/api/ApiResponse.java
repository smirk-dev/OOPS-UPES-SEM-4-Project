package com.upes.campusdelivery.common.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    String traceId,
    Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId, Instant.now());
    }

    public static <T> ApiResponse<T> fail(ApiError error, String traceId) {
        return new ApiResponse<>(false, null, error, traceId, Instant.now());
    }
}
