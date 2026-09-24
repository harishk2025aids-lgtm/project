# Rural Health Network Platform

AI-Based Patient Health Record & Telemedicine Triage Management System.
A Spring Boot + MySQL backend covering secure patient intake, automated
rule-based triage queue sorting, diagnostic service billing, and
departmental budget/accounting reports.

## Stack
- Java 17
- Spring Boot 3.3.4 (Web, Data JPA, Validation)
- MySQL 8
- Maven

## Prerequisites (on your own machine — NOT this sandbox)
1. **JDK 17+** installed (`java -version`)
2. **Maven 3.8+** installed (`mvn -version`), or use an IDE (IntelliJ/Eclipse/VS Code) that has its own bundled Maven
3. **MySQL 8** running locally (or reachable), with a user that can create databases

> This project could not be compiled inside the assistant's sandbox because
> outbound network access there is restricted and does not allow reaching
> Maven Central (`repo.maven.apache.org`) to download Spring Boot's
> dependencies. Every file has been manually checked for consistency, but
> you must run the actual build on a machine with normal internet access.

## Stage 1 — Database

The schema will be **auto-created by Hibernate** on first run
(`spring.jpa.hibernate.ddl-auto=update` in `application.properties`), so
you don't have to run anything by hand — just make sure a MySQL server is
running and the credentials in `application.properties` are correct
(default: `root` / `root`, `localhost:3306`). The app auto-creates the
`rural_health_db` schema itself.

If you'd rather set the schema up manually first (and inspect the table
design), a standalone script is included:

```bash
mysql -u root -p < src/main/resources/schema.sql
```

If you use this script, change `spring.jpa.hibernate.ddl-auto=update` to
`validate` in `application.properties` afterwards so Hibernate doesn't try
to alter what you already created.

## Stage 2 — Run the Spring Boot project

```bash
cd health-platform
mvn spring-boot:run
```

Or build a jar and run it:

```bash
mvn clean package -DskipTests
java -jar target/health-platform.jar
```

The API starts on **http://localhost:8080**.

## Stage 3 — REST APIs (Patient/Doctor/etc. CRUD)

| Resource      | Endpoints |
|---------------|-----------|
| Patients      | `GET/POST /api/patients`, `GET/PUT/DELETE /api/patients/{id}` |
| Doctors       | `GET/POST /api/doctors`, `GET/PUT/DELETE /api/doctors/{id}` |
| Departments   | `GET/POST /api/departments`, `PUT/DELETE /api/departments/{id}` |
| Products/Services | `GET/POST /api/products`, `PUT/DELETE /api/products/{id}` |
| Consultations | `GET /api/consultations`, `GET /api/consultations/{id}`, `GET /api/consultations/patient/{patientId}`, `PATCH /api/consultations/{id}/status` |
| Suppliers     | `GET/POST /api/suppliers` |
| Purchase Orders | `GET/POST /api/purchase-orders`, `PATCH /api/purchase-orders/{id}/receive` |

Example — create a patient:
```bash
curl -X POST http://localhost:8080/api/patients \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Asha Devi","dateOfBirth":"1990-05-12","gender":"F","phone":"9876543210","address":"Pollachi","bloodGroup":"O+","emergencyContact":"9876500000"}'
```

## Stage 4 — AI Triage (rule-based)

`POST /api/triage/evaluate` accepts symptoms + vitals, saves a `Consultation`
record, and returns a computed triage score/priority
(`EMERGENCY` / `URGENT` / `NORMAL`). The scoring logic (in
`TriageService.java`) is a simplified early-warning score built from heart
rate, blood pressure, SpO2, temperature, respiratory rate, plus red/amber
keyword flags in the free-text symptoms.

```bash
curl -X POST http://localhost:8080/api/triage/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "patientId": 1,
    "departmentId": 1,
    "symptoms": "severe chest pain and dizziness",
    "heartRate": 128,
    "systolicBp": 88,
    "diastolicBp": 60,
    "temperatureCelsius": 37.9,
    "spo2": 91,
    "respiratoryRate": 26
  }'
```

`GET /api/triage/queue` returns the current waiting-room queue, ordered by
triage score (most urgent first) — this is the automated triage queue
sorting requirement.

**Swapping in an ML model later:** replace the body of
`TriageService.evaluate()` with a call to your trained model (a REST call
to a Python/FastAPI microservice, or a Java ML runtime like ONNX/DJL). The
method signature (`TriageRequest` in, `TriageResponse` out) and the
`Consultation` persistence step can stay exactly the same, so no
controller or other service needs to change.

## Billing (diagnostic services)

- `POST /api/invoices` — create an invoice from one or more billable
  products/services (uses `BillingService`, which also posts a journal
  entry: Debit Accounts Receivable / Credit Revenue).
- `POST /api/payments` — record a payment against an invoice (posts
  Debit Cash / Credit Accounts Receivable, and updates invoice status to
  `PARTIAL` or `PAID`).

```bash
curl -X POST http://localhost:8080/api/invoices \
  -H "Content-Type: application/json" \
  -d '{"patientId":1,"consultationId":1,"lines":[{"productId":1,"quantity":1}]}'

curl -X POST http://localhost:8080/api/payments \
  -H "Content-Type: application/json" \
  -d '{"invoiceId":1,"amount":500,"method":"CASH"}'
```

## Stage 5 — Reports

| Report | Endpoint |
|--------|----------|
| Profit & Loss | `GET /api/reports/profit-loss?year=2026` |
| Balance Sheet | `GET /api/reports/balance-sheet` |
| Budget Utilization | `GET /api/budgets/utilization?year=2026&month=9` |

The P&L and Balance Sheet are derived live from the double-entry
`chart_of_accounts` / `journal_entries` / `journal_lines` ledger — every
invoice, payment, and received purchase order automatically posts a
balanced journal entry (see `JournalService.java`), so these reports stay
accurate without any separate reconciliation step.

Budget workflow:
```bash
# Allocate a department's monthly budget
curl -X POST http://localhost:8080/api/budgets \
  -H "Content-Type: application/json" \
  -d '{"department":{"departmentId":1},"fiscalYear":2026,"fiscalMonth":9,"allocatedAmount":50000}'

# Record spend against it (e.g. after a purchase order)
curl -X PATCH http://localhost:8080/api/budgets/1/spend \
  -H "Content-Type: application/json" -d '{"amount":1200}'
```

## Project layout

```
src/main/java/com/ruralhealth/platform/
  entity/       15 JPA entities (one per table)
  repository/   Spring Data JPA repositories
  service/      TriageService, BillingService, BudgetService, JournalService, ReportService
  controller/   REST controllers (Patient, Doctor, Department, Product,
                Triage, Consultation, Invoice, Payment, Supplier,
                PurchaseOrder, Budget, Report)
  dto/          Request/response payloads (Triage, reports)
src/main/resources/
  application.properties   DB connection + JPA settings
  schema.sql               Optional manual Stage-1 reference script
```

## Notes / things to adjust before production use

- **Security**: there is no authentication/authorization layer yet. For a
  real deployment, add Spring Security (JWT or session-based) in front of
  all `/api/**` endpoints, since this handles patient health data.
- **Validation**: add `@Valid` + Bean Validation annotations on the
  request DTOs/entities for stricter input checking.
- **Database credentials**: change the default `root/root` credentials in
  `application.properties` before deploying anywhere shared.
