package com.upes.campusdelivery.catalog.service;

import com.upes.campusdelivery.catalog.dto.CatalogListResponse;
import com.upes.campusdelivery.catalog.dto.CatalogProductDetail;
import com.upes.campusdelivery.catalog.dto.CatalogProductSummary;
import com.upes.campusdelivery.common.exceptions.AppException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CatalogService {

    private final JdbcTemplate jdbcTemplate;

    public CatalogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public CatalogListResponse listProducts(
        String vertical,
        String category,
        String stockStatus,
        int page,
        int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);

        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(vertical, category, stockStatus, params);

        String countSql = """
            SELECT COUNT(*)
            FROM products p
            LEFT JOIN vendor_profiles vp ON vp.user_id = p.vendor_id
            WHERE p.is_active = TRUE
            """ + whereClause;

        Long totalElements = jdbcTemplate.queryForObject(countSql, Long.class, params.toArray());
        long total = totalElements == null ? 0 : totalElements;

        String listSql = """
            SELECT p.id,
                   p.name,
                   p.category,
                   p.vertical,
                   p.mrp,
                   p.current_price,
                   p.stock_status,
                   p.flash_discount_percent,
                   COALESCE(vp.shop_name, 'Unknown Vendor') AS vendor_shop_name
            FROM products p
            LEFT JOIN vendor_profiles vp ON vp.user_id = p.vendor_id
            WHERE p.is_active = TRUE
            """ + whereClause + " ORDER BY p.id DESC LIMIT ? OFFSET ?";

        List<Object> listParams = new ArrayList<>(params);
        listParams.add(safeSize);
        listParams.add((long) safePage * safeSize);

        List<CatalogProductSummary> items = jdbcTemplate.query(
            listSql,
            (rs, rowNum) -> {
                BigDecimal mrp = rs.getBigDecimal("mrp");
                BigDecimal currentPrice = rs.getBigDecimal("current_price");
                BigDecimal savings = computeSavings(mrp, currentPrice);

                return new CatalogProductSummary(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getString("vertical"),
                    mrp,
                    currentPrice,
                    savings,
                    rs.getString("stock_status"),
                    rs.getBigDecimal("flash_discount_percent"),
                    rs.getString("vendor_shop_name")
                );
            },
            listParams.toArray()
        );

        int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);
        return new CatalogListResponse(items, safePage, safeSize, total, totalPages);
    }

    public CatalogProductDetail getProductById(Long productId) {
        String detailSql = """
            SELECT p.id,
                   p.name,
                   p.description,
                   p.category,
                   p.vertical,
                   p.mrp,
                   p.current_price,
                   p.stock_status,
                   p.flash_discount_percent,
                   p.is_active,
                   COALESCE(vp.shop_name, 'Unknown Vendor') AS vendor_shop_name
            FROM products p
            LEFT JOIN vendor_profiles vp ON vp.user_id = p.vendor_id
            WHERE p.id = ?
            """;

        List<CatalogProductDetail> results = jdbcTemplate.query(
            detailSql,
            (rs, rowNum) -> {
                BigDecimal mrp = rs.getBigDecimal("mrp");
                BigDecimal currentPrice = rs.getBigDecimal("current_price");
                BigDecimal savings = computeSavings(mrp, currentPrice);

                return new CatalogProductDetail(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("vertical"),
                    mrp,
                    currentPrice,
                    savings,
                    rs.getString("stock_status"),
                    rs.getBigDecimal("flash_discount_percent"),
                    rs.getBoolean("is_active"),
                    rs.getString("vendor_shop_name")
                );
            },
            productId
        );

        if (results.isEmpty()) {
            throw new AppException("PRODUCT_NOT_FOUND", "Requested product does not exist.", HttpStatus.NOT_FOUND);
        }

        return results.get(0);
    }

    private String buildWhereClause(String vertical, String category, String stockStatus, List<Object> params) {
        StringBuilder where = new StringBuilder();

        if (vertical != null && !vertical.isBlank()) {
            where.append(" AND p.vertical = ?");
            params.add(vertical.trim().toUpperCase());
        }

        if (category != null && !category.isBlank()) {
            where.append(" AND p.category = ?");
            params.add(category.trim().toUpperCase());
        }

        if (stockStatus != null && !stockStatus.isBlank()) {
            where.append(" AND p.stock_status = ?");
            params.add(stockStatus.trim().toUpperCase());
        }

        return where.toString();
    }

    private BigDecimal computeSavings(BigDecimal mrp, BigDecimal currentPrice) {
        if (mrp == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal savings = mrp.subtract(currentPrice);
        if (savings.signum() < 0) {
            return BigDecimal.ZERO;
        }
        return savings.setScale(2, RoundingMode.HALF_UP);
    }
}
