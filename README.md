# Insurance Claim Tracker API

A Java 17 / Spring Boot REST service that manages property & casualty insurance policies and
the claims filed against them. It issues a policy, calculates its premium, accepts a claim and
moves the claim through a strict review lifecycle.

Built with **Spring Boot 3**, **Spring Data JPA**, **H2 / PostgreSQL**, **JUnit 5 + Mockito** and
**OpenAPI / Swagger UI**.

---

## Quick start (under 10 minutes)

### Prerequisites
- Java 17 or newer
- Maven 3.9+ (or use the Docker path below)

### Run locally (H2 in-memory — zero setup)

```bash
mvn spring-boot:run
```

Then open:

| What | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |
| H2 console | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:insurancedb`, user `sa`, empty password) |

The API starts with an empty database. Use the examples below to create a policy and a claim.

### Run the tests

```bash
mvn test        # 66 unit + web + integration tests
mvn verify      # also enforces the 80% line-coverage gate (JaCoCo)
```

---

## API

| Method | Endpoint | Purpose | Success | Errors |
|---|---|---|---|---|
| POST | `/policies` | Create a policy, returns calculated premium | 201 | 400 |
| GET | `/policies/{id}` | Fetch a policy | 200 | 404 |
| POST | `/policies/{id}/claims` | File a claim (starts `SUBMITTED`) | 201 | 400, 404 |
| PATCH | `/claims/{id}/status` | Change claim status | 200 | 400, 404, 409 |
| GET | `/claims?status=SUBMITTED` | List claims, filter by status | 200 | 400 |

### Create an auto policy

```bash
curl -X POST http://localhost:8080/policies \
  -H "Content-Type: application/json" \
  -d '{
        "type": "AUTO",
        "holderName": "Asha Rao",
        "coverageAmount": 100000,
        "startDate": "2025-01-01",
        "endDate": "2026-01-01",
        "vehicleNumber": "MH12 AB1234",
        "driverAge": 30
      }'
```

```json
{
  "id": 1,
  "type": "AUTO",
  "holderName": "Asha Rao",
  "coverageAmount": 100000.00,
  "startDate": "2025-01-01",
  "endDate": "2026-01-01",
  "premium": 3000.00,
  "vehicleNumber": "MH12 AB1234",
  "driverAge": 30
}
```

### Create a home policy

```bash
curl -X POST http://localhost:8080/policies \
  -H "Content-Type: application/json" \
  -d '{
        "type": "HOME",
        "holderName": "Vikram Singh",
        "coverageAmount": 200000,
        "startDate": "2025-03-01",
        "endDate": "2026-03-01",
        "propertyValue": 500000,
        "locationRisk": "MEDIUM"
      }'
```

### File a claim

```bash
curl -X POST http://localhost:8080/policies/1/claims \
  -H "Content-Type: application/json" \
  -d '{"description": "Bumper damage", "amount": 5000, "claimDate": "2025-06-01"}'
```

### Move a claim through review

```bash
curl -X PATCH http://localhost:8080/claims/1/status \
  -H "Content-Type: application/json" -d '{"status": "UNDER_REVIEW"}'

curl -X PATCH http://localhost:8080/claims/1/status \
  -H "Content-Type: application/json" -d '{"status": "APPROVED"}'
```

An invalid transition (e.g. `SUBMITTED` straight to `APPROVED`, or any change after a final state)
returns **HTTP 409**:

```json
{
  "timestamp": "2025-06-02T10:15:30Z",
  "status": 409,
  "error": "Conflict",
  "message": "Invalid claim status transition from SUBMITTED to APPROVED",
  "path": "/claims/1/status"
}
```

### List claims

```bash
curl "http://localhost:8080/claims?status=SUBMITTED"
```

---

## Business rules

### Premium calculation (Strategy pattern)

`PremiumCalculator` has one implementation per policy type, resolved by `PremiumCalculatorRegistry`.
New policy types only need a new `PremiumCalculator` bean.

**Auto** — `premium = coverageAmount × 0.03 × driverFactor`

| Driver age | Factor |
|---|---|
| 16–24 | 1.50 |
| 25–60 | 1.00 |
| 61+ | 1.20 |

**Home** — `premium = (coverageAmount × 0.02 + propertyValue × 0.001) × riskFactor`

| Location risk | Factor |
|---|---|
| LOW | 1.00 |
| MEDIUM | 1.20 |
| HIGH | 1.50 |

Premiums are rounded to two decimals (HALF_UP).

### Claim lifecycle

```
SUBMITTED ──► UNDER_REVIEW ──► APPROVED
                            └► REJECTED
```

- New claims start as `SUBMITTED`.
- `APPROVED` and `REJECTED` are terminal; any further change is rejected with 409.
- A claim whose amount exceeds the policy's coverage amount is rejected with 400.

---

## Architecture

Layered, no business logic in controllers:

```
controller/   REST endpoints, validation, HTTP status handling
service/      business logic (PolicyService, ClaimService)
  premium/    PremiumCalculator strategies + registry
  claim/      ClaimStateMachine
  mapper/     entity -> DTO mapping
repository/   Spring Data JPA repositories
model/        JPA entities and enums
dto/          request/response payloads + ApiError
exception/    domain exceptions + GlobalExceptionHandler (@RestControllerAdvice)
config/       OpenAPI metadata
```

| Concern | Status codes |
|---|---|
| Validation failure | 400 |
| Business rule violation | 400 |
| Not found | 404 |
| Conflict (invalid transition) | 409 |
| Unexpected | 500 |

---

## Configuration & deployment

Development uses H2 automatically from `application.yml`. Deployment uses PostgreSQL via the
`postgres` profile (`application-postgres.yml`) — no code changes required.

```bash
export SPRING_PROFILES_ACTIVE=postgres
export DATABASE_URL=jdbc:postgresql://localhost:5432/insurance
export DATABASE_USERNAME=insurance
export DATABASE_PASSWORD=insurance
mvn spring-boot:run
```

### Docker

```bash
docker compose up --build
```

Starts PostgreSQL and the API together on http://localhost:8080.

Or build just the image:

```bash
mvn clean package
docker build -t insurance-claim-tracker .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=postgres \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/insurance \
  -e DATABASE_USERNAME=insurance -e DATABASE_PASSWORD=insurance \
  insurance-claim-tracker
```

---

## Deploy free (Render + Neon)

The repo ships a [`render.yaml`](render.yaml) blueprint and a `Dockerfile`, and the app automatically
converts a `DATABASE_URL` in `postgres://user:pass@host/db` form into the JDBC settings it needs. No
code changes are required for the cloud.

> Free-tier tradeoff: the Render free web service sleeps after ~15 minutes idle (the next request
> cold-starts in ~30–60s) and Neon autosuspends when unused. Neither expires or charges.

1. **Database — [Neon](https://neon.tech)** (free, no expiry): create a project, copy the
   **connection string** (`postgresql://…?sslmode=require`).
2. **App — [Render](https://render.com)**: sign up with GitHub → **New → Blueprint** → pick the
   `Insurance-Claim-Tracker-API` repo. Render reads `render.yaml`.
3. When prompted, set the `DATABASE_URL` environment variable to the Neon connection string.
4. Deploy. Render builds the Docker image and health-checks `/actuator/health`.

Your API is then live at `https://insurance-claim-tracker-api.onrender.com` (Swagger UI at
`/swagger-ui.html`).

Any other provider works too — set `DATABASE_URL` (Railway, Heroku, Koyeb) and expose the port via
`$PORT`, which the app already honours.

## Tech stack

Java 17 · Spring Boot 3.2 · Spring Data JPA · Bean Validation · H2 · PostgreSQL ·
JUnit 5 · Mockito · JaCoCo · SpringDoc OpenAPI (Swagger UI) · Maven · Docker
