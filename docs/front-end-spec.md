Front-End Specification

Design Language

1. Visual Identity
- Base palette: UPES Blue and White.
- Semantic accents:
	- Green for Grocery context, tags, and actionable highlights.
	- Orange for Restaurants/Cafes context and promotional cues.
- MRP and savings must be visually explicit and consistently readable.

2. Typography
- Clean sans-serif system (Inter or Roboto).
- Prioritize legibility at small mobile sizes.
- Use clear scale hierarchy for:
	- screen title,
	- section header,
	- card title,
	- metadata (MRP, stock, ETA).

3. UI Principles
- Mobile-first and thumb-friendly controls.
- Fast visual scanning: price, discount, category, stock.
- Minimal cognitive load from login to checkout.
- Consistent iconography via Lucide icon set.

Layout and Responsiveness

- Primary target: student mobile usage.
- Breakpoint behavior:
	- Mobile: single-column feed and sticky bottom actions.
	- Tablet: 2-column card grids where practical.
	- Desktop/Web: wider cards, hover states, and improved information density.
- Keep critical actions in first viewport on standard mobile screens.

Core Screens

1. Auth Screen
- Center-aligned card with two role toggles: Student and Vendor.
- Input fields are minimal and distraction-free.
- Clear validation states and role-specific helper text.
- No marketplace access before successful authentication.

2. The Pivot (Vertical Selector)
- Two primary cards: Groceries and Restaurants/Cafes.
- Interaction:
	- Web: hover scale/elevation feedback.
	- Mobile: tap ripple/press feedback.
- Each card includes icon, short description, and entry CTA.

3. Marketplace View
- Product grid/list hybrid optimized for quick scan.
- Product card required fields:
	- item name,
	- current price,
	- MRP badge,
	- savings indicator,
	- stock state,
	- quick add button.
- Filters: category and basic availability.
- Optional sticky mini-cart summary on mobile.

4. Product Pricing Display Rules
- MRP always visible near selling price.
- Current price visually dominant.
- MRP styled as secondary (for example strikethrough).
- Savings value shown both per-item and at checkout summary.

5. Checkout (One-Page)
- Single-screen checkout flow to reduce drop-off.
- Sections in order:
	- delivery zone confirmation,
	- cart summary,
	- wallet payment (default selected),
	- price breakdown,
	- place order CTA.
- Show discount explanation labels (flash discount and cluster discount when applied).

6. Wallet UX
- Wallet balance visible in header or checkout summary.
- If insufficient balance:
	- show clear warning,
	- provide recharge CTA,
	- prevent order placement until resolved.

7. Night Mode (Exam Mode)
- Trigger after 10:00 PM local time.
- CSS variable swap to dark theme.
- Neon yellow highlights for Study Essentials.
- Preserve contrast and readability for long-night usage.

Vendor Experience Screens

1. Vendor Dashboard
- Quick metrics: active items, low stock, open orders.
- Shortcut actions for flash discount and stock updates.

2. Product Management
- Add/edit product details including MRP and current price.
- Toggle stock status quickly.
- Flash discount toggle with immediate visual confirmation.

3. Vendor Orders
- Reverse-chronological order list.
- Zone and timestamp visibility to support batch prep.

Interaction and Motion Guidelines

- Keep motion purposeful and short.
- Use lightweight transitions for card hover, selection, and CTA feedback.
- Avoid excessive animation during checkout and payment-critical moments.

Accessibility and Usability

- Touch targets should be comfortably tappable on mobile.
- Sufficient color contrast in both normal and night themes.
- Text and icon meaning should not rely on color alone.
- Error messages must be explicit and actionable.

Implementation Notes (Next.js + Tailwind)

- Use App Router route groups for role-based layout separation.
- Centralize theme tokens via CSS variables for day/night and vertical accents.
- Keep reusable primitives for card, badge, price-row, and CTA button components.
- Enforce consistent spacing and typography tokens for predictable UI quality.