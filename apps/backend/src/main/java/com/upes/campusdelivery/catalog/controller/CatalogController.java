package com.upes.campusdelivery.catalog.controller;

import com.upes.campusdelivery.catalog.dto.CatalogListResponse;
import com.upes.campusdelivery.catalog.dto.CatalogProductDetail;
import com.upes.campusdelivery.catalog.service.CatalogService;
import com.upes.campusdelivery.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<CatalogListResponse>> listProducts(
        @RequestParam(required = false) String vertical,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String stockStatus,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        HttpServletRequest request
    ) {
        CatalogListResponse response = catalogService.listProducts(vertical, category, stockStatus, page, size);
        return ResponseEntity.ok(ApiResponse.ok(response, traceId(request)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<CatalogProductDetail>> getProduct(
        @PathVariable Long productId,
        HttpServletRequest request
    ) {
        CatalogProductDetail response = catalogService.getProductById(productId);
        return ResponseEntity.ok(ApiResponse.ok(response, traceId(request)));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        return traceId == null || traceId.isBlank() ? "n/a" : traceId;
    }
}
