"use client";

import { useEffect, useState } from "react";
import { AppButton } from "@/components/ui/AppButton";
import { AppCard } from "@/components/ui/AppCard";
import { FormError } from "@/components/ui/FormError";
import { FormField } from "@/components/ui/FormField";
import { LoadingState } from "@/components/ui/LoadingState";
import { PageHeader } from "@/components/ui/PageHeader";
import { SectionHeader } from "@/components/ui/SectionHeader";
import { StatusBadge } from "@/components/ui/StatusBadge";
import {
  createVendorProduct,
  getVendorDashboard,
  getVendorOrderDetail,
  getVendorOrders,
  getVendorProducts,
  updateVendorFlashDiscount,
  updateVendorProduct,
  updateVendorStock,
  type VendorDashboardResponse,
  type VendorOrderDetail,
  type VendorOrderSummary,
  type VendorProductView,
} from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";
import { Role, StockStatus, Vertical } from "@/lib/enums";

const DEFAULT_PAGE_SIZE = 8;

const moneyFormatter = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  maximumFractionDigits: 0,
});

const emptyForm = {
  name: "",
  description: "",
  category: "",
  vertical: Vertical.GROCERY,
  mrp: "",
  currentPrice: "",
  stockStatus: StockStatus.IN_STOCK,
  active: true,
  flashDiscountPercent: "0",
};

function formatMoney(value: number) {
  return moneyFormatter.format(value);
}

function formatTime(value: string) {
  return new Date(value).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" });
}

function stockTone(stockStatus: string) {
  if (stockStatus === StockStatus.IN_STOCK) return "success";
  if (stockStatus === StockStatus.LOW_STOCK) return "warn";
  return "neutral";
}

function stockLabel(stockStatus: string) {
  return stockStatus.replace(/_/g, " ");
}

function emptyDashboard(): VendorDashboardResponse {
  return {
    vendorId: 0,
    shopName: "Vendor shop",
    activeItems: 0,
    lowStockItems: 0,
    flashEnabledItems: 0,
    openOrders: 0,
    recentSalesTotal: 0,
  };
}

export default function VendorPage() {
  const [role, setRole] = useState("");
  const [token, setToken] = useState("");
  const [dashboard, setDashboard] = useState<VendorDashboardResponse>(emptyDashboard());
  const [products, setProducts] = useState<VendorProductView[]>([]);
  const [orders, setOrders] = useState<VendorOrderSummary[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<VendorOrderDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [savingProduct, setSavingProduct] = useState(false);
  const [actionMessage, setActionMessage] = useState("");
  const [error, setError] = useState("");
  const [productPage, setProductPage] = useState(0);
  const [orderPage, setOrderPage] = useState(0);
  const [productForm, setProductForm] = useState(emptyForm);
  const [editingProductId, setEditingProductId] = useState<number | null>(null);
  const [productFilter, setProductFilter] = useState("");

  useEffect(() => {
    const storedToken = authStorage.getToken();
    setToken(storedToken);
    setRole(authStorage.getRole());

    if (!storedToken) {
      setLoading(false);
      return;
    }

    const load = async () => {
      try {
        const [dashboardResponse, productsResponse, ordersResponse] = await Promise.all([
          getVendorDashboard(storedToken),
          getVendorProducts(storedToken, {
            stockStatus: productFilter || undefined,
            page: productPage,
            size: DEFAULT_PAGE_SIZE,
          }),
          getVendorOrders(storedToken, orderPage, DEFAULT_PAGE_SIZE),
        ]);

        setDashboard(dashboardResponse);
        setProducts(productsResponse.items);
        setOrders(ordersResponse.items);
        if (ordersResponse.items.length > 0) {
          setSelectedOrder(null);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to load vendor workspace.");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [orderPage, productFilter, productPage]);

  const refresh = async (nextPage = productPage, nextOrderPage = orderPage, nextFilter = productFilter) => {
    if (!token) return;
    const [dashboardResponse, productsResponse, ordersResponse] = await Promise.all([
      getVendorDashboard(token),
      getVendorProducts(token, {
        stockStatus: nextFilter || undefined,
        page: nextPage,
        size: DEFAULT_PAGE_SIZE,
      }),
      getVendorOrders(token, nextOrderPage, DEFAULT_PAGE_SIZE),
    ]);

    setDashboard(dashboardResponse);
    setProducts(productsResponse.items);
    setOrders(ordersResponse.items);
  };

  const resetForm = () => {
    setProductForm(emptyForm);
    setEditingProductId(null);
  };

  const startEdit = (product: VendorProductView) => {
    setEditingProductId(product.id);
    setProductForm({
      name: product.name,
      description: "",
      category: product.category,
      vertical: product.vertical as Vertical,
      mrp: String(product.mrp),
      currentPrice: String(product.currentPrice),
      stockStatus: product.stockStatus as StockStatus,
      active: product.active,
      flashDiscountPercent: String(product.flashDiscountPercent),
    });
  };

  const handleProductSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setActionMessage("");

    if (!token) return;

    const mrp = Number(productForm.mrp);
    const currentPrice = Number(productForm.currentPrice);
    const flashDiscountPercent = Number(productForm.flashDiscountPercent || "0");

    if (!productForm.name.trim() || !productForm.category.trim()) {
      setError("Product name and category are required.");
      return;
    }

    if (!Number.isFinite(mrp) || mrp <= 0 || !Number.isFinite(currentPrice) || currentPrice <= 0) {
      setError("Enter valid pricing values.");
      return;
    }

    setSavingProduct(true);
    try {
      const payload = {
        name: productForm.name.trim(),
        description: productForm.description.trim() || undefined,
        category: productForm.category.trim(),
        vertical: productForm.vertical,
        mrp,
        currentPrice,
        stockStatus: productForm.stockStatus,
        active: productForm.active,
      };

      const saved = editingProductId
        ? await updateVendorProduct(token, editingProductId, payload)
        : await createVendorProduct(token, payload);

      if (Number.isFinite(flashDiscountPercent) && flashDiscountPercent >= 0) {
        await updateVendorFlashDiscount(token, saved.id, { flashDiscountPercent });
      }

      await refresh();
      resetForm();
      setActionMessage(`Saved ${saved.name} successfully.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to save product.");
    } finally {
      setSavingProduct(false);
    }
  };

  const handleStockToggle = async (product: VendorProductView) => {
    if (!token) return;
    setError("");
    try {
      const nextStatus = product.stockStatus === StockStatus.IN_STOCK ? StockStatus.LOW_STOCK : StockStatus.IN_STOCK;
      await updateVendorStock(token, product.id, { stockStatus: nextStatus });
      await refresh();
      setActionMessage(`Updated stock for ${product.name}.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to update stock.");
    }
  };

  const handleActiveToggle = async (product: VendorProductView) => {
    if (!token) return;
    setError("");
    try {
      await updateVendorProduct(token, product.id, {
        name: product.name,
        category: product.category,
        vertical: product.vertical,
        mrp: product.mrp,
        currentPrice: product.currentPrice,
        stockStatus: product.stockStatus,
        active: !product.active,
      });
      await refresh();
      setActionMessage(`${product.name} is now ${product.active ? "inactive" : "active"}.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to change product status.");
    }
  };

  const handleOrderSelect = async (orderId: number) => {
    if (!token) return;
    setError("");
    try {
      const response = await getVendorOrderDetail(token, orderId);
      setSelectedOrder(response);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to load order details.");
    }
  };

  const handleProductFilter = async (nextFilter: string) => {
    setProductFilter(nextFilter);
    setProductPage(0);
  };

  if (loading) {
    return <LoadingState label="Loading vendor tools..." />;
  }

  return (
    <main className="mt-6 space-y-5">
      <PageHeader
        title="Vendor Workspace"
        subtitle={`Signed in as ${role || Role.VENDOR}. Manage products, flash discounts, and order flow from one place.`}
      />

      {error ? <FormError message={error} /> : null}
      {actionMessage ? (
        <div className="rounded-md border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
          {actionMessage}
        </div>
      ) : null}

      <div className="grid gap-4 md:grid-cols-3">
        <AppCard>
          <div className="text-sm text-muted">Active products</div>
          <div className="mt-2 text-3xl font-bold">{dashboard.activeItems}</div>
          <div className="mt-1 text-xs text-muted">Currently saleable items</div>
        </AppCard>
        <AppCard>
          <div className="text-sm text-muted">Low stock</div>
          <div className="mt-2 text-3xl font-bold">{dashboard.lowStockItems}</div>
          <div className="mt-1 text-xs text-muted">Needs restocking attention</div>
        </AppCard>
        <AppCard>
          <div className="text-sm text-muted">Recent sales</div>
          <div className="mt-2 text-3xl font-bold">{formatMoney(dashboard.recentSalesTotal)}</div>
          <div className="mt-1 text-xs text-muted">Open orders: {dashboard.openOrders}</div>
        </AppCard>
      </div>

      <AppCard>
        <SectionHeader
          title="Product Management"
          action={
            <button type="button" className="text-sm font-semibold text-primary" onClick={() => handleProductFilter(productFilter)}>
              Refresh products
            </button>
          }
        />
        <div className="mt-4 grid gap-4 lg:grid-cols-[1.1fr_0.9fr]">
          <form className="space-y-3" onSubmit={handleProductSubmit}>
            <div className="grid gap-3 md:grid-cols-2">
              <FormField label="Product name" name="vendor-product-name" value={productForm.name} onChange={(event) => setProductForm({ ...productForm, name: event.target.value })} />
              <FormField label="Category" name="vendor-product-category" value={productForm.category} onChange={(event) => setProductForm({ ...productForm, category: event.target.value })} />
            </div>
            <div className="grid gap-3 md:grid-cols-2">
              <label className="block text-sm font-medium">
                Vertical
                <select
                  className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
                  value={productForm.vertical}
                  onChange={(event) => setProductForm({ ...productForm, vertical: event.target.value as Vertical })}
                >
                  {Object.values(Vertical).map((value) => (
                    <option key={value} value={value}>
                      {value}
                    </option>
                  ))}
                </select>
              </label>
              <label className="block text-sm font-medium">
                Stock status
                <select
                  className="mt-1 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
                  value={productForm.stockStatus}
                  onChange={(event) => setProductForm({ ...productForm, stockStatus: event.target.value as StockStatus })}
                >
                  {Object.values(StockStatus).map((value) => (
                    <option key={value} value={value}>
                      {value.replace(/_/g, " ")}
                    </option>
                  ))}
                </select>
              </label>
            </div>
            <div className="grid gap-3 md:grid-cols-3">
              <FormField label="MRP" name="vendor-product-mrp" type="number" min="0" step="1" value={productForm.mrp} onChange={(event) => setProductForm({ ...productForm, mrp: event.target.value })} />
              <FormField label="Current price" name="vendor-product-price" type="number" min="0" step="1" value={productForm.currentPrice} onChange={(event) => setProductForm({ ...productForm, currentPrice: event.target.value })} />
              <FormField label="Flash discount %" name="vendor-product-flash" type="number" min="0" max="100" step="1" value={productForm.flashDiscountPercent} onChange={(event) => setProductForm({ ...productForm, flashDiscountPercent: event.target.value })} />
            </div>
            <label className="block text-sm font-medium">
              Description
              <textarea
                className="mt-1 min-h-24 w-full rounded-md border border-[var(--card-border)] px-3 py-2"
                value={productForm.description}
                onChange={(event) => setProductForm({ ...productForm, description: event.target.value })}
                placeholder="Optional notes for your store team"
              />
            </label>
            <label className="flex items-center gap-2 text-sm font-medium">
              <input
                type="checkbox"
                checked={productForm.active}
                onChange={(event) => setProductForm({ ...productForm, active: event.target.checked })}
              />
              Active in storefront
            </label>
            <div className="flex flex-wrap gap-2">
              <AppButton type="submit" disabled={savingProduct}>
                {savingProduct ? "Saving..." : editingProductId ? "Update product" : "Create product"}
              </AppButton>
              <AppButton type="button" variant="outline" onClick={resetForm}>
                Reset form
              </AppButton>
            </div>
          </form>

          <div className="space-y-3 rounded-xl border border-[var(--card-border)] bg-slate-50 p-4">
            <div className="flex items-center justify-between gap-3">
              <h3 className="text-base font-semibold">Products</h3>
              <div className="flex gap-2 text-xs">
                {["", StockStatus.IN_STOCK, StockStatus.LOW_STOCK, StockStatus.UNAVAILABLE].map((item) => (
                  <button
                    key={item || "all"}
                    type="button"
                    onClick={() => void handleProductFilter(item)}
                    className={`rounded-full px-3 py-1 font-semibold ${productFilter === item ? "bg-primary text-white" : "bg-white text-text"}`}
                  >
                    {item ? item.replace(/_/g, " ") : "All"}
                  </button>
                ))}
              </div>
            </div>
            {products.length === 0 ? (
              <LoadingState label="No products found for the selected filter." />
            ) : (
              <div className="space-y-3">
                {products.map((product) => (
                  <div key={product.id} className="rounded-lg border border-[var(--card-border)] bg-white p-3">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <div className="flex flex-wrap items-center gap-2">
                          <h4 className="font-semibold">{product.name}</h4>
                          <StatusBadge text={stockLabel(product.stockStatus)} tone={stockTone(product.stockStatus)} />
                          {product.active ? <StatusBadge text="Active" tone="success" /> : <StatusBadge text="Inactive" tone="neutral" />}
                        </div>
                        <p className="mt-1 text-xs text-muted">
                          {product.category} • {product.vertical} • Updated {formatTime(product.updatedAt)}
                        </p>
                        <p className="mt-1 text-sm text-muted">
                          {formatMoney(product.currentPrice)} from {formatMoney(product.mrp)} • savings {formatMoney(product.savings)}
                        </p>
                      </div>
                      <div className="flex flex-wrap gap-2">
                        <AppButton type="button" variant="outline" onClick={() => startEdit(product)}>
                          Edit
                        </AppButton>
                        <AppButton type="button" variant="outline" onClick={() => void handleStockToggle(product)}>
                          Stock toggle
                        </AppButton>
                        <AppButton type="button" variant="outline" onClick={() => void handleActiveToggle(product)}>
                          {product.active ? "Deactivate" : "Activate"}
                        </AppButton>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
            <div className="flex items-center justify-between text-sm text-muted">
              <button
                type="button"
                disabled={productPage === 0}
                onClick={() => {
                  setProductPage((currentPage) => Math.max(0, currentPage - 1));
                }}
              >
                Previous
              </button>
              <span>Page {productPage + 1}</span>
              <button
                type="button"
                onClick={() => {
                  setProductPage((currentPage) => currentPage + 1);
                }}
              >
                Next
              </button>
            </div>
          </div>
        </div>
      </AppCard>

      <div className="grid gap-4 lg:grid-cols-[1fr_0.95fr]">
        <AppCard>
          <SectionHeader title="Order Queue" />
          <div className="mt-4 space-y-3">
            {orders.length === 0 ? (
              <LoadingState label="No vendor orders available right now." />
            ) : (
              orders.map((order) => (
                <button
                  key={order.orderId}
                  type="button"
                  onClick={() => void handleOrderSelect(order.orderId)}
                  className={`w-full rounded-lg border p-3 text-left transition ${
                    selectedOrder?.orderId === order.orderId ? "border-primary bg-primary/5" : "border-[var(--card-border)] bg-white"
                  }`}
                >
                  <div className="flex items-center justify-between gap-3">
                    <div>
                      <div className="font-semibold">Order #{order.orderId}</div>
                      <div className="text-xs text-muted">{order.zoneName} • {formatTime(order.createdAt)}</div>
                    </div>
                    <StatusBadge text={order.status} tone={order.status === "DELIVERED" ? "success" : order.status === "CANCELLED" ? "neutral" : "warn"} />
                  </div>
                  <div className="mt-2 text-sm text-muted">
                    {order.itemCount} items • Vendor line total {formatMoney(order.vendorLineTotal)}
                  </div>
                </button>
              ))
            )}
          </div>
        </AppCard>

        <AppCard>
          <SectionHeader title="Selected Order" />
          <div className="mt-4">
            {selectedOrder ? (
              <div className="space-y-3">
                <div className="rounded-lg border border-[var(--card-border)] bg-slate-50 p-3">
                  <div className="font-semibold">Order #{selectedOrder.orderId}</div>
                  <div className="text-sm text-muted">{selectedOrder.zoneName} • {formatTime(selectedOrder.createdAt)}</div>
                  <div className="mt-2 text-sm">Vendor total: {formatMoney(selectedOrder.vendorLineTotal)}</div>
                </div>
                <div className="space-y-2">
                  {selectedOrder.items.map((item) => (
                    <div key={item.productId} className="rounded-lg border border-[var(--card-border)] bg-white p-3 text-sm">
                      <div className="flex items-center justify-between gap-3">
                        <div className="font-medium">{item.productName}</div>
                        <div>{formatMoney(item.lineTotal)}</div>
                      </div>
                      <div className="mt-1 text-xs text-muted">
                        {item.quantity} x {formatMoney(item.unitPrice)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <LoadingState label="Select an order to inspect its line items." />
            )}
          </div>
        </AppCard>
      </div>
    </main>
  );
}
