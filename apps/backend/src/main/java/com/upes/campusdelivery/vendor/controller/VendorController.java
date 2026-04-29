package com.upes.campusdelivery.vendor.controller;

import com.upes.campusdelivery.common.api.ApiResponse;
import com.upes.campusdelivery.vendor.dto.VendorDashboardResponse;
import com.upes.campusdelivery.vendor.dto.VendorFlashDiscountRequest;
import com.upes.campusdelivery.vendor.dto.VendorOrderDetail;
import com.upes.campusdelivery.vendor.dto.VendorOrderListResponse;
import com.upes.campusdelivery.vendor.dto.VendorProductListResponse;
import com.upes.campusdelivery.vendor.dto.VendorProductResponse;
import com.upes.campusdelivery.vendor.dto.VendorProductUpsertRequest;
import com.upes.campusdelivery.vendor.dto.VendorOrderStatusUpdateRequest;
import com.upes.campusdelivery.vendor.dto.VendorOrderStatusUpdateResponse;
import com.upes.campusdelivery.vendor.dto.VendorStockUpdateRequest;
import com.upes.campusdelivery.vendor.service.VendorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vendor")
@PreAuthorize("hasRole('VENDOR')")
public class VendorController {

    private final VendorService vendorService;

    public VendorController(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<VendorDashboardResponse>> dashboard(
        @AuthenticationPrincipal String username,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.getDashboard(username), traceId(request)));
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<VendorProductListResponse>> products(
        @AuthenticationPrincipal String username,
        @RequestParam(required = false) String stockStatus,
        @RequestParam(required = false) Boolean activeOnly,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.listProducts(username, stockStatus, activeOnly, page, size), traceId(request)));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse<VendorProductResponse>> createProduct(
        @AuthenticationPrincipal String username,
        @Valid @RequestBody VendorProductUpsertRequest body,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(vendorService.createProduct(username, body, traceId), traceId));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<VendorProductResponse>> updateProduct(
        @AuthenticationPrincipal String username,
        @PathVariable Long productId,
        @Valid @RequestBody VendorProductUpsertRequest body,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(vendorService.updateProduct(username, productId, body, traceId), traceId));
    }

    @PatchMapping("/products/{productId}/stock")
    public ResponseEntity<ApiResponse<VendorProductResponse>> updateStock(
        @AuthenticationPrincipal String username,
        @PathVariable Long productId,
        @Valid @RequestBody VendorStockUpdateRequest body,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(vendorService.updateStock(username, productId, body, traceId), traceId));
    }

    @PatchMapping("/products/{productId}/flash-discount")
    public ResponseEntity<ApiResponse<VendorProductResponse>> updateFlashDiscount(
        @AuthenticationPrincipal String username,
        @PathVariable Long productId,
        @Valid @RequestBody VendorFlashDiscountRequest body,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(vendorService.updateFlashDiscount(username, productId, body, traceId), traceId));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<VendorOrderListResponse>> orders(
        @AuthenticationPrincipal String username,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.listOrders(username, page, size), traceId(request)));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<VendorOrderStatusUpdateResponse>> updateOrderStatus(
        @AuthenticationPrincipal String username,
        @PathVariable Long orderId,
        @Valid @RequestBody VendorOrderStatusUpdateRequest body,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(vendorService.updateOrderStatus(username, orderId, body, traceId), traceId));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<VendorOrderDetail>> orderDetail(
        @AuthenticationPrincipal String username,
        @PathVariable Long orderId,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(vendorService.getOrderDetail(username, orderId), traceId(request)));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        return traceId == null || traceId.isBlank() ? "n/a" : traceId;
    }
}
