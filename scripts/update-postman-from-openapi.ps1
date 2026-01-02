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

            $url.raw  = "{{baseUrl}}" + $raw
            $url.host = @("{{baseUrl}}")
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
# Function: Create Journey Folder
# -------------------------
function New-JourneyFolder {
    return @{
        name = "Journey: Student Registration & Lesson Booking"
        item = @(
            # Step 1: Register Student
            @{
                name = "1. Register New Student - Maria Popescu"
                request = @{
                    method = "POST"
                    header = @(
                        @{ key = "Content-Type"; value = "application/json" }
                        @{ key = "Accept"; value = "*/*" }
                    )
                    body = @{
                        mode = "raw"
                        raw = (@{
                            firstName = "Maria"
                            lastName = "Popescu"
                            cnp = "2980523456789"
                            email = "maria.popescu@email.com"
                            phone = "0723456789"
                            address = "Strada Victoriei nr. 15, Bucuresti"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/students"
                        host = @("{{baseUrl}}")
                        path = @("api", "students")
                    }
                }
                event = @(
                    @{
                        listen = "test"
                        script = @{
                            type = "text/javascript"
                            exec = @(
                                "if (pm.response.code === 201 || pm.response.code === 200) {",
                                "    const response = pm.response.json();",
                                "    if (response.id || (response.data && response.data.id)) {",
                                "        const studentId = response.id || response.data.id;",
                                "        pm.collectionVariables.set('student_id', studentId.toString());",
                                "        console.log('student_id set to: ' + studentId);",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Register new student - Maria Popescu. CNP: 2980523456789"
            },
            # Step 2: Upload ID Copy
            @{
                name = "2. Upload Document - ID_COPY"
                request = @{
                    method = "POST"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/students/{{student_id}}/documents?documentType=ID_COPY&filePath=/documents/students/{{student_id}}/id_copy.pdf"
                        host = @("{{baseUrl}}")
                        path = @("api", "students", "{{student_id}}", "documents")
                        query = @(
                            @{ key = "documentType"; value = "ID_COPY"; description = "Type of document" }
                            @{ key = "filePath"; value = "/documents/students/{{student_id}}/id_copy.pdf"; description = "Path to the document file" }
                        )
                    }
                }
                description = "Upload ID copy document for student"
            },
            # Step 3: Upload Photo
            @{
                name = "3. Upload Document - PHOTO"
                request = @{
                    method = "POST"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/students/{{student_id}}/documents?documentType=PHOTO&filePath=/documents/students/{{student_id}}/photo.jpg"
                        host = @("{{baseUrl}}")
                        path = @("api", "students", "{{student_id}}", "documents")
                        query = @(
                            @{ key = "documentType"; value = "PHOTO" }
                            @{ key = "filePath"; value = "/documents/students/{{student_id}}/photo.jpg" }
                        )
                    }
                }
                description = "Upload photo document for student"
            },
            # Step 4: Upload Medical Certificate
            @{
                name = "4. Upload Document - MEDICAL_CERTIFICATE"
                request = @{
                    method = "POST"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/students/{{student_id}}/documents?documentType=MEDICAL_CERTIFICATE&filePath=/documents/students/{{student_id}}/medical_cert.pdf"
                        host = @("{{baseUrl}}")
                        path = @("api", "students", "{{student_id}}", "documents")
                        query = @(
                            @{ key = "documentType"; value = "MEDICAL_CERTIFICATE" }
                            @{ key = "filePath"; value = "/documents/students/{{student_id}}/medical_cert.pdf" }
                        )
                    }
                }
                description = "Upload medical certificate document for student"
            },
            # Step 5: Get Student Details
            @{
                name = "5. Get Student Details"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/students/{{student_id}}"
                        host = @("{{baseUrl}}")
                        path = @("api", "students", "{{student_id}}")
                        variable = @(@{ key = "id"; value = "{{student_id}}"; description = "Student ID from step 1" })
                    }
                }
                description = "Get complete student information with documents"
            },
            # Step 6: Find Available Vehicles
            @{
                name = "6. Find Available Vehicles"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/vehicles/available?startTime=2024-03-15T10:00:00&endTime=2024-03-15T11:30:00"
                        host = @("{{baseUrl}}")
                        path = @("api", "vehicles", "available")
                        query = @(
                            @{ key = "startTime"; value = "2024-03-15T10:00:00"; description = "Start date and time (ISO format)" }
                            @{ key = "endTime"; value = "2024-03-15T11:30:00"; description = "End date and time (ISO format)" }
                        )
                    }
                }
                event = @(
                    @{
                        listen = "test"
                        script = @{
                            type = "text/javascript"
                            exec = @(
                                "if (pm.response.code === 200) {",
                                "    const response = pm.response.json();",
                                "    const vehicles = response.data || response;",
                                "    if (Array.isArray(vehicles) && vehicles.length > 0) {",
                                "        const vehicleId = vehicles[0].id;",
                                "        if (vehicleId) {",
                                "            pm.collectionVariables.set('vehicle_id', vehicleId.toString());",
                                "            console.log('vehicle_id set to: ' + vehicleId);",
                                "        }",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Find available vehicles for March 15, 2024, 10:00-11:30"
            },
            # Step 7: Find Available Instructors
            @{
                name = "7. Find Available Instructors"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/lessons/instructors/available?startTime=2024-03-15T10:00:00&endTime=2024-03-15T11:30:00"
                        host = @("{{baseUrl}}")
                        path = @("api", "lessons", "instructors", "available")
                        query = @(
                            @{ key = "startTime"; value = "2024-03-15T10:00:00"; description = "Start date and time (ISO format)" }
                            @{ key = "endTime"; value = "2024-03-15T11:30:00"; description = "End date and time (ISO format)" }
                        )
                    }
                }
                event = @(
                    @{
                        listen = "test"
                        script = @{
                            type = "text/javascript"
                            exec = @(
                                "if (pm.response.code === 200) {",
                                "    const response = pm.response.json();",
                                "    const instructors = response.data || response;",
                                "    if (Array.isArray(instructors) && instructors.length > 0) {",
                                "        const instructorId = instructors[0].id;",
                                "        if (instructorId) {",
                                "            pm.collectionVariables.set('instructor_id', instructorId.toString());",
                                "            console.log('instructor_id set to: ' + instructorId);",
                                "        }",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Find available instructors for March 15, 2024, 10:00-11:30"
            },
            # Step 8: Book Lesson
            @{
                name = "8. Book Practical Lesson"
                request = @{
                    method = "POST"
                    header = @(
                        @{ key = "Content-Type"; value = "application/json" }
                        @{ key = "Accept"; value = "*/*" }
                    )
                    body = @{
                        mode = "raw"
                        raw = (@{
                            studentId = "{{student_id}}"
                            instructorId = "{{instructor_id}}"
                            vehicleId = "{{vehicle_id}}"
                            startTime = "2024-03-15T10:00:00"
                            endTime = "2024-03-15T11:30:00"
                            type = "PRACTICAL"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/lessons"
                        host = @("{{baseUrl}}")
                        path = @("api", "lessons")
                    }
                }
                description = "Book practical lesson. Uses IDs from previous steps."
            },
            # Step 9: Process Payment
            @{
                name = "9. Process Payment for Lesson"
                request = @{
                    method = "POST"
                    header = @(
                        @{ key = "Content-Type"; value = "application/json" }
                        @{ key = "Accept"; value = "*/*" }
                    )
                    body = @{
                        mode = "raw"
                        raw = (@{
                            studentId = "{{student_id}}"
                            amount = 150.00
                            paymentMethod = "ONLINE"
                            transactionId = "TXN-2024-03-15-001"
                            notes = "Payment for practical lesson - March 15, 2024"
                            courseId = 1
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/payments"
                        host = @("{{baseUrl}}")
                        path = @("api", "payments")
                    }
                }
                description = "Process payment of 150 RON for lesson. Method: ONLINE"
            },
            # Step 10: Get Student Balance
            @{
                name = "10. Get Student Balance"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/payments/student/{{student_id}}/balance"
                        host = @("{{baseUrl}}")
                        path = @("api", "payments", "student", "{{student_id}}", "balance")
                        variable = @(@{ key = "studentId"; value = "{{student_id}}"; description = "Student ID" })
                    }
                }
                description = "Get total student balance"
            },
            # Step 11: Get Student Payment History
            @{
                name = "11. Get Student Payment History"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/payments/student/{{student_id}}"
                        host = @("{{baseUrl}}")
                        path = @("api", "payments", "student", "{{student_id}}")
                        variable = @(@{ key = "studentId"; value = "{{student_id}}"; description = "Student ID" })
                    }
                }
                description = "Complete payment history for student"
            }
        )
        description = "Complete journey: Register student -> Upload documents -> Book lesson -> Payment"
    }
}

# -------------------------
# Try to get existing collection to preserve Journey folder
# -------------------------
$existingJourneyFolder = $null
$existingJourneyVariables = @()

try {
    Write-Host ""
    Write-Host "[Postman] Fetching existing collection to preserve Journey folder..."
    $existingCollectionResponse = Invoke-RestMethod `
        -Method GET `
        -Uri ("https://api.getpostman.com/collections/{0}" -f $PostmanCollectionUid) `
        -Headers @{ "X-Api-Key" = $PostmanApiKey } `
        -ErrorAction SilentlyContinue

    if ($existingCollectionResponse -and $existingCollectionResponse.collection) {
        $existingCollection = $existingCollectionResponse.collection
        
        # Find Journey folder if it exists
        if ($existingCollection.item) {
            foreach ($item in $existingCollection.item) {
                if ($item.name -like "*Journey*") {
                    $existingJourneyFolder = $item
                    Write-Host "[Postman] Found existing Journey folder, will preserve it."
                    break
                }
            }
        }
        
        # Preserve Journey variables if they exist
        if ($existingCollection.variable) {
            $journeyVarKeys = @("student_id", "instructor_id", "vehicle_id")
            foreach ($var in $existingCollection.variable) {
                if ($var.key -in $journeyVarKeys) {
                    $existingJourneyVariables += $var
                }
            }
        }
    }
} catch {
    Write-Host "[Postman] Could not fetch existing collection (first run?), will create new Journey folder."
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
            key   = "baseUrl"
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

# -------------------------
# Add Journey folder (preserve existing or create new)
# -------------------------
if ($existingJourneyFolder) {
    # Preserve existing Journey folder
    $mergedCollection.item = @($existingJourneyFolder) + $mergedCollection.item
    Write-Host "[Postman] Preserved existing Journey folder."
} else {
    # Create new Journey folder
    $journeyFolder = New-JourneyFolder
    $mergedCollection.item = @($journeyFolder) + $mergedCollection.item
    Write-Host "[Postman] Created new Journey folder."
}

# -------------------------
# Add Journey variables (preserve existing or create new)
# -------------------------
if ($existingJourneyVariables.Count -gt 0) {
    # Preserve existing Journey variables
    foreach ($var in $existingJourneyVariables) {
        $mergedCollection.variable += $var
    }
    Write-Host "[Postman] Preserved existing Journey variables."
} else {
    # Create new Journey variables
    $mergedCollection.variable += @{
        key = "student_id"
        value = "1"
        type = "string"
        description = "Student ID created in step 1"
    }
    $mergedCollection.variable += @{
        key = "instructor_id"
        value = "1"
        type = "string"
        description = "Available instructor ID"
    }
    $mergedCollection.variable += @{
        key = "vehicle_id"
        value = "1"
        type = "string"
        description = "Available vehicle ID"
    }
    Write-Host "[Postman] Created new Journey variables."
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
    if ($existingJourneyFolder) {
        Write-Host "[Postman] Journey folder preserved from existing collection."
    } else {
        Write-Host "[Postman] New Journey folder created in collection."
    }
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


