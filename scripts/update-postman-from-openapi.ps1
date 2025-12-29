Param(
    [string]$PostmanCollectionUid = "",
    [string]$PostmanApiKey = "",
    [string]$BaseUrl = "http://localhost"
)

Write-Host "=== Spring Boot -> Postman collection updater (multi-service) ==="

# -------------------------
# Define all services
# -------------------------
$services = @(
    @{ Name = "API Gateway";        Port = 8080; DocsPath = "/api-docs"; BasePath = "/api" },
    @{ Name = "Student Service";    Port = 8081; DocsPath = "/api-docs"; BasePath = "/api/students" },
    @{ Name = "Scheduling Service"; Port = 8082; DocsPath = "/api-docs"; BasePath = "/api/lessons" },
    @{ Name = "Vehicle Service";    Port = 8083; DocsPath = "/api-docs"; BasePath = "/api/vehicles" },
    @{ Name = "Payment Service";    Port = 8084; DocsPath = "/api-docs"; BasePath = "/api/payments" }
)

# -------------------------
# Resolve API Key
# -------------------------
if (-not $PostmanApiKey) {
    $PostmanApiKey = $env:POSTMAN_API_KEY
}

if (-not $PostmanApiKey) {
    Write-Error "POSTMAN_API_KEY is not set."
    exit 1
}

# -------------------------
# Resolve Collection UID
# -------------------------
if (-not $PostmanCollectionUid) {
    $PostmanCollectionUid = $env:POSTMAN_COLLECTION_UID
}

if (-not $PostmanCollectionUid) {
    Write-Error "POSTMAN_COLLECTION_UID is not set."
    exit 1
}

# -------------------------
# Check converter tool
# -------------------------
$converter = Get-Command openapi2postmanv2 -ErrorAction SilentlyContinue
if (-not $converter) {
    Write-Error "openapi2postmanv2 not found. Install with: npm install -g openapi-to-postmanv2"
    exit 1
}

# -------------------------
# Helper: update URLs to go through API Gateway
# -------------------------
function Set-GatewayUrls {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Item,

        [Parameter(Mandatory = $true)]
        [string]$BasePath
    )

    if ($Item.request -and $Item.request.url) {
        $url = $Item.request.url

        if ($url.raw) {
            $raw = $url.raw -replace '^https?://[^/]+', ''

            if (-not $raw.StartsWith('/')) {
                $raw = '/' + $raw
            }

            if ($raw -notmatch '^/api/') {
                $raw = $BasePath + $raw
            }

            $url.raw  = "{{base_url}}" + $raw
            $url.host = @("{{base_url}}")
            $url.path = ($raw -split '/') | Where-Object { $_ -ne "" }
        }
    }

    if ($Item.item) {
        foreach ($child in $Item.item) {
            Set-GatewayUrls -Item $child -BasePath $BasePath
        }
    }
}

# -------------------------
# Temp directory
# -------------------------
$tempDir = Join-Path -Path $PSScriptRoot -ChildPath ".tmp"
if (Test-Path $tempDir) {
    Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
}
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

# -------------------------
# Fetch and convert OpenAPI for each service
# -------------------------
$serviceCollections = @()

foreach ($service in $services) {
    $serviceName = $service.Name
    $openApiUrl  = "{0}:{1}{2}" -f $BaseUrl, $service.Port, $service.DocsPath

    $safeName    = ($serviceName -replace ' ', '-').ToLower()
    $openApiFile = Join-Path $tempDir ("{0}-openapi.json" -f $safeName)
    $postmanFile = Join-Path $tempDir ("{0}-postman.json" -f $safeName)

    Write-Host ""
    Write-Host ("[Service] Processing {0}" -f $serviceName)
    Write-Host ("[Service]   OpenAPI URL: {0}" -f $openApiUrl)

    try {
        Invoke-RestMethod -Method GET -Uri $openApiUrl -OutFile $openApiFile -ErrorAction Stop
        Write-Host "[Service]   OpenAPI fetched."
    } catch {
        Write-Warning ("[Service]   Failed to fetch OpenAPI for {0} (is the service running?). Skipping." -f $serviceName)
        continue
    }

    try {
        & openapi2postmanv2 -s $openApiFile -o $postmanFile -p --pretty | Out-Null

        if (-not (Test-Path $postmanFile)) {
            Write-Warning ("[Service]   Postman collection was not generated for {0}." -f $serviceName)
            continue
        }

        $svcCollection = Get-Content $postmanFile -Raw | ConvertFrom-Json

        $serviceCollections += [PSCustomObject]@{
            Name       = $serviceName
            Collection = $svcCollection
            BasePath   = $service.BasePath
        }

        Write-Host "[Service]   Converted to Postman collection."
    } catch {
        Write-Warning ("[Service]   Failed to convert OpenAPI for {0}. Skipping." -f $serviceName)
    }
}

if (-not $serviceCollections.Count) {
    Write-Error "No services were successfully processed. Make sure at least one service is running."
    exit 1
}

# -------------------------
# Build merged collection
# -------------------------
$mergedCollection = [ordered]@{
    info = [ordered]@{
        _postman_id = "driving-school-api-collection"
        name        = "Driving School Management System API"
        description = "Combined API collection for the Driving School Management System. All requests go through the API Gateway (port 8080)."
        schema      = "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    }
    item = @()
    variable = @(
        @{
            key   = "base_url"
            value = "http://localhost:8080"
            type  = "string"
        }
    )
}

foreach ($svc in $serviceCollections) {
    $serviceName       = $svc.Name
    $serviceBasePath   = $svc.BasePath
    $serviceCollection = $svc.Collection

    $folder = [ordered]@{
        name        = $serviceName
        item        = @()
        description = "Endpoints for " + $serviceName
    }

    if ($serviceCollection.item) {
        foreach ($it in $serviceCollection.item) {
            $clone = $it | ConvertTo-Json -Depth 100 | ConvertFrom-Json
            Set-GatewayUrls -Item $clone -BasePath $serviceBasePath
            $folder.item += $clone
        }
    }

    $mergedCollection.item += $folder
}

$mergedCollectionJson = $mergedCollection | ConvertTo-Json -Depth 64

# -------------------------
# Update Postman collection
# -------------------------
Write-Host ""
Write-Host ("[Postman] Updating collection {0} ..." -f $PostmanCollectionUid)

$headers = @{
    "X-Api-Key"    = $PostmanApiKey
    "Content-Type" = "application/json"
}

$bodyObject = @{
    collection = ($mergedCollectionJson | ConvertFrom-Json)
}
$body = $bodyObject | ConvertTo-Json -Depth 64

try {
    Invoke-RestMethod `
        -Method PUT `
        -Uri ("https://api.getpostman.com/collections/{0}" -f $PostmanCollectionUid) `
        -Headers $headers `
        -Body $body `
        -ErrorAction Stop

    Write-Host "[Postman] Collection updated successfully."
    Write-Host ("[Postman] Processed {0} service(s)." -f $serviceCollections.Count)
} catch {
    Write-Error "Failed to update Postman collection."
    exit 1
}

# -------------------------
# Cleanup temp files
# -------------------------
try {
    $cleanupPath = Join-Path -Path $tempDir -ChildPath '*'
    Remove-Item $cleanupPath -Recurse -Force -ErrorAction SilentlyContinue
} catch {
}


