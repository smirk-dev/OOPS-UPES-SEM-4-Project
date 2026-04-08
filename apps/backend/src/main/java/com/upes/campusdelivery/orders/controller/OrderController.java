package com.upes.campusdelivery.orders.controller;

import com.upes.campusdelivery.common.api.ApiResponse;
import com.upes.campusdelivery.orders.dto.CreateOrderRequest;
import com.upes.campusdelivery.orders.dto.CreateOrderResponse;
import com.upes.campusdelivery.orders.dto.OrderDetailResponse;
import com.upes.campusdelivery.orders.dto.OrderListResponse;
import com.upes.campusdelivery.orders.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
        @AuthenticationPrincipal String username,
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody CreateOrderRequest request,
        HttpServletRequest httpRequest
    ) {
        String traceId = traceId(httpRequest);
        CreateOrderResponse response = orderService.createOrder(username, idempotencyKey, request, traceId);
        return ResponseEntity.ok(ApiResponse.ok(response, traceId));
    }

    @GetMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<OrderListResponse>> listStudentOrders(
        @AuthenticationPrincipal String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest httpRequest
    ) {
        OrderListResponse response = orderService.listStudentOrders(username, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, traceId(httpRequest)));
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getStudentOrderDetail(
        @AuthenticationPrincipal String username,
        @PathVariable Long orderId,
        HttpServletRequest httpRequest
    ) {
        OrderDetailResponse response = orderService.getStudentOrderDetail(username, orderId);
        return ResponseEntity.ok(ApiResponse.ok(response, traceId(httpRequest)));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        return traceId == null || traceId.isBlank() ? "n/a" : traceId;
    }
}
