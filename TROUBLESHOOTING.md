# Troubleshooting Guide - API Gateway 404 Errors

## Problemă: 404 Not Found când accesezi endpoint-uri prin API Gateway

### Verificări rapide:

1. **Verifică dacă serviciile rulează:**
   ```powershell
   netstat -ano | findstr :8080  # API Gateway
   netstat -ano | findstr :8081  # Student Service
   netstat -ano | findstr :8082  # Scheduling Service
   netstat -ano | findstr :8083  # Vehicle Service
   netstat -ano | findstr :8084  # Payment Service
   ```

2. **Testează serviciile direct (fără Gateway):**
   ```powershell
   # Test Student Service direct
   Invoke-WebRequest -Uri "http://localhost:8081/api/students" -Method GET
   
   # Test prin Gateway (ar trebui să returneze același rezultat)
   Invoke-WebRequest -Uri "http://localhost:8080/api/students" -Method GET
   ```

3. **Verifică logurile Gateway-ului:**
   - Caută erori în consolă când pornești Gateway-ul
   - Verifică dacă există mesaje despre routing sau conectivitate

### Soluții comune:

#### 1. Restart Gateway-ul după modificări de configurare

După ce modifici `application.yml` în api-gateway, **trebuie să restart serviciul**:

```bash
# Oprește Gateway-ul (Ctrl+C în terminal)
# Apoi pornește din nou:
cd api-gateway
mvn spring-boot:run
```

#### 2. Verifică configurația Gateway-ului

Asigură-te că `api-gateway/src/main/resources/application.yml` conține:

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: student-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/students/**
          filters:
            - StripPrefix=0  # IMPORTANT: Păstrează path-ul complet
```

#### 3. Verifică dacă serviciile downstream rulează

Gateway-ul nu poate routea către servicii care nu rulează. Asigură-te că:
- Student Service rulează pe portul 8081
- Scheduling Service rulează pe portul 8082
- Vehicle Service rulează pe portul 8083
- Payment Service rulează pe portul 8084

#### 4. Verifică actuator endpoints pentru debugging

După restart, poți verifica routing-ul prin actuator:

```powershell
# Verifică health
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health"

# Verifică routes configurate
Invoke-WebRequest -Uri "http://localhost:8080/actuator/gateway/routes"
```

#### 5. Verifică logurile pentru detalii

Gateway-ul are logging activat la nivel DEBUG pentru routing. Caută în loguri:
- `org.springframework.cloud.gateway` - pentru detalii despre routing
- Erori de conectivitate către serviciile downstream

### Debugging pas cu pas:

1. **Testează serviciul direct:**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8081/api/students" -Method GET
   ```
   Dacă funcționează → serviciul este OK, problema este la Gateway

2. **Testează Gateway-ul:**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET
   ```
   Dacă returnează 200 → Gateway-ul rulează

3. **Verifică routes:**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/actuator/gateway/routes" -Method GET
   ```
   Ar trebui să vezi toate route-urile configurate

4. **Testează routing-ul:**
   ```powershell
   Invoke-WebRequest -Uri "http://localhost:8080/api/students" -Method GET
   ```

### Probleme comune și soluții:

| Problemă | Cauză | Soluție |
|----------|-------|---------|
| 404 Not Found | Gateway nu routează corect | Verifică `application.yml` și restart Gateway |
| Connection refused | Serviciul downstream nu rulează | Pornește serviciul corespunzător |
| Timeout | Serviciul downstream răspunde lent | Verifică logurile serviciului downstream |
| CORS errors | Configurație CORS incorectă | Verifică `globalcors` în `application.yml` |

### Verificare finală:

După ce ai făcut modificările, asigură-te că:

1. ✅ Toate serviciile rulează
2. ✅ Gateway-ul a fost restartat
3. ✅ Configurația `application.yml` este corectă
4. ✅ Nu există erori în loguri

### Test complet:

```powershell
# 1. Test direct serviciu
Write-Host "Testing Student Service directly..."
Invoke-WebRequest -Uri "http://localhost:8081/api/students" -Method GET

# 2. Test prin Gateway
Write-Host "Testing through API Gateway..."
Invoke-WebRequest -Uri "http://localhost:8080/api/students" -Method GET

# 3. Verifică health
Write-Host "Checking Gateway health..."
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -Method GET
```

Dacă toate testele trec, Gateway-ul ar trebui să funcționeze corect!

