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
- Local PostgreSQL is configured through environment variables (Docker is not required).
- Flyway owns schema changes; Hibernate validates rather than creates schema.
- Health endpoint is exposed with details hidden.
- CORS is configurable for the separate Lovable UI.
- README and architecture/API docs explain how to run and integrate the service.

## Phase 2 acceptance checklist

- Registration normalizes email, validates input, hashes passwords, and always assigns STUDENT.
- Login returns a random opaque bearer token; only its SHA-256 digest is stored in PostgreSQL.
- Authenticated API requests require a valid, unexpired, unrevoked token.
- Logout revokes the caller's current token; replay is rejected.
- A student can read and update only their own profile.
- ADMIN is represented as a role and is never self-selected during public registration.
- Authentication and validation errors avoid exposing credentials or internal details.
- Document request/response shapes for the later Lovable integration.

## Phase 3 acceptance checklist

- Admins can create and update topics and tags, and deactivate them without breaking existing references.
- Admins can create, view, update, publish, unpublish, and archive questions.
- Question types are validated and MCQ options/correct answers are persisted with database constraints.
- Authenticated users can browse only published questions under active topics with topic, tag, difficulty, search, and pagination filters.
- Student-facing question responses never include the answer key or explanation.
- API and schema changes are documented for Lovable integration.

## Phase 4 acceptance checklist

- A user can start a 1–20 question session with optional topic and difficulty filters.
- Sessions snapshot published prompts and answer keys so later edits do not change history.
- Users can submit one answer per session question; MCQs are auto-graded and TEXT answers are saved without auto-grading.
- Answer feedback reveals the answer and explanation only after submission; sessions and history are owner-only.
- Completed MCQ sessions persist a score and all sessions appear in the user's paginated history.
- API and schema changes are documented for Lovable integration.

## Phase 5 acceptance checklist

- Authenticated users can read their own overall completed/in-progress session counts and MCQ accuracy.
- Per-topic answered/correct totals and accuracy use stable topic attribution from practice attempts.
- Topic ratings have explainable thresholds and require a minimum sample size.
- Users can page through their own attempt summaries; no user's metrics can include another user's attempts.
- Free-text attempts are excluded from MCQ accuracy until manual grading exists.

## Phase 6 acceptance checklist

- Admins can manage companies, company roles, and skills with reversible deactivation.
- Admins can replace role-to-skill and skill-to-topic mappings with validated 1–5 weights.
- Admins can curate, publish, update, and archive role preparation sets using published questions.
- Authenticated users can browse active companies/roles and published sets with student-safe question responses.
- API and data model are documented for later frontend integration.

## Phase 7 acceptance checklist

- Authenticated users can calculate readiness for an active role using only their own graded MCQ history.
- Readiness combines active role-skill importance and skill-topic relevance weights; unattempted topics score zero and the response exposes data coverage.
- Per-skill and per-topic results show their weights, answer counts, accuracy, and sample reliability.
- Users receive a prioritized rule-based plan for unattempted, under-sampled, or below-target topics with reasons and estimated practice effort.
- API formulas, thresholds, and response fields are documented; no new persistence is needed.
