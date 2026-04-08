package com.upes.campusdelivery.pricing.service;

import com.upes.campusdelivery.audit.AuditService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    private static final BigDecimal PLATFORM_DISCOUNT_THRESHOLD = new BigDecimal("300.00");
    private static final BigDecimal PLATFORM_DISCOUNT_RATE = new BigDecimal("0.05");
    private static final BigDecimal PLATFORM_DISCOUNT_CAP = new BigDecimal("40.00");
    private static final BigDecimal CLUSTER_DISCOUNT_RATE = new BigDecimal("0.10");
    private static final int CLUSTER_THRESHOLD = 5;
    private static final Duration CLUSTER_WINDOW = Duration.ofMinutes(10);
    private static final Duration CLUSTER_TTL = Duration.ofMinutes(20);

    private final StringRedisTemplate stringRedisTemplate;
    private final AuditService auditService;

    public PricingService(StringRedisTemplate stringRedisTemplate, AuditService auditService) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.auditService = auditService;
    }

    public BigDecimal calculatePlatformDiscount(BigDecimal subtotal) {
        if (subtotal == null || subtotal.compareTo(PLATFORM_DISCOUNT_THRESHOLD) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discount = subtotal.multiply(PLATFORM_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        if (discount.compareTo(PLATFORM_DISCOUNT_CAP) > 0) {
            return PLATFORM_DISCOUNT_CAP.setScale(2, RoundingMode.HALF_UP);
        }

        return discount;
    }

    public ClusterDiscountPreview previewClusterDiscount(Long zoneId, BigDecimal subtotal) {
        String windowKey = clusterWindowKey(zoneId, Instant.now());

        try {
            Long currentCount = readZoneWindowCount(windowKey);
            boolean eligibleIfPlacedNow = currentCount + 1 >= CLUSTER_THRESHOLD;
            BigDecimal discount = eligibleIfPlacedNow ? computeClusterDiscount(subtotal) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            log.debug("pricing-preview zoneId={} windowKey={} currentCount={} eligible={} discount={}", zoneId, windowKey, currentCount, eligibleIfPlacedNow, discount);

            return new ClusterDiscountPreview(true, windowKey, currentCount, eligibleIfPlacedNow, discount);
        } catch (DataAccessException exception) {
            log.warn("pricing-preview-redis-unavailable zoneId={} windowKey={}", zoneId, windowKey, exception);
            return ClusterDiscountPreview.unavailable(windowKey);
        }
    }

    public ClusterDiscountResult registerClusterDiscount(Long zoneId, BigDecimal subtotal) {
        return registerClusterDiscount(zoneId, subtotal, null, null, "n/a");
    }

    public ClusterDiscountResult registerClusterDiscount(Long zoneId, BigDecimal subtotal, String actorUsername, String actorRole, String traceId) {
        String windowKey = clusterWindowKey(zoneId, Instant.now());

        try {
            Long countAfterIncrement = incrementZoneWindowCount(windowKey);
            boolean eligible = countAfterIncrement >= CLUSTER_THRESHOLD;
            BigDecimal discount = eligible ? computeClusterDiscount(subtotal) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            log.info("pricing-register zoneId={} windowKey={} countAfterIncrement={} eligible={} discount={}", zoneId, windowKey, countAfterIncrement, eligible, discount);
            auditService.record(actorUsername, actorRole, eligible ? "CLUSTER_DISCOUNT_APPLIED" : "CLUSTER_DISCOUNT_CHECK", "PRICING_WINDOW", zoneId, traceId, java.util.Map.of("windowKey", windowKey, "countAfterIncrement", countAfterIncrement, "eligible", eligible, "discount", discount));
            return new ClusterDiscountResult(true, windowKey, countAfterIncrement, eligible, discount);
        } catch (DataAccessException exception) {
            log.warn("pricing-register-redis-unavailable zoneId={} windowKey={}", zoneId, windowKey, exception);
            return ClusterDiscountResult.unavailable(windowKey);
        }
    }

    private Long readZoneWindowCount(String windowKey) {
        String rawValue = Optional.ofNullable(stringRedisTemplate.opsForValue().get(windowKey)).orElse("0");
        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException exception) {
            return 0L;
        }
    }

    private Long incrementZoneWindowCount(String windowKey) {
        Long count = stringRedisTemplate.opsForValue().increment(windowKey);
        if (count == null) {
            return 0L;
        }
        stringRedisTemplate.expire(windowKey, CLUSTER_TTL);
        return count;
    }

    private String clusterWindowKey(Long zoneId, Instant timestamp) {
        long windowBucket = timestamp.getEpochSecond() / CLUSTER_WINDOW.getSeconds();
        return "cluster:zone:" + zoneId + ":window:" + windowBucket;
    }

    private BigDecimal computeClusterDiscount(BigDecimal subtotal) {
        if (subtotal == null || subtotal.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return subtotal.multiply(CLUSTER_DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public record ClusterDiscountPreview(
        boolean redisAvailable,
        String windowKey,
        long currentCount,
        boolean eligibleIfPlacedNow,
        BigDecimal discountAmount
    ) {
        public static ClusterDiscountPreview unavailable(String windowKey) {
            return new ClusterDiscountPreview(false, windowKey, 0L, false, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }

    public record ClusterDiscountResult(
        boolean redisAvailable,
        String windowKey,
        long countAfterIncrement,
        boolean eligible,
        BigDecimal discountAmount
    ) {
        public static ClusterDiscountResult unavailable(String windowKey) {
            return new ClusterDiscountResult(false, windowKey, 0L, false, BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        }
    }
}
