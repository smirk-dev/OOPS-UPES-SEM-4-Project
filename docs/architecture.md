System Architecture

This project follows a clean web-client plus API-server architecture tuned for a campus-scale delivery product where pricing transparency and time-window discount logic are critical.

Tech Stack

- Frontend: Next.js (App Router), React, Tailwind CSS, Lucide Icons.
- Backend: Java 17+ with Spring Boot, Spring Security, Spring Data JPA.
- Database: PostgreSQL for durable relational data (users, products, orders, wallet ledger).
- Cache/Realtime Window State: Redis to track 10-minute cluster windows per delivery zone.

High-Level Component View

1. Frontend (Next.js)
- Handles role-aware UI flows for Student and Vendor.
- Calls backend REST APIs for auth, catalog, wallet, and order actions.
- Maintains only short-lived client state; source of truth remains backend.

2. Backend API (Spring Boot)
- Exposes authenticated endpoints.
- Enforces role-based authorization and pricing rules.
- Coordinates PostgreSQL persistence and Redis-based cluster checks.

3. PostgreSQL
- Stores core business entities and immutable wallet/order records.
- Supports reporting and audit queries for transparency.

4. Redis
- Maintains sliding-window counters keyed by zone and time bucket.
- Enables low-latency checks for cluster discount eligibility.

Suggested Module Boundaries (Backend)

- auth-service: login/session/token responsibilities.
- user-service: profile, roles, wallet ownership links.
- catalog-service: products, categories, stock states, MRP versus current price.
- pricing-service: flash discount plus cluster discount evaluation.
- order-service: checkout, order creation, status transitions, zone tagging.
- wallet-service: recharge and debit flows with ledger-safe transaction handling.

Data Model Highlights

1. User
- Fields: id, role (STUDENT or VENDOR or ADMIN), name, phone/email, active flag.
- Student extensions: wallet reference.
- Vendor extensions: shop profile and fulfillment metadata.

2. Wallet and WalletTransaction
- Wallet: currentBalance, ownerUserId.
- WalletTransaction: id, walletId, type (CREDIT or DEBIT), amount, source, timestamp, orderId (nullable).
- Ledger-first approach allows reconstruction and audit.

3. Product
- Fields: id, vendorId, name, category (GROCERY or FOOD), MRP, currentPrice, stockStatus, active flag.
- Optional offer metadata for flash discount periods.

4. Order and OrderItem
- Order: id, studentId, zoneId, totalAmount, discountAmount, finalPayable, status, createdAt, clusterId (nullable).
- OrderItem: product snapshot fields (name, price-at-purchase, quantity).
- Snapshot strategy prevents historical mismatch if catalog price changes later.

5. Delivery Zone
- Zone identity maps to geo-fenced hostels, PGs, or blocks.
- Used by cluster logic and basic routing constraints.

Cluster Discount Logic (10-Minute Window)

- Trigger condition: at least 5 eligible orders in the same zone within 10 minutes.
- Discount rule: apply 10% reduction to each qualifying order.
- Redis key pattern example: cluster:zone:<zoneId>:window:<epochBucket>.
- On checkout attempt:
	1. Validate user and zone.
	2. Increment and read current zone window count in Redis.
	3. If threshold reached, mark order with clusterId and discount metadata.
	4. Persist final pricing result in PostgreSQL.

Security and Authorization

- Spring Security secures all non-public endpoints.
- Role-based restrictions:
	- Student: browse, cart, wallet, place orders.
	- Vendor: manage products, stock, flash discounts, vendor order views.
	- Admin: moderation and policy controls.
- Sensitive operations (wallet debit, order placement) must be idempotent-safe.

Reliability and Consistency Notes

- Pricing computation should happen server-side only.
- Checkout transaction should be atomic for:
	- wallet debit,
	- order creation,
	- order items insertion.
- Redis participation should be resilient; fallback path should create order without cluster discount when cache is unavailable (with alerting).

Scalability Considerations

- Read-heavy catalog endpoints can use pagination and selective caching.
- Redis avoids expensive repeated time-window scans in SQL.
- Zone-partitioned logic naturally limits high-contention hotspots.

Observability (Recommended)

- Log correlation IDs per request.
- Emit domain events/metrics for:
	- order placed,
	- cluster discount applied,
	- wallet recharge and debit,
	- flash discount activation.
- Track p95 API latency and checkout failure reasons.