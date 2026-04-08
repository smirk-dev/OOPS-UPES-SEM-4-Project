param(
    [string]$BaseUrl = "http://localhost:8080/api/v1",
    [switch]$SkipOrderFlow
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
    param(
        [string]$Method,
        [string]$Url,
        [object]$Body,
        [hashtable]$Headers
    )

    $invokeArgs = @{
        Method      = $Method
        Uri         = $Url
        ContentType = "application/json"
        Headers     = $Headers
    }

    if ($null -ne $Body) {
        $invokeArgs["Body"] = ($Body | ConvertTo-Json -Depth 10)
    }

    return Invoke-RestMethod @invokeArgs
}

function Login {
    param(
        [string]$Username,
        [string]$Password,
        [string]$Role
    )

    $response = Invoke-Api -Method "POST" -Url "$BaseUrl/auth/login" -Body @{
        username = $Username
        password = $Password
        role     = $Role
    } -Headers @{ "X-Request-Id" = "demo-$Role" }

    if (-not $response.success) {
        throw "Login failed for $Role"
    }

    return $response.data.token
}

Write-Host "[demo] Checking health endpoint..." -ForegroundColor Cyan
$health = Invoke-RestMethod -Method Get -Uri "$BaseUrl/health"
$healthStatus = if ($null -ne $health.data) { $health.data.status } else { $health.status }
Write-Host "[demo] Health: $healthStatus" -ForegroundColor Green

Write-Host "[demo] Logging in demo users..." -ForegroundColor Cyan
$studentToken = Login -Username "student1" -Password "Student@123" -Role "STUDENT"
$vendorToken = Login -Username "vendor1" -Password "Vendor@123" -Role "VENDOR"
$adminToken = Login -Username "admin1" -Password "Admin@123" -Role "ADMIN"

$studentHeaders = @{ "Authorization" = "Bearer $studentToken"; "X-Request-Id" = "demo-student" }
$vendorHeaders = @{ "Authorization" = "Bearer $vendorToken"; "X-Request-Id" = "demo-vendor" }
$adminHeaders = @{ "Authorization" = "Bearer $adminToken"; "X-Request-Id" = "demo-admin" }

Write-Host "[demo] Student catalog + wallet checks..." -ForegroundColor Cyan
$catalog = Invoke-Api -Method "GET" -Url "$BaseUrl/catalog/products?page=0&size=5" -Body $null -Headers $studentHeaders
$wallet = Invoke-Api -Method "GET" -Url "$BaseUrl/wallet/balance" -Body $null -Headers $studentHeaders
Write-Host "[demo] Catalog items: $($catalog.data.items.Count), Wallet balance: $($wallet.data.currentBalance)" -ForegroundColor Green

Write-Host "[demo] Vendor + admin checks..." -ForegroundColor Cyan
$vendorDashboard = Invoke-Api -Method "GET" -Url "$BaseUrl/vendor/dashboard" -Body $null -Headers $vendorHeaders
$adminDashboard = Invoke-Api -Method "GET" -Url "$BaseUrl/admin/dashboard" -Body $null -Headers $adminHeaders
Write-Host "[demo] Vendor active products: $($vendorDashboard.data.activeProducts)" -ForegroundColor Green
Write-Host "[demo] Admin users: $($adminDashboard.data.totalUsers)" -ForegroundColor Green

if (-not $SkipOrderFlow) {
    if ($catalog.data.items.Count -gt 0) {
        $product = $catalog.data.items[0]

        Write-Host "[demo] Running checkout precheck..." -ForegroundColor Cyan
        $precheck = Invoke-Api -Method "POST" -Url "$BaseUrl/checkout/precheck" -Headers $studentHeaders -Body @{
            zoneId = 1
            items  = @(@{ productId = $product.id; quantity = 1 })
        }

        Write-Host "[demo] Final payable from precheck: $($precheck.data.finalPayable)" -ForegroundColor Green
    }
}

Write-Host "[demo] Demo verification completed successfully." -ForegroundColor Green
