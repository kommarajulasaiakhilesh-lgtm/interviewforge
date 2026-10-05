# InterviewForge

InterviewForge is an adaptive interview preparation platform. This repository contains its Spring Boot API; the user interface will be built separately in Lovable.

## Stack

- Java 21 and Spring Boot 4.1.1
- Maven
- PostgreSQL with Flyway migrations
- Spring MVC REST API, Spring Data JPA, and Actuator

## Local setup

Install Java 21 or newer, Maven 3.6.3 or newer, and PostgreSQL. Start the PostgreSQL Windows service and create a database named `interviewforge` using pgAdmin or `createdb`. Copy `.env.example` to `.env` and set `DB_USERNAME` and `DB_PASSWORD` to a PostgreSQL account that can access the database. Then run:

```powershell
Copy-Item .env.example .env
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Its health endpoint is `http://localhost:8080/actuator/health`.

Set `APP_CORS_ALLOWED_ORIGINS` to the exact origin used by the Lovable frontend when you begin that work. Use comma-separated origins if needed.

## Configuration

The app reads `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`, and `APP_CORS_ALLOWED_ORIGINS` from `.env` or the environment. `.env` is ignored by Git. The values in `.env.example` are placeholders; never commit a real credential.

## Deployment

The API can run as a Java JAR with managed PostgreSQL; Docker is not required. Use the `prod` Spring profile and supply database credentials and exact frontend CORS origins through the host's secret manager. See [docs/deployment.md](docs/deployment.md) for build, health-probe, backup, and operational guidance. CI packages the backend but does not deploy it.

## API conventions

- Business endpoints use `/api/v1/...`.
- JSON request and response bodies use camelCase.
- Timestamps use UTC.
- Input validation belongs at API boundaries; persistence entities are not API response types.
- Health and basic service information are exposed through Actuator; detailed health data is hidden.

## Accounts API (Phase 2)

- `POST /api/v1/auth/register` — create a student account and return a bearer token.
- `POST /api/v1/auth/login` — authenticate and issue a bearer token.
- `POST /api/v1/auth/logout` — revoke the current bearer token.
- `GET /api/v1/users/me` — read the authenticated user's profile.
- `PATCH /api/v1/users/me` — update the authenticated user's display name.

Send authenticated requests with `Authorization: Bearer <accessToken>`. New accounts are always `STUDENT`; assigning `ADMIN` requires a trusted administrative operation and is not available through public registration. See [docs/security.md](docs/security.md) and [docs/api.md](docs/api.md) for details.

## Question bank API (Phase 3)

Authenticated users can browse published questions using topic, tag, difficulty, search, and pagination filters. Admin endpoints manage topics, tags, and questions; question answers and explanations are returned only by admin endpoints. See [docs/api.md](docs/api.md).

## Practice API (Phase 4)

Authenticated users can create practice sessions, submit answers, review feedback, and browse their own history. MCQs are auto-scored; TEXT answers are saved for review without automatic grading. See [docs/api.md](docs/api.md).

## Progress API (Phase 5)

Authenticated users can view overall MCQ accuracy, per-topic performance, strengths and areas to improve, and paginated attempt summaries. Topic ratings use transparent score bands and require five graded answers. See [docs/api.md](docs/api.md).

## Company preparation API (Phase 6)

The API models companies, company roles, required skills and topic links, and curated role-based question sets. Admins manage and publish catalogs; authenticated users browse active, published preparation data. See [docs/api.md](docs/api.md).

## Readiness and study planning API (Phase 7)

Authenticated users can calculate a transparent readiness score for an active job role and receive a prioritized practice plan based on their own MCQ results. See [docs/api.md](docs/api.md) for the weighting formula and thresholds.

## Mock interview API (Phase 8)

Authenticated users can start a role-specific written mock interview, save their answers and self-ratings, then review progress by skill area. Answers are captured for later review and are not automatically graded. See [docs/api.md](docs/api.md).

See [docs/architecture.md](docs/architecture.md), [docs/api.md](docs/api.md), [docs/security.md](docs/security.md), [docs/deployment.md](docs/deployment.md), and [docs/roadmap.md](docs/roadmap.md).
