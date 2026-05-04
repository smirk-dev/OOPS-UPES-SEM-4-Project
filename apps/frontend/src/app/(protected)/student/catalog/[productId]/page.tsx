"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useParams } from "next/navigation";
import { AppButton } from "@/components/ui/AppButton";
import { AppCard } from "@/components/ui/AppCard";
import { FormError } from "@/components/ui/FormError";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { PriceRow } from "@/components/ui/PriceRow";
import { SectionHeader } from "@/components/ui/SectionHeader";
import { StatusBadge } from "@/components/ui/StatusBadge";
import { getCatalogProduct, type CatalogProductDetail } from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";
import { cartStorage } from "@/lib/cart-storage";

function stockTone(status: string): "success" | "warn" | "neutral" {
  if (status === "IN_STOCK") return "success";
  if (status === "LOW_STOCK") return "warn";
  return "neutral";
}

export default function ProductDetailPage() {
  const params = useParams();
  const productId = Number(params.productId);

  const [product, setProduct] = useState<CatalogProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [added, setAdded] = useState(false);

  const token = authStorage.getToken();

  useEffect(() => {
    const load = async () => {
      if (!token || isNaN(productId)) {
        setError("Invalid product or session.");
        setLoading(false);
        return;
      }
      setLoading(true);
      setError("");
      try {
        const data = await getCatalogProduct(token, productId);
        setProduct(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load product.");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [token, productId]);

  const onAddToCart = () => {
    if (!product) return;
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
    setAdded(true);
  };

  if (loading) {
    return (
      <main className="mt-6 space-y-4">
        <LoadingState label="Loading product..." />
      </main>
    );
  }

  if (error || !product) {
    return (
      <main className="mt-6 space-y-4">
        <PageHeader title="Product" subtitle="Unable to load product details" />
        <FormError message={error || "Product not found."} />
        <Link href="/student">
          <AppButton variant="outline">Back to Marketplace</AppButton>
        </Link>
      </main>
    );
  }

  const unavailable = product.stockStatus === "UNAVAILABLE";

  return (
    <main className="mt-6 space-y-4">
      <PageHeader
        title={product.name}
        subtitle={`${product.vertical} · ${product.category} — sold by ${product.vendorShopName}`}
      />

      <div className="grid gap-4 lg:grid-cols-[1.2fr,0.8fr]">
        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="Product Details" />
          <div className="mt-3 space-y-3">
            <div className="flex items-center gap-3">
              <StatusBadge text={product.stockStatus} tone={stockTone(product.stockStatus)} />
              {!product.active ? (
                <StatusBadge text="INACTIVE" tone="neutral" />
              ) : null}
            </div>

            {product.description ? (
              <p className="text-sm text-text">{product.description}</p>
            ) : (
              <p className="text-sm text-muted italic">No description provided.</p>
            )}

            <div className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] p-3 shadow-[4px_4px_0_var(--card-border)]">
              <p className="mb-2 text-[11px] font-extrabold uppercase tracking-[0.14em] text-muted">
                Pricing
              </p>
              <div className="space-y-1">
                <PriceRow label="Current Price" amount={product.currentPrice} />
                <PriceRow label="MRP" amount={product.mrp} muted />
                <PriceRow label="Savings" amount={product.savings} />
              </div>
              {product.flashDiscountPercent > 0 ? (
                <p className="mt-2 text-xs font-bold text-muted">
                  Flash discount: {product.flashDiscountPercent}% applied
                </p>
              ) : null}
            </div>

            <div className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] p-3 shadow-[4px_4px_0_var(--card-border)]">
              <p className="mb-2 text-[11px] font-extrabold uppercase tracking-[0.14em] text-muted">
                Classification
              </p>
              <p className="text-sm font-semibold">
                {product.vertical} &rarr; {product.category}
              </p>
              <p className="text-xs text-muted">Sold by: {product.vendorShopName}</p>
            </div>
          </div>
        </AppCard>

        <div className="space-y-4">
          <AppCard className="bg-[var(--card)]">
            <SectionHeader title="Add to Cart" />
            <div className="mt-3 space-y-3">
              {added ? (
                <p className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[#d9ffb7] px-4 py-3 text-sm font-semibold text-text">
                  Added to cart!
                </p>
              ) : null}
              <AppButton
                className="w-full"
                disabled={unavailable || !product.active}
                onClick={onAddToCart}
              >
                {unavailable ? "Unavailable" : !product.active ? "Product Inactive" : added ? "Add Again" : "Add to Cart"}
              </AppButton>
              <Link href="/student/checkout">
                <AppButton variant="outline" className="w-full">
                  Go to Checkout
                </AppButton>
              </Link>
            </div>
          </AppCard>

          <Link href="/student">
            <AppButton variant="outline" className="w-full">
              Back to Marketplace
            </AppButton>
          </Link>
        </div>
      </div>
    </main>
  );
}
