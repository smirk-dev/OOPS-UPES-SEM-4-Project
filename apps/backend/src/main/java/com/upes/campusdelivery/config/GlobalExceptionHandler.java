package com.upes.campusdelivery.config;

import com.upes.campusdelivery.common.api.ApiError;
import com.upes.campusdelivery.common.api.ApiResponse;
import com.upes.campusdelivery.common.exceptions.AppException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(AppException exception, HttpServletRequest request) {
        log.warn("app-exception traceId={} code={} message={}", traceId(request), exception.getCode(), exception.getMessage());
        ApiError error = new ApiError(exception.getCode(), exception.getMessage(), List.of());
        return ResponseEntity
            .status(exception.getStatus())
            .body(ApiResponse.fail(error, traceId(request)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        List<String> details = exception.getBindingResult()
            .getAllErrors()
            .stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
                }
                return error.getDefaultMessage();
            })
            .collect(Collectors.toList());

        ApiError error = new ApiError("VALIDATION_FAILED", "Please check your input and try again.", details);
        return ResponseEntity.badRequest().body(ApiResponse.fail(error, traceId(request)));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception, HttpServletRequest request) {
        List<String> details = exception.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();

        ApiError error = new ApiError("VALIDATION_FAILED", "Please check your input and try again.", details);
        return ResponseEntity.badRequest().body(ApiResponse.fail(error, traceId(request)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnknown(Exception exception, HttpServletRequest request) {
        log.error("unhandled-exception traceId={}", traceId(request), exception);
        ApiError error = new ApiError(
            "INTERNAL_SERVER_ERROR",
            "Something went wrong. Please try again.",
            List.of()
        );
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.fail(error, traceId(request)));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        return traceId == null || traceId.isBlank() ? "n/a" : traceId;
    }
}
