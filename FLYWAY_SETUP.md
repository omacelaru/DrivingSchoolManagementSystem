# Flyway Setup - Driving School Management System

## Configurare Flyway

Flyway a fost configurat pentru toate serviciile care folosesc baze de date:
- **Student Service**
- **Scheduling Service**
- **Vehicle Service**
- **Payment Service**

## Structura Migrațiilor

Migrațiile SQL sunt stocate în:
```
{service}/src/main/resources/db/migration/
```

Formatul fișierelor de migrație:
```
V{version}__{description}.sql
```

Versiunile folosesc format cu 2 cifre: V00, V01, V02, etc.

Exemplu: `V00__Create_students_table.sql`

## Configurație

Fiecare serviciu are configurația Flyway în `application.yml`:

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
```

### Explicație configurație:

- **enabled: true** - Activează Flyway
- **locations** - Directorul unde sunt migrațiile
- **baseline-on-migrate: true** - Creează baseline dacă baza de date există deja
- **baseline-version: 0** - Versiunea de bază
- **validate-on-migrate: true** - Validează migrațiile la startup

## JPA Configuration

`ddl-auto` a fost schimbat de la `update` la `validate` pentru ca Flyway să gestioneze schema:

```yaml
jpa:
  hibernate:
    ddl-auto: validate  # Flyway gestionează schema
```

## Migrații Create

### Student Service
- `V00__Create_students_table.sql` - Tabela students
- `V01__Create_documents_table.sql` - Tabela documents

### Scheduling Service
- `V00__Create_instructors_table.sql` - Tabela instructors
- `V01__Create_lessons_table.sql` - Tabela lessons

### Vehicle Service
- `V00__Create_vehicles_table.sql` - Tabela vehicles

### Payment Service
- `V00__Create_courses_table.sql` - Tabela courses
- `V01__Create_payments_table.sql` - Tabela payments
- `V02__Create_invoices_table.sql` - Tabela invoices

## Cum funcționează

1. **La startup**, Flyway verifică versiunea bazei de date
2. **Aplică migrațiile** care nu au fost rulate încă, în ordinea versiunilor
3. **Validează** că migrațiile nu au fost modificate
4. **Creează tabela** `flyway_schema_history` pentru a ține evidența migrațiilor

## Creare Migrații Noi

### 1. Creează fișierul SQL

Creează un fișier nou în `{service}/src/main/resources/db/migration/`:

```
V{next_version}__{description}.sql
```

Versiunile folosesc format cu 2 cifre: V00, V01, V02, etc.

Exemplu: `V02__Add_student_phone_index.sql`

### 2. Scrie migrația SQL

```sql
-- Add index for phone number
CREATE INDEX IF NOT EXISTS idx_student_phone ON students(phone);
```

### 3. Restart serviciul

După ce adaugi o migrație nouă, restart serviciul și Flyway o va aplica automat.

## Reguli Importante

1. **NU modifica migrațiile existente** - Dacă o migrație a fost deja aplicată, nu o modifica!
2. **Versiuni unice** - Fiecare migrație trebuie să aibă o versiune unică
3. **Ordine crescătoare** - Versiunile trebuie să fie în ordine crescătoare
4. **Nume descriptive** - Folosește nume descriptive pentru fișiere

## Rollback

Flyway nu suportă rollback automat. Pentru a face rollback:

1. Creează o migrație nouă care anulează schimbările
2. Sau restaurează manual baza de date dintr-un backup

## Verificare Status

Poți verifica statusul migrațiilor prin logurile aplicației sau prin interogarea tabelei:

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## Comenzi Utile

### Verifică migrațiile aplicate
```sql
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank;
```

### Verifică versiunea curentă
```sql
SELECT version FROM flyway_schema_history 
WHERE success = true 
ORDER BY installed_rank DESC 
LIMIT 1;
```

## Troubleshooting

### Eroare: "Migration checksum mismatch"
- **Cauză**: Migrația a fost modificată după ce a fost aplicată
- **Soluție**: Nu modifica migrații existente. Creează o migrație nouă pentru schimbări

### Eroare: "Migration version already exists"
- **Cauză**: Există deja o migrație cu aceeași versiune
- **Soluție**: Folosește o versiune nouă

### Eroare: "Baseline required"
- **Cauză**: Baza de date există dar nu are tabela flyway_schema_history
- **Soluție**: `baseline-on-migrate: true` rezolvă automat această problemă

## Best Practices

1. **Testează migrațiile** înainte de a le aplica în producție
2. **Backup** baza de date înainte de migrații importante
3. **Documentează** migrațiile complexe în comentarii SQL
4. **Folosește IF NOT EXISTS** pentru a evita erori dacă obiectul există deja
5. **Testează rollback** pentru migrații critice

