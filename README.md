# Driving School Management System

Un sistem modern de management pentru școli auto, construit cu arhitectură microservices folosind Spring Boot 4.0.1, API
Gateway, Kafka, Redis și suport pentru multiple baze de date.

## 📋 Despre Aplicație

Sistemul de Management Școală Auto este o platformă completă pentru gestionarea tuturor aspectelor unei școli auto,
inclusiv:

- **Management Studenți** - Înregistrare, actualizare și gestionare documente
- **Programare Lecții** - Rezervare, reschedulare și anulare lecții cu verificare automată disponibilitate
- **Management Instructori** - Înregistrare și gestionare instructori cu specializări
- **Management Vehicule** - Gestionare flotă și mentenanță
- **Management Cursuri** - Creare și gestionare cursuri teoretice și practice
- **Procesare Plăți** - Procesare plăți, calculare balanțe și rambursări
- **Notificări** - Sistem de notificări evenimente prin Kafka

## 🏗️ Arhitectură

Proiectul urmează o arhitectură microservices cu următoarele servicii:

- **API Gateway** (port 8080) - Punct unic de intrare pentru toate cererile client
- **Student Service** (port 8081) - Înregistrare și management studenți
- **Scheduling Service** (port 8082) - Rezervare și programare lecții
- **Vehicle Service** (port 8083) - Management flotă vehicule
- **Payment Service** (port 8084) - Procesare plăți și balanțe
- **Instructor Service** (port 8086) - Management instructori
- **Notification Service** (port 8085) - Notificări evenimente

## 🛠️ Tehnologii

- **Java 21**
- **Spring Boot 4.0.1**
- **Spring Cloud 2025.1.0**
- **Spring Cloud Gateway** - API Gateway
- **Apache Kafka** - Message broker pentru evenimente
- **Redis** - Caching
- **PostgreSQL 17** - Baza de date principală
- **SpringDoc OpenAPI** - Documentație API (Swagger)
- **JUnit 5 & Mockito** - Testing
- **Maven** - Build tool

## 📦 Prerequisituri

- Java 21 sau mai nou
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 17+ (sau folosește Docker Compose)
- Redis 7+ (sau folosește Docker Compose)
- Apache Kafka 3.5+ (sau folosește Docker Compose)

## 🚀 Instalare și Rulare

### 1. Pornire Servicii Infrastructură

Pornește toate serviciile de infrastructură necesare (PostgreSQL, Redis, Kafka, Zookeeper):

```bash
docker-compose up -d
```

Această comandă va porni:

- PostgreSQL (port 5432)
- Redis (port 6379)
- Kafka & Zookeeper (ports 9092, 2181)

Verifică statusul serviciilor:

```bash
docker-compose ps
```

### 2. Build Proiect

Construiește toate modulele proiectului:

```bash
mvn clean install
```

Această comandă va:

- Compila toate modulele
- Rula testele unitare
- Crea JAR-urile pentru fiecare serviciu

### 3. Configurare Baze de Date

Fiecare serviciu va crea automat schema bazei de date la pornire folosind JPA/Hibernate. Asigură-te că PostgreSQL
rulează înainte de a porni serviciile.

### 4. Pornire Servicii

Pornește serviciile în următoarea ordine:

#### Opțiunea 1: Pornire Manuală

1. **Common Module** (build only - deja construit)
2. **Student Service:**
   ```bash
   cd student-service
   mvn spring-boot:run
   ```
3. **Instructor Service:**
   ```bash
   cd instructor-service
   mvn spring-boot:run
   ```
4. **Vehicle Service:**
   ```bash
   cd vehicle-service
   mvn spring-boot:run
   ```
5. **Payment Service:**
   ```bash
   cd payment-service
   mvn spring-boot:run
   ```
6. **Scheduling Service:**
   ```bash
   cd scheduling-service
   mvn spring-boot:run
   ```
7. **Notification Service:**
   ```bash
   cd notification-service
   mvn spring-boot:run
   ```
8. **API Gateway:**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

#### Opțiunea 2: Pornire din IDE

Pornește fiecare serviciu din IDE (IntelliJ IDEA, Eclipse, etc.) rulând clasa principală `*Application.java` din fiecare
modul.

### 5. Verificare Status

După ce toate serviciile sunt pornite, verifică că rulează corect folosind următoarele metode:

#### Verificare Rapidă - Link-uri Utile

| Serviciu | Base URL | Swagger UI | OpenAPI Docs |
|----------|----------|-----------|--------------|
| **API Gateway** | http://localhost:8080 | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| **Student Service** | http://localhost:8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| **Scheduling Service** | http://localhost:8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| **Vehicle Service** | http://localhost:8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| **Payment Service** | http://localhost:8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| **Notification Service** | http://localhost:8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| **Instructor Service** | http://localhost:8086 | http://localhost:8086/swagger-ui.html | http://localhost:8086/v3/api-docs |

## 📚 Documentație API

Toate API-urile sunt documentate folosind Swagger/OpenAPI 3.0. Accesează documentația interactivă la:

- **API Gateway Swagger:** http://localhost:8080/swagger-ui.html
- **Swagger individual pe servicii:**
    - Student Service: http://localhost:8081/swagger-ui.html
    - Scheduling Service: http://localhost:8082/swagger-ui.html
    - Vehicle Service: http://localhost:8083/swagger-ui.html
    - Payment Service: http://localhost:8084/swagger-ui.html
    - Instructor Service: http://localhost:8086/swagger-ui.html

### Generare OpenAPI Combinat

Pentru a genera un singur fișier OpenAPI care conține toate API-urile:

1. **Pornește toate serviciile**

2. **Rulează scriptul de agregare:**
   ```powershell
   cd scripts
   .\generate-combined-openapi.ps1
   ```

3. **Fișierul generat** `DrivingSchoolManagementSystem-API-1.0.0.swagger_collection.json` va fi salvat în root-ul
   proiectului

## 🧪 Testing

### Rulare Teste Unitare

Rulează toate testele unitare:

```bash
mvn test
```

Rulează testele pentru un serviciu specific:

```bash
cd student-service
mvn test
```

Rapoartele de coverage vor fi generate în `target/site/jacoco/index.html` pentru fiecare modul.

## 📁 Structura Proiectului

```
driving-school-platform/
├── api-gateway/          # API Gateway service
├── student-service/      # Management studenți
├── scheduling-service/   # Programare lecții
├── vehicle-service/      # Management vehicule
├── payment-service/      # Procesare plăți
├── instructor-service/   # Management instructori
├── notification-service/ # Notificări evenimente
├── common/              # Cod partajat (exceptions, DTOs, validations)
├── docker-compose.yml   # Configurare infrastructură
├── REQUIREMENTS.md      # Cerințe de business și funcționalități MVP
└── README.md            # Acest fișier
```

## 🎯 Funcționalități MVP

Sistemul implementează următoarele 5 funcționalități principale pentru MVP:

1. **Management Studenți și Documente** - Înregistrare completă cu validare CNP, email, telefon și gestionare documente
2. **Programare și Management Lecții** - Rezervare cu verificare disponibilitate, reschedulare și anulare
3. **Management Cursuri și Instructori** - Creare cursuri, gestionare instructori cu specializări
4. **Management Vehicule și Mentenanță** - Gestionare flotă, mentenanță și tracking status
5. **Procesare Plăți și Balanțe** - Procesare plăți multiple metode, calculare balanțe și rambursări

Pentru detalii complete, vezi [REQUIREMENTS.md](REQUIREMENTS.md).

## 🗄️ Schema Bazei de Date

Sistemul folosește 8 entități principale cu multiple relații:

- **Student** - Informații studenți
- **Document** - Documente studenți (OneToMany cu Student)
- **Instructor** - Informații instructori
- **Vehicle** - Informații vehicule
- **Maintenance** - Operațiuni mentenanță (ManyToOne cu Vehicle)
- **Course** - Cursuri teoretice și practice
- **Lesson** - Lecții programate (ManyToOne cu Course)
- **Payment** - Tranzacții plăți

Pentru diagrama completă a relațiilor, vezi [REQUIREMENTS.md](REQUIREMENTS.md#3-diagrama-relațiilor-dintre-entități).

## 📮 Postman Collection

Importă colecția Postman `DrivingSchoolManagementSystem-API-1.0.0.postman_collection.json` pentru a testa toate
API-urile. Colecția include:

- Toate endpoint-urile organizate pe servicii
- Request-uri exemple cu date de test
- Variabile de mediu pentru configurare ușoară
- Acoperire completă API

### Actualizare Automată Postman Collection (din OpenAPI)

Poți actualiza automat colecția Postman din definiția OpenAPI expusă de API Gateway:

1. **Pornește serviciile** astfel încât OpenAPI să fie disponibil la:
    - `http://localhost:8080/v3/api-docs`

2. **Setează credențiale Postman** (o dată per mașină):
    - `POSTMAN_API_KEY` – cheia ta API Postman
    - `POSTMAN_COLLECTION_UID` – UID-ul colecției `DrivingSchool_API` din Postman

3. **Rulează scriptul de actualizare (PowerShell pe Windows):**
   ```powershell
   .\scripts\update-postman-from-openapi.ps1
   ```

Scriptul va:

- Preia JSON-ul OpenAPI de la URL-ul dat
- Trimite la Postman Import API pentru a genera o colecție
- Suprascrie colecția Postman existentă cu noua definiție (același UID)

## 🔧 Configurare

### Variabile de Mediu

Fiecare serviciu poate fi configurat prin fișierele `application.yml` din fiecare modul. Configurările principale
includ:

- **Database connection** - PostgreSQL connection string
- **Redis connection** - Redis host și port
- **Kafka connection** - Kafka bootstrap servers
- **Service ports** - Porturi pentru fiecare serviciu
- **API Gateway routes** - Rute pentru fiecare serviciu
