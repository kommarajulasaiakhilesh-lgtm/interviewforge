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

## Current status

- Phases 1–7 are complete and pushed to `main` through commit `2d6ab09`.
- Remaining phases are deferred until the user asks to resume. Do not start Phase 8 or later work yet.
- Next when resumed: Phase 8, mock interview sessions, answer capture, and skill-area results.
- Frontend remains planned for Lovable after backend phases are complete. Voice interview assistant remains a later feature discussion.
