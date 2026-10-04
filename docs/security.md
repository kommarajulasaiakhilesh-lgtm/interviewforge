# Security and account model

- Public registration accepts email, password, and display name. It always assigns `STUDENT`; clients cannot submit a role.
- Emails are normalized to lowercase and uniquely constrained in PostgreSQL.
- Passwords are hashed with BCrypt (work factor 12). Passwords must be at least 12 characters and are capped at BCrypt's 72-byte input limit.
- Login issues a cryptographically random opaque bearer token that expires after 12 hours. The database stores only its SHA-256 digest, never the raw token.
- Logout revokes only the current authenticated session. Each API request verifies the token against an unexpired, unrevoked session.
- Spring Security is stateless. CSRF is disabled because authentication uses an explicit Authorization bearer header rather than ambient browser cookies.
- Profile routes are scoped to the authenticated user. Password hashes, tokens, and internal error details are never included in API responses.
- The `ADMIN` role is reserved for trusted provisioning. There is no public role-promotion endpoint; do not promote accounts through an untrusted client.

Account recovery, verification email, login throttling, admin provisioning workflow, and session cleanup are future security work. Do not treat the application as production-ready until those controls and a security review are complete.
