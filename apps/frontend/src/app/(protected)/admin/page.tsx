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
  getAdminAudits,
  getAdminDashboard,
  getAdminUsers,
  toggleAdminProductActive,
  toggleAdminUserActive,
  type AdminAuditView,
  type AdminDashboardResponse,
  type AdminUserView,
} from "@/lib/api-client";
import { authStorage } from "@/lib/auth-storage";
import { Role } from "@/lib/enums";

const DEFAULT_PAGE_SIZE = 10;

function emptyDashboard(): AdminDashboardResponse {
  return {
    totalUsers: 0,
    activeStudents: 0,
    activeVendors: 0,
    activeProducts: 0,
    totalOrders: 0,
    auditEvents: 0,
  };
}

function formatTime(value: string) {
  return new Date(value).toLocaleString("en-IN", { dateStyle: "medium", timeStyle: "short" });
}

export default function AdminPage() {
  const [token, setToken] = useState("");
  const [role, setRole] = useState("");
  const [dashboard, setDashboard] = useState<AdminDashboardResponse>(emptyDashboard());
  const [users, setUsers] = useState<AdminUserView[]>([]);
  const [audits, setAudits] = useState<AdminAuditView[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionMessage, setActionMessage] = useState("");
  const [userRoleFilter, setUserRoleFilter] = useState("");
  const [activeOnly, setActiveOnly] = useState<string>("all");
  const [userPage, setUserPage] = useState(0);
  const [auditPage, setAuditPage] = useState(0);
  const [productModerationId, setProductModerationId] = useState("");
  const [productModerationReason, setProductModerationReason] = useState("Admin moderation action");

  useEffect(() => {
    const storedToken = authStorage.getToken();
    setToken(storedToken);
    setRole(authStorage.getRole());

    if (!storedToken) {
      setLoading(false);
      return;
    }

    const load = async () => {
      setError("");
      try {
        const [dashboardResponse, usersResponse, auditsResponse] = await Promise.all([
          getAdminDashboard(storedToken),
          getAdminUsers(storedToken, {
            role: userRoleFilter || undefined,
            activeOnly: activeOnly === "all" ? undefined : activeOnly === "active",
            page: userPage,
            size: DEFAULT_PAGE_SIZE,
          }),
          getAdminAudits(storedToken, auditPage, DEFAULT_PAGE_SIZE),
        ]);
        setDashboard(dashboardResponse);
        setUsers(usersResponse.items);
        setAudits(auditsResponse.items);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to load admin workspace.");
      } finally {
        setLoading(false);
      }
    };

    void load();
  }, [activeOnly, auditPage, userPage, userRoleFilter]);

  const refresh = async () => {
    if (!token) {
      return;
    }

    const [dashboardResponse, usersResponse, auditsResponse] = await Promise.all([
      getAdminDashboard(token),
      getAdminUsers(token, {
        role: userRoleFilter || undefined,
        activeOnly: activeOnly === "all" ? undefined : activeOnly === "active",
        page: userPage,
        size: DEFAULT_PAGE_SIZE,
      }),
      getAdminAudits(token, auditPage, DEFAULT_PAGE_SIZE),
    ]);

    setDashboard(dashboardResponse);
    setUsers(usersResponse.items);
    setAudits(auditsResponse.items);
  };

  const toggleUser = async (userId: number, nextActive: boolean) => {
    if (!token) {
      return;
    }

    setError("");
    setActionMessage("");

    try {
      await toggleAdminUserActive(token, userId, nextActive, nextActive ? "User re-activated" : "User temporarily suspended");
      await refresh();
      setActionMessage(`User #${userId} is now ${nextActive ? "active" : "inactive"}.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to update user state.");
    }
  };

  const toggleProduct = async (nextActive: boolean) => {
    if (!token) {
      return;
    }

    const numericId = Number(productModerationId);
    if (!Number.isInteger(numericId) || numericId <= 0) {
      setError("Enter a valid numeric product ID for moderation.");
      return;
    }

    setError("");
    setActionMessage("");

    try {
      await toggleAdminProductActive(token, numericId, nextActive, productModerationReason.trim() || "Admin moderation action");
      await refresh();
      setActionMessage(`Product #${numericId} is now ${nextActive ? "active" : "inactive"}.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to update product state.");
    }
  };

  if (loading) {
    return <LoadingState label="Loading admin workspace..." />;
  }

  return (
    <main className="mt-6 space-y-5">
      <PageHeader
        title="Admin Workspace"
        subtitle={`Signed in as ${role || Role.ADMIN}. Govern users, product visibility, and operational audit logs from a louder control surface.`}
      />

      {error ? <FormError message={error} /> : null}
      {actionMessage ? (
        <div className="neo-panel rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[#d9ffb7] px-4 py-3 text-sm font-semibold text-text">
          {actionMessage}
        </div>
      ) : null}

      <div className="grid gap-4 md:grid-cols-3">
        <AppCard className="bg-[var(--surface-alt)]">
          <div className="text-sm font-bold uppercase tracking-[0.1em] text-muted">Users</div>
          <div className="mt-2 text-3xl font-black">{dashboard.totalUsers}</div>
          <div className="mt-1 text-xs font-medium text-muted">Students: {dashboard.activeStudents} • Vendors: {dashboard.activeVendors}</div>
        </AppCard>
        <AppCard className="bg-[var(--surface-alt-2)]">
          <div className="text-sm font-bold uppercase tracking-[0.1em] text-muted">Orders</div>
          <div className="mt-2 text-3xl font-black">{dashboard.totalOrders}</div>
          <div className="mt-1 text-xs font-medium text-muted">Active products: {dashboard.activeProducts}</div>
        </AppCard>
        <AppCard className="bg-[#d7f4c1]">
          <div className="text-sm font-bold uppercase tracking-[0.1em] text-muted">Audit events</div>
          <div className="mt-2 text-3xl font-black">{dashboard.auditEvents}</div>
          <div className="mt-1 text-xs font-medium text-muted">Operational traceability feed</div>
        </AppCard>
      </div>

      <div className="grid gap-4 xl:grid-cols-[1.15fr_0.85fr]">
        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="User Moderation" />
          <div className="mt-3 grid gap-3 md:grid-cols-3">
            <label className="block text-sm font-semibold text-text">
              <span className="mb-1 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.14em] shadow-[4px_4px_0_var(--card-border)]">
                Role
              </span>
              <select
                className="mt-2 w-full px-4 py-3"
                value={userRoleFilter}
                onChange={(event) => {
                  setUserRoleFilter(event.target.value);
                  setUserPage(0);
                }}
              >
                <option value="">All</option>
                <option value="STUDENT">STUDENT</option>
                <option value="VENDOR">VENDOR</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </label>
            <label className="block text-sm font-semibold text-text">
              <span className="mb-1 inline-flex rounded-full border-[3px] border-[var(--card-border)] bg-[var(--surface-alt-2)] px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.14em] shadow-[4px_4px_0_var(--card-border)]">
                Active state
              </span>
              <select
                className="mt-2 w-full px-4 py-3"
                value={activeOnly}
                onChange={(event) => {
                  setActiveOnly(event.target.value);
                  setUserPage(0);
                }}
              >
                <option value="all">All</option>
                <option value="active">Only active</option>
                <option value="inactive">Only inactive</option>
              </select>
            </label>
            <div className="flex items-end">
              <AppButton type="button" variant="outline" onClick={() => void refresh()}>
                Refresh
              </AppButton>
            </div>
          </div>

          <div className="mt-4 space-y-3">
            {users.length === 0 ? (
              <LoadingState label="No users found for current filters." />
            ) : (
              users.map((user) => (
                <div key={user.userId} className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--card)] p-3 shadow-[4px_4px_0_var(--card-border)]">
                  <div className="flex flex-wrap items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-black uppercase tracking-[0.06em]">{user.fullName}</h3>
                        <StatusBadge text={user.role} tone="neutral" />
                        {user.active ? <StatusBadge text="Active" tone="success" /> : <StatusBadge text="Inactive" tone="warn" />}
                      </div>
                      <p className="mt-1 text-xs text-muted">
                        @{user.username} • Created {formatTime(user.createdAt)}
                      </p>
                      <p className="mt-1 text-xs text-muted">{user.email || "No email"} • {user.phone || "No phone"}</p>
                    </div>
                    <AppButton
                      type="button"
                      variant="outline"
                      onClick={() => void toggleUser(user.userId, !user.active)}
                    >
                      {user.active ? "Deactivate" : "Activate"}
                    </AppButton>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="mt-4 flex items-center justify-between text-sm text-muted">
            <button type="button" disabled={userPage === 0} onClick={() => setUserPage((value) => Math.max(0, value - 1))}>
              Previous
            </button>
            <span>Page {userPage + 1}</span>
            <button type="button" onClick={() => setUserPage((value) => value + 1)}>
              Next
            </button>
          </div>
        </AppCard>

        <AppCard className="bg-[var(--card)]">
          <SectionHeader title="Product Moderation" />
          <form
            className="mt-3 space-y-3"
            onSubmit={(event) => {
              event.preventDefault();
              void toggleProduct(false);
            }}
          >
            <FormField
              label="Product ID"
              name="admin-product-id"
              type="number"
              min="1"
              value={productModerationId}
              onChange={(event) => setProductModerationId(event.target.value)}
            />
            <FormField
              label="Reason"
              name="admin-product-reason"
              value={productModerationReason}
              onChange={(event) => setProductModerationReason(event.target.value)}
            />
            <div className="flex flex-wrap gap-2">
              <AppButton type="submit">Deactivate Product</AppButton>
              <AppButton type="button" variant="outline" onClick={() => void toggleProduct(true)}>
                Activate Product
              </AppButton>
            </div>
          </form>

          <SectionHeader title="Audit Feed" />
          <div className="mt-3 space-y-2">
            {audits.length === 0 ? (
              <LoadingState label="No audit events found yet." />
            ) : (
              audits.map((audit) => (
                <div key={audit.auditId} className="rounded-[1rem] border-[3px] border-[var(--card-border)] bg-[var(--surface)] p-3 text-xs shadow-[4px_4px_0_var(--card-border)]">
                  <div className="flex items-center justify-between gap-2">
                    <span className="font-black uppercase tracking-[0.06em]">{audit.action}</span>
                    <span className="text-muted">{formatTime(audit.createdAt)}</span>
                  </div>
                  <p className="mt-1 text-muted">
                    {audit.actorUsername || "system"} • {audit.entityType} {audit.entityId ?? "-"}
                  </p>
                </div>
              ))
            )}
          </div>

          <div className="mt-3 flex items-center justify-between text-sm text-muted">
            <button type="button" disabled={auditPage === 0} onClick={() => setAuditPage((value) => Math.max(0, value - 1))}>
              Previous
            </button>
            <span>Page {auditPage + 1}</span>
            <button type="button" onClick={() => setAuditPage((value) => value + 1)}>
              Next
            </button>
          </div>
        </AppCard>
      </div>
    </main>
  );
}
