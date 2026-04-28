# OOPS-UPES-SEM-4-Project

Campus delivery platform for UPES with role-aware access, transparent pricing, wallet-first checkout, and zone-based discount foundations.

## Overview

This repository contains the full campus delivery product for UPES students, vendors, and admins. The application is designed to make ordering simple for students, catalog and order handling simple for vendors, and pricing and access control easy to understand and audit.

The implementation follows the documents in `docs/` and is currently complete through phases 1 to 16 in `docs/implementation-guide.md`.

## Current Status

The project is implemented end to end for the current scope.

- The backend includes auth, catalog, pricing, wallet, checkout, orders, vendor, admin, and audit-related modules.
- The frontend includes role-aware App Router screens for student, vendor, and admin flows.
- The shared contracts package keeps backend and frontend aligned.
- Local test, demo, deployment, CI, and release documentation has been added.
- The last phase includes contract freeze notes and viva preparation material.

## Product Goals

- Make ordering fast and low-friction for students.
- Keep price transparency visible at every step.
- Make wallet payment the default checkout path.
- Keep student, vendor, and admin access separated.
- Support simple cluster-based pricing rules for delivery zones.
- Keep the implementation explainable in a viva or review.

## Repository Layout

The repository is organized as a monorepo.

- `apps/backend`: Spring Boot API in Java 21.
- `apps/frontend`: Next.js 14 App Router frontend.
- `packages/contracts`: Shared JSON contracts for enums and API envelope shapes.
- `scripts`: Local automation for dev, demo, and release validation.
- `docs`: Product, architecture, implementation, release, and viva documentation.

### Backend Modules

The backend is split by domain instead of using one large service layer.

- `auth`: login and token handling.
- `catalog`: product browsing and listing.
- `pricing`: MRP, flash discount, and cluster discount logic.
- `wallet`: wallet balance and transaction ledger.
- `checkout`: order precheck and checkout orchestration.
- `orders`: order creation and lifecycle.
- `vendor`: vendor catalog and order operations.
- `admin`: internal management screens and APIs.
- `audit`: audit logging support.
- `common`: shared enums, response wrappers, and error types.
- `config`: security and request handling configuration.

### Frontend Route Groups

The frontend uses route groups to separate the user journeys.

- `src/app/(auth)`: login and authentication screens.
- `src/app/(protected)/student`: student dashboard and order flow.
- `src/app/(protected)/student/checkout`: checkout screen.
- `src/app/(protected)/vendor`: vendor dashboard and catalog management.
- `src/app/(protected)/admin`: admin surface.
- `src/app/(protected)/dashboard`: shared post-login landing area.
- `src/components/ui`: reusable UI building blocks such as cards, badges, buttons, form fields, price rows, and empty states.
- `src/lib`: auth storage, cart storage, API client, API mapping, and enum helpers.

## How The App Works

### Student Journey

- Log in and reach the student dashboard.
- Choose a vertical such as Grocery or Restaurants/Cafes.
- Browse products with MRP, current price, stock status, and savings.
- Add items to cart.
- Open checkout.
- Confirm delivery zone and review the price breakdown.
- Use wallet payment and place the order.
- Track the order status after placement.

### Vendor Journey

- Log in as a vendor.
- Reach the vendor dashboard.
- Add or edit products and stock.
- Toggle flash discounts when needed.
- Review orders by recency and zone.

### Admin Journey

- Log in with admin access.
- Reach the internal admin surface.
- Review users, orders, and audit data.
- Perform moderation or policy actions separately from normal user flows.

## Pricing And Business Rules

The product keeps pricing explicit so the user can see exactly where the final number comes from.

### MRP And Savings

- MRP is always visible.
- Current price is the most prominent number.
- Savings are shown clearly.
- Checkout shows the final payable amount before confirmation.

### Cluster Discounts

- If 5 or more eligible orders are placed in the same delivery zone within 10 minutes, the qualifying orders receive a 10% discount.
- The rule is zone-bound, not campus-wide.
- The backend stores the final pricing result so the order can be explained later.

### Flash Discounts

- Vendors can temporarily reduce the price of selected items.
- Flash discount state is visible in the UI.
- The reduction is designed to help move fast-expiring or overstocked items.

### Wallet Behavior

- Wallet is the default payment path.
- If the balance is insufficient, the user must recharge before the order can be placed.
- Wallet activity is tracked as an immutable ledger.

### Exam Mode

- Exam mode triggers after 10:00 PM.
- It is a visual and merchandising state, not a separate app flow.
- The interface keeps emphasis on late-night study essentials and clear readability.

## Tech Stack

- Frontend: Next.js 14, React 18, TypeScript, Tailwind CSS, Lucide icons.
- Backend: Java 21, Spring Boot 3.3, Spring Security, Spring Data JPA, Flyway.
- Database: PostgreSQL.
- Cache: Redis.
- Tooling: Maven, npm, Docker, GitHub Actions.

## Prerequisites

Install these before running the project locally:

- Java 21+
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker Desktop
- PowerShell if you want to use the provided `.ps1` helper scripts on Windows

## Environment Variables

### Backend Environment

Use `apps/backend/.env.example` as the reference.

| Variable | Purpose | Example |
| --- | --- | --- |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://localhost:5432/campus_delivery` |
| `DB_USERNAME` | Database user | `postgres` |
| `DB_PASSWORD` | Database password | `postgres` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `SERVER_PORT` | Backend HTTP port | `8080` |
| `JWT_SECRET` | Signing secret for tokens | strong 32+ byte secret |
| `JWT_EXPIRATION_SECONDS` | Token lifetime | `86400` |

### Frontend Environment

Use `apps/frontend/.env.example` as the reference.

| Variable | Purpose | Example |
| --- | --- | --- |
| `NEXT_PUBLIC_API_BASE_URL` | Backend API base URL used by the browser | `http://localhost:8080/api/v1` |
| `JWT_SECRET` | Secret used by Next.js middleware to validate `upes_session`; must match backend signing secret | strong 32+ byte secret |

## Local Setup

### Fastest Start

Use either helper script if you want the shortest path to a working local environment:

```powershell
./start-project.ps1
```

or

```bat
start-project.bat
```

This script starts PostgreSQL and Redis with Docker, opens the backend on port 8080, and starts the frontend on port 3000.

### Manual Start

- Start infrastructure:

```bash
docker compose up -d
```

- Start the backend:

```bash
cd apps/backend
mvn spring-boot:run
```

- Start the frontend:

```bash
cd apps/frontend
npm install
npm run dev
```

- Open the app:

```text
Frontend: http://localhost:3000
Backend health: http://localhost:8080/api/v1/health
```

## Packaged Deployment

If you want to run the containerized stack, use the combined compose file:

```bash
docker compose -f docker-compose.app.yml up --build
```

That starts PostgreSQL, Redis, the backend container, and the frontend container.

Then open:

```text
http://localhost:3000
```

The deployment assets are:

- Backend Dockerfile: `apps/backend/Dockerfile`
- Frontend Dockerfile: `apps/frontend/Dockerfile`
- App compose file: `docker-compose.app.yml`

## Scripts

### Demo Script

The demo script performs a quick API-level validation flow.

```powershell
./scripts/demo.ps1
```

Optional parameters:

- `-BaseUrl http://localhost:8080/api/v1`
- `-SkipOrderFlow`

### Release Check Script

The release check script runs the checks used for a local release gate.

```powershell
./scripts/release-check.ps1
```

It runs:

- backend tests,
- backend package build,
- frontend lint,
- frontend production build.

## Testing

Run these commands when you want to verify the project manually:

```bash
mvn -q -f apps/backend/pom.xml test
mvn -q -f apps/backend/pom.xml package -DskipTests
```

```bash
cd apps/frontend
npm run lint
npm run build
```

## CI

The repository includes a GitHub Actions workflow at `.github/workflows/ci.yml`.

It runs the main quality gates on push and pull requests to `main`:

- backend tests and package build,
- frontend lint,
- frontend build.

## Documentation

If you want to understand the project in more depth, start here:

- `docs/prd.md` for the business problem and MVP scope.
- `docs/architecture.md` for the backend and infrastructure design.
- `docs/front-end-spec.md` for the visual language and page structure.
- `docs/implementation-guide.md` for the phase-by-phase build plan.
- `docs/execution-status.md` for the current implementation progress.
- `docs/release-checklist.md` for release readiness checks.
- `docs/api-contract-freeze.md` for the frozen API contract summary.
- `docs/viva-notes.md` for presentation and explanation notes.

## Demo Credentials

The backend seed data includes local demo accounts:

- Student: `student1` / `Student@123`
- Vendor: `vendor1` / `Vendor@123`
- Admin: `admin1` / `Admin@123`

## Troubleshooting

### Docker Containers Do Not Start

- Make sure Docker Desktop is running.
- Check whether ports 5432, 6379, 8080, or 3000 are already in use.
- Restart the compose stack if a container exits during initialization.

### Backend Fails To Start

- Confirm the backend environment variables match `apps/backend/.env.example`.
- Confirm PostgreSQL and Redis are reachable.
- Confirm you are using Java 21 or a compatible JDK.

### Frontend Build Or Lint Fails

- Confirm Node.js 20+ and npm are installed.
- Reinstall frontend dependencies with `npm install`.
- Check that `NEXT_PUBLIC_API_BASE_URL` points to the backend API.

### Release Check Script Fails On Maven Path

The release check script does not rely on a fixed Maven path. It uses `MAVEN_CMD` when that environment variable is set, and otherwise runs `mvn`. If Maven is not found, set `MAVEN_CMD` to your Maven executable or ensure `mvn` is available on your `PATH`.

## Notes

- The backend uses JWT-based stateless authentication.
- Public endpoints are intentionally limited to health and login-related routes.
- Pricing and discount logic stay on the server so the result is auditable.
- Role mismatch is redirected to the correct role home instead of allowing cross-role access.
- The app is designed to be easy to explain in a viva, so the implementation favors clarity over unnecessary abstraction.
