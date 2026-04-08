# OOPS-UPES-SEM-4-Project

Campus delivery platform for UPES with role-aware access, transparent pricing, wallet-first checkout, and zone-based discount foundations.

## Sprint 1 Status

Sprint 1 foundation is implemented with:

- Monorepo structure for backend, frontend, and shared contracts.
- Spring Boot backend baseline with auth, JWT security, health endpoint, and global error envelope.
- Flyway migrations for initial schema and seed data.
- Next.js App Router frontend baseline with login flow, protected route middleware, and reusable UI primitives.
- Local development infrastructure with PostgreSQL and Redis via Docker Compose.

## Repository Structure

- `apps/backend`: Java 17 Spring Boot API.
- `apps/frontend`: Next.js 14 App Router frontend.
- `packages/contracts`: Shared enum and API envelope JSON contracts.
- `docs`: PRD, architecture, front-end specification, and implementation guide.

## Prerequisites

- Java 17+
- Maven 3.9+
- Node.js 20+
- npm 10+
- Docker Desktop (for PostgreSQL and Redis)

## Quick Start

1. Start infrastructure:
   - `docker compose up -d`

2. Start backend:
   - `cd apps/backend`
   - Copy `.env.example` values into your environment.
   - `mvn spring-boot:run`

3. Start frontend:
   - `cd apps/frontend`
   - `npm install`
   - `npm run dev`

4. Open app:
   - Frontend: `http://localhost:3000`
   - Backend health: `http://localhost:8080/api/v1/health`

## Demo Credentials

These are seeded for local demo:

- Student: `student1` / `Student@123`
- Vendor: `vendor1` / `Vendor@123`
- Admin: `admin1` / `Admin@123`

## Security Notes

- JWT-based stateless auth is active for protected endpoints.
- Public endpoints are restricted to health and login only.
- Role mismatch is explicitly blocked at login.

## Next Sprint Focus

- Sprint 2: catalog listing and filtering APIs, plus marketplace UI.
- Sprint 3: cart and checkout transaction flow.
- Sprint 4: wallet ledger operations and recharge flow.
