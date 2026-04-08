package com.upes.campusdelivery.admin.controller;

import com.upes.campusdelivery.admin.dto.AdminAuditListResponse;
import com.upes.campusdelivery.admin.dto.AdminDashboardResponse;
import com.upes.campusdelivery.admin.dto.AdminToggleResponse;
import com.upes.campusdelivery.admin.dto.AdminUserListResponse;
import com.upes.campusdelivery.admin.service.AdminService;
import com.upes.campusdelivery.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> dashboard(HttpServletRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getDashboard(), traceId(request)));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserListResponse>> users(
        @RequestParam(required = false) String role,
        @RequestParam(required = false) Boolean activeOnly,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listUsers(role, activeOnly, page, size), traceId(request)));
    }

    @GetMapping("/audits")
    public ResponseEntity<ApiResponse<AdminAuditListResponse>> audits(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        HttpServletRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.listAuditLogs(page, size), traceId(request)));
    }

    @PatchMapping("/users/{userId}/active")
    public ResponseEntity<ApiResponse<AdminToggleResponse>> toggleUser(
        @AuthenticationPrincipal String username,
        @PathVariable Long userId,
        @RequestParam boolean active,
        @RequestParam(required = false, defaultValue = "Admin moderation action") String reason,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(
            adminService.setUserActive(username, "ADMIN", userId, active, traceId, reason),
            traceId
        ));
    }

    @PatchMapping("/products/{productId}/active")
    public ResponseEntity<ApiResponse<AdminToggleResponse>> toggleProduct(
        @AuthenticationPrincipal String username,
        @PathVariable Long productId,
        @RequestParam boolean active,
        @RequestParam(required = false, defaultValue = "Admin moderation action") String reason,
        HttpServletRequest request
    ) {
        String traceId = traceId(request);
        return ResponseEntity.ok(ApiResponse.ok(
            adminService.setProductActive(username, "ADMIN", productId, active, traceId, reason),
            traceId
        ));
    }

    private String traceId(HttpServletRequest request) {
        String traceId = request.getHeader("X-Request-Id");
        return traceId == null || traceId.isBlank() ? "n/a" : traceId;
    }
}
