Param(
    [string]$BaseUrl = "http://localhost",
    [string]$OutputFile = "DrivingSchoolManagementSystem-API-1.0.0.swagger_collection.json",
    [string]$OutputFormat = "json"  # json or yaml
)

Write-Host "=== Combined OpenAPI Generator for Driving School Management System ===" -ForegroundColor Cyan
Write-Host ""

# -------------------------
# Define all services
# -------------------------
$services = @(
    @{ Name = "Student Service";    Port = 8081; DocsPath = "/api-docs"; BasePath = "/api/students" },
    @{ Name = "Scheduling Service"; Port = 8082; DocsPath = "/api-docs"; BasePath = "/api/lessons" },
    @{ Name = "Instructor Service"; Port = 8086; DocsPath = "/api-docs"; BasePath = "/api/instructors" },
    @{ Name = "Vehicle Service";    Port = 8083; DocsPath = "/api-docs"; BasePath = "/api/vehicles" },
    @{ Name = "Payment Service";    Port = 8084; DocsPath = "/api-docs"; BasePath = "/api/payments" }
)

# -------------------------
# Temp directory
# -------------------------
$tempDir = Join-Path -Path $PSScriptRoot -ChildPath ".tmp-openapi"
if (Test-Path $tempDir) {
    Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
}
New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

# -------------------------
# Fetch OpenAPI specs from each service
# -------------------------
$serviceSpecs = @()

foreach ($service in $services) {
    $serviceName = $service.Name
    $openApiUrl  = "{0}:{1}{2}" -f $BaseUrl, $service.Port, $service.DocsPath

    $safeName    = ($serviceName -replace ' ', '-').ToLower()
    $openApiFile = Join-Path $tempDir ("{0}-openapi.json" -f $safeName)

    Write-Host "[Service] Processing $serviceName" -ForegroundColor Yellow
    Write-Host "  OpenAPI URL: $openApiUrl"

    try {
        Invoke-RestMethod -Method GET -Uri $openApiUrl -OutFile $openApiFile -ErrorAction Stop
        $spec = Get-Content $openApiFile -Raw | ConvertFrom-Json
        
        $serviceSpecs += [PSCustomObject]@{
            Name       = $serviceName
            Spec       = $spec
            BasePath   = $service.BasePath
        }
        
        Write-Host "  [OK] OpenAPI fetched successfully" -ForegroundColor Green
    } catch {
        Write-Warning "  [FAILED] Failed to fetch OpenAPI for $serviceName (is the service running?). Skipping."
        Write-Warning "    Error: $($_.Exception.Message)"
    }
}

if (-not $serviceSpecs.Count) {
    Write-Error "No services were successfully processed. Make sure at least one service is running."
    exit 1
}

Write-Host ""
Write-Host "[Merge] Combining OpenAPI specs from $($serviceSpecs.Count) service(s)..." -ForegroundColor Cyan

# -------------------------
# Merge OpenAPI specs
# -------------------------
$combinedSpec = @{
    openapi = "3.0.1"
    info = @{
        title = "Driving School Management System API"
        description = "Combined API documentation for all microservices in the Driving School Management System. All endpoints are accessible through the API Gateway."
        version = "1.0.0"
        contact = @{
            name = "Driving School API Support"
        }
    }
    servers = @(
        @{
            url = "http://localhost:8080"
            description = "API Gateway (Development)"
        }
    )
    tags = @()
    paths = @{}
    components = @{
        schemas = @{}
        responses = @{}
        parameters = @{}
        requestBodies = @{}
        securitySchemes = @{}
    }
}

# Helper function to extract all paths from spec (preserve all paths, don't filter by BasePath)
function Get-AllPaths {
    param(
        [Parameter(Mandatory = $true)]
        [object]$Spec
    )
    
    # Extract all paths from the spec - keep them as they are
    $allPaths = @{}
    
    # Get paths from the spec
    if ($Spec.paths) {
        $pathsObj = $Spec.paths
        # Handle both hashtable and PSCustomObject
        if ($pathsObj -is [hashtable] -or $pathsObj.GetType().Name -eq 'Hashtable') {
            foreach ($pathKey in $pathsObj.Keys) {
                $originalPath = $pathKey
                $pathValue = $pathsObj[$pathKey]
                
                # Keep original path as-is (it's already correct from the service)
                $newPath = $originalPath
                
                # Ensure path starts with /
                if (-not $newPath.StartsWith("/")) {
                    $newPath = "/" + $newPath
                }
                
                $allPaths[$newPath] = $pathValue
            }
        } else {
            # PSCustomObject
            foreach ($path in $pathsObj.PSObject.Properties) {
                $originalPath = $path.Name
                $pathValue = $path.Value
                
                $newPath = $originalPath
                if (-not $originalPath.StartsWith("/")) {
                    $newPath = "/" + $originalPath
                }
                
                $allPaths[$newPath] = $pathValue
            }
        }
    }
    
    return $allPaths
}

# Merge each service spec
foreach ($svc in $serviceSpecs) {
    $serviceName = $svc.Name
    $spec = $svc.Spec
    $basePath = $svc.BasePath
    
    Write-Host "  Merging $serviceName..." -ForegroundColor Gray
    
    # Debug: Check if paths exist
    if ($spec.paths) {
        $pathCount = 0
        if ($spec.paths -is [hashtable] -or $spec.paths.GetType().Name -eq 'Hashtable') {
            $pathCount = $spec.paths.Keys.Count
        } else {
            $pathCount = ($spec.paths.PSObject.Properties | Measure-Object).Count
        }
        Write-Host "    Found $pathCount paths" -ForegroundColor DarkGray
    } else {
        Write-Host "    No paths found in spec" -ForegroundColor DarkYellow
    }
    
    # Merge tags
    if ($spec.tags) {
        foreach ($tag in $spec.tags) {
            # Avoid duplicates
            $existingTag = $combinedSpec.tags | Where-Object { $_.name -eq $tag.name }
            if (-not $existingTag) {
                $combinedSpec.tags += $tag
            }
        }
    }
    
    # Merge paths - get ALL paths from the spec (don't filter by BasePath)
    if ($spec.paths) {
        $allPaths = Get-AllPaths -Spec $spec
        
        Write-Host "    Paths found: $($allPaths.Keys -join ', ')" -ForegroundColor DarkGray
        
        foreach ($pathKey in $allPaths.Keys) {
            $pathValue = $allPaths[$pathKey]
            
            # If path already exists, merge operations
            if ($combinedSpec.paths.ContainsKey($pathKey)) {
                $existingPath = $combinedSpec.paths[$pathKey]
                
                # Convert to hashtable if needed for merging
                if ($existingPath -isnot [hashtable]) {
                    $existingPathHash = @{}
                    foreach ($prop in $existingPath.PSObject.Properties) {
                        if ($prop.Name -notmatch '^\$') {
                            $existingPathHash[$prop.Name] = $prop.Value
                        }
                    }
                    $existingPath = $existingPathHash
                }
                
                # Merge HTTP methods from new path
                if ($pathValue -is [hashtable]) {
                    foreach ($methodKey in $pathValue.Keys) {
                        $existingPath[$methodKey] = $pathValue[$methodKey]
                    }
                } else {
                    foreach ($method in $pathValue.PSObject.Properties) {
                        if ($method.Name -notmatch '^\$') {
                            $existingPath[$method.Name] = $method.Value
                        }
                    }
                }
                
                $combinedSpec.paths[$pathKey] = $existingPath
            } else {
                $combinedSpec.paths[$pathKey] = $pathValue
            }
        }
    }
    
    # Merge components
    if ($spec.components) {
        # Helper function to merge component objects
        function Merge-Component {
            param(
                [hashtable]$target,
                [object]$source,
                [string]$componentName
            )
            
            if ($source) {
                if ($source -is [hashtable] -or $source.GetType().Name -eq 'Hashtable') {
                    foreach ($key in $source.Keys) {
                        if (-not $target.ContainsKey($key)) {
                            $target[$key] = $source[$key]
                        }
                    }
                } else {
                    foreach ($prop in $source.PSObject.Properties) {
                        if ($prop.Name -notmatch '^\$' -and -not $target.ContainsKey($prop.Name)) {
                            $target[$prop.Name] = $prop.Value
                        }
                    }
                }
            }
        }
        
        # Merge schemas (keep original names, don't prefix - OpenAPI references need original names)
        if ($spec.components.schemas) {
            Merge-Component -target $combinedSpec.components.schemas -source $spec.components.schemas -componentName "schemas"
        }
        
        # Merge responses
        if ($spec.components.responses) {
            Merge-Component -target $combinedSpec.components.responses -source $spec.components.responses -componentName "responses"
        }
        
        # Merge parameters
        if ($spec.components.parameters) {
            Merge-Component -target $combinedSpec.components.parameters -source $spec.components.parameters -componentName "parameters"
        }
        
        # Merge requestBodies
        if ($spec.components.requestBodies) {
            Merge-Component -target $combinedSpec.components.requestBodies -source $spec.components.requestBodies -componentName "requestBodies"
        }
        
        # Merge securitySchemes
        if ($spec.components.securitySchemes) {
            Merge-Component -target $combinedSpec.components.securitySchemes -source $spec.components.securitySchemes -componentName "securitySchemes"
        }
    }
}

# -------------------------
# Convert to JSON and save
# -------------------------
$outputPath = Join-Path -Path $PSScriptRoot -ChildPath ".." -Resolve | Join-Path -ChildPath $OutputFile

Write-Host ""
Write-Host "[Output] Saving combined OpenAPI spec..." -ForegroundColor Cyan
Write-Host "  Output file: $outputPath"

try {
    # Convert hashtable to JSON with proper formatting
    # Use -Compress:$false for readable format and high depth for nested objects
    $jsonContent = $combinedSpec | ConvertTo-Json -Depth 50 -Compress:$false
    
    # Save to file with UTF8 encoding (no BOM)
    [System.IO.File]::WriteAllText($outputPath, $jsonContent, [System.Text.UTF8Encoding]::new($false))
    
    Write-Host "  [OK] Combined OpenAPI spec saved successfully!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Summary:" -ForegroundColor Cyan
    Write-Host "  - Services processed: $($serviceSpecs.Count)"
    
    # Count paths correctly
    $pathCount = 0
    if ($combinedSpec.paths -is [hashtable] -or $combinedSpec.paths.GetType().Name -eq 'Hashtable') {
        $pathCount = $combinedSpec.paths.Keys.Count
    } else {
        $pathCount = ($combinedSpec.paths.PSObject.Properties | Measure-Object).Count
    }
    Write-Host "  - Total paths: $pathCount"
    
    Write-Host "  - Total tags: $($combinedSpec.tags.Count)"
    
    # Count schemas
    $schemaCount = 0
    if ($combinedSpec.components.schemas -is [hashtable] -or $combinedSpec.components.schemas.GetType().Name -eq 'Hashtable') {
        $schemaCount = $combinedSpec.components.schemas.Keys.Count
    } else {
        $schemaCount = ($combinedSpec.components.schemas.PSObject.Properties | Measure-Object).Count
    }
    Write-Host "  - Total schemas: $schemaCount"
    
    Write-Host "  - Output file: $outputPath"
    Write-Host ""
    Write-Host "You can now:" -ForegroundColor Yellow
    Write-Host "  1. View it in Swagger UI: https://editor.swagger.io/ (upload the file)"
    Write-Host "  2. Use it with Postman: Import -> File -> Select the JSON file"
    Write-Host "  3. Share it with your team for API documentation"
    
} catch {
    Write-Error "Failed to save combined OpenAPI spec: $($_.Exception.Message)"
    exit 1
}

# -------------------------
# Cleanup temp files
# -------------------------
try {
    Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue
} catch {
    # Ignore cleanup errors
}

Write-Host ""
Write-Host "Done! [SUCCESS]" -ForegroundColor Green

