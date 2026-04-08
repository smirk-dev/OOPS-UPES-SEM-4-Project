$ErrorActionPreference = "Stop"

Write-Host "[release-check] Backend tests and package" -ForegroundColor Cyan
& "C:\tools\apache-maven-3.9.9\bin\mvn.cmd" -q -f "apps/backend/pom.xml" test
& "C:\tools\apache-maven-3.9.9\bin\mvn.cmd" -q -f "apps/backend/pom.xml" package -DskipTests

Write-Host "[release-check] Frontend lint and build" -ForegroundColor Cyan
Push-Location "apps/frontend"
try {
    npm run lint
    npm run build
} finally {
    Pop-Location
}

Write-Host "[release-check] All checks passed." -ForegroundColor Green
