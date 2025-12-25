# Ghid de Utilizare - Driving School Management System

## Cum funcționează aplicația?

**DA, ar trebui să apelezi MEREU doar API Gateway!**

API Gateway este singurul punct de intrare pentru toate cererile. Gateway-ul routează automat cererile către serviciile corespunzătoare în funcție de path-ul URL-ului.

### Configurație API Gateway

- **Port:** 8080
- **URL Base:** `http://localhost:8080`

### Routing automat

API Gateway routează automat cererile către serviciile corespunzătoare:

| Path | Serviciu | Port |
|------|----------|------|
| `/api/students/**` | Student Service | 8081 |
| `/api/lessons/**` | Scheduling Service | 8082 |
| `/api/vehicles/**` | Vehicle Service | 8083 |
| `/api/payments/**` | Payment Service | 8084 |

## Exemple de Endpoint-uri

### 1. Student Service (`/api/students`)

#### Creare student nou
```http
POST http://localhost:8080/api/students
Content-Type: application/json

{
  "firstName": "Ion",
  "lastName": "Popescu",
  "cnp": "1234567890123",
  "email": "ion.popescu@example.com",
  "phone": "0712345678",
  "address": "Strada Exemplu, Nr. 1, București"
}
```

#### Obține student după ID
```http
GET http://localhost:8080/api/students/1
```

#### Obține toți studenții
```http
GET http://localhost:8080/api/students
```

#### Obține studenți filtrat după status
```http
GET http://localhost:8080/api/students?status=ACTIVE
```

Statusuri valide: `PENDING`, `ACTIVE`, `SUSPENDED`, `GRADUATED`

#### Caută studenți după nume
```http
GET http://localhost:8080/api/students/search?name=Popescu
```

#### Actualizează student
```http
PUT http://localhost:8080/api/students/1
Content-Type: application/json

{
  "firstName": "Ion",
  "lastName": "Popescu",
  "cnp": "1234567890123",
  "email": "ion.popescu.nou@example.com",
  "phone": "0712345678",
  "address": "Strada Nouă, Nr. 2, București"
}
```

#### Șterge student
```http
DELETE http://localhost:8080/api/students/1
```

#### Încarcă document pentru student
```http
POST http://localhost:8080/api/students/1/documents?documentType=ID_CARD&filePath=/path/to/document.pdf
```

#### Obține documentele unui student
```http
GET http://localhost:8080/api/students/1/documents
```

---

### 2. Scheduling Service (`/api/lessons`)

#### Rezervă lecție nouă
```http
POST http://localhost:8080/api/lessons
Content-Type: application/json

{
  "studentId": 1,
  "instructorId": 1,
  "vehicleId": 1,
  "startTime": "2024-12-20T10:00:00",
  "endTime": "2024-12-20T11:30:00",
  "type": "PRACTICAL"
}
```

Tipuri valide: `THEORETICAL`, `PRACTICAL`

#### Obține lecție după ID
```http
GET http://localhost:8080/api/lessons/1
```

#### Actualizează lecție (reprogramare)
```http
PUT http://localhost:8080/api/lessons/1
Content-Type: application/json

{
  "studentId": 1,
  "instructorId": 1,
  "vehicleId": 1,
  "startTime": "2024-12-21T14:00:00",
  "endTime": "2024-12-21T15:30:00",
  "type": "PRACTICAL"
}
```

#### Anulează lecție
```http
DELETE http://localhost:8080/api/lessons/1
```

#### Obține lecțiile unui instructor
```http
GET http://localhost:8080/api/lessons/instructors/1
```

#### Obține instructori disponibili
```http
GET http://localhost:8080/api/lessons/instructors/available?startTime=2024-12-20T10:00:00&endTime=2024-12-20T11:30:00
```

---

### 3. Vehicle Service (`/api/vehicles`)

#### Înregistrează vehicul nou
```http
POST http://localhost:8080/api/vehicles
Content-Type: application/json

{
  "licensePlate": "B-123-ABC",
  "make": "Dacia",
  "model": "Logan",
  "year": 2020,
  "insuranceExpiry": "2025-12-31"
}
```

#### Obține vehicul după ID
```http
GET http://localhost:8080/api/vehicles/1
```

#### Obține toate vehiculele
```http
GET http://localhost:8080/api/vehicles
```

#### Obține vehicule filtrat după status
```http
GET http://localhost:8080/api/vehicles?status=AVAILABLE
```

Statusuri valide: `AVAILABLE`, `IN_USE`, `MAINTENANCE`, `OUT_OF_SERVICE`

#### Actualizează vehicul
```http
PUT http://localhost:8080/api/vehicles/1
Content-Type: application/json

{
  "licensePlate": "B-123-ABC",
  "make": "Dacia",
  "model": "Logan",
  "year": 2020,
  "insuranceExpiry": "2026-12-31"
}
```

#### Obține vehicule disponibile pentru un interval de timp
```http
GET http://localhost:8080/api/vehicles/available?startTime=2024-12-20T10:00:00&endTime=2024-12-20T11:30:00
```

---

### 4. Payment Service (`/api/payments`)

#### Procesează plată
```http
POST http://localhost:8080/api/payments
Content-Type: application/json

{
  "studentId": 1,
  "amount": 500.00,
  "paymentMethod": "CARD",
  "courseId": 1
}
```

Metode de plată valide: `CARD`, `CASH`, `BANK_TRANSFER`

#### Obține plată după ID
```http
GET http://localhost:8080/api/payments/1
```

#### Obține toate plățile unui student
```http
GET http://localhost:8080/api/payments/student/1
```

#### Obține balanța unui student
```http
GET http://localhost:8080/api/payments/student/1/balance
```

---

## Răspunsuri API

Toate răspunsurile urmează formatul standard `ApiResponse`:

### Succes
```json
{
  "success": true,
  "message": "Student registered successfully",
  "data": {
    "id": 1,
    "firstName": "Ion",
    "lastName": "Popescu",
    ...
  }
}
```

### Eroare
```json
{
  "success": false,
  "message": "Validation error",
  "errors": [
    "Email should be valid",
    "CNP is required"
  ]
}
```

## Swagger UI

Poți accesa documentația interactivă Swagger la:
- **URL:** http://localhost:8080/swagger-ui.html

Aici poți testa toate endpoint-urile direct din browser!

## Importare Postman Collection

Pentru a testa rapid toate endpoint-urile, importă colecția Postman din fișierul `DrivingSchool_API.postman_collection.json`.

Colecția conține:
- Toate endpoint-urile organizate pe servicii
- Exemple de request-uri cu date de test
- Variabile de environment pentru URL-uri
- Exemple pentru toate scenariile comune

## Note importante

1. **Folosește întotdeauna API Gateway (port 8080)** - Nu apela direct serviciile individuale
2. **Format dată/timp:** Folosește format ISO 8601: `YYYY-MM-DDTHH:mm:ss` (ex: `2024-12-20T10:00:00`)
3. **Format dată:** Folosește format ISO: `YYYY-MM-DD` (ex: `2024-12-20`)
4. **CNP:** Trebuie să fie valid (13 cifre)
5. **Telefon:** Trebuie să fie exact 10 cifre
6. **CORS:** API Gateway permite cereri din orice origine (`*`)

## Flux tipic de utilizare

1. **Înregistrare student:**
   ```
   POST /api/students
   ```

2. **Înregistrare vehicul:**
   ```
   POST /api/vehicles
   ```

3. **Rezervare lecție:**
   ```
   POST /api/lessons
   ```

4. **Procesare plată:**
   ```
   POST /api/payments
   ```

5. **Verificare balanță:**
   ```
   GET /api/payments/student/{studentId}/balance
   ```

