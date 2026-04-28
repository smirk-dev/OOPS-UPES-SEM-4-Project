$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$backendDir = Join-Path $repoRoot 'apps/backend'
$frontendDir = Join-Path $repoRoot 'apps/frontend'

function Test-TcpPort {
	param(
		[string]$TargetHost,
		[int]$Port
	)

	$client = New-Object System.Net.Sockets.TcpClient
	try {
		$iar = $client.BeginConnect($TargetHost, $Port, $null, $null)
		$connected = $iar.AsyncWaitHandle.WaitOne(1200, $false)
		if (-not $connected) {
			return $false
		}
		$client.EndConnect($iar)
		return $true
	}
	catch {
		return $false
	}
	finally {
		$client.Close()
	}
}

# Some Docker setups print non-fatal warnings to stderr; keep startup resilient.
if (Get-Variable -Name PSNativeCommandUseErrorActionPreference -ErrorAction SilentlyContinue) {
	$PSNativeCommandUseErrorActionPreference = $false
}

if (-not $env:JWT_SECRET) {
	$env:JWT_SECRET = 'dev-jwt-secret-please-change-in-production'
	Write-Host "JWT_SECRET not found in environment. Using local development default." -ForegroundColor Yellow
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
	Write-Host "Docker CLI is not installed or not in PATH." -ForegroundColor Red
	Write-Host "Install Docker Desktop, reopen PowerShell, and run this script again." -ForegroundColor Yellow
	return
}

Write-Host "Checking Docker daemon..." -ForegroundColor Cyan
docker info --format '{{.ServerVersion}}' > $null 2>&1
if ($LASTEXITCODE -ne 0) {
	Write-Host "Docker daemon is not running." -ForegroundColor Red
	Write-Host "Start Docker Desktop and wait until it is ready, then rerun this script." -ForegroundColor Yellow
	return
}
# If Postgres and Redis are already running on localhost, skip bringing them up in docker
$postgresReachable = Test-TcpPort -TargetHost '127.0.0.1' -Port 5432
$redisReachable = Test-TcpPort -TargetHost '127.0.0.1' -Port 6379
if ($postgresReachable -and $redisReachable) {
	Write-Host "Infrastructure ports are already in use (5432/6379). Skipping docker compose infra startup." -ForegroundColor Yellow
}
else {
	Write-Host "Starting infrastructure (PostgreSQL + Redis)..." -ForegroundColor Cyan
	docker compose up -d --remove-orphans
	if ($LASTEXITCODE -ne 0) {
		Write-Host "Failed to start infrastructure with docker compose." -ForegroundColor Red
		Write-Host "Ensure ports 5432 and 6379 are free, or start compatible Postgres/Redis services on those ports." -ForegroundColor Yellow
		return
	}
}

Write-Host "Starting backend at :8080..." -ForegroundColor Cyan
$backendCommand = $null

if (Get-Command mvn -ErrorAction SilentlyContinue) {
	$backendCommand = 'mvn spring-boot:run'
}
elseif (Test-Path (Join-Path $backendDir 'mvnw.cmd')) {
	$backendCommand = '.\\mvnw.cmd spring-boot:run'
}
else {
	Write-Host "Maven is not available for local backend run." -ForegroundColor Yellow
	Write-Host "Falling back to full containerized app startup (backend + frontend + infra)..." -ForegroundColor Cyan
	# If local Postgres/Redis are already reachable on the host, avoid starting them inside compose
	$postgresReachable = Test-TcpPort -TargetHost '127.0.0.1' -Port 5432
	$redisReachable = Test-TcpPort -TargetHost '127.0.0.1' -Port 6379
	if ($postgresReachable -and $redisReachable) {
		Write-Host "Local Postgres/Redis detected. Building and starting backend + frontend only (no-deps)." -ForegroundColor Cyan
		# Build images then start services without their dependencies to avoid binding host ports twice
		docker compose -f docker-compose.app.yml build backend frontend
		docker compose -f docker-compose.app.yml up --no-deps --remove-orphans backend frontend
		return
	}
	else {
		docker compose -f docker-compose.app.yml up --build --remove-orphans
		return
	}
}

Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$backendDir'; $backendCommand"

Write-Host "Starting frontend at :3000..." -ForegroundColor Cyan
Set-Location $frontendDir
if (-not (Test-Path "node_modules")) {
	Write-Host "Installing frontend dependencies (npm ci)..." -ForegroundColor Cyan
	npm ci
}
npm run dev
