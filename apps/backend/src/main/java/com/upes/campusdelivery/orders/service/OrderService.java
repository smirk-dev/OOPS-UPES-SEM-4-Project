package com.upes.campusdelivery.orders.service;

import com.upes.campusdelivery.audit.AuditService;
import com.upes.campusdelivery.common.exceptions.AppException;
import com.upes.campusdelivery.orders.dto.CreateOrderItemRequest;
import com.upes.campusdelivery.orders.dto.CreateOrderRequest;
import com.upes.campusdelivery.orders.dto.CreateOrderResponse;
import com.upes.campusdelivery.orders.dto.OrderDetailResponse;
import com.upes.campusdelivery.orders.dto.OrderItemView;
import com.upes.campusdelivery.orders.dto.OrderListResponse;
import com.upes.campusdelivery.orders.dto.OrderSummaryView;
import com.upes.campusdelivery.pricing.service.PricingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final JdbcTemplate jdbcTemplate;
    private final PricingService pricingService;
    private final AuditService auditService;

    public OrderService(JdbcTemplate jdbcTemplate, PricingService pricingService, AuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.pricingService = pricingService;
        this.auditService = auditService;
    }

    @Transactional
    public CreateOrderResponse createOrder(String username, String idempotencyKey, CreateOrderRequest request) {
        return createOrder(username, idempotencyKey, request, "n/a");
    }

    @Transactional
    public CreateOrderResponse createOrder(String username, String idempotencyKey, CreateOrderRequest request, String traceId) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        UserWalletRow userWallet = getUserWalletOrThrow(username);

        CreateOrderResponse replay = findReplayOrder(userWallet.userId(), normalizedKey);
        if (replay != null) {
            log.info("order-replay username={} idempotencyKey={} orderId={}", username, normalizedKey, replay.orderId());
            auditService.record(username, "STUDENT", "ORDER_REPLAY", "ORDER", replay.orderId(), traceId, java.util.Map.of("idempotencyKey", normalizedKey));
            return replay;
        }

        validateZoneOrThrow(request.zoneId());

        List<OrderItemSnapshot> snapshots = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CreateOrderItemRequest item : request.items()) {
            ProductRow product = getProductOrThrow(item.productId());
            validateProductAvailability(product);

            BigDecimal lineTotal = product.currentPrice().multiply(BigDecimal.valueOf(item.quantity())).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(lineTotal);

            snapshots.add(
                new OrderItemSnapshot(
                    product.id(),
                    product.name(),
                    product.mrp(),
                    product.currentPrice(),
                    item.quantity(),
                    lineTotal
                )
            );
        }

        BigDecimal finalSubtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformDiscount = pricingService.calculatePlatformDiscount(finalSubtotal);
        PricingService.ClusterDiscountPreview clusterPreview = pricingService.previewClusterDiscount(request.zoneId(), finalSubtotal);
        BigDecimal clusterDiscountAmount = clusterPreview.discountAmount();
        BigDecimal totalDiscount = platformDiscount.add(clusterDiscountAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalPayable = finalSubtotal.subtract(totalDiscount).setScale(2, RoundingMode.HALF_UP);
        final BigDecimal initialClusterDiscountAmount = clusterDiscountAmount;
        final BigDecimal initialTotalDiscount = totalDiscount;
        final BigDecimal initialFinalPayable = finalPayable;
        final boolean initialClusterDiscountApplied = clusterPreview.eligibleIfPlacedNow();
        final String initialClusterWindowKey = clusterPreview.eligibleIfPlacedNow() ? clusterPreview.windowKey() : null;

        BigDecimal walletBalanceAfter =
            jdbcTemplate.queryForObject(
                """
                UPDATE wallets
                SET current_balance = current_balance - ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND current_balance >= ?
                RETURNING current_balance
                """,
                BigDecimal.class,
                finalPayable,
                userWallet.walletId(),
                finalPayable);

        if (walletBalanceAfter == null) {
            throw new AppException("INSUFFICIENT_WALLET_BALANCE", "Wallet balance is insufficient.", HttpStatus.BAD_REQUEST);
        }
        final BigDecimal initialWalletBalanceAfter = walletBalanceAfter;

        KeyHolder orderKeyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var preparedStatement = connection.prepareStatement(
                """
                INSERT INTO orders (student_id, zone_id, status, subtotal_amount, discount_amount, platform_discount_amount, cluster_discount_amount, final_payable, wallet_balance_after_debit, cluster_discount_applied, cluster_window_key, idempotency_key)
                VALUES (?, ?, 'PLACED', ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                new String[] {"id", "created_at"}
            );
            preparedStatement.setLong(1, userWallet.userId());
            preparedStatement.setLong(2, request.zoneId());
            preparedStatement.setBigDecimal(3, finalSubtotal);
            preparedStatement.setBigDecimal(4, initialTotalDiscount);
            preparedStatement.setBigDecimal(5, platformDiscount);
            preparedStatement.setBigDecimal(6, initialClusterDiscountAmount);
            preparedStatement.setBigDecimal(7, initialFinalPayable);
            preparedStatement.setBigDecimal(8, initialWalletBalanceAfter);
            preparedStatement.setBoolean(9, initialClusterDiscountApplied);
            preparedStatement.setString(10, initialClusterWindowKey);
            preparedStatement.setString(11, normalizedKey);
            return preparedStatement;
        }, orderKeyHolder);

        Long orderId = getLongKey(orderKeyHolder, "id");
        Timestamp createdAtTs = orderKeyHolder.getKeys() == null ? null : (Timestamp) orderKeyHolder.getKeys().get("created_at");

        if (orderId == null) {
            throw new AppException("ORDER_CREATE_FAILED", "Unable to create order.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        for (OrderItemSnapshot snapshot : snapshots) {
            jdbcTemplate.update(
                """
                INSERT INTO order_items (order_id, product_id, product_name_snapshot, mrp_snapshot, unit_price_snapshot, quantity, line_total)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                orderId,
                snapshot.productId(),
                snapshot.productName(),
                snapshot.mrp(),
                snapshot.unitPrice(),
                snapshot.quantity(),
                snapshot.lineTotal()
            );
        }

        jdbcTemplate.update(
            """
            INSERT INTO wallet_transactions (wallet_id, transaction_type, payment_source, amount, reason, order_id)
            VALUES (?, 'DEBIT', 'ORDER_CHECKOUT', ?, ?, ?)
            """,
            userWallet.walletId(),
            finalPayable,
            "Order placement",
            orderId
        );

        Instant createdAt = createdAtTs == null ? Instant.now() : createdAtTs.toInstant();
        boolean clusterDiscountApplied = clusterPreview.eligibleIfPlacedNow();
        String clusterWindowKey = clusterPreview.eligibleIfPlacedNow() ? clusterPreview.windowKey() : null;

        PricingService.ClusterDiscountResult clusterResult = pricingService.registerClusterDiscount(request.zoneId(), finalSubtotal, username, "STUDENT", traceId);
        BigDecimal registeredClusterDiscount = clusterResult.redisAvailable()
            ? clusterResult.discountAmount()
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        boolean registeredClusterDiscountApplied = clusterResult.redisAvailable() && clusterResult.eligible();
        String registeredClusterWindowKey = registeredClusterDiscountApplied ? clusterResult.windowKey() : null;
        BigDecimal registeredTotalDiscount = platformDiscount.add(registeredClusterDiscount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal registeredFinalPayable = finalSubtotal.subtract(registeredTotalDiscount).setScale(2, RoundingMode.HALF_UP);

        BigDecimal payableDelta = registeredFinalPayable.subtract(finalPayable).setScale(2, RoundingMode.HALF_UP);
        if (payableDelta.signum() > 0) {
            BigDecimal adjustedBalanceAfter =
                jdbcTemplate.queryForObject(
                    """
                    UPDATE wallets
                    SET current_balance = current_balance - ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND current_balance >= ?
                    RETURNING current_balance
                    """,
                    BigDecimal.class,
                    payableDelta,
                    userWallet.walletId(),
                    payableDelta);

            if (adjustedBalanceAfter == null) {
                throw new AppException("INSUFFICIENT_WALLET_BALANCE", "Wallet balance is insufficient.", HttpStatus.BAD_REQUEST);
            }

            walletBalanceAfter = adjustedBalanceAfter;
            jdbcTemplate.update(
                """
                INSERT INTO wallet_transactions (wallet_id, transaction_type, payment_source, amount, reason, order_id)
                VALUES (?, 'DEBIT', 'ORDER_CHECKOUT_ADJUSTMENT', ?, ?, ?)
                """,
                userWallet.walletId(),
                payableDelta,
                "Order discount reconciliation debit",
                orderId
            );
        } else if (payableDelta.signum() < 0) {
            BigDecimal refundAmount = payableDelta.abs();
            BigDecimal adjustedBalanceAfter =
                jdbcTemplate.queryForObject(
                    """
                    UPDATE wallets
                    SET current_balance = current_balance + ?, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ?
                    RETURNING current_balance
                    """,
                    BigDecimal.class,
                    refundAmount,
                    userWallet.walletId());

            if (adjustedBalanceAfter == null) {
                throw new AppException("ORDER_CREATE_FAILED", "Unable to update wallet balance.", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            walletBalanceAfter = adjustedBalanceAfter;
            jdbcTemplate.update(
                """
                INSERT INTO wallet_transactions (wallet_id, transaction_type, payment_source, amount, reason, order_id)
                VALUES (?, 'CREDIT', 'ORDER_CHECKOUT_ADJUSTMENT', ?, ?, ?)
                """,
                userWallet.walletId(),
                refundAmount,
                "Order discount reconciliation refund",
                orderId
            );
        }

        clusterDiscountAmount = registeredClusterDiscount;
        totalDiscount = registeredTotalDiscount;
        finalPayable = registeredFinalPayable;
        clusterDiscountApplied = registeredClusterDiscountApplied;
        clusterWindowKey = registeredClusterWindowKey;

        jdbcTemplate.update(
            """
            UPDATE orders
            SET cluster_discount_applied = ?,
                cluster_window_key = ?,
                cluster_discount_amount = ?,
                discount_amount = ?,
                final_payable = ?,
                wallet_balance_after_debit = ?
            WHERE id = ?
            """,
            clusterDiscountApplied,
            clusterWindowKey,
            clusterDiscountAmount,
            totalDiscount,
            finalPayable,
            walletBalanceAfter,
            orderId
        );

        log.info("order-created username={} orderId={} zoneId={} finalPayable={} totalDiscount={}", username, orderId, request.zoneId(), finalPayable, totalDiscount);
        auditService.record(username, "STUDENT", "ORDER_CREATED", "ORDER", orderId, traceId, java.util.Map.of("zoneId", request.zoneId(), "subtotal", finalSubtotal, "totalDiscount", totalDiscount, "finalPayable", finalPayable));
        return new CreateOrderResponse(
            orderId,
            "PLACED",
            platformDiscount,
            clusterDiscountAmount,
            totalDiscount,
            finalPayable,
            walletBalanceAfter,
            createdAt,
            false,
            clusterDiscountApplied,
            clusterWindowKey
        );
    }

    @Transactional(readOnly = true)
    public OrderListResponse listStudentOrders(String username, int page, int size) {
        UserWalletRow userWallet = getUserWalletOrThrow(username);

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        long offset = (long) safePage * safeSize;

        Long totalElements = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM orders WHERE student_id = ?",
            Long.class,
            userWallet.userId()
        );

        long total = totalElements == null ? 0 : totalElements;

        List<OrderSummaryView> items = jdbcTemplate.query(
            """
            SELECT o.id,
                   o.status,
                   dz.name AS zone_name,
                   o.subtotal_amount,
                   o.discount_amount,
                   o.final_payable,
                   o.created_at,
                   COALESCE(COUNT(oi.id), 0) AS item_count
            FROM orders o
            INNER JOIN delivery_zones dz ON dz.id = o.zone_id
            LEFT JOIN order_items oi ON oi.order_id = o.id
            WHERE o.student_id = ?
            GROUP BY o.id, dz.name
            ORDER BY o.created_at DESC
            LIMIT ? OFFSET ?
            """,
            (rs, rowNum) -> new OrderSummaryView(
                rs.getLong("id"),
                rs.getString("status"),
                rs.getString("zone_name"),
                rs.getBigDecimal("subtotal_amount"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("final_payable"),
                rs.getInt("item_count"),
                rs.getTimestamp("created_at").toInstant()
            ),
            userWallet.userId(),
            safeSize,
            offset
        );

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new OrderListResponse(items, safePage, safeSize, total, totalPages);
    }

    @Transactional(readOnly = true)
    public OrderDetailResponse getStudentOrderDetail(String username, Long orderId) {
        UserWalletRow userWallet = getUserWalletOrThrow(username);

        List<OrderDetailRow> orderRows = jdbcTemplate.query(
            """
            SELECT o.id,
                   o.status,
                   dz.name AS zone_name,
                   o.subtotal_amount,
                   o.platform_discount_amount,
                   o.cluster_discount_amount,
                   o.discount_amount,
                   o.final_payable,
                     o.wallet_balance_after_debit,
                   o.created_at,
                   o.cluster_discount_applied,
                   o.cluster_window_key
            FROM orders o
            INNER JOIN delivery_zones dz ON dz.id = o.zone_id
            WHERE o.id = ? AND o.student_id = ?
            """,
            (rs, rowNum) -> new OrderDetailRow(
                rs.getLong("id"),
                rs.getString("status"),
                rs.getString("zone_name"),
                rs.getBigDecimal("subtotal_amount"),
                rs.getBigDecimal("platform_discount_amount"),
                rs.getBigDecimal("cluster_discount_amount"),
                rs.getBigDecimal("discount_amount"),
                rs.getBigDecimal("final_payable"),
                rs.getBigDecimal("wallet_balance_after_debit"),
                rs.getTimestamp("created_at").toInstant(),
                rs.getBoolean("cluster_discount_applied"),
                rs.getString("cluster_window_key")
            ),
            orderId,
            userWallet.userId()
        );

        if (orderRows.isEmpty()) {
            throw new AppException("ORDER_NOT_FOUND", "Requested order was not found.", HttpStatus.NOT_FOUND);
        }

        List<OrderItemView> items = jdbcTemplate.query(
            """
            SELECT product_id, product_name_snapshot, mrp_snapshot, unit_price_snapshot, quantity, line_total
            FROM order_items
            WHERE order_id = ?
            ORDER BY id ASC
            """,
            (rs, rowNum) -> new OrderItemView(
                rs.getLong("product_id"),
                rs.getString("product_name_snapshot"),
                rs.getBigDecimal("mrp_snapshot"),
                rs.getBigDecimal("unit_price_snapshot"),
                rs.getInt("quantity"),
                rs.getBigDecimal("line_total")
            ),
            orderId
        );

        OrderDetailRow order = orderRows.get(0);
        return new OrderDetailResponse(
            order.orderId(),
            order.status(),
            order.zoneName(),
            order.subtotalAmount(),
            order.platformDiscountAmount(),
            order.clusterDiscountAmount(),
            order.discountAmount(),
            order.finalPayable(),
            order.walletBalanceAfterDebit(),
            order.createdAt(),
            items,
            order.clusterDiscountApplied(),
            order.clusterWindowKey()
        );
    }

    private CreateOrderResponse findReplayOrder(Long userId, String idempotencyKey) {
        List<CreateOrderResponse> rows = jdbcTemplate.query(
            """
            SELECT o.id,
                   o.status,
                   o.platform_discount_amount,
                   o.cluster_discount_amount,
                   o.discount_amount,
                   o.final_payable,
                     o.wallet_balance_after_debit,
                   o.created_at,
                   o.cluster_discount_applied,
                   o.cluster_window_key
            FROM orders o
            WHERE o.student_id = ? AND o.idempotency_key = ?
            """,
            (rs, rowNum) -> toReplayResponse(rs),
            userId,
            idempotencyKey
        );

        if (rows.isEmpty()) {
            return null;
        }

        return rows.get(0);
    }

    private CreateOrderResponse toReplayResponse(ResultSet rs) throws SQLException {
        return new CreateOrderResponse(
            rs.getLong("id"),
            rs.getString("status"),
            rs.getBigDecimal("platform_discount_amount"),
            rs.getBigDecimal("cluster_discount_amount"),
            rs.getBigDecimal("discount_amount"),
            rs.getBigDecimal("final_payable"),
            rs.getBigDecimal("wallet_balance_after_debit"),
            rs.getTimestamp("created_at").toInstant(),
            true,
            rs.getBoolean("cluster_discount_applied"),
            rs.getString("cluster_window_key")
        );
    }

    private UserWalletRow getUserWalletOrThrow(String username) {
        List<UserWalletRow> rows = jdbcTemplate.query(
            """
            SELECT u.id AS user_id, w.id AS wallet_id, w.current_balance
            FROM users u
            INNER JOIN wallets w ON w.user_id = u.id
            WHERE u.username = ?
            """,
            (rs, rowNum) -> new UserWalletRow(
                rs.getLong("user_id"),
                rs.getLong("wallet_id"),
                rs.getBigDecimal("current_balance")
            ),
            username
        );

        if (rows.isEmpty()) {
            throw new AppException("WALLET_NOT_FOUND", "Wallet not found for current user.", HttpStatus.NOT_FOUND);
        }

        return rows.get(0);
    }

    private void validateZoneOrThrow(Long zoneId) {
        List<Boolean> rows = jdbcTemplate.query(
            "SELECT is_active FROM delivery_zones WHERE id = ?",
            (rs, rowNum) -> rs.getBoolean("is_active"),
            zoneId
        );

        if (rows.isEmpty() || !rows.get(0)) {
            throw new AppException("ZONE_NOT_FOUND", "Selected delivery zone is not available.", HttpStatus.BAD_REQUEST);
        }
    }

    private ProductRow getProductOrThrow(Long productId) {
        List<ProductRow> rows = jdbcTemplate.query(
            """
            SELECT id, name, mrp, current_price, stock_status, is_active
            FROM products
            WHERE id = ?
            """,
            (rs, rowNum) -> new ProductRow(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getBigDecimal("mrp"),
                rs.getBigDecimal("current_price"),
                rs.getString("stock_status"),
                rs.getBoolean("is_active")
            ),
            productId
        );

        if (rows.isEmpty()) {
            throw new AppException("PRODUCT_NOT_FOUND", "One or more products were not found.", HttpStatus.BAD_REQUEST);
        }

        return rows.get(0);
    }

    private void validateProductAvailability(ProductRow product) {
        if (!product.isActive()) {
            throw new AppException("PRODUCT_INACTIVE", "One or more products are inactive.", HttpStatus.BAD_REQUEST);
        }

        if (!"IN_STOCK".equalsIgnoreCase(product.stockStatus()) && !"LOW_STOCK".equalsIgnoreCase(product.stockStatus())) {
            throw new AppException("PRODUCT_OUT_OF_STOCK", "One or more products are out of stock.", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new AppException("IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required.", HttpStatus.BAD_REQUEST);
        }
        String value = idempotencyKey.strip();
        if (value.length() > 100) {
            throw new AppException("IDEMPOTENCY_KEY_INVALID", "Idempotency-Key is too long.", HttpStatus.BAD_REQUEST);
        }
        return value;
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

    private record UserWalletRow(Long userId, Long walletId, BigDecimal currentBalance) {}

    private record ProductRow(Long id, String name, BigDecimal mrp, BigDecimal currentPrice, String stockStatus, boolean isActive) {}

    private record OrderItemSnapshot(Long productId, String productName, BigDecimal mrp, BigDecimal unitPrice, Integer quantity, BigDecimal lineTotal) {}

    private record OrderDetailRow(
        Long orderId,
        String status,
        String zoneName,
        BigDecimal subtotalAmount,
        BigDecimal platformDiscountAmount,
        BigDecimal clusterDiscountAmount,
        BigDecimal discountAmount,
        BigDecimal finalPayable,
        BigDecimal walletBalanceAfterDebit,
        Instant createdAt,
        boolean clusterDiscountApplied,
        String clusterWindowKey
    ) {}
}
