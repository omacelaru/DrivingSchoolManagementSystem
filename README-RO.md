# Driving School Management System

Acest proiect reprezintă o platformă distribuită pentru gestionarea activității unei școli auto, implementată pe o arhitectură de tip microservices. Soluția utilizează Spring Boot 4.0.1, API Gateway, Apache Kafka pentru comunicare asincronă, Redis pentru caching și suportă persistență multiplă.

## Prezentare Generală

Sistemul centralizează procesele operaționale ale școlii, oferind funcționalități pentru:

- **Administrare Studenți:** Înregistrare, validare date (CNP, contact) și gestionarea dosarului de școlarizare.
- **Programări:** Sistem de rezervare a lecțiilor cu verificare în timp real a disponibilității.
- **Management Instructori:** Gestionarea instructorilor și a specializărilor acestora.
- **Management Vehicule:** Evidența vehiculelor și a stării tehnice (mentenanță).
- **Cursuri:** Structurarea modulelor teoretice și practice.
- **Procesare Plăți:** Procesarea plăților și calculul balanțelor per student.
- **Notificări:** Distribuirea evenimentelor din sistem prin Kafka.

## Arhitectură

Sistemul este compus din următoarele microservicii:

- **API Gateway** (port 8080): Entry point pentru toate request-urile externe.
- **Student Service** (port 8081): Gestiune date studenți.
- **Scheduling Service** (port 8082): Logică rezervări și calendar.
- **Vehicle Service** (port 8083): Gestiune parc auto.
- **Payment Service** (port 8084): Tranzacții și balanțe.
- **Notification Service** (port 8085): Consumer Kafka pentru notificări.
- **Instructor Service** (port 8086): Gestiune personal didactic.

## Stack Tehnologic

- **Limbaj:** Java 21
- **Framework:** Spring Boot 4.0.1, Spring Cloud 2025.1.0
- **Gateway:** Spring Cloud Gateway
- **Messaging:** Apache Kafka
- **Caching:** Redis
- **Database:** PostgreSQL 17
- **Documentație:** SpringDoc OpenAPI (Swagger)
- **Testing:** JUnit 5, Mockito
- **Build:** Maven

## Cerințe de Sistem

Pentru rularea proiectului sunt necesare:

- Java 21 JDK
- Maven 3.8+
- Docker & Docker Compose
- PostgreSQL 17+
- Redis 7+
- Apache Kafka 3.5+

## Instalare și Configurare

### 1. Inițializare Infrastructură

Porniți serviciile dependente (Baza de date, Broker, Cache) folosind Docker Compose:

```bash
docker-compose up -d
```

Aceasta va expune:
- PostgreSQL: port 5432
- Redis: port 6379
- Kafka & Zookeeper: porturi 9092, 2181

Verificare status containere:

```bash
docker-compose ps
```

### 2. Build Proiect

Compilarea modulelor, rularea testelor și crearea arhivelor JAR:

```bash
mvn clean install
```

### 3. Configurare Bază de Date

Schema bazei de date este generată automat la pornirea serviciilor prin JPA/Hibernate. Este necesar ca containerul de PostgreSQL să fie activ înainte de pornirea aplicațiilor.

### 4. Pornirea Serviciilor

Serviciile trebuie pornite în următoarea ordine pentru a evita erori de dependență la startup:

1. Student Service
2. Instructor Service
3. Vehicle Service
4. Payment Service
5. Scheduling Service
6. Notification Service
7. API Gateway

Se pot rula manual din terminal (ex: `cd student-service && mvn spring-boot:run`) sau direct din IDE rulând clasa `*Application.java` corespunzătoare fiecărui modul.

### 5. Verificare Status

După pornire, serviciile pot fi verificate la următoarele adrese:

| Serviciu | Base URL | Swagger UI | OpenAPI Docs |
|----------|----------|-----------|--------------|
| API Gateway | http://localhost:8080 | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| Student Service | http://localhost:8081 | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| Scheduling Service | http://localhost:8082 | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |
| Vehicle Service | http://localhost:8083 | http://localhost:8083/swagger-ui.html | http://localhost:8083/v3/api-docs |
| Payment Service | http://localhost:8084 | http://localhost:8084/swagger-ui.html | http://localhost:8084/v3/api-docs |
| Notification Service | http://localhost:8085 | http://localhost:8085/swagger-ui.html | http://localhost:8085/v3/api-docs |
| Instructor Service | http://localhost:8086 | http://localhost:8086/swagger-ui.html | http://localhost:8086/v3/api-docs |

## Documentație API

API-urile sunt documentate prin Swagger/OpenAPI 3.0. Punctul principal de acces este Swagger-ul expus de API Gateway: http://localhost:8080/swagger-ui.html.

### Generare OpenAPI Combinat

Pentru a genera un fișier JSON unic care conține definițiile tuturor serviciilor:

1. Asigurați-vă că toate serviciile rulează.
2. Rulați scriptul de agregare:
   ```powershell
   cd scripts
   .\generate-combined-openapi.ps1
   ```
3. Fișierul `DrivingSchoolManagementSystem-API-1.0.0.swagger_collection.json` va fi generat în rădăcina proiectului.

## Testare

Rularea tuturor testelor unitare:

```bash
mvn test
```

Rularea testelor pentru un singur modul:

```bash
cd student-service
mvn test
```

Rapoartele de coverage (Jacoco) se generează în `target/site/jacoco/index.html`.

## Structura Proiectului

```
driving-school-platform/
├── api-gateway/          # API Gateway service
├── student-service/      # Management studenți
├── scheduling-service/   # Programare lecții
├── vehicle-service/      # Management vehicule
├── payment-service/      # Procesare plăți
├── instructor-service/   # Management instructori
├── notification-service/ # Notificări evenimente
├── common/               # Cod partajat (exceptions, DTOs, validations)
├── docker-compose.yml    # Configurare infrastructură
├── REQUIREMENTS.md       # Specificații funcționale
└── README.md             # Documentație tehnică
```

## Funcționalități MVP

Versiunea curentă implementează următoarele funcționalități de bază:

- **Management Studenți:** Validare CNP/date contact și stocare documente.
- **Programare Lecții:** Rezervare cu verificare disponibilitate resurse.
- **Instructori & Cursuri:** Definire tipuri cursuri și asocieri instructori.
- **Vehicule:** Evidență flotă și status mentenanță.
- **Financiar:** Procesare plăți și calcul sold curent.

Detalii complete în fișierul [REQUIREMENTS.md](REQUIREMENTS-RO.md).

## Schema Bazei de Date

Entitățile principale ale sistemului sunt:

- **Student:** Date personale cursant.
- **Document:** Fișiere asociate studenților.
- **Instructor:** Date personale și profesionale instructori.
- **Vehicle:** Date tehnice vehicule.
- **Maintenance:** Operațiuni service vehicule.
- **Course:** Tipuri de cursuri disponibile.
- **Lesson:** Lecții efective (planificate/realizate).
- **Payment:** Tranzacții financiare.

## Postman Collection

Pentru testare manuală, importați fișierul `DrivingSchoolManagementSystem-API-1.0.0.postman_collection.json`. Acesta conține request-uri pre-configurate pentru toate endpoint-urile.

### Actualizare Automată Postman

Dacă s-au făcut modificări în API, colecția Postman poate fi actualizată automat pe baza OpenAPI:

1. Porniți serviciile.
2. Setați variabilele de mediu `POSTMAN_API_KEY` și `POSTMAN_COLLECTION_UID`.
3. Rulați scriptul:
   ```powershell
   .\scripts\update-postman-from-openapi.ps1
   ```

## Configurare

Configurarea serviciilor se face prin fișierele `application.yml` din fiecare modul. Parametrii principali includ:

- String-ul de conectare la baza de date (PostgreSQL).
- Host/Port pentru Redis și Kafka.
- Porturile specifice fiecărui serviciu.
- Rutele definite în API Gateway.