package com.upes.campusdelivery.orders.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.upes.campusdelivery.audit.AuditService;
import com.upes.campusdelivery.common.exceptions.AppException;
import com.upes.campusdelivery.orders.dto.CreateOrderItemRequest;
import com.upes.campusdelivery.orders.dto.CreateOrderRequest;
import com.upes.campusdelivery.orders.dto.CreateOrderResponse;
import com.upes.campusdelivery.pricing.service.PricingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.mockito.ArgumentMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class OrderServiceTest {

  private JdbcTemplate jdbcTemplate;
  private PricingService pricingService;
  private AuditService auditService;
  private OrderService orderService;

  @BeforeEach
  void setUp() {
    jdbcTemplate = mock(JdbcTemplate.class);
    pricingService = mock(PricingService.class);
    auditService = mock(AuditService.class);
    orderService = new OrderService(jdbcTemplate, pricingService, auditService);
  }

  @Test
  void replayReturnsExistingOrderBeforeZoneValidation() {
    Object userWallet = newUserWalletRow(1L, 9L, new BigDecimal("500.00"));
    CreateOrderResponse replay =
        new CreateOrderResponse(
            77L,
            "PLACED",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            new BigDecimal("100.00"),
            new BigDecimal("400.00"),
            Instant.now(),
            true,
            false,
            null);

    when(jdbcTemplate.query(
          argThat(sqlContains("FROM users u")), any(RowMapper.class), eq("student1")))
        .thenReturn(List.of(userWallet));
    when(jdbcTemplate.query(
          argThat(sqlContains("WHERE o.student_id = ? AND o.idempotency_key = ?")),
            any(RowMapper.class),
            eq(1L),
            eq("idem-1")))
        .thenReturn(List.of(replay));

    CreateOrderRequest request =
        new CreateOrderRequest(999L, List.of(new CreateOrderItemRequest(10L, 1)));

    CreateOrderResponse response = orderService.createOrder("student1", "idem-1", request);

    assertEquals(77L, response.orderId());
    verify(jdbcTemplate, never())
        .query(
        argThat(sqlContains("delivery_zones")), any(RowMapper.class), any());
  }

  @Test
  void createOrderFailsWhenAtomicWalletDebitReturnsNull() {
    Object userWallet = newUserWalletRow(1L, 9L, new BigDecimal("500.00"));
    Object product =
        newProductRow(10L, "Item A", new BigDecimal("120.00"), new BigDecimal("100.00"), "IN_STOCK", true);

    when(jdbcTemplate.query(
        argThat(sqlContains("FROM users u")), any(RowMapper.class), eq("student1")))
        .thenReturn(List.of(userWallet));
    when(jdbcTemplate.query(
        argThat(sqlContains("WHERE o.student_id = ? AND o.idempotency_key = ?")),
            any(RowMapper.class),
            eq(1L),
            eq("idem-2")))
        .thenReturn(List.of());
    when(jdbcTemplate.query(
        argThat(sqlContains("SELECT is_active FROM delivery_zones")),
            any(RowMapper.class),
            eq(1L)))
        .thenReturn(List.of(true));
    when(jdbcTemplate.query(
        argThat(sqlContains("FROM products")), any(RowMapper.class), eq(10L)))
        .thenReturn(List.of(product));

    when(pricingService.calculatePlatformDiscount(any()))
        .thenReturn(BigDecimal.ZERO.setScale(2));
    when(pricingService.previewClusterDiscount(eq(1L), any()))
        .thenReturn(
            new PricingService.ClusterDiscountPreview(
                true, "cluster:zone:1:window:test", 0L, false, BigDecimal.ZERO.setScale(2)));

    when(jdbcTemplate.queryForObject(
        argThat(sqlContainsAll("UPDATE wallets", "RETURNING current_balance")),
            eq(BigDecimal.class),
            any(),
            any(),
            any()))
        .thenReturn(null);

    CreateOrderRequest request =
        new CreateOrderRequest(1L, List.of(new CreateOrderItemRequest(10L, 1)));

    AppException exception =
        assertThrows(AppException.class, () -> orderService.createOrder("student1", "idem-2", request));

    assertEquals("INSUFFICIENT_WALLET_BALANCE", exception.getCode());
    assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    verify(pricingService, never()).registerClusterDiscount(any(), any());
  }

  private Object newUserWalletRow(Long userId, Long walletId, BigDecimal currentBalance) {
    try {
      Class<?> clazz =
          Class.forName("com.upes.campusdelivery.orders.service.OrderService$UserWalletRow");
      var constructor = clazz.getDeclaredConstructor(Long.class, Long.class, BigDecimal.class);
      constructor.setAccessible(true);
      return constructor.newInstance(userId, walletId, currentBalance);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  private Object newProductRow(
      Long id,
      String name,
      BigDecimal mrp,
      BigDecimal currentPrice,
      String stockStatus,
      boolean isActive) {
    try {
      Class<?> clazz =
          Class.forName("com.upes.campusdelivery.orders.service.OrderService$ProductRow");
      var constructor =
          clazz.getDeclaredConstructor(
              Long.class, String.class, BigDecimal.class, BigDecimal.class, String.class, boolean.class);
      constructor.setAccessible(true);
      return constructor.newInstance(id, name, mrp, currentPrice, stockStatus, isActive);
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  private static ArgumentMatcher<String> sqlContains(String fragment) {
    return sql -> sql != null && sql.contains(fragment);
  }

  private static ArgumentMatcher<String> sqlContainsAll(String... fragments) {
    return sql -> {
      if (sql == null) {
        return false;
      }
      for (String fragment : fragments) {
        if (!sql.contains(fragment)) {
          return false;
        }
      }
      return true;
    };
  }
}
