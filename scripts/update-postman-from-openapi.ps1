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
    @{ Name = "Instructor Service"; Port = 8086; DocsPath = "/api-docs"; BasePath = "/api/instructors" },
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
            # Step 6.0: Register Vehicle
            @{
                name = "6.0. Register Vehicle"
                request = @{
                    method = "POST"
                    header = @(
                        @{ key = "Content-Type"; value = "application/json" }
                        @{ key = "Accept"; value = "*/*" }
                    )
                    body = @{
                        mode = "raw"
                        raw = (@{
                            licensePlate = "B-123-ABC"
                            make = "Toyota"
                            model = "Corolla"
                            year = 2020
                            insuranceExpiry = "2027-12-31"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/vehicles"
                        host = @("{{baseUrl}}")
                        path = @("api", "vehicles")
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
                                "    if (response.data && response.data.id) {",
                                "        const vehicleId = response.data.id;",
                                "        pm.collectionVariables.set('vehicle_id', vehicleId.toString());",
                                "        console.log('vehicle_id set to: ' + vehicleId);",
                                "    } else if (response.id) {",
                                "        pm.collectionVariables.set('vehicle_id', response.id.toString());",
                                "        console.log('vehicle_id set to: ' + response.id);",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Register a new vehicle - Toyota Corolla 2020"
            },
            # Step 6.1.: Find Available Vehicles
            @{
                name = "6.1. Find Available Vehicles"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/vehicles/available?startTime=2027-01-01T10:00:00&endTime=2027-01-01T11:30:00"
                        host = @("{{baseUrl}}")
                        path = @("api", "vehicles", "available")
                        query = @(
                            @{ key = "startTime"; value = "2027-01-01T10:00:00"; description = "Start date and time (ISO format)" }
                            @{ key = "endTime"; value = "2027-01-01T11:30:00"; description = "End date and time (ISO format)" }
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
                description = "Find available vehicles for January 1, 2027, 10:00-11:30"
            },
            # Step 7.0: Register Instructor
            @{
                name = "7.0. Register Instructor"
                request = @{
                    method = "POST"
                    header = @(
                        @{ key = "Content-Type"; value = "application/json" }
                        @{ key = "Accept"; value = "*/*" }
                    )
                    body = @{
                        mode = "raw"
                        raw = (@{
                            firstName = "Ion"
                            lastName = "Popescu"
                            licenseNumber = "LIC-12345"
                            email = "ion.popescu@drivingschool.com"
                            phone = "0712345678"
                            specialization = "BOTH"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/instructors"
                        host = @("{{baseUrl}}")
                        path = @("api", "instructors")
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
                                "    if (response.data && response.data.id) {",
                                "        const instructorId = response.data.id;",
                                "        pm.collectionVariables.set('instructor_id', instructorId.toString());",
                                "        console.log('instructor_id set to: ' + instructorId);",
                                "    } else if (response.id) {",
                                "        pm.collectionVariables.set('instructor_id', response.id.toString());",
                                "        console.log('instructor_id set to: ' + response.id);",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Register a new instructor - Ion Popescu"
            },
            # Step 7.1.: Find Available Instructors
            @{
                name = "7.1. Find Available Instructors"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/instructors/available?startTime=2027-01-01T10:00:00&endTime=2027-01-01T11:30:00"
                        host = @("{{baseUrl}}")
                        path = @("api", "instructors", "available")
                        query = @(
                            @{ key = "startTime"; value = "2027-01-01T10:00:00"; description = "Start date and time (ISO format)" }
                            @{ key = "endTime"; value = "2027-01-01T11:30:00"; description = "End date and time (ISO format)" }
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
                description = "Find available instructors for January 1, 2027, 10:00-11:30"
            },
            # Step 8: Create Course
            @{
                name = "8. Create Course"
                request = @{
                    method = "POST"
                    header = @(
                        @{ key = "Content-Type"; value = "application/json" }
                        @{ key = "Accept"; value = "*/*" }
                    )
                    body = @{
                        mode = "raw"
                        raw = (@{
                            name = "Beginner Course"
                            description = "Complete beginner course with 10 practical lessons"
                            price = 1000.00
                            numberOfLessons = 10
                            courseType = "PRACTICAL"
                            instructorId = "{{instructor_id}}"
                            vehicleId = "{{vehicle_id}}"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/courses"
                        host = @("{{baseUrl}}")
                        path = @("api", "courses")
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
                                "    if (response.data && response.data.id) {",
                                "        const courseId = response.data.id;",
                                "        pm.collectionVariables.set('course_id', courseId.toString());",
                                "        console.log('course_id set to: ' + courseId);",
                                "        console.log('Course created with 10 lessons. Price per lesson: 100 RON (1000/10)');",
                                "    } else if (response.id) {",
                                "        pm.collectionVariables.set('course_id', response.id.toString());",
                                "        console.log('course_id set to: ' + response.id);",
                                "        console.log('Course created with 10 lessons. Price per lesson: 100 RON (1000/10)');",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Create a new course with instructor_id={{instructor_id}}, vehicle_id={{vehicle_id}}, price 1000 RON, numberOfLessons=10, courseType=PRACTICAL. Price per lesson will be 100 RON (1000/10). Each lesson booking will automatically create a pending payment. When booking lessons from this course, only studentId, courseId, startTime, and endTime are required."
            },
            # Step 9: Book First Lesson from Course (creates payment automatically)
            @{
                name = "9. Book First Lesson from Course"
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
                            courseId = "{{course_id}}"
                            startTime = "2027-01-01T10:00:00"
                            endTime = "2027-01-01T11:30:00"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/lessons"
                        host = @("{{baseUrl}}")
                        path = @("api", "lessons")
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
                                "    if (response.data && response.data.id) {",
                                "        const lessonId = response.data.id;",
                                "        pm.collectionVariables.set('course_lesson_1_id', lessonId.toString());",
                                "        console.log('course_lesson_1_id set to: ' + lessonId);",
                                "        console.log('Note: A pending payment of 100 RON (1000/10) was automatically created for this lesson');",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Book the first lesson from the course. Only studentId, courseId, startTime, and endTime are required. instructorId, vehicleId, and type are automatically taken from the course. This automatically creates a pending payment of 100 RON (course price / numberOfLessons)."
            },
            # Step 9b: Book Second Lesson from Course
            @{
                name = "9b. Book Second Lesson from Course"
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
                            courseId = "{{course_id}}"
                            startTime = "2027-01-02T10:00:00"
                            endTime = "2027-01-02T11:30:00"
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
                event = @(
                    @{
                        listen = "test"
                        script = @{
                            type = "text/javascript"
                            exec = @(
                                "if (pm.response.code === 201 || pm.response.code === 200) {",
                                "    const response = pm.response.json();",
                                "    if (response.data && response.data.id) {",
                                "        const lessonId = response.data.id;",
                                "        pm.collectionVariables.set('course_lesson_2_id', lessonId.toString());",
                                "        console.log('course_lesson_2_id set to: ' + lessonId);",
                                "        console.log('Note: Another pending payment of 100 RON was automatically created');",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Book the second lesson from the course. Only studentId, courseId, startTime, and endTime are required. Another pending payment of 100 RON is automatically created. Student has now booked 2/10 lessons."
            },
            # Step 10: Get Course (Verify booked lessons count)
            @{
                name = "10. Get Course (Verify Booked Lessons)"
                request = @{
                    method = "GET"
                    header = @(@{ key = "Accept"; value = "*/*" })
                    url = @{
                        raw = "{{baseUrl}}/api/courses/{{course_id}}"
                        host = @("{{baseUrl}}")
                        path = @("api", "courses", "{{course_id}}")
                        variable = @(@{ key = "id"; value = "{{course_id}}"; description = "Course ID" })
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
                                "    const course = response.data || response;",
                                "    console.log('Course numberOfLessons (configured): ' + course.numberOfLessons);",
                                "    console.log('Course bookedLessons (actual): ' + course.bookedLessons);",
                                "    console.log('Course duration (calculated): ' + course.duration + ' hours');",
                                "}"
                            )
                        }
                    }
                )
                description = "Get course details to verify bookedLessons count. Should show numberOfLessons=10 (configured) and bookedLessons=2 (actually booked so far)."
            },
            # Step 11: Book Extra Lesson from Course (beyond limit - double price)
            @{
                name = "11. Book Extra Lesson from Course (Beyond Limit)"
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
                            courseId = "{{course_id}}"
                            startTime = "2027-01-20T14:00:00"
                            endTime = "2027-01-20T15:30:00"
                        } | ConvertTo-Json)
                        options = @{ raw = @{ language = "json" } }
                    }
                    url = @{
                        raw = "{{baseUrl}}/api/lessons"
                        host = @("{{baseUrl}}")
                        path = @("api", "lessons")
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
                                "    if (response.data && response.data.id) {",
                                "        const lessonId = response.data.id;",
                                "        pm.collectionVariables.set('extra_lesson_id', lessonId.toString());",
                                "        console.log('extra_lesson_id set to: ' + lessonId);",
                                "        console.log('Note: This is lesson 11/10, so price is DOUBLE (200 RON = 2 x 100 RON)');",
                                "        console.log('A pending payment of 200 RON was automatically created');",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Book an extra lesson beyond the course limit (11th lesson when course has 10). Only studentId, courseId, startTime, and endTime are required. This automatically creates a pending payment of 200 RON (double the normal price per lesson)."
            },
            # Step 12: Book Additional Lesson (not part of any course)
            @{
                name = "12. Book Additional Lesson (Not Part of Course)"
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
                            startTime = "2027-01-25T14:00:00"
                            endTime = "2027-01-25T15:30:00"
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
                event = @(
                    @{
                        listen = "test"
                        script = @{
                            type = "text/javascript"
                            exec = @(
                                "if (pm.response.code === 201 || pm.response.code === 200) {",
                                "    const response = pm.response.json();",
                                "    if (response.data && response.data.id) {",
                                "        const lessonId = response.data.id;",
                                "        pm.collectionVariables.set('additional_lesson_id', lessonId.toString());",
                                "        console.log('additional_lesson_id set to: ' + lessonId);",
                                "        console.log('Note: A pending payment with standard lesson price was automatically created');",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Book an additional lesson (not part of any course). When courseId is NOT provided, studentId, instructorId, vehicleId, type, startTime, and endTime are all required. This automatically creates a PENDING payment with standard lesson price."
            },
            # Step 13: Get Student Payments (Check Pending)
            @{
                name = "13. Get Student Payments (Check Pending)"
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
                event = @(
                    @{
                        listen = "test"
                        script = @{
                            type = "text/javascript"
                            exec = @(
                                "if (pm.response.code === 200) {",
                                "    const response = pm.response.json();",
                                "    const payments = response.data || response;",
                                "    if (Array.isArray(payments)) {",
                                "        // Find pending payment for additional lesson",
                                "        const pendingPayment = payments.find(p => p.status === 'PENDING' && p.lessonId);",
                                "        if (pendingPayment && pendingPayment.id) {",
                                "            pm.collectionVariables.set('pending_payment_id', pendingPayment.id.toString());",
                                "            console.log('pending_payment_id set to: ' + pendingPayment.id);",
                                "        }",
                                "    }",
                                "}"
                            )
                        }
                    }
                )
                description = "Get all payments for student. Should show multiple PENDING payments: 2x 100 RON for course lessons, 1x 200 RON for extra course lesson, and 1x standard price for additional lesson."
            },
            # Step 14: Get Student Balance
            @{
                name = "14. Get Student Balance"
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
                description = "Get total student balance (sum of all COMPLETED payments). Note: Payments are created as PENDING and need to be completed first."
            },
            # Step 15: Get Student Payment History
            @{
                name = "15. Get Student Payment History"
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
                description = "Complete payment history for student. Should show multiple pending payments: course lessons (100 RON each), extra course lesson (200 RON), and additional lesson (standard price)."
            }
        )
        description = "Complete journey: Register student -> Upload documents -> Create course (with numberOfLessons=10) -> Book lessons from course (each creates pending payment of 100 RON = 1000/10) -> Book extra lesson beyond course limit (creates pending payment of 200 RON = 2x price) -> Book additional lesson not in course (creates pending payment with standard price)"
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
            $journeyVarKeys = @("student_id", "instructor_id", "vehicle_id", "course_id", "course_lesson_1_id", "course_lesson_2_id", "extra_lesson_id", "additional_lesson_id", "pending_payment_id")
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
    $mergedCollection.variable += @{
        key = "course_id"
        value = "1"
        type = "string"
        description = "Course ID (created in step 8)"
    }
    $mergedCollection.variable += @{
        key = "course_lesson_1_id"
        value = ""
        type = "string"
        description = "First lesson ID from course (set in step 9)"
    }
    $mergedCollection.variable += @{
        key = "course_lesson_2_id"
        value = ""
        type = "string"
        description = "Second lesson ID from course (set in step 9b)"
    }
    $mergedCollection.variable += @{
        key = "extra_lesson_id"
        value = ""
        type = "string"
        description = "Extra lesson ID beyond course limit (set in step 11)"
    }
    $mergedCollection.variable += @{
        key = "additional_lesson_id"
        value = ""
        type = "string"
        description = "Lesson ID for additional lesson not in course (set in step 12)"
    }
    $mergedCollection.variable += @{
        key = "pending_payment_id"
        value = ""
        type = "string"
        description = "Pending payment ID (set in step 13)"
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


