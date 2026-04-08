Write-Host "Starting infrastructure (PostgreSQL + Redis)..." -ForegroundColor Cyan
docker compose up -d

Write-Host "Starting backend at :8080..." -ForegroundColor Cyan
Start-Process powershell -ArgumentList '-NoExit', '-Command', 'Set-Location apps/backend; mvn spring-boot:run'

Write-Host "Starting frontend at :3000..." -ForegroundColor Cyan
Set-Location apps/frontend
npm run dev
