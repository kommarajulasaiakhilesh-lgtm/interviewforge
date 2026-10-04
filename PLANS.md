# Delivery plan

Build one reviewable vertical slice at a time. Each phase should define its API contract, data migration, implementation, and focused verification before moving on.

1. Foundation: Spring Boot API, PostgreSQL, Flyway, configuration, health endpoint, conventions.
2. Accounts: registration, login/logout, roles, profile, authorization.
3. Question bank: topics, tags, questions, filters, admin management.
4. Practice: session creation, answers, scoring, history.
5. Progress: accuracy, topic strengths and weaknesses, attempt history.
6. Company preparation: companies, roles, skills, topic mappings and curated sets.
7. Readiness and study planning: explainable scores and rule-based plans.
8. Mock interviews: sessions, answers and skill-area results.
9. Production polish: security review, deployment, monitoring and integration fixes.

Defer AI assistance, leaderboards, and arbitrary submitted-code execution until the core platform is stable. Never execute user code inside the main API process.

## Phase 1 acceptance checklist

- Maven project uses the agreed Java/Spring stack.
- Local PostgreSQL is configured through environment variables.
- Flyway owns schema changes; Hibernate validates rather than creates schema.
- Health endpoint is exposed with details hidden.
- CORS is configurable for the separate Lovable UI.
- README and architecture/API docs explain how to run and integrate the service.
