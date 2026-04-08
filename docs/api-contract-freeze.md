# API Contract Freeze (Phase 16)

This document freezes the MVP API contract for viva and demo.

## Version
- Contract version: v1
- Freeze date: 2026-04-08
- Base URL: /api/v1

## Stable Endpoints

### Public
- GET /health
- POST /auth/login

### Student
- GET /catalog/products
- GET /catalog/products/{productId}
- GET /wallet/balance
- POST /wallet/recharge
- GET /wallet/transactions?page=&size=
- POST /checkout/precheck
- POST /orders (requires Idempotency-Key header)
- GET /orders?page=&size=
- GET /orders/{orderId}

### Vendor
- GET /vendor/dashboard
- GET /vendor/products?stockStatus=&activeOnly=&page=&size=
- POST /vendor/products
- PUT /vendor/products/{productId}
- PATCH /vendor/products/{productId}/stock
- PATCH /vendor/products/{productId}/flash-discount
- GET /vendor/orders?page=&size=
- GET /vendor/orders/{orderId}

### Admin
- GET /admin/dashboard
- GET /admin/users?role=&activeOnly=&page=&size=
- GET /admin/audits?page=&size=
- PATCH /admin/users/{userId}/active?active=&reason=
- PATCH /admin/products/{productId}/active?active=&reason=

## Response Envelope
All endpoints except `/health` use this envelope:

{
  "success": true,
  "data": {},
  "error": null,
  "traceId": "n/a",
  "timestamp": "2026-04-08T00:00:00Z"
}

## Frozen Business Invariants
- Auth is required for all endpoints except health and login.
- Role mismatch at login is forbidden.
- Pricing is computed server-side only.
- MRP and current price are returned separately in catalog/order views.
- Wallet is required for student checkout and is debited exactly once per idempotent order request.
- Cluster discount is zone-based with a 10-minute window and threshold of 5.
- Redis outage does not block checkout; it disables cluster discount for that path.
- Vendor actions are restricted to vendor scope.
- Admin actions are restricted to admin role.
- Audit records are written for critical moderation, pricing, wallet, and order actions.

## Change Policy After Freeze
- Non-breaking additions are allowed (optional fields, new endpoints).
- Breaking changes require a new versioned contract document.
