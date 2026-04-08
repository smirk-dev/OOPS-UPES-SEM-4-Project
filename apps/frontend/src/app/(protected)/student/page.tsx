"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { AppCard } from "@/components/ui/AppCard";
import { AppButton } from "@/components/ui/AppButton";
import { EmptyState } from "@/components/ui/EmptyState";
import { FormError } from "@/components/ui/FormError";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { PriceRow } from "@/components/ui/PriceRow";
import { SectionHeader } from "@/components/ui/SectionHeader";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { getCatalogProducts, type CatalogProduct } from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";
import { cartStorage } from "@/lib/cart-storage";

const categories = ["", "ESSENTIALS", "SNACKS", "BEVERAGES", "DAIRY", "PRODUCE", "READY_TO_EAT"];
const verticals = ["", "GROCERY", "RESTAURANT"];
const stockStatuses = ["", "IN_STOCK", "LOW_STOCK", "UNAVAILABLE"];

function stockTone(status: string): "success" | "warn" | "neutral" {
  if (status === "IN_STOCK") return "success";
  if (status === "LOW_STOCK") return "warn";
  return "neutral";
}

export default function StudentPage() {
  const [products, setProducts] = useState<CatalogProduct[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [vertical, setVertical] = useState("");
  const [category, setCategory] = useState("");
  const [stockStatus, setStockStatus] = useState("");
  const [cartVersion, setCartVersion] = useState(0);

  const token = authStorage.getToken();

  useEffect(() => {
    const loadCatalog = async () => {
      if (!token) {
        setError("Session missing. Please login again.");
        setLoading(false);
        return;
      }

      setLoading(true);
      setError("");

      try {
        const response = await getCatalogProducts(token, {
          vertical: vertical || undefined,
          category: category || undefined,
          stockStatus: stockStatus || undefined,
          page: 0,
          size: 18,
        });
        setProducts(response.items);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load products.");
      } finally {
        setLoading(false);
      }
    };

    void loadCatalog();
  }, [token, vertical, category, stockStatus]);

  const totalQuickAdds = cartStorage.getSummary().itemCount;

  const onQuickAdd = (product: CatalogProduct) => {
    cartStorage.addItem({
      productId: product.id,
      name: product.name,
      vendorShopName: product.vendorShopName,
      category: product.category,
      vertical: product.vertical,
      mrp: product.mrp,
      currentPrice: product.currentPrice,
      stockStatus: product.stockStatus,
    });
    setCartVersion((value) => value + 1);
  };

  return (
    <main className="mt-6 space-y-4">
      <PageHeader
        title="Marketplace"
        subtitle="Live catalog from backend APIs with real filters and pricing fields"
      />

      <AppCard>
        <SectionHeader
          title="Filters"
          action={
            <div className="flex items-center gap-2">
              <span className="text-sm text-muted">Quick Adds: {totalQuickAdds}</span>
              <Link href="/student/checkout">
                <AppButton variant="outline">Checkout</AppButton>
              </Link>
            </div>
          }
        />

        <div className="mt-3 grid gap-3 sm:grid-cols-3">
          <label className="text-sm">
            Vertical
            <select
              className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
              value={vertical}
              onChange={(event) => setVertical(event.target.value)}
            >
              {verticals.map((item) => (
                <option key={item || "ALL"} value={item}>
                  {item || "All"}
                </option>
              ))}
            </select>
          </label>

          <label className="text-sm">
            Category
            <select
              className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
              value={category}
              onChange={(event) => setCategory(event.target.value)}
            >
              {categories.map((item) => (
                <option key={item || "ALL"} value={item}>
                  {item || "All"}
                </option>
              ))}
            </select>
          </label>

          <label className="text-sm">
            Availability
            <select
              className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
              value={stockStatus}
              onChange={(event) => setStockStatus(event.target.value)}
            >
              {stockStatuses.map((item) => (
                <option key={item || "ALL"} value={item}>
                  {item || "All"}
                </option>
              ))}
            </select>
          </label>
        </div>
      </AppCard>

      <FormError message={error} />

      {loading ? <LoadingState label="Loading catalog..." /> : null}

      {!loading && products.length === 0 ? (
        <EmptyState title="No matching products" description="Try relaxing one or more filters." />
      ) : null}

      {!loading && products.length > 0 ? (
        <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {products.map((product) => {
            const unavailable = product.stockStatus === "UNAVAILABLE";

            return (
              <AppCard key={product.id}>
                <div className="flex items-start justify-between gap-2">
                  <h2 className="text-base font-semibold">{product.name}</h2>
                  <StatusBadge text={product.stockStatus} tone={stockTone(product.stockStatus)} />
                </div>

                <p className="mt-1 text-xs uppercase tracking-wide text-muted">{product.vendorShopName}</p>
                <p className="mt-1 text-xs text-muted">{product.vertical} • {product.category}</p>

                <div className="mt-3 space-y-1">
                  <PriceRow label="Current Price" amount={product.currentPrice} />
                  <PriceRow label="MRP" amount={product.mrp} muted />
                  <PriceRow label="Savings" amount={product.savings} />
                </div>

                <div className="mt-3 flex items-center justify-between">
                  <span className="text-xs text-muted">Flash: {product.flashDiscountPercent}%</span>
                  <AppButton disabled={unavailable} onClick={() => onQuickAdd(product)}>
                    {unavailable ? "Unavailable" : "Quick Add"}
                  </AppButton>
                </div>
              </AppCard>
            );
          })}
        </section>
      ) : null}
    </main>
  );
}
