# InterviewForge development rules

- Build an API-first Java backend for a separate Lovable frontend.
- Use Java 21, Spring Boot, Maven, PostgreSQL, Flyway, Spring Data JPA, and Spring MVC.
- Keep a modular monolith; group code by business capability as features are added.
- Keep `/api/v1` as the versioned API prefix and return DTOs rather than persistence entities.
- Store configuration and secrets outside source code; update `.env.example` when configuration changes.
- Use Flyway migrations for schema changes. Keep Hibernate schema generation in `validate` mode.
- Validate request input and avoid exposing stack traces, SQL details, or internal errors to clients.
- Do not execute submitted code in the main application process.
- Do not add microservices or replace the agreed stack without discussing the change first.
- Add focused tests for implemented features and document how to run them.
