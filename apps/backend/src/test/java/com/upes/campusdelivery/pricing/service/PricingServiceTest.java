package com.upes.campusdelivery.pricing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.upes.campusdelivery.audit.AuditService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class PricingServiceTest {

    private FakeStringRedisTemplate stringRedisTemplate;
    private FakeAuditService auditService;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        stringRedisTemplate = new FakeStringRedisTemplate();
        auditService = new FakeAuditService();
        pricingService = new PricingService(stringRedisTemplate, auditService);
    }

    @Test
    void calculatePlatformDiscountReturnsZeroBelowThreshold() {
        BigDecimal discount = pricingService.calculatePlatformDiscount(new BigDecimal("299.99"));

        assertEquals(new BigDecimal("0.00"), discount);
    }

    @Test
    void calculatePlatformDiscountAppliesRateAtThreshold() {
        BigDecimal discount = pricingService.calculatePlatformDiscount(new BigDecimal("300.00"));

        assertEquals(new BigDecimal("15.00"), discount);
    }

    @Test
    void calculatePlatformDiscountAppliesCapOnLargeSubtotal() {
        BigDecimal discount = pricingService.calculatePlatformDiscount(new BigDecimal("1500.00"));

        assertEquals(new BigDecimal("40.00"), discount);
    }

    @Test
    void previewClusterDiscountShowsEligibilityAtFifthOrder() {
        stringRedisTemplate.setGetValue("4");

        PricingService.ClusterDiscountPreview preview = pricingService.previewClusterDiscount(1L, new BigDecimal("200.00"));

        assertTrue(preview.redisAvailable());
        assertTrue(preview.eligibleIfPlacedNow());
        assertEquals(new BigDecimal("20.00"), preview.discountAmount());
    }

    @Test
    void previewClusterDiscountFallsBackWhenRedisUnavailable() {
        stringRedisTemplate.throwOnGet = true;

        PricingService.ClusterDiscountPreview preview = pricingService.previewClusterDiscount(2L, new BigDecimal("200.00"));

        assertFalse(preview.redisAvailable());
        assertFalse(preview.eligibleIfPlacedNow());
        assertEquals(new BigDecimal("0.00"), preview.discountAmount());
    }

    @Test
    void registerClusterDiscountUsesThresholdAndSetsTtl() {
        stringRedisTemplate.setIncrementValue(5L);

        PricingService.ClusterDiscountResult result = pricingService.registerClusterDiscount(3L, new BigDecimal("500.00"));

        assertTrue(result.redisAvailable());
        assertTrue(result.eligible());
        assertEquals(new BigDecimal("50.00"), result.discountAmount());
        assertEquals(1, stringRedisTemplate.expireCallCount);
        assertEquals(1, auditService.recordCallCount);
    }

    @Test
    void registerClusterDiscountFallsBackWhenRedisUnavailable() {
        stringRedisTemplate.throwOnIncrement = true;

        PricingService.ClusterDiscountResult result = pricingService.registerClusterDiscount(4L, new BigDecimal("500.00"));

        assertFalse(result.redisAvailable());
        assertFalse(result.eligible());
        assertEquals(new BigDecimal("0.00"), result.discountAmount());
        assertEquals(0, stringRedisTemplate.expireCallCount);
    }

    private static final class FakeAuditService extends AuditService {
        private int recordCallCount = 0;

        private FakeAuditService() {
            super(null);
        }

        @Override
        public void record(String actorUsername, String actorRole, String action, String entityType, Long entityId, String traceId, Map<String, ?> metadata) {
            recordCallCount++;
        }
    }

    private static final class FakeStringRedisTemplate extends StringRedisTemplate {
        private final Map<String, String> store = new HashMap<>();
        private final ValueOperations<String, String> valueOperations;
        private String forcedGetValue;
        private Long forcedIncrementSeed;
        private boolean throwOnGet = false;
        private boolean throwOnIncrement = false;
        private int expireCallCount = 0;

        private FakeStringRedisTemplate() {
            InvocationHandler handler = (proxy, method, args) -> {
                String methodName = method.getName();
                if ("get".equals(methodName)) {
                    if (throwOnGet) {
                        throw new DataAccessResourceFailureException("redis-down");
                    }
                    if (forcedGetValue != null) {
                        return forcedGetValue;
                    }
                    return store.getOrDefault(String.valueOf(args[0]), "0");
                }
                if ("increment".equals(methodName)) {
                    if (throwOnIncrement) {
                        throw new DataAccessResourceFailureException("redis-down");
                    }
                    String key = String.valueOf(args[0]);
                    if (forcedIncrementSeed != null) {
                        long next = forcedIncrementSeed;
                        forcedIncrementSeed = null;
                        store.put(key, String.valueOf(next));
                        return next;
                    }
                    long next = Long.parseLong(store.getOrDefault(key, "0")) + 1;
                    store.put(key, String.valueOf(next));
                    return next;
                }
                if ("getOperations".equals(methodName)) {
                    return null;
                }
                throw new UnsupportedOperationException("Method not required in test: " + methodName);
            };

            @SuppressWarnings("unchecked")
            ValueOperations<String, String> ops = (ValueOperations<String, String>) Proxy.newProxyInstance(
                ValueOperations.class.getClassLoader(),
                new Class<?>[] {ValueOperations.class},
                handler
            );

            this.valueOperations = ops;
        }

        @Override
        public ValueOperations<String, String> opsForValue() {
            return valueOperations;
        }

        @Override
        public Boolean expire(String key, Duration timeout) {
            expireCallCount++;
            return true;
        }

        public void setGetValue(String value) {
            forcedGetValue = value;
        }

        public void setIncrementValue(long value) {
            forcedIncrementSeed = value;
        }
    }
}
