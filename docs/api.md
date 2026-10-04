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
