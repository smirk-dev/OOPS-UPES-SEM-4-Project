"use client";

import { useEffect, useState } from "react";
import { AppButton } from "@/components/ui/AppButton";
import { AppCard } from "@/components/ui/AppCard";
import { FormError } from "@/components/ui/FormError";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { PriceRow } from "@/components/ui/PriceRow";
import { SectionHeader } from "@/components/ui/SectionHeader";
import { StatusBadge } from "@/components/ui/StatusBadge";
import {
  getWalletBalance,
  getWalletTransactions,
  rechargeWallet,
  type WalletBalanceResponse,
  type WalletTransaction,
} from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";

function txTone(type: string): "success" | "warn" | "neutral" {
  if (type === "CREDIT") return "success";
  if (type === "DEBIT") return "warn";
  return "neutral";
}

export default function WalletPage() {
  const [wallet, setWallet] = useState<WalletBalanceResponse | null>(null);
  const [transactions, setTransactions] = useState<WalletTransaction[]>([]);
  const [txPage, setTxPage] = useState(0);
  const [txTotalPages, setTxTotalPages] = useState(0);
  const [loadingBalance, setLoadingBalance] = useState(true);
  const [loadingTx, setLoadingTx] = useState(true);
  const [balanceError, setBalanceError] = useState("");
  const [txError, setTxError] = useState("");

  const [rechargeAmount, setRechargeAmount] = useState("");
  const [rechargeNote, setRechargeNote] = useState("");
  const [recharging, setRecharging] = useState(false);
  const [rechargeError, setRechargeError] = useState("");
  const [rechargeSuccess, setRechargeSuccess] = useState("");

  const token = authStorage.getToken();

  const fetchBalance = async () => {
    if (!token) {
      setBalanceError("Session missing. Please login again.");
      setLoadingBalance(false);
      return;
    }
    setLoadingBalance(true);
    setBalanceError("");
    try {
      const data = await getWalletBalance(token);
      setWallet(data);
    } catch (err) {
      setBalanceError(err instanceof Error ? err.message : "Failed to load balance.");
    } finally {
      setLoadingBalance(false);
    }
  };

  const fetchTransactions = async (page: number) => {
    if (!token) return;
    setLoadingTx(true);
    setTxError("");
    try {
      const data = await getWalletTransactions(token, page, 10);
      setTransactions(data.items);
      setTxTotalPages(data.totalPages);
    } catch (err) {
      setTxError(err instanceof Error ? err.message : "Failed to load transactions.");
    } finally {
      setLoadingTx(false);
    }
  };

  useEffect(() => {
    void fetchBalance();
    void fetchTransactions(0);
  }, [token]);

  const onRecharge = async () => {
    const amount = parseFloat(rechargeAmount);
    if (!token) return;
    if (isNaN(amount) || amount < 1) {
      setRechargeError("Enter a valid amount (minimum ₹1).");
      return;
    }

    setRecharging(true);
    setRechargeError("");
    setRechargeSuccess("");

    try {
      const result = await rechargeWallet(token, amount, rechargeNote || undefined);
      setRechargeSuccess(`₹${result.creditedAmount} added. New balance: ₹${result.updatedBalance}.`);
      setRechargeAmount("");
      setRechargeNote("");
      await fetchBalance();
      await fetchTransactions(0);
      setTxPage(0);
    } catch (err) {
      setRechargeError(err instanceof Error ? err.message : "Recharge failed.");
    } finally {
      setRecharging(false);
    }
  };

  const onPageChange = async (next: number) => {
    setTxPage(next);
    await fetchTransactions(next);
  };

  return (
    <main className="mt-6 space-y-4">
      <PageHeader title="Wallet" subtitle="Manage your campus delivery balance and view transaction history" />

      <div className="grid gap-4 lg:grid-cols-[1fr,1.4fr]">
        <div className="space-y-4">
          <AppCard className="bg-[var(--card)]">
            <SectionHeader title="Balance" />
            {loadingBalance ? (
              <LoadingState label="Loading balance..." />
            ) : (
              <>
                <FormError message={balanceError} />
                {wallet ? (
                  <div className="mt-3">
                    <PriceRow label="Current Balance" amount={wallet.currentBalance} />
                    <p className="mt-2 text-xs text-muted">Wallet ID: {wallet.walletId}</p>
                  </div>
                ) : null}
              </>
            )}
          </AppCard>

          <AppCard className="bg-[var(--card)]">
            <SectionHeader title="Recharge" />
            <FormError message={rechargeError} />
            {rechargeSuccess ? (
              <p className="mt-2 rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[#d9ffb7] px-4 py-3 text-sm font-semibold text-text">
                {rechargeSuccess}
              </p>
            ) : null}
            <div className="mt-3 space-y-3">
              <label className="block text-sm font-semibold text-text">
                <span className="mb-1 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.14em] shadow-[4px_4px_0_var(--card-border)]">
                  Amount (₹)
                </span>
                <input
                  type="number"
                  min="1"
                  step="1"
                  className="mt-2 w-full px-4 py-3"
                  placeholder="e.g. 500"
                  value={rechargeAmount}
                  onChange={(e) => setRechargeAmount(e.target.value)}
                />
              </label>
              <label className="block text-sm font-semibold text-text">
                <span className="mb-1 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.14em] shadow-[4px_4px_0_var(--card-border)]">
                  Note (optional)
                </span>
                <input
                  type="text"
                  maxLength={255}
                  className="mt-2 w-full px-4 py-3"
                  placeholder="e.g. Monthly top-up"
                  value={rechargeNote}
                  onChange={(e) => setRechargeNote(e.target.value)}
                />
              </label>
              <AppButton
                className="w-full"
                disabled={recharging || !rechargeAmount}
                onClick={onRecharge}
              >
                {recharging ? "Recharging..." : "Recharge Wallet"}
              </AppButton>
            </div>
          </AppCard>
        </div>

        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="Transaction History" />
          <FormError message={txError} />
          {loadingTx ? (
            <LoadingState label="Loading transactions..." />
          ) : transactions.length === 0 ? (
            <p className="mt-3 text-sm text-muted">No transactions yet.</p>
          ) : (
            <div className="mt-3 space-y-2">
              {transactions.map((tx) => (
                <div
                  key={tx.transactionId}
                  className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] p-3 shadow-[4px_4px_0_var(--card-border)]"
                >
                  <div className="flex items-center justify-between gap-2">
                    <div>
                      <p className="text-sm font-black uppercase tracking-[0.06em]">
                        {tx.transactionType}
                      </p>
                      <p className="text-xs text-muted">{tx.paymentSource}</p>
                      {tx.reason ? <p className="text-xs text-muted">{tx.reason}</p> : null}
                      {tx.orderId ? (
                        <p className="text-xs text-muted">Order #{tx.orderId}</p>
                      ) : null}
                    </div>
                    <div className="flex flex-col items-end gap-1">
                      <StatusBadge
                        text={`${tx.transactionType === "CREDIT" ? "+" : "-"}₹${tx.amount}`}
                        tone={txTone(tx.transactionType)}
                      />
                      <span className="text-[10px] text-muted">
                        {new Date(tx.createdAt).toLocaleString()}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}

          {txTotalPages > 1 ? (
            <div className="mt-4 flex items-center justify-between">
              <AppButton
                variant="outline"
                disabled={txPage === 0 || loadingTx}
                onClick={() => onPageChange(txPage - 1)}
              >
                Prev
              </AppButton>
              <span className="text-xs font-semibold text-muted">
                Page {txPage + 1} / {txTotalPages}
              </span>
              <AppButton
                variant="outline"
                disabled={txPage >= txTotalPages - 1 || loadingTx}
                onClick={() => onPageChange(txPage + 1)}
              >
                Next
              </AppButton>
            </div>
          ) : null}
        </AppCard>
      </div>
    </main>
  );
}
