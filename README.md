# KuzaHealth Backend

KuzaHealth is a Java 17 and Spring Boot backend for managing maternal and infant health records. It provides REST APIs for health workers, parents, pregnancies, visits, visit notes, infants, vaccinations, nutrition information, and audit logs, with PostgreSQL persistence and email/SMS integrations.

This README describes the implementation in this repository, including configuration limitations that affect local setup and deployment.

## Contents

- [Technology](#technology)
- [Local setup](#local-setup)
- [Configuration](#configuration)
- [Docker](#docker)
- [Authentication](#authentication)
- [API reference](#api-reference)
- [Project structure](#project-structure)
- [Build and tests](#build-and-tests)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## Technology

| Component | Implementation |
| --- | --- |
| Runtime | Java 17 |
| Framework | Spring Boot 3.3.4 |
| Build | Maven; wrapper downloads Maven 3.9.9 |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL, HikariCP |
| Authentication | Spring Security, BCrypt passwords, JJWT 0.11.5 |
| API documentation | Springdoc OpenAPI 2.2.0 |
| Messaging | Jakarta Mail / Spring Mail, Pindo SMS through OkHttp |
| Code generation | Lombok and MapStruct |
| Operations | Spring Boot Actuator, Docker, GitHub Actions / Render deploy hook |

## Local setup

### Prerequisites

- JDK 17, with `JAVA_HOME` pointing to your JDK.
- A reachable PostgreSQL database. Docker is optional for running a local database.
- Network access for the first Maven wrapper run and dependency downloads.
- Your own Gmail SMTP credentials and Pindo token to exercise OTP delivery and messaging.

Run commands from the repository root. On Windows, use `mvnw.cmd` instead of `./mvnw` and configure environment variables through PowerShell or your IDE.

### 1. Prepare PostgreSQL

Use an existing empty development database, or start a local instance:

```bash
docker run --name kuzahealth-postgres \
  -e POSTGRES_DB=kuzahealth \
  -e POSTGRES_USER=kuzahealth \
  -e POSTGRES_PASSWORD=local-development-only \
  -p 5432:5432 \
  -v kuzahealth-postgres-data:/var/lib/postgresql/data \
  -d postgres:16
```

The volume preserves database contents across container removal. For an existing container, use `docker start kuzahealth-postgres`. Hibernate uses `ddl-auto=update` to create/update tables at application startup; no migration framework is configured.

### 2. Configure the application

Explicitly override the checked-in database and messaging settings before running the application:

```bash
export SPRING_DATASOURCE_URL='jdbc:postgresql://localhost:5432/kuzahealth'
export SPRING_DATASOURCE_USERNAME='kuzahealth'
export SPRING_DATASOURCE_PASSWORD='local-development-only'
export SPRING_PROFILES_ACTIVE='local'
export PORT='8080'

export SPRING_MAIL_USERNAME='your-account@gmail.com'
export SPRING_MAIL_PASSWORD='your-gmail-app-password'
export MAIL_FROM='your-account@gmail.com'
export PINDO_TOKEN='your-pindo-token'
```

Replace messaging placeholders with your credentials before testing delivery. The `local` profile selects the shared application settings without enabling the development seeder; there is no separate `application-local.properties` file. Spring Boot does **not** automatically load the repository's `.env` file when launched with Maven or `java -jar`.

### 3. Start the server

```bash
./mvnw spring-boot:run
```

Check the server and explore the API:

```bash
curl http://localhost:8080/api/v1/test/greet
curl http://localhost:8080/actuator/health
```

- [Swagger UI](http://localhost:8080/swagger-ui/index.html)
- [OpenAPI JSON](http://localhost:8080/v3/api-docs)
- [Health](http://localhost:8080/actuator/health)

### Development data

Setting `SPRING_PROFILES_ACTIVE=dev` enables `ParentSeeder`, which inserts 100 generated parents only when the parent table is empty. It does not create login accounts. Its generated phone numbers and email addresses are not verified test destinations; avoid sending notifications to seeded records.

CSV, SQL, and database dump files are included under `src/main/resources/data/`, with another dump at the repository root. They are not required for this setup, and the parent seeder does not import them.

## Configuration

The shared configuration is in [application.properties](src/main/resources/application.properties). Environment variables override corresponding Spring properties unless code uses a hardcoded value.

| Environment variable | Purpose / current behavior |
| --- | --- |
| `PORT` | HTTP port; defaults to `8080`. |
| `SPRING_PROFILES_ACTIVE` | Defaults to `prod`; `dev` activates the parent seeder. |
| `SPRING_DATASOURCE_URL` | PostgreSQL JDBC URL, including SSL options if required by your database. |
| `SPRING_DATASOURCE_USERNAME` | Database username. |
| `SPRING_DATASOURCE_PASSWORD` | Database password. |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Schema handling; checked-in value is `update`. |
| `SPRING_MAIL_HOST` / `SPRING_MAIL_PORT` | Spring Mail transport; defaults to Gmail on port `587`. See OTP limitation below. |
| `SPRING_MAIL_USERNAME` / `SPRING_MAIL_PASSWORD` | Mail credentials. |
| `MAIL_FROM` | Sender address; also the SMTP authentication username for the OTP email path. |
| `PINDO_TOKEN` | Token used by `PindoSmsService`. |
| `PINDO_APIURL` / `PINDO_BULKAPIURL` | Override `pindo.api-url` / `pindo.bulk-api-url`; default to Pindo single/bulk SMS endpoints. |
| `BACKEND_BASEURL` | Base URL for the optional bulk-recipient lookup service. No default is configured. |
| `BACKEND_ENDPOINTS_GETPHONENUMBERS` | Path appended to the lookup base URL. No default is configured. |
| `MANAGEMENT_ENDPOINTS_WEB_CORS_ALLOWED_ORIGINS` | Allowed origins for Actuator endpoints only. |

HikariCP is configured with a maximum pool size of two connections and a 20-second connection timeout.

### Current configuration limitations

- **JWT:** `JwtService` uses a hardcoded signing key and a seven-day token lifetime. `JWT_SECRET` and `JWT_EXPIRATION` do not change token behavior even though matching properties exist. Externalizing these values requires a code change.
- **CORS:** regular API origins are hardcoded in `utils/WebConfig.java` to `http://localhost:5173` and `https://kuzahealth.netlify.app`. `CORS_ORIGINS` is not wired up. Changing Actuator CORS settings does not change API CORS.
- **OTP email:** `UserService` builds its own Gmail SMTP connection on port 587, using `MAIL_FROM` and `SPRING_MAIL_PASSWORD`. Changing Spring Mail host/port alone does not redirect OTP email.
- **Legacy SMS service:** `SmsService` contains a separate hardcoded token. `PINDO_TOKEN` configures `PindoSmsService`, which is used by the SMS controller and OTP flow.
- **Secrets:** credentials and a deploy-hook fallback are present in tracked configuration/source. Replace and rotate those values before deployment; environment overrides alone do not remove them from source history. `.env` is not currently excluded by `.gitignore`.

## Docker

The supplied [docker-compose.yml](docker-compose.yml) starts **only the application**. It requires `.env`, sets the `prod` profile, and publishes port `8080`. PostgreSQL must be provided separately.

Create or update a private `.env` with your own values:

```dotenv
PORT=8080
SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/kuzahealth
SPRING_DATASOURCE_USERNAME=kuzahealth
SPRING_DATASOURCE_PASSWORD=local-development-only
SPRING_MAIL_USERNAME=your-account@gmail.com
SPRING_MAIL_PASSWORD=your-gmail-app-password
MAIL_FROM=your-account@gmail.com
PINDO_TOKEN=your-pindo-token
```

`host.docker.internal` refers to the host on Docker Desktop. For another environment, use a database hostname reachable from the app container. `localhost` inside that container refers to the app container itself.

```bash
docker compose up --build -d
docker compose logs -f app
docker compose down
```

Compose's `environment` entries take precedence over `.env` values, including the profile and declared JWT settings. JWT behavior still has the code limitation described above. Keep `PORT=8080` unless you also adjust the container port mapping.

The [Dockerfile](Dockerfile) uses a Maven/Java 17 build stage, skips tests, and copies the executable JAR into an `openjdk:17-jdk-slim` runtime image. It launches `java -jar app.jar`; it does not expand a `JAVA_OPTS` variable. Use `JAVA_TOOL_OPTIONS` if JVM options are needed.

## Authentication

Authentication uses a password plus a six-digit OTP, followed by a bearer JWT. Account roles are `ADMIN`, `HEALTH_WORKER`, and `DATA_ANALYST`; registration defaults to `HEALTH_WORKER` when no role is supplied.

Use your own deliverable email address and phone number in these examples. Requesting an OTP attempts both email and SMS delivery.

### 1. Register an account

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Test",
    "lastName": "Worker",
    "username": "test.worker",
    "email": "your-account@example.com",
    "password": "replace-with-a-strong-password",
    "phoneNumber": "+250780000000",
    "role": "HEALTH_WORKER"
  }'
```

The endpoint accepts the `User` entity, including `role` (not the unused `RegisterRequest.userType` field). Supply a unique username because JWT subjects use it. Health-worker registration also attempts to create a linked health-worker record. The response is a text message, not a token; inspect the message because HTTP 201 can also report failure to create the linked record.

### 2. Request an OTP

```bash
curl -X POST http://localhost:8080/api/v1/auth/send-otp \
  -H 'Content-Type: application/json' \
  -d '{"email":"your-account@example.com","password":"replace-with-a-strong-password"}'
```

The `email` field accepts either the registered email address or phone number. Credentials are checked before generating an OTP, which expires after 20 minutes. The response contains `status` and `message`. A success response does not guarantee email delivery: the email helper logs delivery failures without propagating them.

### 3. Exchange the OTP for a token

Replace `123456` with the received code:

```bash
curl -X POST 'http://localhost:8080/api/v1/auth/login?otp=123456' \
  -H 'Content-Type: application/json' \
  -d '{"email":"your-account@example.com","password":"replace-with-a-strong-password"}'
```

A successful response contains `token`, `email`, `userType`, and `message`.

### 4. Call an authenticated endpoint

```bash
export TOKEN='paste-the-returned-token-here'
curl http://localhost:8080/api/v1/auth/profile \
  -H "Authorization: Bearer $TOKEN"
```

The security filter requires authentication for routes other than explicitly public endpoints. Password-reset routes currently require authentication too. Role values are present in the model/token, but the filter chain does not establish a per-resource role policy.

## API reference

Routes do not all share the same version prefix. Use the exact paths below and consult Swagger UI for request schemas and parameters.

| Area | Base path | Operations |
| --- | --- | --- |
| Authentication | `/api/v1/auth` | Register, send OTP, log in, profile, password-reset request/reset |
| Users | `/api/users` | List, get, patch, delete |
| Health workers | `/api/health-workers` | Create, list, get, update, delete |
| Parents | `/api/parents` | Register with `POST /register`; list, get, update, delete |
| Pregnancy records | `/api/pregnancy-records` | CRUD; records by parent, active record, pregnancy weeks |
| Visits | `/api/visits` | CRUD; visits by patient |
| Visit notes | `/api/visit-notes` | Create, list, get, delete |
| Infants | `/api/infants` | CRUD; infants by mother |
| Vaccinations | `/api/vaccinations` | CRUD; by infant/health worker/parent, due vaccinations, notification trigger |
| Nutrition | `/api/nutrition-info` | POST nutrition information |
| SMS | `/api/sms` | POST `/send`, `/send-bulk`, `/send-bulk-from-backend` |
| Audit | `/api/v1/audit` | GET `/logs` |
| Recent API logging | `/api/logging` | GET `/recent` |

`/send-bulk-from-backend` requires the two `BACKEND_*` settings in the configuration table. The lookup service must return a JSON array matching `sms/model/BulkSmsRecipient.java`; the current client does not attach an authentication header to that lookup.

### Documentation and monitoring

| Path | Purpose |
| --- | --- |
| `/swagger-ui/index.html` or `/swagger-ui.html` | Interactive API documentation |
| `/v3/api-docs` | OpenAPI JSON |
| `/actuator/health` | Application health |
| `/actuator/health/liveness` | Liveness probe |
| `/actuator/health/readiness` | Readiness probe |
| `/actuator/info` | Application info endpoint |
| `/actuator/metrics` | Available metrics |
| `/api/v1/test/greet` | Public greeting endpoint |

The security configuration permits public access to registration, OTP requests, login, the greeting, Swagger/OpenAPI, `/api/logging/recent`, and `/actuator/**`. Health details are configured as `always`. Review this exposure before making the service public.

## Project structure

```text
src/main/java/rw/ac/auca/kuzahealth/
├── KuzahealthServerApplication.java  # Spring Boot entry point
├── controller/                      # REST endpoints and some request DTOs
├── core/                            # Domain entities, services, repositories, DTOs
├── security/                        # Security filter chain, JWT, user details
├── sms/                             # Pindo integration and recipient lookup
├── config/                          # API logging controller
└── utils/                           # Shared entities, mail, CORS, errors, metrics
src/main/resources/
├── application.properties           # Shared runtime configuration
└── data/                            # CSV, SQL, and dump files
src/test/java/                       # Application-context smoke test
.mvn/wrapper/                        # Maven wrapper configuration
.github/workflows/render-deploy.yml  # Build and Render hook workflow
Dockerfile                           # Multi-stage container build
docker-compose.yml                   # Application-only Compose service
```

The code is organized around domain areas under `core` and exposed through controllers. Some existing package names use `pregancyrecord`; preserve the actual spelling when navigating or importing them.

## Build and tests

```bash
# Compile, run tests, and package
./mvnw clean verify

# Run the test suite
./mvnw test

# Package without running tests
./mvnw clean package -DskipTests

# Run the packaged application with your exported configuration
java -jar target/kuzahealth-0.0.1-SNAPSHOT.jar
```

The current test suite contains one `@SpringBootTest` context-loading test. It uses the application configuration and needs a reachable PostgreSQL database; no in-memory test database or dedicated test profile is provided. Point test runs at a disposable database because Hibernate can update its schema. The Docker build and deployment workflow skip tests.

For IDE development, import `pom.xml`, select Java 17, and enable annotation processing for Lombok and MapStruct.

## Deployment

[The GitHub Actions workflow](.github/workflows/render-deploy.yml) runs on pushes to `main`, sets up Java 17, packages with tests skipped, and POSTs to a Render deploy hook.

Configure the repository secret `RENDER_DEPLOY_HOOK` with your service's hook URL. The workflow currently falls back to a checked-in hook when the secret is absent; remove that fallback and rotate the exposed hook when preparing your deployment. A successful hook call means Render accepted the request, not that the deployed app passed health checks.

Configure the database, mail, and Pindo settings on the hosting service. The application listens on `PORT`, defaulting to 8080. Verify `/actuator/health` after deployment. The repository does not configure versioned database migrations; plan schema changes explicitly before using `ddl-auto=update` against shared data.

## Troubleshooting

| Symptom | What to check |
| --- | --- |
| Java compilation or wrapper failure | Confirm `java -version` reports Java 17, check `JAVA_HOME`, and allow dependency downloads. |
| Database connection refused or authentication failure | Verify all three datasource variables, database availability, port, credentials, and required SSL options. |
| App container cannot reach PostgreSQL | Use a hostname reachable from the container; `localhost` points to the app container. |
| `.env` changes have no effect with Maven | Export variables in the launching shell or set them in the IDE; Maven startup does not load `.env`. |
| Protected request rejected | Send `Authorization: Bearer <token>` and obtain a fresh token if expired. |
| OTP email is missing | Check Gmail app credentials and `MAIL_FROM`; inspect logs even when `/send-otp` reports success. |
| Invalid or expired OTP | Request a new code and pass it as the `otp` query parameter with the same account's credentials. |
| Browser CORS error | Check `utils/WebConfig.java`; `CORS_ORIGINS` does not configure the API. |
| SMS delivery fails | Check your Pindo token, sender, recipient format, and provider response. |
| Bulk recipient lookup fails | Configure both `BACKEND_*` variables and confirm the lookup returns the expected JSON array. |
| JWT configuration changes have no effect | The signing key and lifetime are hardcoded in `JwtService`. |
| Context-loading test fails | Configure a separate reachable test database; the suite uses the full Spring context. |

## Contributing

1. Create a branch for the change.
2. Follow the existing domain/controller/service/repository organization.
3. Add meaningful validation for behavior changes and run the relevant checks against a development database.
4. Update API documentation and configuration notes when behavior changes.
5. Open a pull request describing the problem, changes, and validation performed.

Keep credentials, personal data, and private database exports out of contributions.

## License

No project license file is currently included. Confirm usage and redistribution terms with the repository owner.
