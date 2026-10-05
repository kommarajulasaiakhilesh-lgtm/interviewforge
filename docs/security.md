# Security and account model

- Public registration accepts email, password, and display name. It always assigns `STUDENT`; clients cannot submit a role.
- Emails are normalized to lowercase and uniquely constrained in PostgreSQL.
- Passwords are hashed with BCrypt (work factor 12). Passwords must be at least 12 characters and are capped at BCrypt's 72-byte input limit.
- Login issues a cryptographically random opaque bearer token that expires after 12 hours. The database stores only its SHA-256 digest, never the raw token.
- Logout revokes only the current authenticated session. Each API request verifies the token against an unexpired, unrevoked session.
- Spring Security is stateless. CSRF is disabled because authentication uses an explicit Authorization bearer header rather than ambient browser cookies.
- API responses deny framing and use a `no-referrer` policy. HSTS is emitted for secure requests; in production, TLS must terminate at a trusted proxy that overwrites forwarded headers.
- Only sanitized health and build-info actuator endpoints are public. API routes require authentication; admin routes additionally require the `ADMIN` role.
- Profile routes are scoped to the authenticated user. Password hashes, tokens, and internal error details are never included in API responses.
- The `ADMIN` role is reserved for trusted provisioning. There is no public role-promotion endpoint; do not promote accounts through an untrusted client.

## Remaining security work before public launch

- Apply edge rate limits to registration and login; application-level distributed throttling is not implemented.
- Add email verification/account recovery and a trusted administrator provisioning workflow.
- Define session cleanup/retention and account deletion policies.
- Run a deployment-specific security review, confirm proxy/header behavior, database network isolation, backups, and secret rotation.

Do not treat this checklist as a substitute for a security assessment. See [deployment.md](deployment.md) for launch operations.
