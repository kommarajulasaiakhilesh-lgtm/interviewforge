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
- Phase 12: dashboard role selection uses alphabetically sorted roles and persists the chosen target role to the learner profile; Companies role cards can set a target role; Practice includes quick MCQ sessions for core concepts and shows per-option explanations, theory, examples, and misconceptions. Migration V12 seeds seven core topics, 21 original MCQs, and eight generic role tracks with skill mappings.
- Phase 13: authenticated job-requirement evidence map connects the learner's saved job description to active role skills, topics, MCQ availability, and their own practice history. Role details can save the JD and start topic practice from matches. Matching is transparent and deterministic; there is no new schema or external AI dependency.
- Phase 14: private owner-scoped STAR story bank for work, project, education, volunteer, or other experiences, with optional mapped competencies and reflection. Flyway V13 creates the story and competency-link tables. Commit `9826935` is pushed to `main`.

## Current status

- The Lovable frontend was pushed under `frontend/` in commit `af36cb3` and fast-forwarded into the local checkout. Its API client uses the backend's documented `/api/v1` endpoints.
- Local integration is configured with ignored `frontend/.env` pointing to `http://localhost:8080`; backend CORS allows `http://localhost:5173`.
- Frontend verification: TypeScript check passed, production build passed, and the existing routing test passed. Local HTTP smoke check returned frontend `200`, backend readiness `UP`, and CORS preflight `200`.
- The frontend, backend, and PostgreSQL were stopped at the user's request on 2026-10-09. Start them manually when the user wants to preview locally.
- Phase 11 frontend calls use the existing authenticated API client, which attaches the current bearer token and handles unauthorized responses consistently with the rest of the app.
- Phase 11 Java compilation and Phase 12 frontend TypeScript checking passed; ESLint passed on the new shared frontend components. Maven packaged the backend with tests skipped. Local Flyway V12 applied successfully and seeded 7 topics, 21 published MCQs, and 8 generic roles. The backend health endpoint was `UP`, and the dashboard route returned HTTP 200. The production frontend build had previously hit `EPERM` clearing the OneDrive-synced `frontend/.output`; the current Phase 13 production build passed.
- Phase 13 verification: `mvn test` passed (2 focused matcher tests), frontend `npm run build` passed, frontend `tsc --noEmit` passed, and `git diff --check` passed. The endpoint and matching privacy/limits are documented. No database migration is required.
- Phase 14 verification: `mvn test` passed (4 backend tests total), frontend `npm test` passed (2 routing tests), frontend `npm run build` passed, frontend `tsc --noEmit` passed, and `git diff --check` passed. PostgreSQL remained stopped, so Flyway V13 has not yet been applied to the local database.
- Phase 9 does not provision cloud resources or perform a deployment; choose a host and complete the listed launch controls when deployment is planned.
- Voice interview assistant remains a later feature discussion.

## Phase 11 product direction (backend and frontend wired)

The user wants InterviewForge to feel distinct from Google Interview Warmup and Yoodli, with a focus on teaching job-related reasoning rather than centering speech analytics. Phase 11 implements **Branching Workplace Cases**. A learner chooses an action in a realistic case, receives new facts or constraints, makes a follow-up decision, and reviews trade-offs, misconception-specific feedback, all-option explanations, and an outcome lesson.

Related candidate features to consider for future phases:

- Explain why each wrong choice seemed plausible, identify its misconception, and show when that choice could be appropriate.
- Constraint-change drills that vary scale, cost, security, team size, or time limits to test transfer beyond memorized answers.
- A traceable job-requirement evidence map connecting job-description requirements to skills, questions, and demonstrated readiness (implemented in Phase 13).
- A private personal-experience bank mapping project/work/school examples to competencies and STAR structure (implemented in Phase 14).
- A decision trade-off journal that captures the learner's assumptions, priorities, and evidence that would change their decision.
- Source trust/freshness labels, with clear separation between official, original/editorial, and community-reported questions.
- Two-way interview preparation for questions the candidate should ask about success measures, team challenges, and early role expectations.

Keep the agreed API-first Java backend and separate Lovable frontend. The first case release uses authored, rule-based paths and does not require paid AI. Phases 13 and 14 implement the job-requirement evidence map and private personal-experience bank. Voice AI remains deferred.

## Phase 13 complete — Job-requirement evidence map

- Added an authenticated endpoint at `GET /api/v1/users/me/preparation/evidence-map` that uses the learner's saved job description and target role.
- The deterministic matcher extracts short requirement statements and maps curated role skill phrases to active role skills and their topics. Unmatched requirements are returned for manual review.
- Results include published MCQ availability and the requesting learner's topic attempts and accuracy. The JD is not sent to an AI or external service; practice history is not presented as proof of professional experience.
- Added a role-page form to save a JD and show its map, with direct practice actions for mapped topics.
- Added focused matcher tests and API/roadmap documentation. `mvn test`, frontend production build, TypeScript check, and diff whitespace check passed.

## Pause point — 2026-10-26 (resumed 2026-10-27)

- Phase 13 is committed and pushed as `24f6f08` on `main`; the pause-note update is `724d16f`.
- Frontend, backend, and PostgreSQL were stopped at the last pause. They have not been started during the current implementation work.
- Voice interview assistance remains deferred.
- Several unrelated untracked files with `Copy` in their names are present in the workspace. Preserve them and exclude them from project commits unless the user later asks otherwise.

## Phase 14 complete — Personal experience story bank

- Building a private, owner-scoped CRUD experience bank with Situation, Task, Actions, Result, optional reflection, source type, and competency links.
- Adding a signed-in Story bank page with editing, deletion, and reminders not to store confidential employer/customer information.
- Added Flyway V13 and focused service tests for owner isolation. Maven tests, frontend production build, TypeScript check, and diff whitespace check passed. Migration application awaits the next time local PostgreSQL is started.

## Phase 15 — Market-aligned roles and topic coverage

- Selecting market-aligned topics from the World Economic Forum Future of Jobs Report 2025, U.S. Bureau of Labor Statistics 2024–2034 projections, and India-focused LinkedIn/NASSCOM trend reporting: AI/data, cybersecurity, cloud/platform operations, and software quality.
- Adding six technical topics and eighteen original MCQs (three per topic) with answer explanations, theory, examples, and misconception guidance.
- Adding alphabetically sortable role tracks (AI Engineer, Cloud Engineer, Cybersecurity Analyst, Data Analyst, Data Engineer, Data Scientist, DevOps Engineer, Frontend Engineer, Generative AI Engineer, Machine Learning Engineer, MLOps Engineer, Platform Engineer, QA Engineer, Security Engineer, Site Reliability Engineer, Software Engineer) across eight company preparation catalogs.
- Company-role data is preparation guidance only; it does not claim current openings or verified company questions.
- Updated Practice topic priorities, role skill mappings, database/roadmap/phase documentation.
- Build verification: frontend production build and direct TypeScript compiler check passed; Maven Java compilation passed. Packaging could not complete because the Spring Boot Maven plugin failed while renaming the existing JAR during repackaging. The Flyway migration has not yet been applied to PostgreSQL, so a running backend will not show the new catalog until it is restarted after migration packaging.

## Phase 16 — Adaptive practice planning

- Extended the role study plan to select Easy, Medium, or Hard from the learner's topic attempt history and accuracy using a transparent rule.
- Matched recommendations to published MCQs actually available at each difficulty and capped session size to the inventory; the role page shows the rationale.
- The plan offers a harder challenge for one mastered topic when the learner meets the accuracy/sample target.
- No new schema or external AI provider is required. API documentation describes the new response fields and decision rules.
- Added focused advisor tests. `mvn -DskipTests test-compile`, frontend TypeScript check, and production build passed. The tests were compiled but not executed in this turn.

## Phase 17 — Written-answer coaching

- Added deterministic, answer-specific coaching to owner-only mock-interview details, based on word count and explicit action, result, and concrete-detail cues.
- Added literal overlap notes for author-provided rubric lines. The UI calls this possible text overlap, not a criterion score.
- Feedback is derived from the snapshotted rubric and the learner's private answer; there is no separate persistence or external AI request.
- Added focused coach tests. Maven Java/test-source compilation passed with mvn -DskipTests test-compile; frontend TypeScript check and production build passed. Tests were compiled but not executed in this turn.
- Commit 70d8420 is pushed to origin/main.

## Pause point — 2026-10-27

- Phases 16 and 17 are implemented and pushed: adaptive practice planning (aa359fb) and written-answer coaching (70d8420).
- Phase 17 feedback is heuristic writing guidance and literal rubric-term overlap, not semantic grading; answer text stays within the private application and is not sent to an AI provider.
- Phase 17 Java/test-source compilation, frontend TypeScript check, and production build passed. Focused JUnit tests were added and compiled but not executed.
- The local backend JAR repackage has previously failed while renaming the existing JAR. Restart/package verification and local runtime preview remain unresolved; Phase 15 Flyway V14 has not been confirmed on local PostgreSQL.
- Next planned work is Phase 18: sourced job and company guidance with freshness and provenance labels. Phase 19: voice interviews. Phase 20: isolated coding practice. Phase 21: goals and reminders.
- Preserve the unrelated untracked files with “Copy” in their names; do not include them in project commits.

## Phase 18 — Sourced company and job guidance (implementation, 2026-10-30)

- Added company/optional-role guidance records with employer/editorial source type, source label and URL, publication date, last verification date, optional validity date, and soft deactivation.
- Added authenticated company guidance reads and admin CRUD endpoints. Company-role association is checked, source URLs must be HTTP(S), and future or inconsistent dates are rejected.
- Freshness is derived as recently verified (within 90 days), review due (older), or expired (validity date passed). Employer pages are outbound links; no scraped postings or vacancy availability claims are made.
- Flyway V15 seeds official career/search/interview links for Amazon, Google, Infosys, Microsoft, and TCS, verified on 2026-10-30. API and schema docs updated.
- `mvn test` passes all 14 tests, including three focused freshness tests; `git diff --check` passes. Local PostgreSQL/backend remain stopped; V14/V15 application should be checked at next startup.
- Corrected the existing Phase 16 fallback explanation to display difficulty names in title case; this resolved a pre-existing failing assertion.

## Phase 18 — Sourced company and job guidance (implementation underway, 2026-10-30)

- Added company/optional-role guidance records with employer/editorial source type, source label and URL, publication date, last verification date, optional validity date, and soft deactivation.
- Added authenticated company guidance reads and admin CRUD endpoints. Company-role association is checked, source URLs must be HTTP(S), and future or inconsistent dates are rejected.
- Freshness is derived as recently verified (within 90 days), review due (older), or expired (validity date passed). Employer pages are outbound links; no scraped postings or vacancy availability claims are made.
- Flyway V15 seeds official career/search/interview links for Amazon, Google, Infosys, Microsoft, and TCS, checked on 2026-10-30. API and schema docs updated.
- Verification and delivery remain pending: focused JUnit tests, Maven build, git review, commit/push. Local PostgreSQL/backend remain stopped; V14/V15 application should be checked at next startup.
