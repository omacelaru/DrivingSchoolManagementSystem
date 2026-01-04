# Cerințe de Business - Sistem de Management Școală Auto

## 1. Cerințe de Business (10 cerințe)

### BR1: Management Studenți
Sistemul trebuie să permită înregistrarea, actualizarea și ștergerea studenților. Fiecare student trebuie să aibă un CNP unic, email unic, număr de telefon valid (10 cifre) și adresă. Statusul studentului poate fi: PENDING, ACTIVE, SUSPENDED sau GRADUATED.

### BR2: Management Documente Studenți
Sistemul trebuie să permită încărcarea și gestionarea documentelor pentru fiecare student. Tipurile de documente acceptate sunt: copie CI, certificat medical, fotografie și copie permis de conducere. Fiecare document are un status: PENDING, APPROVED sau REJECTED.

### BR3: Management Instructori
Sistemul trebuie să permită înregistrarea și gestionarea instructorilor. Fiecare instructor are un număr de licență unic, email unic, număr de telefon valid și o specializare (THEORETICAL, PRACTICAL sau BOTH). Sistemul trebuie să permită căutarea instructorilor după specializare și verificarea disponibilității lor.

### BR4: Management Vehicule
Sistemul trebuie să permită înregistrarea și gestionarea flotei de vehicule. Fiecare vehicul are un număr de înmatriculare unic, marcă, model, an, dată de expirare a asigurării și status (AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE). Sistemul trebuie să permită trimiterea vehiculelor în mentenanță și returnarea lor în serviciu.

### BR5: Management Mentenanță Vehicule
Sistemul trebuie să permită înregistrarea și urmărirea operațiunilor de mentenanță pentru fiecare vehicul. Fiecare operațiune de mentenanță are o dată, tip (ROUTINE, REPAIR, INSPECTION, OTHER), descriere și cost.

### BR6: Management Cursuri
Sistemul trebuie să permită crearea și gestionarea cursurilor. Fiecare curs are un nume, descriere, preț, instructor asociat, vehicul asociat, număr de lecții incluse și tip (THEORETICAL sau PRACTICAL). Sistemul trebuie să permită actualizarea și ștergerea cursurilor (doar dacă nu au lecții asociate).

### BR7: Programare Lecții
Sistemul trebuie să permită rezervarea, actualizarea și anularea lecțiilor. La rezervarea unei lecții, sistemul trebuie să verifice disponibilitatea instructorului și a vehiculului pentru intervalul de timp solicitat. Fiecare lecție are un student, curs (opțional), timp de început, timp de sfârșit și status (SCHEDULED, COMPLETED, CANCELLED, NO_SHOW).

### BR8: Verificare Disponibilitate
Sistemul trebuie să permită verificarea disponibilității instructorilor și vehiculelor pentru un interval de timp specificat. Verificarea trebuie să ia în considerare toate lecțiile programate existente și să prevină suprapunerea programărilor.

### BR9: Management Plăți
Sistemul trebuie să permită procesarea plăților pentru lecții și cursuri. Fiecare plată are un student asociat, sumă, metodă de plată (CARD, CASH, BANK_TRANSFER, ONLINE), status (PENDING, COMPLETED, FAILED, REFUNDED, CANCELLED), dată tranzacție și ID tranzacție unic. Sistemul trebuie să permită calcularea balanței totale a unui student și procesarea rambursărilor.

### BR10: Notificări Evenimente
Sistemul trebuie să trimită notificări automatizate pentru evenimente importante precum: rezervarea unei lecții, anularea unei lecții, confirmarea unei plăți, trimiterea unui vehicul în mentenanță etc. Notificările trebuie să fie gestionate printr-un sistem de mesagerie asincronă.

---

## 2. Funcționalități MVP (Minimum Viable Product)

### Funcționalitate 1: Management Studenți și Documente
**Descriere:** Permite înregistrarea completă a studenților cu validare CNP, email și telefon. Sistemul permite încărcarea și gestionarea documentelor necesare pentru fiecare student, cu tracking al statusului de aprobare.

**Endpoints principale:**
- POST `/api/students` - Înregistrare student nou
- GET `/api/students/{id}` - Vizualizare detalii student
- PUT `/api/students/{id}` - Actualizare date student
- DELETE `/api/students/{id}` - Ștergere student
- POST `/api/students/{id}/documents` - Încărcare document
- GET `/api/students/{id}/documents` - Listare documente student

**Valori de business:**
- Validare CNP unic și valid
- Validare email unic
- Validare telefon (10 cifre)
- Status documente: PENDING → APPROVED/REJECTED

---

### Funcționalitate 2: Programare și Management Lecții
**Descriere:** Permite rezervarea lecțiilor cu verificare automată a disponibilității instructorului și vehiculului. Sistemul previne suprapunerea programărilor și permite reschedularea sau anularea lecțiilor.

**Endpoints principale:**
- POST `/api/lessons` - Rezervare lecție nouă
- GET `/api/lessons/{id}` - Vizualizare detalii lecție
- PUT `/api/lessons/{id}` - Reschedulare lecție
- DELETE `/api/lessons/{id}` - Anulare lecție
- GET `/api/lessons/instructors/{instructorId}/availability` - Verificare disponibilitate instructor
- GET `/api/lessons/vehicles/{vehicleId}/availability` - Verificare disponibilitate vehicul
- GET `/api/lessons/students/{studentId}` - Listare lecții student

**Valori de business:**
- Verificare conflict programări
- Validare interval timp (startTime < endTime)
- Status lecție: SCHEDULED → COMPLETED/CANCELLED/NO_SHOW
- Prevenire rezervări în trecut

---

### Funcționalitate 3: Management Cursuri și Instructori
**Descriere:** Permite crearea și gestionarea cursurilor cu instructor și vehicul asociat. Sistemul permite căutarea instructorilor după specializare și verificarea disponibilității lor pentru programări.

**Endpoints principale:**
- POST `/api/courses` - Creare curs nou
- GET `/api/courses/{id}` - Vizualizare detalii curs
- PUT `/api/courses/{id}` - Actualizare curs
- DELETE `/api/courses/{id}` - Ștergere curs
- POST `/api/instructors` - Înregistrare instructor nou
- GET `/api/instructors/{id}` - Vizualizare detalii instructor
- GET `/api/instructors/available` - Listare instructori disponibili
- GET `/api/instructors/specialization/{specialization}` - Căutare după specializare

**Valori de business:**
- Validare număr licență instructor unic
- Validare email instructor unic
- Specializare: THEORETICAL, PRACTICAL, BOTH
- Tip curs: THEORETICAL, PRACTICAL
- Preț curs pozitiv
- Număr lecții pozitiv

---

### Funcționalitate 4: Management Vehicule și Mentenanță
**Descriere:** Permite gestionarea flotei de vehicule, inclusiv trimiterea în mentenanță și returnarea în serviciu. Sistemul înregistrează toate operațiunile de mentenanță cu costuri și tipuri.

**Endpoints principale:**
- POST `/api/vehicles` - Înregistrare vehicul nou
- GET `/api/vehicles/{id}` - Vizualizare detalii vehicul
- PUT `/api/vehicles/{id}` - Actualizare vehicul
- GET `/api/vehicles/available` - Listare vehicule disponibile
- PUT `/api/vehicles/{id}/maintenance` - Trimite vehicul în mentenanță
- PUT `/api/vehicles/{id}/maintenance/return` - Returnează vehicul din mentenanță

**Valori de business:**
- Validare număr înmatriculare unic
- Validare dată expirare asigurare (viitoare)
- Status vehicul: AVAILABLE, IN_USE, MAINTENANCE, OUT_OF_SERVICE
- Tip mentenanță: ROUTINE, REPAIR, INSPECTION, OTHER
- Prevenire utilizare vehicul în mentenanță

---

### Funcționalitate 5: Procesare Plăți și Balanțe
**Descriere:** Permite procesarea plăților pentru lecții și cursuri, cu suport pentru multiple metode de plată. Sistemul calculează balanțele studenților și permite rambursări.

**Endpoints principale:**
- PUT `/api/payments` - Procesare plată
- POST `/api/payments/pending` - Creare plată pending
- GET `/api/payments/{id}` - Vizualizare detalii plată
- GET `/api/payments/student/{studentId}` - Listare plăți student
- GET `/api/payments/student/{studentId}/balance` - Calculare balanță student
- PUT `/api/payments/{id}/refund` - Procesare rambursare
- PUT `/api/payments/{id}/status` - Actualizare status plată

**Valori de business:**
- Validare sumă pozitivă
- Metodă plată: CARD, CASH, BANK_TRANSFER, ONLINE
- Status plată: PENDING → COMPLETED/FAILED → REFUNDED
- ID tranzacție unic
- Calcul balanță = sumă totală plăți COMPLETED
- Prevenire rambursare dublă (pessimistic locking)

---

## 3. Diagrama Relațiilor dintre Entități

```
┌─────────────────┐
│    Student      │
│─────────────────│
│ id (PK)         │
│ firstName       │
│ lastName        │
│ cnp (UNIQUE)    │
│ email (UNIQUE)  │
│ phone           │
│ address         │
│ status          │
│ registrationDate│
└────────┬────────┘
         │
         │ 1
         │
         │ *
         │
┌────────▼────────┐         ┌─────────────────┐
│    Document     │         │     Lesson      │
│─────────────────│         │─────────────────│
│ id (PK)         │         │ id (PK)         │
│ student_id (FK) │◄────────┤ student_id (FK) │
│ documentType    │         │ course_id (FK)   │
│ filePath        │         │ startTime        │
│ status          │         │ endTime          │
│ uploadDate      │         │ status           │
└─────────────────┘         └────────┬────────┘
                                      │
                                      │ *
                                      │
                                      │ 1
                                      │
                            ┌─────────▼─────────┐
                            │      Course       │
                            │───────────────────│
                            │ id (PK)           │
                            │ name              │
                            │ description       │
                            │ price             │
                            │ instructor_id (FK)│
                            │ vehicle_id (FK)   │
                            │ numberOfLessons   │
                            │ courseType        │
                            └───────────────────┘
                                      │
                                      │
                    ┌─────────────────┼─────────────────┐
                    │                 │                 │
                    │ 1               │ 1               │
                    │                 │                 │
                    │                 │                 │
        ┌───────────▼──────┐  ┌───────▼──────────┐     │
        │   Instructor    │  │     Vehicle      │     │
        │─────────────────│  │──────────────────│     │
        │ id (PK)         │  │ id (PK)          │     │
        │ firstName       │  │ licensePlate(UNQ)│     │
        │ lastName        │  │ make             │     │
        │ licenseNumber   │  │ model            │     │
        │ email (UNIQUE)  │  │ year             │     │
        │ phone           │  │ insuranceExpiry   │     │
        │ specialization  │  │ status           │     │
        │ rating          │  └────────┬─────────┘     │
        └─────────────────┘          │                │
                                     │ 1              │
                                     │                │
                                     │ *              │
                                     │                │
                            ┌────────▼─────────┐      │
                            │   Maintenance    │      │
                            │──────────────────│      │
                            │ id (PK)          │      │
                            │ vehicle_id (FK)  │      │
                            │ maintenanceDate  │      │
                            │ description      │      │
                            │ cost             │      │
                            │ type             │      │
                            └──────────────────┘      │
                                                       │
                                                       │
                            ┌─────────────────────────▼──────┐
                            │         Payment                │
                            │────────────────────────────────│
                            │ id (PK)                        │
                            │ student_id (FK)                │
                            │ lesson_id (FK, nullable)        │
                            │ amount                          │
                            │ paymentMethod                   │
                            │ status                          │
                            │ transactionDate                 │
                            │ transactionId (UNIQUE)          │
                            │ notes                           │
                            └────────────────────────────────┘
```

### Relații JPA Implementate:

1. **Student ↔ Document** (OneToMany/ManyToOne)
   - Un Student poate avea multiple Documente
   - Un Document aparține unui singur Student
   - Cascade: ALL, OrphanRemoval: true

2. **Vehicle ↔ Maintenance** (OneToMany/ManyToOne)
   - Un Vehicle poate avea multiple Maintenance records
   - Un Maintenance record aparține unui singur Vehicle

3. **Course ↔ Lesson** (OneToMany/ManyToOne)
   - Un Course poate avea multiple Lessons
   - O Lesson poate aparține unui Course (opțional)
   - Cascade: ALL

4. **Lesson → Student** (ManyToOne - referință logică)
   - O Lesson are un studentId (Long) - referință către Student service
   - Nu este relație JPA directă (microservices)

5. **Course → Instructor** (ManyToOne - referință logică)
   - Un Course are un instructorId (Long) - referință către Instructor service

6. **Course → Vehicle** (ManyToOne - referință logică)
   - Un Course are un vehicleId (Long) - referință către Vehicle service

7. **Payment → Student** (ManyToOne - referință logică)
   - Un Payment are un studentId (Long) - referință către Student service

8. **Payment → Lesson** (ManyToOne - referință logică)
   - Un Payment poate avea un lessonId (Long, nullable) - referință către Lesson service

### Note importante:
- Sistemul folosește arhitectură microservices, deci unele relații sunt logice (prin ID-uri) și nu relații JPA directe
- Toate entitățile au audit fields: `createdAt`, `lastModifiedDate`
- Toate entitățile au validări la nivel de câmp (@NotNull, @NotBlank, @Email, etc.)
- Indexuri sunt create pe câmpuri frecvent căutate (CNP, email, status, etc.)

