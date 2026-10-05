# Project progress and working agreement

## Product and ownership

- InterviewForge is an adaptive interview preparation platform.
- This repository contains the backend API. The user will build the frontend later in Lovable, after the backend is complete.
- Keep API contracts documented for that separate frontend.

## Development setup

- Develop in this repository at `C:\Users\vysh1\OneDrive\ドキュメント\ChatGPT\ipa 2`.
- Use Java 21-compatible Spring Boot, Maven, PostgreSQL, Spring Data JPA, and Flyway.
- Docker is not supported on the user's system; use locally installed PostgreSQL.
- Keep credentials in the ignored `.env` file. Never commit it.

## Delivery agreement

- Work in the phases listed in `PLANS.md`.
- Commit and push each completed phase to `https://github.com/kommarajulasaiakhilesh-lgtm/interviewforge.git` on `main`.
- Do not start frontend implementation in this repository; frontend work belongs to the user's later Lovable work.

## Completed phases

- Phase 1: backend foundation, PostgreSQL configuration, Flyway, health endpoint, CORS, and API documentation. Commit `a4fb93e`.
- Phase 2: student registration, login/logout, opaque bearer sessions, profile management, and authorization. Commit `33bd270`.
- Phase 3: topic/tag catalog, searchable question bank, admin management, student-safe reads, migration, and integration docs. Commit `fb37c40`, pushed to `main`.
- Phase 4: practice session creation, answer submission, MCQ scoring, TEXT response capture, answer review, owner-only history, migration, and API docs. Completed in this phase delivery.
- Phase 5: overall accuracy, per-topic ratings with minimum-sample thresholds, and paginated attempt summaries. Completed and documented in this phase delivery.
- Phase 6: company and role catalogs, skill/topic relevance mappings, curated preparation sets, admin publishing, student-safe reads, migration, and API documentation. Completed in this phase delivery.
- Phase 7: authenticated role readiness assessments with explainable weighted scores, sample coverage, and a prioritized rule-based study plan. Completed in this phase delivery.
- Phase 8: role-specific mock interview sessions, answer capture, self-ratings, and per-skill completion/results. Completed in this phase delivery.
- Phase 9: security headers and route review, sanitized health probes/build info, production configuration, Java JAR deployment guidance, and GitHub Actions packaging workflow. Completed in this phase delivery.

## Current status

- Phases 1–9 are implemented; the web application is the next planned deliverable in Lovable.
- Phase 9 does not provision cloud resources or perform a deployment; choose a host and complete the listed launch controls when deployment is planned.
- The local backend was started successfully, PostgreSQL connected, and Flyway migrated the local database through schema version 7. `/actuator/health` and `/actuator/health/readiness` both returned `UP`.
- The local backend process was stopped at the user's request. Restart it later with `java -jar target/interviewforge-api-0.1.0-SNAPSHOT.jar` from the repository directory, or `mvn spring-boot:run` with Maven's local repository set to `C:/Users/vysh1/.m2/repository`.
- Next time: the user plans to build the web frontend in Lovable and integrate it with this backend. Do not start frontend work until the user returns and asks.
- Voice interview assistant remains a later feature discussion.
