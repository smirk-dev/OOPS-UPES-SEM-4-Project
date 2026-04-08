# Release Checklist (Phase 15)

## Pre-Release
- Run scripts/release-check.ps1.
- Ensure backend tests pass.
- Ensure frontend lint and build pass.
- Verify docker-compose.app.yml boots all services.
- Verify seeded demo users can log in.

## Environment
- Backend env: DB_URL, DB_USERNAME, DB_PASSWORD, REDIS_HOST, REDIS_PORT, JWT_SECRET.
- Frontend env: NEXT_PUBLIC_API_BASE_URL.
- Keep secrets out of repo and CI logs.

## Smoke Verification
- Health endpoint returns UP.
- Student can fetch catalog and wallet balance.
- Vendor dashboard loads.
- Admin dashboard and audits load.
- Checkout precheck returns final payable and wallet sufficiency.

## Release Artifacts
- Backend jar from Maven package.
- Frontend Next.js production build.
- Backend and frontend Docker images.

## Sign-Off
- Contract freeze reviewed in docs/api-contract-freeze.md.
- Viva notes reviewed in docs/viva-notes.md.
- docs/execution-status.md updated.
