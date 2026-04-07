Project Overview

A unified, MRP-first delivery platform designed for UPES students (Bidholi and Kandoli campuses). The product removes multi-layer middlemen by directly aggregating nearby grocery vendors and food outlets in one app experience.

Core Problem

Students currently depend on fragmented ordering options with inconsistent pricing, variable delivery fees, and limited trust in whether they are getting fair value. Local vendors also lack a simple digital channel tailored to hostels, PG blocks, and peak-time student demand.

Product Vision

Build the default campus utility app where:
- students can order essentials and food quickly at transparent prices,
- parents can trust wallet spending patterns,
- and local merchants grow through predictable, zone-based demand.

Target Users

- Students: Need low-friction ordering, fair pricing, and reliable delivery to exact campus zones.
- Vendors: Need easy catalog management, discount controls, and clear order visibility.
- Admin (internal): Needs governance over users, vendors, and operational policy settings.

Key Features

1. Forced Auth
- Users must authenticate before any browsing or ordering.
- Role-based entry points: Student and Vendor.
- Admin access is managed separately and hidden from regular auth UX.

2. Dual-Vertical Gateway
- Primary decision screen after login: Groceries or Restaurants/Cafes.
- Users can switch verticals without logging out.
- Each vertical has independent catalog filters and merchandising.

3. Open Wallet
- UPI-rechargeable wallet for students.
- Wallet is default payment mode at checkout.
- Balance and transaction history are transparent and export-friendly for financial clarity.

4. Cluster Delivery (Dorm-Drop)
- If 5 or more eligible orders are placed in the same delivery zone within a 10-minute window, each qualifying order receives a 10% discount.
- Objective: drive batch routing efficiency and lower per-order delivery cost.
- Cluster eligibility is zone and window bound, not campus-wide.

5. Exam Night Mode
- After 10:00 PM, the UI shifts to a high-focus mode.
- Study essentials and high-utility late-night items are promoted more aggressively.
- The mode is a merchandising and visual state change, not a separate app flow.

6. Vendor Flash Discounts
- Vendors can quickly apply temporary markdowns to fast-expiring/perishable inventory.
- Discount windows are intentionally short and visible as time-sensitive offers.

Functional Requirements (MVP)

1. Authentication and Access
- Login required for all protected routes.
- Role-specific dashboard landing.
- Session handling with secure token strategy.

2. Catalog and Discovery
- View products by vertical and category.
- Show MRP and current selling price side-by-side.
- Basic stock visibility (in stock, low stock, unavailable).

3. Cart and Checkout
- Add/remove/update item quantities.
- One-page checkout with wallet preselected.
- Show savings breakdown (MRP delta, flash discount, cluster discount).

4. Wallet
- Recharge wallet via UPI flow.
- Maintain immutable ledger of credit and debit events.
- Prevent checkout if wallet balance is insufficient.

5. Orders
- Create order with zone mapping and timestamp.
- Assign cluster identifier when window criteria are met.
- Track order status from placed to delivered.

6. Vendor Operations
- Manage catalog items and stock states.
- Trigger flash discount toggles.
- View incoming orders by recency and zone.

Non-Functional Requirements

- Mobile-first UX optimized for low-attention, quick decision journeys.
- Pricing and discount calculations must be deterministic and auditable.
- Reliable time-window handling for cluster logic.
- Clear role isolation to avoid student-vendor privilege leakage.

Out of Scope for MVP

- Dynamic surge pricing.
- Multi-campus expansion beyond Bidholi/Kandoli.
- Subscription plans and loyalty tiers.
- Complex route optimization beyond zone clustering.

Success Metrics

1. Price Competitiveness
- Average order value lower than mainstream alternatives (for comparable baskets).

2. Merchant Adoption
- Active participation from key local vendors including neighborhood staples and cafes.

3. Wallet Adoption
- Percentage of student orders completed via wallet.

4. Cluster Efficiency
- Share of total orders receiving cluster discounts.

5. Repeat Behavior
- Weekly returning student users and order frequency growth.