# Execution Status

This file tracks implementation progress against the delivery sequence in implementation-guide.md.

## Phase Progress

- [x] Phase 1: Project skeleton and shared conventions
- [x] Phase 2: Database entities and migration strategy (baseline)
- [x] Phase 3: API contracts and DTOs (baseline + catalog)
- [x] Phase 4: Authentication and role gating (baseline)
- [x] Phase 5: Catalog browsing and product retrieval
- [x] Phase 6: Cart behavior and checkout precheck contract (frontend + backend precheck)
- [x] Phase 7: Wallet ledger, recharge, and debit (balance/recharge/transactions APIs added; debit implemented through order checkout)
- [x] Phase 8: Order creation and order lifecycle (create-order, idempotency, wallet debit, and student order history)
- [x] Phase 9: Pricing rules and cluster window logic
- [x] Phase 10: Vendor management flows
- [x] Phase 11: Admin control screens and restrictions
- [x] Phase 12: UI polish, responsiveness, and exam mode
- [x] Phase 13: Logging, monitoring, and audit fields
- [x] Phase 14: Test coverage and demo scripts
- [x] Phase 15: Deployment packaging and release checks
- [x] Phase 16: Freeze contract and prepare viva notes

## Latest Completed Work

- Added backend wallet APIs for student role:
  - GET /api/v1/wallet/balance
  - POST /api/v1/wallet/recharge
  - GET /api/v1/wallet/transactions?page=&size=
- Added backend checkout precheck endpoint:
  - POST /api/v1/checkout/precheck
  - validates zone and cart items,
  - computes subtotal, platform discount, final payable,
  - returns wallet sufficiency signal.
- Wired frontend checkout to backend precheck and wallet APIs:
  - zone now maps to backend zoneId,
  - checkout sends cart snapshot to /checkout/precheck,
  - screen renders server subtotal, platform discount, final payable, wallet balance, and sufficiency status.
- Added backend order creation API:
  - POST /api/v1/orders (student-only),
  - requires Idempotency-Key header,
  - creates order + order_items snapshot,
  - debits wallet and writes DEBIT wallet transaction in one transaction.
- Added idempotency migration:
  - V3__orders_idempotency.sql with idempotency_key and unique index per student.
- Added student order history APIs:
  - GET /api/v1/orders?page=&size=
  - GET /api/v1/orders/{orderId}
- Verified runtime API flows against live backend on port 8081:
  - login succeeded,
  - order list returned expected data,
  - order create succeeded,
  - idempotent replay returned same order without double-charging.
- Wired frontend checkout Place Order action:
  - sends zoneId + cart items to /orders,
  - sends Idempotency-Key header,
  - clears local cart on success,
  - keeps server precheck and wallet sufficiency as gate before placement.
- Added cluster-aware pricing flow:
  - Redis-backed 10-minute zone windows,
  - 5-order eligibility threshold,
  - 10% cluster discount when eligible,
  - safe no-discount fallback if Redis is unavailable,
  - checkout and order responses now expose pricing breakdown fields.
- Added vendor management backend + UI flows:
  - vendor dashboard metrics,
  - vendor product listing with stock/active filters,
  - create/update product,
  - stock state update,
  - flash discount update,
  - vendor order queue and order detail.
- Added admin backend + UI controls:
  - admin dashboard,
  - user list with role/active filters,
  - user activate/deactivate controls,
  - product activate/deactivate controls,
  - audit feed viewer.
- Added route-level role restrictions and shell improvements:
  - middleware now blocks cross-role protected routes,
  - login redirect now role-targeted,
  - header now displays role-aware shortcuts.
- Added UI polish + exam mode behavior:
  - exam mode is auto-toggled in shell (10 PM to 6 AM),
  - protected vendor/admin pages are responsive and data-driven.
- Added audit + logging support:
  - audit log table and service,
  - wallet/order/pricing/vendor/admin actions now write audit events,
  - structured service logs added for key operations.
- Added Phase 14 test and demo assets:
  - backend unit tests for auth and pricing services,
  - reusable demo verification script at scripts/demo.ps1.
- Added Phase 15 packaging and release checks:
  - backend and frontend Dockerfiles,
  - app-level docker-compose.app.yml,
  - CI workflow for backend/frontend quality gates,
  - local release-check script at scripts/release-check.ps1.
- Added Phase 16 freeze documentation:
  - API contract freeze in docs/api-contract-freeze.md,
  - viva preparation notes in docs/viva-notes.md,
  - release checklist in docs/release-checklist.md.
- Verified validations:
  - frontend production build succeeded,
  - backend compile succeeded,
  - backend tests pass with new phase-14 coverage.

## Next Concrete Target

- Project execution plan completed through Phase 16.
