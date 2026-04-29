import { unwrapResponse, type ApiEnvelope } from "@/lib/api-mapper";
import { Role, Vertical } from "@/lib/enums";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api/v1";

type LoginRequest = {
  username: string;
  password: string;
  role: Role;
};

type LoginResponse = {
  userId: number;
  username: string;
  role: Role;
  token: string;
  expiresInSeconds: number;
};

export type SignupRequest = {
  fullName: string;
  username: string;
  email?: string;
  phone?: string;
  password: string;
  role: Role;
  shopName?: string;
  vertical?: Vertical;
};

export type CatalogProduct = {
  id: number;
  name: string;
  category: string;
  vertical: string;
  mrp: number;
  currentPrice: number;
  savings: number;
  stockStatus: string;
  flashDiscountPercent: number;
  vendorShopName: string;
};

export type CatalogListResponse = {
  items: CatalogProduct[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

type CatalogFilters = {
  vertical?: string;
  category?: string;
  stockStatus?: string;
  page?: number;
  size?: number;
};

export type WalletBalanceResponse = {
  walletId: number;
  userId: number;
  currentBalance: number;
};

export type CheckoutPrecheckItemRequest = {
  productId: number;
  quantity: number;
};

export type CheckoutPrecheckRequest = {
  zoneId: number;
  items: CheckoutPrecheckItemRequest[];
};

export type CheckoutPrecheckItemView = {
  productId: number;
  productName: string;
  stockStatus: string;
  quantity: number;
  unitPrice: number;
  lineTotal: number;
};

export type CheckoutPrecheckResponse = {
  zoneId: number;
  zoneName: string;
  items: CheckoutPrecheckItemView[];
  subtotal: number;
  platformDiscount: number;
  clusterDiscount: number;
  totalDiscount: number;
  finalPayable: number;
  walletBalance: number;
  walletSufficient: boolean;
  clusterEligible: boolean;
  clusterWindowKey?: string | null;
};

export type CreateOrderRequest = {
  zoneId: number;
  items: CheckoutPrecheckItemRequest[];
};

export type CreateOrderResponse = {
  orderId: number;
  status: string;
  platformDiscountAmount: number;
  clusterDiscountAmount: number;
  totalDiscountAmount: number;
  finalPayable: number;
  walletBalanceAfterDebit: number;
  createdAt: string;
  idempotentReplay: boolean;
  clusterDiscountApplied: boolean;
  clusterWindowKey?: string | null;
};

export type VendorDashboardResponse = {
  vendorId: number;
  shopName: string;
  activeItems: number;
  lowStockItems: number;
  flashEnabledItems: number;
  openOrders: number;
  recentSalesTotal: number;
};

export type VendorProductView = {
  id: number;
  name: string;
  category: string;
  vertical: string;
  mrp: number;
  currentPrice: number;
  savings: number;
  stockStatus: string;
  active: boolean;
  flashDiscountPercent: number;
  updatedAt: string;
};

export type VendorProductListResponse = {
  items: VendorProductView[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type VendorProductUpsertRequest = {
  name: string;
  description?: string;
  category: string;
  vertical: string;
  mrp: number;
  currentPrice: number;
  stockStatus: string;
  active?: boolean;
};

export type VendorStockUpdateRequest = {
  stockStatus: string;
};

export type VendorFlashDiscountRequest = {
  flashDiscountPercent: number;
};

export type VendorOrderItemView = {
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type VendorOrderSummary = {
  orderId: number;
  status: string;
  zoneName: string;
  itemCount: number;
  vendorLineTotal: number;
  createdAt: string;
};

export type VendorOrderListResponse = {
  items: VendorOrderSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type VendorOrderDetail = {
  orderId: number;
  status: string;
  zoneName: string;
  vendorLineTotal: number;
  createdAt: string;
  items: VendorOrderItemView[];
};

export type AdminDashboardResponse = {
  totalUsers: number;
  activeStudents: number;
  activeVendors: number;
  activeProducts: number;
  totalOrders: number;
  auditEvents: number;
};

export type AdminUserView = {
  userId: number;
  username: string;
  fullName: string;
  role: string;
  active: boolean;
  email?: string | null;
  phone?: string | null;
  createdAt: string;
};

export type AdminUserListResponse = {
  items: AdminUserView[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type AdminAuditView = {
  auditId: number;
  actorUsername?: string | null;
  actorRole?: string | null;
  action: string;
  entityType: string;
  entityId?: number | null;
  traceId?: string | null;
  metadataJson?: string | null;
  createdAt: string;
};

export type AdminAuditListResponse = {
  items: AdminAuditView[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type AdminToggleResponse = {
  entityType: string;
  entityId: number;
  active: boolean;
  reason: string;
  updatedAt: string;
};

export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });

  const payload = (await response.json()) as ApiEnvelope<LoginResponse>;
  if (!response.ok) {
    throw new Error(payload.error?.message ?? "Login failed");
  }

  return unwrapResponse(payload);
}

export async function signup(request: SignupRequest): Promise<LoginResponse> {
  const response = await fetch(`${API_BASE_URL}/auth/signup`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });

  const payload = (await response.json()) as ApiEnvelope<LoginResponse>;
  if (!response.ok) {
    throw new Error(payload.error?.message ?? "Signup failed");
  }

  return unwrapResponse(payload);
}

export async function getCatalogProducts(
  token: string,
  filters: CatalogFilters
): Promise<CatalogListResponse> {
  const query = new URLSearchParams();

  if (filters.vertical) {
    query.set("vertical", filters.vertical);
  }
  if (filters.category) {
    query.set("category", filters.category);
  }
  if (filters.stockStatus) {
    query.set("stockStatus", filters.stockStatus);
  }

  query.set("page", String(filters.page ?? 0));
  query.set("size", String(filters.size ?? 12));

  const response = await fetch(`${API_BASE_URL}/catalog/products?${query.toString()}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });

  const payload = (await response.json()) as ApiEnvelope<CatalogListResponse>;
  if (!response.ok) {
    throw new Error(payload.error?.message ?? "Unable to load catalog");
  }

  return unwrapResponse(payload);
}

export async function getWalletBalance(token: string): Promise<WalletBalanceResponse> {
  const response = await fetch(`${API_BASE_URL}/wallet/balance`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });

  const payload = (await response.json()) as ApiEnvelope<WalletBalanceResponse>;
  if (!response.ok) {
    throw new Error(payload.error?.message ?? "Unable to load wallet balance");
  }

  return unwrapResponse(payload);
}

export async function checkoutPrecheck(
  token: string,
  request: CheckoutPrecheckRequest
): Promise<CheckoutPrecheckResponse> {
  const response = await fetch(`${API_BASE_URL}/checkout/precheck`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });

  const payload = (await response.json()) as ApiEnvelope<CheckoutPrecheckResponse>;
  if (!response.ok) {
    throw new Error(payload.error?.message ?? "Checkout precheck failed");
  }

  return unwrapResponse(payload);
}

export async function createOrder(
  token: string,
  idempotencyKey: string,
  request: CreateOrderRequest
): Promise<CreateOrderResponse> {
  const response = await fetch(`${API_BASE_URL}/orders`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "Idempotency-Key": idempotencyKey,
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });

  const payload = (await response.json()) as ApiEnvelope<CreateOrderResponse>;
  if (!response.ok) {
    throw new Error(payload.error?.message ?? "Order creation failed");
  }

  return unwrapResponse(payload);
}

export async function getVendorDashboard(token: string): Promise<VendorDashboardResponse> {
  const response = await fetch(`${API_BASE_URL}/vendor/dashboard`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<VendorDashboardResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load vendor dashboard");
  return unwrapResponse(payload);
}

export async function getVendorProducts(
  token: string,
  filters: { stockStatus?: string; activeOnly?: boolean; page?: number; size?: number }
): Promise<VendorProductListResponse> {
  const query = new URLSearchParams();
  if (filters.stockStatus) query.set("stockStatus", filters.stockStatus);
  if (typeof filters.activeOnly === "boolean") query.set("activeOnly", String(filters.activeOnly));
  query.set("page", String(filters.page ?? 0));
  query.set("size", String(filters.size ?? 10));

  const response = await fetch(`${API_BASE_URL}/vendor/products?${query.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<VendorProductListResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load vendor products");
  return unwrapResponse(payload);
}

export async function createVendorProduct(
  token: string,
  request: VendorProductUpsertRequest
): Promise<VendorProductView> {
  const response = await fetch(`${API_BASE_URL}/vendor/products`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });
  const payload = (await response.json()) as ApiEnvelope<VendorProductView>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to create product");
  return unwrapResponse(payload);
}

export async function updateVendorProduct(
  token: string,
  productId: number,
  request: VendorProductUpsertRequest
): Promise<VendorProductView> {
  const response = await fetch(`${API_BASE_URL}/vendor/products/${productId}`, {
    method: "PUT",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });
  const payload = (await response.json()) as ApiEnvelope<VendorProductView>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to update product");
  return unwrapResponse(payload);
}

export async function updateVendorStock(
  token: string,
  productId: number,
  request: VendorStockUpdateRequest
): Promise<VendorProductView> {
  const response = await fetch(`${API_BASE_URL}/vendor/products/${productId}/stock`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });
  const payload = (await response.json()) as ApiEnvelope<VendorProductView>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to update stock");
  return unwrapResponse(payload);
}

export async function updateVendorFlashDiscount(
  token: string,
  productId: number,
  request: VendorFlashDiscountRequest
): Promise<VendorProductView> {
  const response = await fetch(`${API_BASE_URL}/vendor/products/${productId}/flash-discount`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  });
  const payload = (await response.json()) as ApiEnvelope<VendorProductView>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to update flash discount");
  return unwrapResponse(payload);
}

export async function getVendorOrders(
  token: string,
  page = 0,
  size = 10
): Promise<VendorOrderListResponse> {
  const response = await fetch(`${API_BASE_URL}/vendor/orders?page=${page}&size=${size}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<VendorOrderListResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load vendor orders");
  return unwrapResponse(payload);
}

export type VendorOrderStatusUpdateResponse = {
  orderId: number;
  previousStatus: string;
  newStatus: string;
  updatedAt: string;
};

export async function updateVendorOrderStatus(
  token: string,
  orderId: number,
  status: string
): Promise<VendorOrderStatusUpdateResponse> {
  const response = await fetch(`${API_BASE_URL}/vendor/orders/${orderId}/status`, {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
      "X-Request-Id": crypto.randomUUID(),
    },
    body: JSON.stringify({ status }),
  });
  const payload = (await response.json()) as ApiEnvelope<VendorOrderStatusUpdateResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to update order status");
  return unwrapResponse(payload);
}

export async function getVendorOrderDetail(token: string, orderId: number): Promise<VendorOrderDetail> {
  const response = await fetch(`${API_BASE_URL}/vendor/orders/${orderId}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<VendorOrderDetail>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load vendor order");
  return unwrapResponse(payload);
}

export async function getAdminDashboard(token: string): Promise<AdminDashboardResponse> {
  const response = await fetch(`${API_BASE_URL}/admin/dashboard`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<AdminDashboardResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load admin dashboard");
  return unwrapResponse(payload);
}

export async function getAdminUsers(
  token: string,
  filters: { role?: string; activeOnly?: boolean; page?: number; size?: number }
): Promise<AdminUserListResponse> {
  const query = new URLSearchParams();
  if (filters.role) query.set("role", filters.role);
  if (typeof filters.activeOnly === "boolean") query.set("activeOnly", String(filters.activeOnly));
  query.set("page", String(filters.page ?? 0));
  query.set("size", String(filters.size ?? 10));

  const response = await fetch(`${API_BASE_URL}/admin/users?${query.toString()}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<AdminUserListResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load admin users");
  return unwrapResponse(payload);
}

export async function getAdminAudits(
  token: string,
  page = 0,
  size = 10
): Promise<AdminAuditListResponse> {
  const response = await fetch(`${API_BASE_URL}/admin/audits?page=${page}&size=${size}`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "X-Request-Id": crypto.randomUUID(),
    },
  });
  const payload = (await response.json()) as ApiEnvelope<AdminAuditListResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to load audit events");
  return unwrapResponse(payload);
}

export async function toggleAdminUserActive(
  token: string,
  userId: number,
  active: boolean,
  reason = "Admin moderation action"
): Promise<AdminToggleResponse> {
  const response = await fetch(
    `${API_BASE_URL}/admin/users/${userId}/active?active=${active}&reason=${encodeURIComponent(reason)}`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${token}`,
        "X-Request-Id": crypto.randomUUID(),
      },
    }
  );
  const payload = (await response.json()) as ApiEnvelope<AdminToggleResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to update user status");
  return unwrapResponse(payload);
}

export async function toggleAdminProductActive(
  token: string,
  productId: number,
  active: boolean,
  reason = "Admin moderation action"
): Promise<AdminToggleResponse> {
  const response = await fetch(
    `${API_BASE_URL}/admin/products/${productId}/active?active=${active}&reason=${encodeURIComponent(reason)}`,
    {
      method: "PATCH",
      headers: {
        Authorization: `Bearer ${token}`,
        "X-Request-Id": crypto.randomUUID(),
      },
    }
  );
  const payload = (await response.json()) as ApiEnvelope<AdminToggleResponse>;
  if (!response.ok) throw new Error(payload.error?.message ?? "Unable to update product status");
  return unwrapResponse(payload);
}
