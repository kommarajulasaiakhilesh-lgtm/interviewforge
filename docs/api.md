# API integration contract

The API is designed for the separate Lovable frontend.

- Base URL in local development: `http://localhost:8080`
- Business routes: `/api/v1/...`
- Payload format: JSON (`application/json`)
- CORS origins: configured with `APP_CORS_ALLOWED_ORIGINS` as a comma-separated list of exact origins
- Health check: `GET /actuator/health`

## Accounts (Phase 2)

### Register

`POST /api/v1/auth/register` creates an account with the `STUDENT` role. The password must contain 12–72 characters and no more than 72 UTF-8 bytes. Email addresses are trimmed and normalized to lowercase.

Request:

```json
{
  "email": "student@example.com",
  "password": "a-long-example-password",
  "displayName": "A Student"
}
```

Response (`201 Created`):

```json
{
  "accessToken": "<opaque-token>",
  "tokenType": "Bearer",
  "expiresAt": "2026-10-04T12:00:00Z",
  "user": {
    "id": "<uuid>",
    "email": "student@example.com",
    "role": "STUDENT",
    "displayName": "A Student",
    "createdAt": "2026-10-04T00:00:00Z"
  }
}
```

Duplicate email returns `409 Conflict`; invalid input returns `400 Bad Request`.

### Login

`POST /api/v1/auth/login` accepts `{ "email": "...", "password": "..." }` and returns the same response shape as registration. Invalid credentials return `401 Unauthorized` with a generic message.

### Logout

`POST /api/v1/auth/logout` requires a bearer token and returns `204 No Content`. The token becomes unusable immediately.

### Current profile

- `GET /api/v1/users/me` requires a bearer token and returns the `user` object shown above.
- `PATCH /api/v1/users/me` requires a bearer token and accepts `{ "displayName": "New name" }`; it returns the updated user object.

Send the token in `Authorization: Bearer <accessToken>`. Tokens expire after 12 hours. Protected routes return `401 Unauthorized` when the token is missing, invalid, expired, or revoked. Error bodies use `application/problem+json`.

## Question bank (Phase 3)

All routes below require a bearer token. Admin routes also require the `ADMIN` role and return `403 Forbidden` for students.

### Catalog reads

- `GET /api/v1/topics` — active topics, alphabetically ordered.
- `GET /api/v1/tags` — active tags, alphabetically ordered.

### Browse questions

`GET /api/v1/questions?topicId=<uuid>&tagId=<uuid>&difficulty=MEDIUM&search=hash&page=0&size=20`
accepts optional filters. `difficulty` is `EASY`, `MEDIUM`, or `HARD`; page is zero-based and size is 1–100. Results contain `content`, `page`, `size`, `totalElements`, and `totalPages`. Only published, non-archived questions under active topics are returned. `GET /api/v1/questions/{id}` returns the same student-safe question shape.

Student question objects include `id`, `title`, `questionText`, `difficulty`, `type`, `options`, `topic`, `tags`, and `createdAt`. They never include `correctOptionIndex`, `answerText`, or `explanation`.

### Admin catalog management

- `POST /api/v1/admin/topics` and `PUT /api/v1/admin/topics/{id}`
- `POST /api/v1/admin/tags` and `PUT /api/v1/admin/tags/{id}`
- `GET /api/v1/admin/topics` and `GET /api/v1/admin/tags` — include active and inactive entries for admin management.

Catalog input is `{ "name": "Data Structures", "slug": "data-structures", "description": "...", "active": true }`. `slug` may be omitted to derive it from the name. Set `active` to false to hide a catalog item from student browsing while preserving references.

### Admin question management

- `GET /api/v1/admin/questions?page=0&size=20` — list drafts and published questions.
- `GET /api/v1/admin/questions/{id}` — retrieve the full admin representation.
- `POST /api/v1/admin/questions` — create a question.
- `PUT /api/v1/admin/questions/{id}` — replace editable question fields.
- `DELETE /api/v1/admin/questions/{id}` — archive (soft-delete) a question.

Example MCQ request:

```json
{
  "topicId": "<topic-uuid>",
  "title": "Hash map lookup",
  "questionText": "What is the average lookup complexity?",
  "difficulty": "EASY",
  "type": "MCQ",
  "options": ["O(1)", "O(n)"],
  "correctOptionIndex": 0,
  "answerText": "O(1) on average",
  "explanation": "Hashing provides constant-time average lookup.",
  "optionExplanations": ["Correct on average; collisions can still occur.", "This is the worst-case behavior with many collisions."],
  "theoryNotes": "A hash table maps keys to buckets. Average lookup is constant time under a suitable hash distribution.",
  "workplaceExample": "Use a map for request ID to request metadata lookups.",
  "misconceptionLabel": "Confusing average and worst-case complexity",
  "scenarioContext": "A service needs to look up a user's session by token.",
  "interviewStage": "TECHNICAL_SCREEN",
  "sourceType": "ORIGINAL",
  "sourceLabel": "InterviewForge editorial",
  "sourceUrl": null,
  "sourceVerifiedAt": null,
  "evaluationCriteria": "State average and worst-case complexity; mention collisions.",
  "followUpPrompt": "How would your answer change if the hash function had many collisions?",
  "tagIds": [],
  "published": false
}
```

For `TEXT`, send no `options` and no `correctOptionIndex`; `answerText` may hold the expected answer. Question creation defaults to draft unless `published` is true. `PUT` replaces the question, including its tag list and publication state. Duplicate catalog names/slugs return `409`; missing IDs return `404`; invalid request data returns `400`.

Question authoring also accepts `optionExplanations` (one reason per MCQ option), `theoryNotes`, `workplaceExample`, `misconceptionLabel`, `scenarioContext`, `interviewStage`, provenance fields `sourceType`, `sourceLabel`, `sourceUrl`, and `sourceVerifiedAt`, plus written-interview `evaluationCriteria` and `followUpPrompt`. Source types are `ORIGINAL`, `OFFICIAL`, `COMMUNITY_REPORTED`, `EDITORIAL`, or `UNKNOWN`. The student browse and practice prompt expose context, stage, and provenance but never the answer key or teaching notes. After an answer is submitted, practice feedback exposes the answer key, general explanation, per-option explanations, theory, workplace example, and misconception label. Do not represent community reports as verified employer questions.

## Practice (Phase 4)

All routes require a bearer token and return data for the authenticated user's sessions only.

- `POST /api/v1/practice/sessions` — create a random session. Request: `{ "topicId": "<uuid>", "difficulty": "MEDIUM", "type": "MCQ", "questionCount": 10 }`. `topicId`, `difficulty`, and `type` are optional; type defaults to MCQ. Count is required from 1 to 20. If too few matching published questions exist, response is `422`.
- `POST /api/v1/practice/sessions/{sessionId}/answers` — submit one answer for a question in the session. MCQ: `{ "questionId": "<uuid>", "selectedOptionIndex": 1, "confidenceRating": 3 }`. TEXT: `{ "questionId": "<uuid>", "answerText": "My response", "confidenceRating": 2 }`. Confidence is optional and ranges from 1 (guessing) to 5 (very confident).
- `GET /api/v1/practice/sessions/{sessionId}` — review prompts and submitted answers. Correct answers/explanations are hidden until that question is answered.
- `GET /api/v1/practice/sessions?page=0&size=20` — paged personal history, newest first (size 1–100).
- `GET /api/v1/practice/review-queue?limit=20` — due MCQs missed or answered with low confidence, ordered by due date. Limit is 1–50.
- `POST /api/v1/practice/review-queue/sessions?questionCount=10` — start a practice session using due MCQs. Count is 1–20; returns `422` when fewer due questions are available.

Sessions complete when each question is answered once. MCQs are graded immediately; the answer response includes correctness, key, and explanation. `scorePercent` is available on completion and equals correct answers divided by session question count. TEXT sessions are stored for review but have no automatic score (`scorePercent` is null). Prompts and keys are snapshotted at session creation. Another user's/nonexistent session returns `404`; repeated submissions or submissions after completion return `400`.

MCQ attempts create or update a personal spaced-review entry. Wrong answers and confidence ratings of 1–2 become due after one day. Correct, high-confidence answers lengthen the interval up to one year. The queue is per user and excludes archived/unpublished questions.

## Learner preparation goals

- `GET /api/v1/users/me/preparation` — read the authenticated user's target role, interview date, weekly study minutes, and optional job description.
- `PATCH /api/v1/users/me/preparation` — replace those goal fields with `{ "targetRoleId": "<uuid>", "interviewDate": "2026-11-15", "weeklyStudyMinutes": 180, "jobDescription": "..." }`. Fields may be null to clear them. Weekly study time must be 15–1200 minutes; job description is limited to 12,000 characters. The target role must be active. The stored role ID can be used with the Readiness and Study Planning endpoints above. Job description text is stored for the user's preparation workflow and is not sent to an external AI service.

## Progress (Phase 5)

All progress endpoints require a bearer token and report only the authenticated user's data.

- `GET /api/v1/progress/overview` — total completed and in-progress sessions, MCQ questions answered/correct, overall accuracy, and the five most recent attempts. Accuracy is correct MCQ answers divided by answered MCQ questions; it is `null` before any MCQ answer.
- `GET /api/v1/progress/topics` — answered/correct MCQ counts, accuracy, most recent answer time, and a topic rating for each topic with graded answers.
- `GET /api/v1/progress/learning-insights` — accuracy by self-reported confidence and recurring misconception labels. Confidence data is grouped by rating; a minimum sample of three is suggested before treating calibration as meaningful. A misconception is listed after two misses.
- `GET /api/v1/progress/attempts?page=0&size=20` — paginated personal session history, newest first.

Topic ratings use at least five answered MCQs: `STRONG` is 80% or above; `DEVELOPING` is 60% to below 80%; `NEEDS_WORK` is below 60%. Topics with fewer attempts are `NOT_ENOUGH_DATA`. Text answers do not affect accuracy because they are not automatically graded. Existing attempt topic IDs are backfilled from their linked question during the Phase 5 migration; new attempts snapshot the topic when they start.

## Company preparation (Phase 6)

All routes require a bearer token. Public-facing catalog reads are authenticated; all `/api/v1/admin/...` routes require `ADMIN`.

### Student reads

- `GET /api/v1/companies` — active companies.
- `GET /api/v1/companies/{companyId}/roles` — active roles for an active company.
- `GET /api/v1/roles/{roleId}` — active role details with its mapped skills and each skill's active related topics.
- `GET /api/v1/preparation-sets?roleId=<uuid>` — published sets for an active role.
- `GET /api/v1/preparation-sets/{setId}` — set details and currently published questions in curated order. Student question objects omit answer keys and explanations.

### Admin catalogs and mappings

- `GET/POST /api/v1/admin/companies`; `PUT /api/v1/admin/companies/{id}`
- `GET/POST /api/v1/admin/roles`; `PUT /api/v1/admin/roles/{id}`; `GET /api/v1/admin/roles/{id}`
- `GET/POST /api/v1/admin/skills`; `PUT /api/v1/admin/skills/{id}`
- `PUT /api/v1/admin/roles/{id}/skills` with `{ "mappings": [{ "skillId": "<uuid>", "importance": 4 }] }` replaces the role's skill links. Send an empty list to clear them.
- `GET/PUT /api/v1/admin/skills/{id}/topics` with `{ "mappings": [{ "topicId": "<uuid>", "relevance": 3 }] }` reads/replaces a skill's topic links. Link weights must be 1–5.

Catalog create/update bodies use `{ "name": "Acme", "slug": "acme", "description": "...", "active": true }`. Slugs are optional and derived from the name. On update, omitting `active` preserves its current value; setting it false hides the catalog entry from student reads while retaining mappings.

### Curated sets

- `GET /api/v1/admin/preparation-sets` and `GET /api/v1/admin/preparation-sets/{id}` include drafts/archived entries for management.
- `POST /api/v1/admin/preparation-sets` and `PUT /api/v1/admin/preparation-sets/{id}` accept `{ "roleId": "<uuid>", "title": "Backend interview", "description": "...", "published": false, "questionIds": ["<published-question-uuid>"] }`. Updates replace the ordered question list; the list must contain 1–50 distinct published questions.
- `DELETE /api/v1/admin/preparation-sets/{id}` archives the set.

Only active companies/roles and published, non-archived sets are visible to users. Sets never expose question answer keys. Missing resources return `404`, duplicate names/slugs return `409`, and invalid mappings or question lists return `400`.

## Readiness and study planning (Phase 7)

Both endpoints require a bearer token and use only the authenticated user's graded MCQ history. The role, company, skill, and topic must be active. An unknown or inactive role returns `404`.

- `GET /api/v1/readiness/roles/{roleId}` — readiness score, weighted skill/topic breakdown, and data coverage.
- `GET /api/v1/readiness/roles/{roleId}/study-plan?maxTasks=10` — prioritized practice recommendations; `maxTasks` defaults to 10 and must be 1–20.

Each topic's accuracy is correct answers divided by answered MCQs (unattempted topics have a score of zero in aggregate calculations). A topic weight is `role skill importance × skill topic relevance`; both source weights are 1–5. Overall readiness is the weighted mean of topic accuracy percentages across active mappings. Each skill score is the relevance-weighted mean of its topic accuracies. `dataCoveragePercent` is the fraction of total topic weight with at least five answers; five answers are required for `reliableSample: true`. With no active mappings, readiness is `null` and coverage is zero.

The study plan includes topics with fewer than five attempts or below 80% accuracy. Each task recommends five MCQs and 30 minutes of practice, and includes reasons, current accuracy/count, and a priority (`topic weight × (100 − accuracy)`, rounded to the nearest integer). Higher priority is listed first, then topic name. Topics already at 80% or better with at least five attempts are omitted. Text answers are not counted because they are not automatically graded.

Learner goal preferences can be stored at `/api/v1/users/me/preparation`; select that target role for these existing readiness and study-plan endpoints. The job description is retained as private preparation input and is not parsed or sent to an AI provider. The frontend can compose the dashboard daily brief from goal, readiness/study-plan, progress, and review-queue APIs.

## Mock interviews (Phase 8)

All routes require a bearer token and operate only on the authenticated user's interview sessions. Interviews use published `TEXT` questions from active topics mapped to an active role. The role and company must also be active. Questions are selected at random and their prompt and skill attribution are snapshotted into the session. If fewer questions are available than requested, the API returns `422`.

- `POST /api/v1/mock-interviews` — start a session. Request: `{ "roleId": "<uuid>", "questionCount": 5 }`, with question count 1–20. The response contains the session and prompts (no expected answers).
- `POST /api/v1/mock-interviews/{sessionId}/answers` — submit one answer: `{ "questionId": "<uuid>", "answerText": "...", "selfRating": 4 }`. Answer text is required (maximum 10,000 characters); self-rating is required from 1 to 5. A question can be answered only once.
- `GET /api/v1/mock-interviews/{sessionId}` — retrieve owned prompts, submitted answers, status, and per-skill results.
- `GET /api/v1/mock-interviews?page=0&size=20` — paginated personal history, newest first (size 1–100).

A session completes when every question is answered. Skill results report answered count, completion percentage, and the weighted mean of the user's self-ratings (`role skill importance × topic relevance`). These self-ratings are reflection aids, not a score of answer quality. The current backend does not automatically grade free-text answers. Non-owned or missing sessions return `404`; duplicate answers and answers after completion return `400`.

Authors may provide an evaluation rubric and a follow-up prompt for a text question. These are snapshotted into a mock interview and returned in the owned session detail only after the answer is submitted, so the learner can reflect against consistent criteria. Follow-up prompts are curated and fixed, not AI-generated or adaptive.

## Branching workplace cases (Phase 11)

All learner routes require a bearer token. Session reads and decisions are limited to the authenticated owner. Admin routes require the `ADMIN` role.

### Learner routes

- `GET /api/v1/workplace-cases?roleId=<uuid>&topicId=<uuid>&difficulty=MEDIUM&page=0&size=20` — published cases for active roles, companies, and topics; all filters are optional.
- `GET /api/v1/workplace-cases/{caseId}` — student-safe case catalog details. This does not expose the decision graph or feedback.
- `POST /api/v1/workplace-cases/{caseId}/sessions` — start a case and receive its introduction, source provenance, and first step. No request body is required.
- `POST /api/v1/workplace-cases/sessions/{sessionId}/decisions` — submit `{ "nodeKey": "INITIAL", "choiceKey": "INVESTIGATE" }`. The response explains the chosen action, its trade-offs, any misconception, and the next step.
- `GET /api/v1/workplace-cases/sessions/{sessionId}` — resume or review an owned session, showing prior feedback and the current step.
- `GET /api/v1/workplace-cases/sessions?page=0&size=20` — paginated personal case history, newest first.

Each START/DECISION node offers 2–6 choices. The chosen path changes which situation appears next. OUTCOME nodes show the lesson and end the session. Before answering, the user sees only choice text; after choosing, feedback explains every option, including why alternatives may be risky and when they could be appropriate. Case graphs are snapshotted at session start; later admin edits do not alter active or historical sessions.

Decision quality is author-curated: `STRONG` contributes 2 points, `VIABLE` 1, and `RISKY` 0. Multiple choices may be strong or viable when the scenario supports more than one defensible trade-off. The session's decision score summarizes this case rubric only; it is not a prediction of hiring outcome or general role readiness. Avoid presenting one choice as universally correct when its quality depends on context. A case must have one START node, reachable steps, no cycles, and every path must terminate at an OUTCOME.

### Admin authoring routes

- `GET /api/v1/admin/workplace-cases?page=0&size=20` and `GET /api/v1/admin/workplace-cases/{caseId}` — include unpublished and archived cases for authoring.
- `POST /api/v1/admin/workplace-cases` — create a case.
- `PUT /api/v1/admin/workplace-cases/{caseId}` — replace the case and its complete graph; updates do not affect existing sessions because their graph is snapshotted.
- `DELETE /api/v1/admin/workplace-cases/{caseId}` — archive the case without deleting session history.

Create/update input contains case metadata (`title`, optional `slug`, `description`, `roleId`, `topicId`, `difficulty`, `estimatedMinutes`, `scenarioIntro`, `learningObjective`, source fields, `published`) and an ordered `nodes` array. Each node includes `nodeKey`, `nodeType` (`START`, `DECISION`, `OUTCOME`), `heading`, `situationText`, optional `lessonText`, and `choices`. Each choice includes `choiceKey`, `choiceLabel`, `choiceText`, `decisionQuality`, `explanation`, optional `whenAppropriate`, `tradeoffSummary`, and `misconceptionLabel`, plus `nextNodeKey`. Source types match question authoring: `ORIGINAL`, `OFFICIAL`, `COMMUNITY_REPORTED`, `EDITORIAL`, or `UNKNOWN`. Every choice must explain its consequences and point to another node in the same case. Invalid or cyclic graphs return `400`; duplicate slugs return `409`; insufficient pagination bounds return `400`.

Example graph shape (IDs are existing active role/topic IDs):

```json
{
  "title": "Investigate a latency spike",
  "roleId": "<role-uuid>",
  "topicId": "<topic-uuid>",
  "difficulty": "MEDIUM",
  "estimatedMinutes": 10,
  "scenarioIntro": "A production API's p95 latency doubled after a release.",
  "learningObjective": "Choose safe first steps and adapt as evidence arrives.",
  "sourceType": "ORIGINAL",
  "sourceLabel": "InterviewForge scenario",
  "published": true,
  "nodes": [
    {
      "nodeKey": "INITIAL", "nodeType": "START", "heading": "Choose the first move",
      "situationText": "Errors are flat, but the latency alert is still active.",
      "choices": [
        { "choiceKey": "CHECK", "choiceLabel": "Inspect traces", "choiceText": "Compare traces and recent changes.", "decisionQuality": "STRONG", "explanation": "This gathers evidence while limiting risk.", "nextNodeKey": "TRACE_RESULT" },
        { "choiceKey": "ROLLBACK", "choiceLabel": "Rollback now", "choiceText": "Immediately revert the latest release.", "decisionQuality": "VIABLE", "explanation": "Rollback can be right if user impact is severe or the release is clearly causal.", "whenAppropriate": "Use this when impact is high and a safe rollback is available.", "nextNodeKey": "ROLLBACK_OUTCOME" }
      ]
    },
    {
      "nodeKey": "TRACE_RESULT", "nodeType": "DECISION", "heading": "New evidence changes the decision",
      "situationText": "Traces show a connection-pool wait after a query change. The error rate is now rising.",
      "choices": [
        { "choiceKey": "MITIGATE", "choiceLabel": "Reduce pool pressure", "choiceText": "Limit the affected traffic while validating the query change.", "decisionQuality": "STRONG", "explanation": "It reduces immediate impact and keeps the cause under investigation.", "tradeoffSummary": "Traffic is temporarily constrained while the issue is isolated.", "nextNodeKey": "MITIGATION_OUTCOME" },
        { "choiceKey": "WAIT", "choiceLabel": "Wait for more data", "choiceText": "Avoid changing anything until the metrics stabilize.", "decisionQuality": "RISKY", "explanation": "The rising error rate makes waiting costly; collect more evidence while taking a reversible mitigation.", "misconceptionLabel": "Treating observation and mitigation as mutually exclusive", "nextNodeKey": "WAIT_OUTCOME" }
      ]
    },
    { "nodeKey": "MITIGATION_OUTCOME", "nodeType": "OUTCOME", "heading": "Mitigation outcome", "situationText": "The error rate stops rising while the team checks the query change.", "lessonText": "During an incident, pair evidence gathering with reversible steps that reduce user impact." },
    { "nodeKey": "WAIT_OUTCOME", "nodeType": "OUTCOME", "heading": "Delay outcome", "situationText": "The error rate continues to rise while the team waits.", "lessonText": "When impact worsens, observation alone may be insufficient; choose a reversible mitigation and keep collecting evidence." },
    { "nodeKey": "ROLLBACK_OUTCOME", "nodeType": "OUTCOME", "heading": "Rollback result", "situationText": "The rollback restores latency, but hides the exact source of the regression.", "lessonText": "When impact warrants a fast rollback, follow it with evidence collection and a safe regression analysis." }
  ]
}
```
