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

Phases 16–21 extend the stable core with adaptive practice, written-answer coaching, sourced job/company guidance, voice interviews, isolated coding exercises, and learner reminders. Keep external-provider usage optional, disclose data sent to providers, and never execute user code inside the main API process.

## Phase 18 acceptance checklist

- Each company can expose employer-authored career/search/interview guidance through attributable outbound links; optionally associate a resource with one of that company's roles.
- Each resource records source type/label and publication, last-verified, and optional validity dates. Reads derive recently-verified, review-due, or expired state.
- Admins can create, update, list, and soft-deactivate guidance. Invalid URLs, dates, and cross-company role references are rejected.
- Seed only official source destinations; do not scrape or claim live job availability, and keep editorial InterviewForge content distinctly labeled.
- Document endpoints, migration, freshness thresholds, and limitations. Add focused tests for freshness rules.

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

## Phase 8 acceptance checklist

- Authenticated users can start a role-specific mock interview with 1–20 published TEXT questions drawn from active mapped topics.
- Prompts and role/skill attribution are snapshotted; questions and answers belong only to the session owner.
- Users submit one written answer and a 1–5 self-rating per prompt; sessions complete after all answers are submitted.
- Session detail/history report completion and per-skill weighted self-ratings without claiming automated answer grading.
- Flyway migration and API contract are documented for the later web frontend.

## Phase 9 acceptance checklist

- Runtime exposes safe liveness/readiness health probes and build metadata without secrets or component details.
- Production profile requires explicit database and frontend-origin configuration; proxy HTTPS headers and graceful shutdown are configured.
- Security response headers and route access rules are reviewed and documented, including operational controls still required at the hosting edge.
- A CI workflow packages the Java 21 backend on pushes and pull requests.
- Deployment and operational guidance covers managed PostgreSQL, migrations, HTTPS, backups, monitoring, and Lovable CORS configuration without requiring Docker.

## Phase 10 acceptance checklist

- Question authors can provide per-option explanations, theory, workplace examples, misconception labels, scenario context, interview stage, source provenance, and verification dates.
- Student-facing prompts expose provenance/context while answer keys and teaching content remain hidden until answer submission.
- Practice captures optional confidence, returns detailed learning feedback, and schedules missed or low-confidence MCQs for review.
- Users can start sessions from due review items and inspect confidence calibration and recurring misconception insights.
- Users can save their target role, interview date, weekly study time, and private job description; the existing readiness and study plan APIs support that target role.
- Mock interview questions can include a curated evaluation rubric and follow-up prompt, revealed only after the answer; the API clearly states these are not automatic AI grading.
- Flyway migrations, API contract, privacy behavior, and frontend integration types/endpoints are documented for Lovable.

## Phase 11 acceptance checklist

- Admins can author, replace, publish, unpublish, and archive role/topic-specific workplace case graphs with provenance.
- Graph validation requires exactly one initial step, 2–6 choices at each decision step, valid references, full reachability, no cycles, and terminal outcome steps with lessons.
- Learners see only the current situation and choices before answering; after choosing they see feedback for each option, trade-offs, misconceptions, and the next situation on their chosen branch.
- Sessions snapshot the full graph and keep owner-only decision history even if the source case is edited or archived later.
- Rule-based rubric points distinguish strong, viable, and risky choices without claiming to predict hiring outcomes.
- Flyway schema, API examples, validation behavior, and typed frontend endpoints are documented for Lovable.

## Phase 12 acceptance checklist

- Learners can select a target role from alphabetically sorted active roles on the dashboard; the selection persists through the preparation-goal API.
- The Companies role list allows learners to set a role as their target role without making an admin-only catalog change.
- Practice highlights common interview concepts and starts MCQ sessions from published questions for the selected topic.
- MCQ review presents option-level explanations, theory notes, workplace examples, and misconception guidance when available.
- Flyway provides an original starter question bank and clearly labeled generic role tracks for a new local installation.

## Phase 13 acceptance checklist

- Authenticated learners can map their saved job description against their selected target role without sending it to an external service.
- Requirement statements map transparently to active role skills and topics; unmatched statements remain visible for manual review.
- Each match shows role importance, published practice-question availability, and only the learner's own MCQ history.
- The role page lets learners save a job description, review the map, and start practice directly for mapped topics.
- The API documents deterministic phrase matching and clearly distinguishes practice activity from professional experience evidence.
- Focused tests cover requirement extraction and phrase matching; frontend and backend verification instructions are documented.

## Phase 14 acceptance checklist

- A learner can create, list, read, edit, and delete private experience stories, with every read/write scoped to the authenticated owner.
- Each story records experience type, optional context, Situation, Task, Actions, Result, and optional reflection.
- Learners can map a story to up to ten active competencies; IDs are validated and duplicate competency links are rejected.
- The UI provides a STAR editor, a private story list, edit/delete controls, and clear guidance to omit confidential information.
- Flyway owns the story and competency-link schema; API and privacy behavior are documented.
- Focused tests cover owner isolation; Maven tests, frontend build, and TypeScript checking pass.

## Phase 15 acceptance checklist

- Active practice topics include AI/ML, generative AI applications, data engineering, cloud/platform engineering, cybersecurity, and quality engineering.
- Each added topic has at least three original MCQs with correct-answer explanations, theory notes, workplace examples, and misconception guidance.
- Expanded role tracks are alphabetically listed and mapped to role-relevant skills; company association is labeled as preparation content, not a live job listing or official question bank.
- Flyway owns repeatable seeded catalog content; API response contracts remain unchanged.

## Phase 16 acceptance checklist — Adaptive practice planning

- The role study plan recommends a question difficulty from the learner's topic attempt count and accuracy using an explainable rule.
- Recommendations use the actual published MCQ inventory, fall back to an available difficulty when needed, and never request more questions than exist.
- When all mapped topics meet the target with a reliable sample, the plan offers one higher-difficulty challenge when content exists.
- The frontend displays the difficulty and rationale and starts a matching session; no content or history is shared with an AI provider.
- Focused advisor tests cover beginner, struggling, advancing, fallback, and empty-content cases. API behavior is documented.

## Phase 17 acceptance checklist — Written-answer coaching

- A learner receives structured rubric feedback on a written mock answer, including evidence, completeness, clarity, and actionable next steps.
- Feedback distinguishes rubric evidence from inferred suggestions and does not claim to predict hiring outcomes.
- Any external model integration is optional, uses server-side secret configuration, discloses what answer data is sent, and has a no-provider fallback.
- Owner-only answers and feedback remain private; response limits and abuse controls are documented.

## Phase 18 acceptance checklist — Sourced job and company guidance

- Learners can attach a current job posting link or enter its requirements; imported data retains source URL and retrieval/verification date.
- Company role tracks, employer-authored hiring guidance, and InterviewForge original questions have distinct provenance labels.
- Stale or inaccessible data is visibly marked; the product does not imply an open vacancy or employer endorsement without evidence.
- Provider/API terms, refresh policy, attribution, and user privacy behavior are documented.

## Phase 19 acceptance checklist — Voice mock interviews

- Learners can conduct a spoken interview with a text fallback and review their transcript before saving or sharing it with any provider.
- The interviewer asks role-relevant questions and follow-ups; transcript, recording, retention, and deletion behavior are explicit.
- Browser speech capability and external AI provider availability are handled gracefully; no audio is retained by default without consent.
- Voice feedback is clearly a coaching aid, not an employment prediction.

## Phase 20 acceptance checklist — Isolated coding practice

- Learners solve timed role/topic coding prompts with examples, hidden tests, and explanatory review.
- Submitted code runs only in a dedicated, restricted execution environment isolated from the main API and database credentials.
- Time, memory, network, filesystem, concurrency, and retention limits are enforced and documented before enabling execution.
- A non-execution editor/review mode remains available when the runner is unavailable.

## Phase 21 acceptance checklist — Goals and reminders

- Learners can set a weekly practice goal and optional interview-date reminders, control notification preferences, and disable reminders.
- Progress milestones are calculated from owner-scoped history and explain their criteria.
- In-app reminders work without a paid email/SMS provider; external delivery is an optional later integration.
- Time zone, quiet-hour, deletion, and notification privacy behavior are documented.
