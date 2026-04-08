package com.upes.campusdelivery.wallet.service;

import com.upes.campusdelivery.audit.AuditService;
import com.upes.campusdelivery.common.exceptions.AppException;
import com.upes.campusdelivery.wallet.dto.WalletBalanceResponse;
import com.upes.campusdelivery.wallet.dto.WalletRechargeRequest;
import com.upes.campusdelivery.wallet.dto.WalletRechargeResponse;
import com.upes.campusdelivery.wallet.dto.WalletTransactionView;
import com.upes.campusdelivery.wallet.dto.WalletTransactionsResponse;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

  private static final Logger log = LoggerFactory.getLogger(WalletService.class);

  private final JdbcTemplate jdbcTemplate;
  private final AuditService auditService;

  public WalletService(JdbcTemplate jdbcTemplate, AuditService auditService) {
    this.jdbcTemplate = jdbcTemplate;
    this.auditService = auditService;
  }

  @Transactional(readOnly = true)
  public WalletBalanceResponse getCurrentUserWalletBalance(String username) {
    WalletRow wallet = getWalletByUsernameOrThrow(username);
    return new WalletBalanceResponse(wallet.walletId(), wallet.userId(), wallet.currentBalance());
  }

  @Transactional
  public WalletRechargeResponse rechargeCurrentUserWallet(String username, WalletRechargeRequest request) {
    WalletRow wallet = getWalletByUsernameOrThrow(username);
    BigDecimal updatedBalance = wallet.currentBalance().add(request.amount());

    jdbcTemplate.update(
        "UPDATE wallets SET current_balance = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
        updatedBalance,
        wallet.walletId());

    jdbcTemplate.update(
        """
        INSERT INTO wallet_transactions (wallet_id, transaction_type, payment_source, amount, reason)
        VALUES (?, 'CREDIT', 'WALLET_RECHARGE', ?, ?)
        """,
        wallet.walletId(),
        request.amount(),
        safeRechargeReason(request.note()));

    Long transactionId =
        jdbcTemplate.queryForObject(
            "SELECT currval(pg_get_serial_sequence('wallet_transactions','id'))", Long.class);

    if (transactionId == null) {
      throw new AppException(
          "WALLET_RECHARGE_FAILED",
          "Wallet recharge was completed but transaction tracking failed.",
          HttpStatus.INTERNAL_SERVER_ERROR);
    }

    auditService.record(username, "STUDENT", "WALLET_RECHARGE", "WALLET", wallet.walletId(), "n/a", java.util.Map.of("amount", request.amount(), "updatedBalance", updatedBalance));
    log.info("wallet-recharge username={} walletId={} amount={} updatedBalance={}", username, wallet.walletId(), request.amount(), updatedBalance);

    return new WalletRechargeResponse(
        wallet.walletId(),
        transactionId,
        request.amount(),
        updatedBalance);
  }

  @Transactional(readOnly = true)
  public WalletTransactionsResponse getCurrentUserTransactions(String username, int page, int size) {
    WalletRow wallet = getWalletByUsernameOrThrow(username);

    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 50);
    long offset = (long) safePage * safeSize;

    Long totalElements =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM wallet_transactions WHERE wallet_id = ?",
            Long.class,
            wallet.walletId());

    long total = totalElements == null ? 0 : totalElements;

    List<WalletTransactionView> items =
        jdbcTemplate.query(
            """
            SELECT id, transaction_type, payment_source, amount, reason, order_id, created_at
            FROM wallet_transactions
            WHERE wallet_id = ?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """,
            (rs, rowNum) -> toTransactionView(rs),
            wallet.walletId(),
            safeSize,
            offset);

    int totalPages = total == 0 ? 0 : (int) Math.ceil((double) total / safeSize);

    return new WalletTransactionsResponse(items, safePage, safeSize, total, totalPages);
  }

  private WalletRow getWalletByUsernameOrThrow(String username) {
    List<WalletRow> results =
        jdbcTemplate.query(
            """
            SELECT w.id AS wallet_id, u.id AS user_id, w.current_balance
            FROM wallets w
            INNER JOIN users u ON u.id = w.user_id
            WHERE u.username = ?
            """,
            (rs, rowNum) ->
                new WalletRow(
                    rs.getLong("wallet_id"),
                    rs.getLong("user_id"),
                    rs.getBigDecimal("current_balance")),
            username);

    if (results.isEmpty()) {
      throw new AppException("WALLET_NOT_FOUND", "Wallet not found for current user.", HttpStatus.NOT_FOUND);
    }

    return results.get(0);
  }

  private WalletTransactionView toTransactionView(ResultSet rs) throws SQLException {
    return new WalletTransactionView(
        rs.getLong("id"),
        rs.getString("transaction_type"),
        rs.getString("payment_source"),
        rs.getBigDecimal("amount"),
        rs.getString("reason"),
        rs.getObject("order_id") == null ? null : rs.getLong("order_id"),
        rs.getTimestamp("created_at").toInstant());
  }

  private String safeRechargeReason(String note) {
    if (note == null || note.isBlank()) {
      return "Wallet recharge";
    }
    return note.strip();
  }

  private record WalletRow(Long walletId, Long userId, BigDecimal currentBalance) {}
}
