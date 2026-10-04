# Architecture

InterviewForge is a modular monolith. The Lovable web client calls the Spring MVC JSON API. The API owns validation and business rules, services will coordinate use cases, repositories will persist data through Spring Data JPA, and PostgreSQL stores application data. Flyway is the only schema migration mechanism.

```text
Lovable UI → Spring MVC API (/api/v1) → feature services → Spring Data JPA → PostgreSQL
                                                      Flyway migrations
```

Organize future code by capability (`question`, `practice`, `user`) with controllers, services, repositories, DTOs, and entities kept close to the capability they serve. Authentication and authorization will be introduced in Phase 2 using Spring Security. Until then, the service is a local development foundation and must not be treated as production-ready.

User-submitted programs, if added later, must run in an isolated worker/sandbox and never in the API JVM.
