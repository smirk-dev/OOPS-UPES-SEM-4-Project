$ErrorActionPreference = "Stop"

Write-Host "[release-check] Backend tests and package" -ForegroundColor Cyan
$mavenCommand = if ($env:MAVEN_CMD -and $env:MAVEN_CMD.Trim().Length -gt 0) { $env:MAVEN_CMD.Trim() } else { "mvn" }
& $mavenCommand -q -f "apps/backend/pom.xml" test
& $mavenCommand -q -f "apps/backend/pom.xml" package -DskipTests

Write-Host "[release-check] Frontend lint and build" -ForegroundColor Cyan
Push-Location "apps/frontend"
try {
    npm run lint
    npm run build
} finally {
    Pop-Location
}

Write-Host "[release-check] All checks passed." -ForegroundColor Green
