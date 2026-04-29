"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";
import { AppButton } from "@/components/ui/AppButton";
import { AppCard } from "@/components/ui/AppCard";
import { EmptyState } from "@/components/ui/EmptyState";
import { FormError } from "@/components/ui/FormError";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { PriceRow } from "@/components/ui/PriceRow";
import { SectionHeader } from "@/components/ui/SectionHeader";
import {
  checkoutPrecheck,
  createOrder,
  type CheckoutPrecheckResponse,
} from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";
import { cartStorage, type CartItem } from "@/lib/cart-storage";

const zones = [
  { id: 1, code: "BIDHOLI-A" },
  { id: 2, code: "BIDHOLI-B" },
  { id: 3, code: "KANDOLI-C" },
];

export default function CheckoutPage() {
  const [items, setItems] = useState<CartItem[]>(() => cartStorage.getItems());
  const [zoneId, setZoneId] = useState<number>(zones[0].id);
  const [apiError, setApiError] = useState("");
  const [syncLoading, setSyncLoading] = useState(false);
  const [placingOrder, setPlacingOrder] = useState(false);
  const [orderMessage, setOrderMessage] = useState("");
  const [precheck, setPrecheck] = useState<CheckoutPrecheckResponse | null>(null);
  const idempotencyKeyRef = useRef("");
  const token = authStorage.getToken();

  const summary = useMemo(() => {
    const subtotal = items.reduce(
      (sum, item) => sum + item.product.currentPrice * item.quantity,
      0
    );
    const mrpTotal = items.reduce((sum, item) => sum + item.product.mrp * item.quantity, 0);
    const savings = Math.max(0, mrpTotal - subtotal);

    return {
      itemCount: items.reduce((sum, item) => sum + item.quantity, 0),
      subtotal,
      savings,
      payable: subtotal,
    };
  }, [items]);

  const updateQuantity = (productId: number, nextQty: number) => {
    const nextItems = cartStorage.updateQuantity(productId, nextQty);
    setItems([...nextItems]);
  };

  useEffect(() => {
    idempotencyKeyRef.current = "";
    setOrderMessage("");
  }, [items, zoneId]);

  useEffect(() => {
    const syncCheckoutData = async () => {
      if (!token) {
        setApiError("Session missing. Please login again.");
        return;
      }

      if (items.length === 0) {
        setPrecheck(null);
        return;
      }

      setSyncLoading(true);
      setApiError("");

      try {
        const response = await checkoutPrecheck(token, {
          zoneId,
          items: items.map((item) => ({
            productId: item.product.productId,
            quantity: item.quantity,
          })),
        });

        setPrecheck(response);
      } catch (error) {
        setPrecheck(null);
        setApiError(error instanceof Error ? error.message : "Unable to run checkout precheck.");
      } finally {
        setSyncLoading(false);
      }
    };

    void syncCheckoutData();
  }, [items, zoneId, token]);

  const onPlaceOrder = async () => {
    if (!token) {
      setApiError("Session missing. Please login again.");
      return;
    }

    if (items.length === 0) {
      setApiError("Cart is empty.");
      return;
    }

    setPlacingOrder(true);
    setApiError("");
    setOrderMessage("");

    try {
      if (!idempotencyKeyRef.current) {
        idempotencyKeyRef.current = crypto.randomUUID();
      }

      const response = await createOrder(token, idempotencyKeyRef.current, {
        zoneId,
        items: items.map((item) => ({
          productId: item.product.productId,
          quantity: item.quantity,
        })),
      });

      cartStorage.clear();
      setItems([]);
      setPrecheck(null);
      setOrderMessage(
        `Order #${response.orderId} placed (${response.idempotentReplay ? "replay" : "new"}).`
      );
      idempotencyKeyRef.current = "";
    } catch (error) {
      setApiError(error instanceof Error ? error.message : "Unable to place order.");
    } finally {
      setPlacingOrder(false);
    }
  };

  if (items.length === 0) {
    return (
      <main className="mt-6 space-y-4">
        <PageHeader title="Checkout" subtitle="Your cart is empty, but the checkout shell still stays loud" />
        <EmptyState
          title="Cart is empty"
          description="Add items from marketplace to continue to checkout."
        />
        <Link href="/student">
          <AppButton>Back to Marketplace</AppButton>
        </Link>
      </main>
    );
  }

  return (
    <main className="mt-6 grid gap-4 lg:grid-cols-[1.15fr,0.85fr]">
      <section className="space-y-4">
        <PageHeader title="Checkout" subtitle="Single-screen local checkout state with a bold cost breakdown" />

        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="Delivery Zone" />
          <select
            className="mt-3 w-full px-4 py-3"
            value={zoneId}
            onChange={(event) => setZoneId(Number(event.target.value))}
          >
            {zones.map((item) => (
              <option key={item.id} value={item.id}>
                {item.code}
              </option>
            ))}
          </select>
        </AppCard>

        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="Cart Items" />
          <div className="mt-3 space-y-3">
            {items.map((item) => (
              <div
                key={item.product.productId}
                className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] p-3 shadow-[4px_4px_0_var(--card-border)]"
              >
                <div className="flex items-start justify-between gap-2">
                  <div>
                    <p className="font-black uppercase tracking-[0.06em]">{item.product.name}</p>
                    <p className="text-xs font-bold text-muted">{item.product.vendorShopName}</p>
                  </div>
                  <div className="flex items-center gap-2">
                    <AppButton
                      variant="outline"
                      onClick={() => updateQuantity(item.product.productId, item.quantity - 1)}
                    >
                      -
                    </AppButton>
                    <span className="min-w-6 text-center text-sm font-black">{item.quantity}</span>
                    <AppButton
                      variant="outline"
                      onClick={() => updateQuantity(item.product.productId, item.quantity + 1)}
                    >
                      +
                    </AppButton>
                  </div>
                </div>
                <div className="mt-2 space-y-1 text-sm">
                  <PriceRow label="Current" amount={item.product.currentPrice * item.quantity} />
                  <PriceRow label="MRP" amount={item.product.mrp * item.quantity} muted />
                </div>
              </div>
            ))}
          </div>
        </AppCard>
      </section>

      <aside className="space-y-4">
        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="Price Breakdown (Server)" />
          <FormError message={apiError} />
          {orderMessage ? (
            <p className="neo-panel rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[#d9ffb7] px-4 py-3 text-sm font-semibold text-text">{orderMessage}</p>
          ) : null}
          {syncLoading ? <LoadingState label="Syncing wallet and checkout precheck..." /> : null}
          <div className="mt-3 space-y-2">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted">Items</span>
              <span className="font-semibold">{summary.itemCount}</span>
            </div>
            <PriceRow label="Subtotal" amount={precheck?.subtotal ?? summary.subtotal} />
            <PriceRow label="Savings" amount={summary.savings} />
            <PriceRow label="Platform Discount" amount={precheck?.platformDiscount ?? 0} />
            <PriceRow label="Cluster Discount" amount={precheck?.clusterDiscount ?? 0} />
            <PriceRow label="Total Discount" amount={precheck?.totalDiscount ?? 0} />
            <PriceRow label="Final Payable" amount={precheck?.finalPayable ?? summary.payable} />
            <PriceRow label="Wallet Balance" amount={precheck?.walletBalance ?? 0} />
          </div>
          <p className="mt-3 text-xs text-muted">
            {precheck?.clusterEligible
              ? `Cluster discount is currently active${precheck.clusterWindowKey ? ` for ${precheck.clusterWindowKey}` : ""}.`
              : "Cluster discount is not active yet for this zone window."}
          </p>
          <p className="mt-3 text-xs text-muted">
            {precheck?.walletSufficient
              ? "Wallet has sufficient balance for this order draft."
              : "Wallet is insufficient. Recharge your wallet before placing this order."}
          </p>
          <AppButton
            className="mt-3 w-full"
            disabled={
              placingOrder ||
              syncLoading ||
              !precheck ||
              !precheck.walletSufficient ||
              items.length === 0
            }
            onClick={onPlaceOrder}
          >
            {placingOrder ? "Placing Order..." : "Place Order"}
          </AppButton>
        </AppCard>

        <Link href="/student">
          <AppButton variant="outline" className="w-full">
            Continue Shopping
          </AppButton>
        </Link>
      </aside>
    </main>
  );
}
