package com.upes.campusdelivery.admin.service;

import com.upes.campusdelivery.admin.dto.AdminAuditListResponse;
import com.upes.campusdelivery.admin.dto.AdminAuditView;
import com.upes.campusdelivery.admin.dto.AdminDashboardResponse;
import com.upes.campusdelivery.admin.dto.AdminToggleResponse;
import com.upes.campusdelivery.admin.dto.AdminUserListResponse;
import com.upes.campusdelivery.admin.dto.AdminUserView;
import com.upes.campusdelivery.audit.AuditService;
import com.upes.campusdelivery.common.exceptions.AppException;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;

    public AdminService(JdbcTemplate jdbcTemplate, AuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        long totalUsers = count("SELECT COUNT(*) FROM users");
        long activeStudents = count("SELECT COUNT(*) FROM users WHERE role = 'STUDENT' AND is_active = TRUE");
        long activeVendors = count("SELECT COUNT(*) FROM users WHERE role = 'VENDOR' AND is_active = TRUE");
        long activeProducts = count("SELECT COUNT(*) FROM products WHERE is_active = TRUE");
        long totalOrders = count("SELECT COUNT(*) FROM orders");
        long auditEvents = count("SELECT COUNT(*) FROM audit_logs");
        return new AdminDashboardResponse(totalUsers, activeStudents, activeVendors, activeProducts, totalOrders, auditEvents);
    }

    @Transactional(readOnly = true)
    public AdminUserListResponse listUsers(String role, Boolean activeOnly, int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        long offset = (long) safePage * safeSize;

        StringBuilder where = new StringBuilder(" WHERE 1 = 1");
        java.util.ArrayList<Object> params = new java.util.ArrayList<>();

        if (role != null && !role.isBlank()) {
            where.append(" AND role = ?");
            params.add(role.strip().toUpperCase());
        }
        if (activeOnly != null) {
            where.append(" AND is_active = ?");
            params.add(activeOnly);
        }

        Long totalElements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users" + where,
            Long.class,
            params.toArray()
        );
        long total = totalElements == null ? 0 : totalElements;

        String sql = """
            SELECT id, username, full_name, role, is_active, email, phone, created_at
            FROM users
            """ + where + " ORDER BY created_at DESC LIMIT ? OFFSET ?";
        java.util.ArrayList<Object> queryParams = new java.util.ArrayList<>(params);
        queryParams.add(safeSize);
        queryParams.add(offset);

        List<AdminUserView> items = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new AdminUserView(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getBoolean("is_active"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getTimestamp("created_at").toInstant()
            ),
            queryParams.toArray()
        );

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new AdminUserListResponse(items, safePage, safeSize, total, totalPages);
    }

    @Transactional(readOnly = true)
    public AdminAuditListResponse listAuditLogs(int page, int size) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        long offset = (long) safePage * safeSize;

        long total = count("SELECT COUNT(*) FROM audit_logs");

        List<AdminAuditView> items = jdbcTemplate.query(
            """
            SELECT id, actor_username, actor_role, action, entity_type, entity_id, trace_id, metadata_json, created_at
            FROM audit_logs
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """,
            (rs, rowNum) -> new AdminAuditView(
                rs.getLong("id"),
                rs.getString("actor_username"),
                rs.getString("actor_role"),
                rs.getString("action"),
                rs.getString("entity_type"),
                rs.getObject("entity_id") == null ? null : rs.getLong("entity_id"),
                rs.getString("trace_id"),
                rs.getString("metadata_json"),
                rs.getTimestamp("created_at").toInstant()
            ),
            safeSize,
            offset
        );

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new AdminAuditListResponse(items, safePage, safeSize, total, totalPages);
    }

    @Transactional
    public AdminToggleResponse setUserActive(String actorUsername, String actorRole, Long userId, boolean active, String traceId, String reason) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = ?", Integer.class, userId);
        if (count == null || count == 0) {
            throw new AppException("USER_NOT_FOUND", "Requested user was not found.", HttpStatus.NOT_FOUND);
        }

        jdbcTemplate.update("UPDATE users SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", active, userId);
        auditService.record(actorUsername, actorRole, active ? "ACTIVATE_USER" : "DEACTIVATE_USER", "USER", userId, traceId, java.util.Map.of("reason", reason));
        log.info("admin-user-active-updated userId={} active={} traceId={}", userId, active, traceId);
        return new AdminToggleResponse("USER", userId, active, reason, Instant.now());
    }

    @Transactional
    public AdminToggleResponse setProductActive(String actorUsername, String actorRole, Long productId, boolean active, String traceId, String reason) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM products WHERE id = ?", Integer.class, productId);
        if (count == null || count == 0) {
            throw new AppException("PRODUCT_NOT_FOUND", "Requested product was not found.", HttpStatus.NOT_FOUND);
        }

        jdbcTemplate.update("UPDATE products SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", active, productId);
        auditService.record(actorUsername, actorRole, active ? "ACTIVATE_PRODUCT" : "DEACTIVATE_PRODUCT", "PRODUCT", productId, traceId, java.util.Map.of("reason", reason));
        log.info("admin-product-active-updated productId={} active={} traceId={}", productId, active, traceId);
        return new AdminToggleResponse("PRODUCT", productId, active, reason, Instant.now());
    }

    private long count(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result == null ? 0 : result;
    }
}
