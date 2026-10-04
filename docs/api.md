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
  "tagIds": [],
  "published": false
}
```

For `TEXT`, send no `options` and no `correctOptionIndex`; `answerText` may hold the expected answer. Question creation defaults to draft unless `published` is true. `PUT` replaces the question, including its tag list and publication state. Duplicate catalog names/slugs return `409`; missing IDs return `404`; invalid request data returns `400`.

## Practice (Phase 4)

All routes require a bearer token and return data for the authenticated user's sessions only.

- `POST /api/v1/practice/sessions` — create a random session. Request: `{ "topicId": "<uuid>", "difficulty": "MEDIUM", "type": "MCQ", "questionCount": 10 }`. `topicId`, `difficulty`, and `type` are optional; type defaults to MCQ. Count is required from 1 to 20. If too few matching published questions exist, response is `422`.
- `POST /api/v1/practice/sessions/{sessionId}/answers` — submit one answer for a question in the session. MCQ: `{ "questionId": "<uuid>", "selectedOptionIndex": 1 }`. TEXT: `{ "questionId": "<uuid>", "answerText": "My response" }`.
- `GET /api/v1/practice/sessions/{sessionId}` — review prompts and submitted answers. Correct answers/explanations are hidden until that question is answered.
- `GET /api/v1/practice/sessions?page=0&size=20` — paged personal history, newest first (size 1–100).

Sessions complete when each question is answered once. MCQs are graded immediately; the answer response includes correctness, key, and explanation. `scorePercent` is available on completion and equals correct answers divided by session question count. TEXT sessions are stored for review but have no automatic score (`scorePercent` is null). Prompts and keys are snapshotted at session creation. Another user's/nonexistent session returns `404`; repeated submissions or submissions after completion return `400`.
