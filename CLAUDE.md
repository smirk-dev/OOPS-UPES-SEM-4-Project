# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Campus Delivery — a full-stack food/goods ordering platform for a university campus. Demonstrates OOP principles with role-based access (Student, Vendor, Admin), wallet-based checkout, and a cluster discount mechanism.

## Architecture

**Monorepo layout:**
- `apps/backend/` — Spring Boot 3.3 (Java 21), PostgreSQL 16, Redis 7, Flyway migrations
- `apps/frontend/` — Next.js 14 App Router (TypeScript, Tailwind CSS)
- `packages/contracts/` — Shared JSON definitions for the API envelope shape and enums
- `docs/` — PRD, architecture, front-end spec, API contract freeze, viva notes

**Backend modules** (`apps/backend/src/main/java/com/upes/campusdelivery/`):
- `auth/` — Login/signup, JWT issuance
- `config/` — Spring Security, JWT filter, CORS
- `catalog/` — Product browsing (students)
- `checkout/` — Precheck (price calculation, wallet sufficiency, cluster discount eligibility)
- `orders/` — Order creation & lifecycle (PLACED → CONFIRMED → PREPARING → OUT_FOR_DELIVERY → DELIVERED)
- `pricing/` — MRP, flash discount, cluster discount logic (all pricing is server-side only)
- `vendor/` — Vendor product management & order views
- `wallet/` — Balance & immutable transaction ledger
- `admin/` — User management, order auditing
- `audit/` — Immutable audit log for all significant events
- `common/` — Shared DTOs, enums, `ApiResponse` envelope, error types

**Frontend route groups** (`apps/frontend/src/app/`):
- `(auth)/` — Login, signup (public)
- `(protected)/student/` — Catalog, cart (localStorage), checkout, order tracking
- `(protected)/vendor/` — Product management, flash discounts, order views
- `(protected)/admin/` — User management, order audits, audit logs
- `(protected)/dashboard/` — Post-login landing, redirects by role
- `components/ui/` — Shared UI components (AppButton, AppCard, StatusBadge, etc.)
- `lib/` — API client, auth storage (localStorage), cart storage

## Commands

### Backend
```bash
cd apps/backend
mvn spring-boot:run            # Start on :8080
mvn -q test                    # Run all tests
mvn -q test -Dtest=ClassName   # Run a single test class
mvn -q package -DskipTests     # Build JAR
```

### Frontend
```bash
cd apps/frontend
npm run dev                    # Start dev server on :3000
npm run build                  # Production build
npm run lint                   # ESLint
```

### Infrastructure
```bash
docker compose up -d                                  # Postgres + Redis only (local dev)
docker compose -f docker-compose.app.yml up --build   # Full containerized stack
```

### Release validation
```powershell
./scripts/release-check.ps1   # Tests + lint + builds (CI gate)
```

## Key Patterns & Constraints

**All pricing is computed server-side.** The frontend never calculates prices — it calls `POST /api/v1/checkout/precheck` and displays what the server returns. Pricing rules:
1. Flash discount: vendor-toggled, stored in `flash_discount_percent`
2. Platform discount: 5% on orders ≥ ₹300, capped at ₹40
3. Cluster discount: 10% when ≥5 orders in the same zone within 10 minutes (tracked in Redis, falls back gracefully if Redis is down)

**API response envelope** — every backend response (success or error) wraps in:
```json
{ "success": bool, "data": ..., "error": { "code", "message", "details" }, "traceId": "...", "timestamp": "..." }
```
Defined in `packages/contracts/api-envelope.json`.

**Authentication** — stateless JWT. Token stored in `localStorage` by the frontend (`lib/auth-storage.ts`). Backend validates via `JwtAuthFilter`. Roles: `STUDENT`, `VENDOR`, `ADMIN`. Use `@PreAuthorize("hasRole('...')")` for method-level checks.

**Immutable ledgers** — `wallet_transactions` and `audit_logs` are append-only. Never update or delete rows in these tables.

**Order item snapshots** — product name, MRP, and price at purchase are frozen in `order_items` at order creation time; changing the catalog does not affect historical orders.

**Redis cluster window** — key pattern `cluster:zone:<zoneId>:window:<epochBucket>`, TTL 20 minutes. If Redis is unavailable, checkout proceeds without the cluster discount (no hard failure).

**Schema migrations** — managed by Flyway. Migration files live in `apps/backend/src/main/resources/db/migration/`. Always create a new numbered `V<n>__description.sql` file; never edit existing migrations.

## Environment Variables

**Backend** (set in `docker-compose.app.yml` or local shell):
| Variable | Default |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/campus_delivery` |
| `DB_USERNAME` | `postgres` |
| `DB_PASSWORD` | `postgres` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |
| `JWT_SECRET` | *(required, no default)* |
| `JWT_EXPIRATION_SECONDS` | `86400` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` |

**Frontend** (`apps/frontend/.env.local`):
| Variable | Default |
|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `http://localhost:8080/api/v1` |

## Seed / Demo Credentials

| Role | Username | Password |
|---|---|---|
| Student | `student1` | `Student@123` |
| Vendor | `vendor1` | `Vendor@123` |
| Admin | `admin1` | `Admin@123` |
