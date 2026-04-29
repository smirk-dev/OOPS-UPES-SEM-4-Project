package com.upes.campusdelivery.vendor.service;

import com.upes.campusdelivery.audit.AuditService;
import com.upes.campusdelivery.common.exceptions.AppException;
import com.upes.campusdelivery.vendor.dto.VendorDashboardResponse;
import com.upes.campusdelivery.vendor.dto.VendorFlashDiscountRequest;
import com.upes.campusdelivery.vendor.dto.VendorOrderDetail;
import com.upes.campusdelivery.vendor.dto.VendorOrderItemView;
import com.upes.campusdelivery.vendor.dto.VendorOrderListResponse;
import com.upes.campusdelivery.vendor.dto.VendorOrderSummary;
import com.upes.campusdelivery.vendor.dto.VendorProductListResponse;
import com.upes.campusdelivery.vendor.dto.VendorProductResponse;
import com.upes.campusdelivery.vendor.dto.VendorProductUpsertRequest;
import com.upes.campusdelivery.vendor.dto.VendorProductView;
import com.upes.campusdelivery.vendor.dto.VendorOrderStatusUpdateRequest;
import com.upes.campusdelivery.vendor.dto.VendorOrderStatusUpdateResponse;
import com.upes.campusdelivery.vendor.dto.VendorStockUpdateRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendorService {

    private static final Logger log = LoggerFactory.getLogger(VendorService.class);

    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;

    public VendorService(JdbcTemplate jdbcTemplate, AuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public VendorDashboardResponse getDashboard(String username) {
        VendorContext context = getVendorContextOrThrow(username);

        int activeItems = countProducts(context.vendorId(), true, null);
        int lowStockItems = countProducts(context.vendorId(), true, "LOW_STOCK");
        int flashEnabledItems = countFlashProducts(context.vendorId());
        int openOrders = countOpenOrders(context.vendorId());
        BigDecimal recentSalesTotal = sumVendorOrderValue(context.vendorId());

        return new VendorDashboardResponse(
            context.vendorId(),
            context.shopName(),
            activeItems,
            lowStockItems,
            flashEnabledItems,
            openOrders,
            recentSalesTotal
        );
    }

    @Transactional(readOnly = true)
    public VendorProductListResponse listProducts(String username, String stockStatus, Boolean activeOnly, int page, int size) {
        VendorContext context = getVendorContextOrThrow(username);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        long offset = (long) safePage * safeSize;

        List<Object> params = new ArrayList<>();
        StringBuilder where = new StringBuilder(" WHERE vendor_id = ?");
        params.add(context.vendorId());

        if (stockStatus != null && !stockStatus.isBlank()) {
            where.append(" AND stock_status = ?");
            params.add(stockStatus.strip().toUpperCase());
        }

        if (activeOnly != null) {
            where.append(" AND is_active = ?");
            params.add(activeOnly);
        }

        Long totalElements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products" + where,
            Long.class,
            params.toArray()
        );
        long total = totalElements == null ? 0 : totalElements;

        String sql = """
            SELECT id, name, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent, updated_at
            FROM products
            """ + where + " ORDER BY updated_at DESC LIMIT ? OFFSET ?";

        List<Object> queryParams = new ArrayList<>(params);
        queryParams.add(safeSize);
        queryParams.add(offset);

        List<VendorProductView> items = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> mapProduct(rs),
            queryParams.toArray()
        );

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new VendorProductListResponse(items, safePage, safeSize, total, totalPages);
    }

    @Transactional
    public VendorProductResponse createProduct(String username, VendorProductUpsertRequest request, String traceId) {
        VendorContext context = getVendorContextOrThrow(username);
        validateProductRequest(request);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var statement = connection.prepareStatement(
                """
                INSERT INTO products (vendor_id, name, description, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                new String[] {"id", "updated_at"}
            );
            bindProduct(statement, context.vendorId(), request);
            return statement;
        }, keyHolder);

        Long productId = getLongKey(keyHolder, "id");
        Instant updatedAt = extractTimestamp(keyHolder, "updated_at");
        if (productId == null) {
            throw new AppException("PRODUCT_CREATE_FAILED", "Unable to create product.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        auditService.record(username, "VENDOR", "CREATE_PRODUCT", "PRODUCT", productId, traceId, productMetadata(request));
        log.info("vendor-product-created vendorId={} productId={} traceId={}", context.vendorId(), productId, traceId);
        return toProductResponse(productId, updatedAt, request);
    }

    @Transactional
    public VendorProductResponse updateProduct(String username, Long productId, VendorProductUpsertRequest request, String traceId) {
        VendorContext context = getVendorContextOrThrow(username);
        ensureProductOwnership(context.vendorId(), productId);
        validateProductRequest(request);

        VendorProductResponse existing = getProductResponse(productId);

        jdbcTemplate.update(
            """
            UPDATE products
            SET name = ?, description = ?, category = ?, vertical = ?, mrp = ?, current_price = ?, stock_status = ?, is_active = ?, flash_discount_percent = ?, updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """,
            request.name().strip(),
            normalizeDescription(request.description()),
            request.category().strip().toUpperCase(),
            request.vertical().strip().toUpperCase(),
            request.mrp().setScale(2, RoundingMode.HALF_UP),
            request.currentPrice().setScale(2, RoundingMode.HALF_UP),
            request.stockStatus().strip().toUpperCase(),
            request.active() == null || request.active(),
            existing.flashDiscountPercent(),
            productId
        );

        VendorProductResponse response = getProductResponse(productId);
        auditService.record(username, "VENDOR", "UPDATE_PRODUCT", "PRODUCT", productId, traceId, productMetadata(request));
        log.info("vendor-product-updated vendorId={} productId={} traceId={}", context.vendorId(), productId, traceId);
        return response;
    }

    @Transactional
    public VendorProductResponse updateStock(String username, Long productId, VendorStockUpdateRequest request, String traceId) {
        VendorContext context = getVendorContextOrThrow(username);
        ensureProductOwnership(context.vendorId(), productId);

        String normalizedStock = request.stockStatus().strip().toUpperCase();
        jdbcTemplate.update(
            "UPDATE products SET stock_status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            normalizedStock,
            productId
        );

        VendorProductResponse response = getProductResponse(productId);
        auditService.record(username, "VENDOR", "UPDATE_STOCK", "PRODUCT", productId, traceId, Map.of("stockStatus", normalizedStock));
        log.info("vendor-stock-updated vendorId={} productId={} stockStatus={} traceId={}", context.vendorId(), productId, normalizedStock, traceId);
        return response;
    }

    @Transactional
    public VendorProductResponse updateFlashDiscount(String username, Long productId, VendorFlashDiscountRequest request, String traceId) {
        VendorContext context = getVendorContextOrThrow(username);
        ensureProductOwnership(context.vendorId(), productId);

        BigDecimal flashDiscount = request.flashDiscountPercent().setScale(2, RoundingMode.HALF_UP);
        jdbcTemplate.update(
            "UPDATE products SET flash_discount_percent = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            flashDiscount,
            productId
        );

        VendorProductResponse response = getProductResponse(productId);
        auditService.record(username, "VENDOR", "UPDATE_FLASH_DISCOUNT", "PRODUCT", productId, traceId, Map.of("flashDiscountPercent", flashDiscount));
        log.info("vendor-flash-discount-updated vendorId={} productId={} percent={} traceId={}", context.vendorId(), productId, flashDiscount, traceId);
        return response;
    }

    @Transactional(readOnly = true)
    public VendorOrderListResponse listOrders(String username, int page, int size) {
        VendorContext context = getVendorContextOrThrow(username);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        long offset = (long) safePage * safeSize;

        Long totalElements = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(DISTINCT o.id)
            FROM orders o
            INNER JOIN order_items oi ON oi.order_id = o.id
            INNER JOIN products p ON p.id = oi.product_id
            WHERE p.vendor_id = ?
            """,
            Long.class,
            context.vendorId()
        );
        long total = totalElements == null ? 0 : totalElements;

        List<VendorOrderSummary> items = jdbcTemplate.query(
            """
            SELECT o.id AS order_id,
                   o.status,
                   dz.name AS zone_name,
                   COUNT(oi.id) AS item_count,
                   COALESCE(SUM(oi.line_total), 0) AS vendor_line_total,
                   o.created_at
            FROM orders o
            INNER JOIN delivery_zones dz ON dz.id = o.zone_id
            INNER JOIN order_items oi ON oi.order_id = o.id
            INNER JOIN products p ON p.id = oi.product_id
            WHERE p.vendor_id = ?
            GROUP BY o.id, o.status, dz.name, o.created_at
            ORDER BY o.created_at DESC
            LIMIT ? OFFSET ?
            """,
            (rs, rowNum) -> new VendorOrderSummary(
                rs.getLong("order_id"),
                rs.getString("status"),
                rs.getString("zone_name"),
                rs.getInt("item_count"),
                rs.getBigDecimal("vendor_line_total"),
                rs.getTimestamp("created_at").toInstant()
            ),
            context.vendorId(),
            safeSize,
            offset
        );

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new VendorOrderListResponse(items, safePage, safeSize, total, totalPages);
    }

    @Transactional(readOnly = true)
    public VendorOrderDetail getOrderDetail(String username, Long orderId) {
        VendorContext context = getVendorContextOrThrow(username);
        ensureOrderVisibleToVendor(context.vendorId(), orderId);

        List<VendorOrderDetail> details = jdbcTemplate.query(
            """
            SELECT o.id AS order_id,
                   o.status,
                   dz.name AS zone_name,
                   COALESCE(SUM(oi.line_total), 0) AS vendor_line_total,
                   o.created_at
            FROM orders o
            INNER JOIN delivery_zones dz ON dz.id = o.zone_id
            INNER JOIN order_items oi ON oi.order_id = o.id
            INNER JOIN products p ON p.id = oi.product_id
            WHERE o.id = ? AND p.vendor_id = ?
            GROUP BY o.id, o.status, dz.name, o.created_at
            """,
            (rs, rowNum) -> new VendorOrderDetail(
                rs.getLong("order_id"),
                rs.getString("status"),
                rs.getString("zone_name"),
                rs.getBigDecimal("vendor_line_total"),
                rs.getTimestamp("created_at").toInstant(),
                loadVendorOrderItems(orderId, context.vendorId())
            ),
            orderId,
            context.vendorId()
        );

        if (details.isEmpty()) {
            throw new AppException("ORDER_NOT_FOUND", "Requested order was not found for this vendor.", HttpStatus.NOT_FOUND);
        }

        return details.get(0);
    }

    @Transactional
    public VendorOrderStatusUpdateResponse updateOrderStatus(
        String username,
        Long orderId,
        VendorOrderStatusUpdateRequest request,
        String traceId
    ) {
        VendorContext context = getVendorContextOrThrow(username);
        ensureOrderVisibleToVendor(context.vendorId(), orderId);

        String newStatus = request.status().strip().toUpperCase();
        validateOrderStatus(newStatus);

        List<String> currentRows = jdbcTemplate.queryForList(
            "SELECT status FROM orders WHERE id = ?",
            String.class,
            orderId
        );
        if (currentRows.isEmpty()) {
            throw new AppException("ORDER_NOT_FOUND", "Order not found.", HttpStatus.NOT_FOUND);
        }
        String currentStatus = currentRows.get(0);
        validateStatusTransition(currentStatus, newStatus);

        jdbcTemplate.update(
            "UPDATE orders SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            newStatus,
            orderId
        );

        auditService.record(
            username, "VENDOR", "UPDATE_ORDER_STATUS", "ORDER", orderId, traceId,
            Map.of("from", currentStatus, "to", newStatus)
        );
        log.info("vendor-order-status-updated vendorId={} orderId={} from={} to={} traceId={}",
            context.vendorId(), orderId, currentStatus, newStatus, traceId);

        return new VendorOrderStatusUpdateResponse(orderId, currentStatus, newStatus, Instant.now());
    }

    private void validateOrderStatus(String status) {
        if (!java.util.Set.of("PLACED", "CONFIRMED", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED").contains(status)) {
            throw new AppException("INVALID_ORDER_STATUS", "Invalid order status: " + status, HttpStatus.BAD_REQUEST);
        }
    }

    private void validateStatusTransition(String from, String to) {
        boolean allowed = switch (from) {
            case "PLACED"            -> java.util.Set.of("CONFIRMED", "CANCELLED").contains(to);
            case "CONFIRMED"         -> java.util.Set.of("PREPARING", "CANCELLED").contains(to);
            case "PREPARING"         -> "OUT_FOR_DELIVERY".equals(to);
            case "OUT_FOR_DELIVERY"  -> "DELIVERED".equals(to);
            default                  -> false;
        };
        if (!allowed) {
            throw new AppException(
                "INVALID_STATUS_TRANSITION",
                "Cannot transition order from " + from + " to " + to + ".",
                HttpStatus.BAD_REQUEST
            );
        }
    }

    @Transactional
    public VendorProductResponse changeProductState(String username, Long productId, boolean active, String traceId) {
        VendorContext context = getVendorContextOrThrow(username);
        ensureProductOwnership(context.vendorId(), productId);

        jdbcTemplate.update(
            "UPDATE products SET is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
            active,
            productId
        );

        VendorProductResponse response = getProductResponse(productId);
        auditService.record(username, "VENDOR", active ? "ACTIVATE_PRODUCT" : "DEACTIVATE_PRODUCT", "PRODUCT", productId, traceId, Map.of("active", active));
        log.info("vendor-product-state-updated vendorId={} productId={} active={} traceId={}", context.vendorId(), productId, active, traceId);
        return response;
    }

    private VendorContext getVendorContextOrThrow(String username) {
        List<VendorContext> results = jdbcTemplate.query(
            """
            SELECT u.id AS vendor_id, COALESCE(vp.shop_name, u.full_name) AS shop_name
            FROM users u
            INNER JOIN vendor_profiles vp ON vp.user_id = u.id
            WHERE u.username = ? AND u.role = 'VENDOR' AND u.is_active = TRUE
            """,
            (rs, rowNum) -> new VendorContext(rs.getLong("vendor_id"), rs.getString("shop_name")),
            username
        );

        if (results.isEmpty()) {
            throw new AppException("VENDOR_NOT_FOUND", "Vendor profile was not found for current user.", HttpStatus.NOT_FOUND);
        }

        return results.get(0);
    }

    private int countProducts(Long vendorId, boolean active, String stockStatus) {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM products WHERE vendor_id = ?");
        params.add(vendorId);
        if (active) {
            sql.append(" AND is_active = TRUE");
        }
        if (stockStatus != null) {
            sql.append(" AND stock_status = ?");
            params.add(stockStatus);
        }
        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count == null ? 0 : count;
    }

    private int countFlashProducts(Long vendorId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products WHERE vendor_id = ? AND is_active = TRUE AND COALESCE(flash_discount_percent, 0) > 0",
            Integer.class,
            vendorId
        );
        return count == null ? 0 : count;
    }

    private int countOpenOrders(Long vendorId) {
        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(DISTINCT o.id)
            FROM orders o
            INNER JOIN order_items oi ON oi.order_id = o.id
            INNER JOIN products p ON p.id = oi.product_id
            WHERE p.vendor_id = ? AND o.status IN ('PLACED', 'CONFIRMED', 'PREPARING', 'OUT_FOR_DELIVERY')
            """,
            Integer.class,
            vendorId
        );
        return count == null ? 0 : count;
    }

    private BigDecimal sumVendorOrderValue(Long vendorId) {
        BigDecimal total = jdbcTemplate.queryForObject(
            """
            SELECT COALESCE(SUM(oi.line_total), 0)
            FROM order_items oi
            INNER JOIN products p ON p.id = oi.product_id
            INNER JOIN orders o ON o.id = oi.order_id
            WHERE p.vendor_id = ?
            """,
            BigDecimal.class,
            vendorId
        );
        return total == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateProductRequest(VendorProductUpsertRequest request) {
        if (request.mrp().compareTo(request.currentPrice()) < 0) {
            throw new AppException("INVALID_PRICE", "Current price cannot exceed MRP.", HttpStatus.BAD_REQUEST);
        }
        String stockStatus = request.stockStatus().strip().toUpperCase();
        if (!"IN_STOCK".equals(stockStatus) && !"LOW_STOCK".equals(stockStatus) && !"UNAVAILABLE".equals(stockStatus)) {
            throw new AppException("INVALID_STOCK_STATUS", "Unsupported stock status.", HttpStatus.BAD_REQUEST);
        }
    }

    private void ensureProductOwnership(Long vendorId, Long productId) {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM products WHERE id = ? AND vendor_id = ?",
            Integer.class,
            productId,
            vendorId
        );
        if (count == null || count == 0) {
            throw new AppException("PRODUCT_NOT_FOUND", "Requested product does not exist for this vendor.", HttpStatus.NOT_FOUND);
        }
    }

    private void ensureOrderVisibleToVendor(Long vendorId, Long orderId) {
        Integer count = jdbcTemplate.queryForObject(
            """
            SELECT COUNT(*)
            FROM orders o
            INNER JOIN order_items oi ON oi.order_id = o.id
            INNER JOIN products p ON p.id = oi.product_id
            WHERE o.id = ? AND p.vendor_id = ?
            """,
            Integer.class,
            orderId,
            vendorId
        );
        if (count == null || count == 0) {
            throw new AppException("ORDER_NOT_FOUND", "Requested order does not exist for this vendor.", HttpStatus.NOT_FOUND);
        }
    }

    private List<VendorOrderItemView> loadVendorOrderItems(Long orderId, Long vendorId) {
        return jdbcTemplate.query(
            """
            SELECT oi.product_id, oi.product_name_snapshot, oi.unit_price_snapshot, oi.quantity, oi.line_total
            FROM order_items oi
            INNER JOIN products p ON p.id = oi.product_id
            WHERE oi.order_id = ? AND p.vendor_id = ?
            ORDER BY oi.id ASC
            """,
            (rs, rowNum) -> new VendorOrderItemView(
                rs.getLong("product_id"),
                rs.getString("product_name_snapshot"),
                rs.getBigDecimal("unit_price_snapshot"),
                rs.getInt("quantity"),
                rs.getBigDecimal("line_total")
            ),
            orderId,
            vendorId
        );
    }

    private VendorProductView mapProduct(ResultSet rs) throws SQLException {
        BigDecimal mrp = rs.getBigDecimal("mrp");
        BigDecimal currentPrice = rs.getBigDecimal("current_price");
        return new VendorProductView(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("category"),
            rs.getString("vertical"),
            mrp,
            currentPrice,
            computeSavings(mrp, currentPrice),
            rs.getString("stock_status"),
            rs.getBoolean("is_active"),
            rs.getBigDecimal("flash_discount_percent"),
            rs.getTimestamp("updated_at").toInstant()
        );
    }

    private VendorProductResponse toProductResponse(Long productId, Instant updatedAt, VendorProductUpsertRequest request) {
        BigDecimal mrp = request.mrp().setScale(2, RoundingMode.HALF_UP);
        BigDecimal currentPrice = request.currentPrice().setScale(2, RoundingMode.HALF_UP);
        return new VendorProductResponse(
            productId,
            request.name().strip(),
            request.category().strip().toUpperCase(),
            request.vertical().strip().toUpperCase(),
            mrp,
            currentPrice,
            computeSavings(mrp, currentPrice),
            request.stockStatus().strip().toUpperCase(),
            request.active() == null || request.active(),
            BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
            updatedAt
        );
    }

    private VendorProductResponse getProductResponse(Long productId) {
        List<VendorProductResponse> results = jdbcTemplate.query(
            """
            SELECT id, name, category, vertical, mrp, current_price, stock_status, is_active, flash_discount_percent, updated_at
            FROM products
            WHERE id = ?
            """,
            (rs, rowNum) -> new VendorProductResponse(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("vertical"),
                rs.getBigDecimal("mrp"),
                rs.getBigDecimal("current_price"),
                computeSavings(rs.getBigDecimal("mrp"), rs.getBigDecimal("current_price")),
                rs.getString("stock_status"),
                rs.getBoolean("is_active"),
                rs.getBigDecimal("flash_discount_percent"),
                rs.getTimestamp("updated_at").toInstant()
            ),
            productId
        );
        if (results.isEmpty()) {
            throw new AppException("PRODUCT_NOT_FOUND", "Requested product does not exist.", HttpStatus.NOT_FOUND);
        }
        return results.get(0);
    }

    private void bindProduct(java.sql.PreparedStatement statement, Long vendorId, VendorProductUpsertRequest request) throws SQLException {
        statement.setLong(1, vendorId);
        statement.setString(2, request.name().strip());
        statement.setString(3, normalizeDescription(request.description()));
        statement.setString(4, request.category().strip().toUpperCase());
        statement.setString(5, request.vertical().strip().toUpperCase());
        statement.setBigDecimal(6, request.mrp().setScale(2, RoundingMode.HALF_UP));
        statement.setBigDecimal(7, request.currentPrice().setScale(2, RoundingMode.HALF_UP));
        statement.setString(8, request.stockStatus().strip().toUpperCase());
        statement.setBoolean(9, request.active() == null || request.active());
        statement.setBigDecimal(10, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.strip();
    }

    private BigDecimal computeSavings(BigDecimal mrp, BigDecimal currentPrice) {
        if (mrp == null || currentPrice == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal savings = mrp.subtract(currentPrice);
        return savings.signum() < 0 ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : savings.setScale(2, RoundingMode.HALF_UP);
    }

    private Long getLongKey(KeyHolder keyHolder, String key) {
        if (keyHolder.getKeys() == null) {
            return null;
        }
        Object value = keyHolder.getKeys().get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        return null;
    }

    private Instant extractTimestamp(KeyHolder keyHolder, String key) {
        if (keyHolder.getKeys() == null) {
            return Instant.now();
        }
        Object value = keyHolder.getKeys().get(key);
        if (value instanceof Timestamp timestamp) {
            return timestamp.toInstant();
        }
        return Instant.now();
    }

    private Map<String, Object> productMetadata(VendorProductUpsertRequest request) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("name", request.name());
        metadata.put("category", request.category());
        metadata.put("vertical", request.vertical());
        metadata.put("mrp", request.mrp());
        metadata.put("currentPrice", request.currentPrice());
        metadata.put("stockStatus", request.stockStatus());
        metadata.put("active", request.active() == null || request.active());
        return metadata;
    }

    private record VendorContext(Long vendorId, String shopName) {}
}
