# Architecture

InterviewForge is a modular monolith. The planned Lovable web client calls the Spring MVC JSON API. The API owns validation and business rules, feature services coordinate use cases, repositories persist data through Spring Data JPA, and PostgreSQL stores application data. Flyway is the only schema migration mechanism.

```text
Lovable UI → Spring MVC API (/api/v1) → feature services → Spring Data JPA → PostgreSQL
                                                      Flyway migrations
```

Code is organized by capability (`questionbank`, `practice`, `user`, `preparation`, `readiness`, `mockinterview`, `workplacecase`) with controllers, services, repositories, DTOs, and entities kept close to the capability they serve. Spring Security authenticates opaque bearer tokens and protects API routes; admin routes also use method-level role checks. Health probes are sanitized, and the `prod` profile requires explicit database and CORS settings.

Production topology is intentionally provider-neutral: HTTPS reverse proxy/load balancer → Java API JAR → private PostgreSQL. The current CI workflow packages but does not deploy. See [deployment.md](deployment.md) and [security.md](security.md) for operational requirements and outstanding controls.

User-submitted programs, if added later, must run in an isolated worker/sandbox and never in the API JVM.
