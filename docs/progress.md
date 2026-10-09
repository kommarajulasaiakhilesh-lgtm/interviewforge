# Project progress and working agreement

## Product and ownership

- InterviewForge is an adaptive interview preparation platform.
- This repository contains the Java backend and the Lovable-synced frontend under `frontend/`.
- Keep the API-first separation: the frontend calls documented `/api/v1` endpoints and never accesses persistence entities directly.

## Development setup

- Develop in this repository at `C:\Users\vysh1\OneDrive\ドキュメント\ChatGPT\ipa 2`.
- Use Java 21-compatible Spring Boot, Maven, PostgreSQL, Spring Data JPA, and Flyway.
- Docker is not supported on the user's system; use locally installed PostgreSQL.
- Keep credentials in the ignored `.env` file. Never commit it.

## Delivery agreement

- Work in the phases listed in `PLANS.md`.
- Commit and push each completed phase to `https://github.com/kommarajulasaiakhilesh-lgtm/interviewforge.git` on `main`.
- The user builds the frontend in Lovable; commits pushed to the connected GitHub branch sync to Lovable.

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
- Phase 10: provenance-aware question content, per-option coaching, theory and workplace examples, misconception labels, interview scenarios and stages, confidence capture, spaced review, learning insights, learner goals, and mock-interview rubrics/follow-ups. API contracts and migrations V8–V10 are documented.
- Phase 10 was committed and pushed to `main` as `16720f3` (`feat: add adaptive interview coaching loop`). Java source compilation and frontend TypeScript checking passed. The runnable JAR packaging attempt was blocked because Windows denied Maven's rename of the existing JAR under `target`; backend startup and the V8–V10 Flyway application were not verified. The user asked to pause and continue later.
- Phase 11: role/topic-linked branching workplace cases with admin graph authoring, strict graph validation, evolving decision paths, post-choice feedback for every option, trade-off/misconception explanations, outcome lessons, rubric points, owner-only history, and session graph snapshots. Flyway migration V11 and Lovable API types/endpoints are documented.
- Phase 11 frontend integration: signed-in learners can browse published cases, start and resume sessions, submit branching choices, review selected and alternative-option coaching, inspect outcomes, and open recent case history.

## Current status

- The Lovable frontend was pushed under `frontend/` in commit `af36cb3` and fast-forwarded into the local checkout. Its API client uses the backend's documented `/api/v1` endpoints.
- Local integration is configured with ignored `frontend/.env` pointing to `http://localhost:8080`; backend CORS allows `http://localhost:5173`.
- Frontend verification: TypeScript check passed, production build passed, and the existing routing test passed. Local HTTP smoke check returned frontend `200`, backend readiness `UP`, and CORS preflight `200`.
- The backend was stopped at the user's request. Frontend runs from `frontend/` on port 5173 and calls the backend at `http://localhost:8080` using ignored `frontend/.env`.
- Phase 11 frontend calls use the existing authenticated API client, which attaches the current bearer token and handles unauthorized responses consistently with the rest of the app.
- Phase 11 Java source compilation and frontend TypeScript checking passed. The production build could not clear the existing OneDrive-synced `frontend/.output` directory (`EPERM`); application startup and Flyway migrations V8–V11 against local PostgreSQL still need confirmation. No tests were added or run.
- Phase 9 does not provision cloud resources or perform a deployment; choose a host and complete the listed launch controls when deployment is planned.
- Voice interview assistant remains a later feature discussion.

## Phase 11 product direction (backend and frontend wired; runtime verification pending)

The user wants InterviewForge to feel distinct from Google Interview Warmup and Yoodli, with a focus on teaching job-related reasoning rather than centering speech analytics. Phase 11 implements **Branching Workplace Cases**. A learner chooses an action in a realistic case, receives new facts or constraints, makes a follow-up decision, and reviews trade-offs, misconception-specific feedback, all-option explanations, and an outcome lesson.

Related candidate features to consider when work resumes:

- Explain why each wrong choice seemed plausible, identify its misconception, and show when that choice could be appropriate.
- Constraint-change drills that vary scale, cost, security, team size, or time limits to test transfer beyond memorized answers.
- A traceable job-requirement evidence map connecting job-description requirements to skills, questions, and demonstrated readiness.
- A private personal-experience bank mapping project/work/school examples to competencies and STAR structure.
- A decision trade-off journal that captures the learner's assumptions, priorities, and evidence that would change their decision.
- Source trust/freshness labels, with clear separation between official, original/editorial, and community-reported questions.
- Two-way interview preparation for questions the candidate should ask about success measures, team challenges, and early role expectations.

Keep the agreed API-first Java backend and separate Lovable frontend. The first case release uses authored, rule-based paths and does not require paid AI. The next product backlog candidates are the job-requirement evidence map and private personal-experience bank; revisit these after the user reviews Phase 11. Voice AI remains deferred.
