package com.upes.campusdelivery.checkout.service;

import com.upes.campusdelivery.checkout.dto.CheckoutItemRequest;
import com.upes.campusdelivery.checkout.dto.CheckoutPrecheckItemView;
import com.upes.campusdelivery.checkout.dto.CheckoutPrecheckRequest;
import com.upes.campusdelivery.checkout.dto.CheckoutPrecheckResponse;
import com.upes.campusdelivery.common.exceptions.AppException;
import com.upes.campusdelivery.pricing.service.PricingService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CheckoutService {

    private final JdbcTemplate jdbcTemplate;
    private final PricingService pricingService;

    public CheckoutService(JdbcTemplate jdbcTemplate, PricingService pricingService) {
        this.jdbcTemplate = jdbcTemplate;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public CheckoutPrecheckResponse precheck(String username, CheckoutPrecheckRequest request) {
        ZoneRow zone = getZoneOrThrow(request.zoneId());
        BigDecimal walletBalance = getWalletBalanceOrThrow(username);

        List<CheckoutPrecheckItemView> itemViews = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CheckoutItemRequest item : request.items()) {
            ProductRow product = getProductOrThrow(item.productId());
            validateProductAvailability(product);

            BigDecimal lineTotal = product.currentPrice().multiply(BigDecimal.valueOf(item.quantity()));
            lineTotal = lineTotal.setScale(2, RoundingMode.HALF_UP);

            itemViews.add(
                new CheckoutPrecheckItemView(
                    product.id(),
                    product.name(),
                    product.stockStatus(),
                    item.quantity(),
                    product.currentPrice(),
                    lineTotal
                )
            );

            subtotal = subtotal.add(lineTotal);
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal platformDiscount = pricingService.calculatePlatformDiscount(subtotal);
        PricingService.ClusterDiscountPreview clusterPreview = pricingService.previewClusterDiscount(zone.id(), subtotal);
        BigDecimal clusterDiscount = clusterPreview.discountAmount();
        BigDecimal totalDiscount = platformDiscount.add(clusterDiscount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal finalPayable = subtotal.subtract(totalDiscount).setScale(2, RoundingMode.HALF_UP);
        boolean walletSufficient = walletBalance.compareTo(finalPayable) >= 0;

        return new CheckoutPrecheckResponse(
            zone.id(),
            zone.name(),
            itemViews,
            subtotal,
            platformDiscount,
            clusterDiscount,
            totalDiscount,
            finalPayable,
            walletBalance,
            walletSufficient,
            clusterPreview.eligibleIfPlacedNow(),
            clusterPreview.redisAvailable() ? clusterPreview.windowKey() : null
        );
    }

    private ZoneRow getZoneOrThrow(Long zoneId) {
        List<ZoneRow> rows = jdbcTemplate.query(
            "SELECT id, name, is_active FROM delivery_zones WHERE id = ?",
            (rs, rowNum) -> new ZoneRow(rs.getLong("id"), rs.getString("name"), rs.getBoolean("is_active")),
            zoneId
        );

        if (rows.isEmpty() || !rows.get(0).isActive()) {
            throw new AppException("ZONE_NOT_FOUND", "Selected delivery zone is not available.", HttpStatus.BAD_REQUEST);
        }

        return rows.get(0);
    }

    private BigDecimal getWalletBalanceOrThrow(String username) {
        List<BigDecimal> balances = jdbcTemplate.query(
            """
            SELECT w.current_balance
            FROM wallets w
            INNER JOIN users u ON u.id = w.user_id
            WHERE u.username = ?
            """,
            (rs, rowNum) -> rs.getBigDecimal("current_balance"),
            username
        );

        if (balances.isEmpty()) {
            throw new AppException("WALLET_NOT_FOUND", "Wallet not found for current user.", HttpStatus.NOT_FOUND);
        }

        return balances.get(0).setScale(2, RoundingMode.HALF_UP);
    }

    private ProductRow getProductOrThrow(Long productId) {
        List<ProductRow> rows = jdbcTemplate.query(
            """
            SELECT id, name, current_price, stock_status, is_active
            FROM products
            WHERE id = ?
            """,
            (rs, rowNum) ->
                new ProductRow(
                    rs.getLong("id"),
                    rs.getString("name"),
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

    private record ZoneRow(Long id, String name, boolean isActive) {}

    private record ProductRow(Long id, String name, BigDecimal currentPrice, String stockStatus, boolean isActive) {}
}
