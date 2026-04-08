# Viva Notes (Phase 16)

## 1) Project in One Minute
- Campus delivery platform for UPES.
- Three roles: Student, Vendor, Admin.
- Two verticals: Grocery and Restaurants/Cafes.
- Transparent pricing with MRP + current price.
- Wallet-first checkout with idempotent order placement.
- Cluster discount based on zone activity in a 10-minute window.

## 2) Architecture Rationale
- Frontend: Next.js App Router for role-based protected routes.
- Backend: Spring Boot API as source of truth.
- Database: PostgreSQL for durable entities and ledgers.
- Cache: Redis for transient cluster-window counters.
- Reasoning: durable data in Postgres, fast window checks in Redis.

## 3) Key Design Decisions
- Pricing done only on backend to prevent client tampering.
- Wallet uses immutable transaction ledger (credit/debit history).
- Orders store item snapshots to preserve historical pricing truth.
- Idempotency-Key prevents duplicate wallet debit on retries.
- Role gates exist in both API auth and frontend route restrictions.

## 4) How Cluster Discount Works
- Key format: cluster:zone:{zoneId}:window:{bucket}.
- Bucket size: 10 minutes.
- Threshold: 5 eligible orders.
- Discount: 10 percent of subtotal.
- Redis unavailable: checkout continues, cluster discount becomes zero.

## 5) Demo Flow (Recommended)
- Student login -> browse catalog -> checkout precheck -> place order.
- Vendor login -> dashboard metrics -> stock or flash discount update.
- Admin login -> user moderation -> audit feed.
- Show logs/audit entry for one critical action.

## 6) Common Viva Questions
- Why server-side pricing?
  - Security and consistency across clients.
- Why order snapshots?
  - Old orders must remain explainable after product changes.
- Why Redis for cluster logic?
  - Fast, transient counters; not permanent truth.
- What if Redis fails?
  - Fallback path disables cluster discount, order still succeeds.
- How do you avoid double charge?
  - Idempotency key + transactional order and wallet write.

## 7) Production Readiness Checks Completed
- Backend compile passes.
- Frontend production build passes.
- Backend unit tests added for auth and pricing logic.
- CI workflow added for backend/frontend quality gates.
- Docker packaging added for backend and frontend.
