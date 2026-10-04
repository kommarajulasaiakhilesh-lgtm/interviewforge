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

## Current phase

- Phase 3: topic/tag catalog and searchable question bank with admin management and student-safe reads. Implementation and API/schema documentation are complete; the phase commit and push finish this delivery.
