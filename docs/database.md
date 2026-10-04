# Database strategy

PostgreSQL is the primary database. Flyway SQL migrations under `src/main/resources/db/migration` are the source of truth for schema changes. Hibernate uses `ddl-auto: validate` and must not create or update tables automatically. Store timestamps in UTC. Add tables only as their owning product phase is implemented.

Phase 1 added no domain tables. Phase 2 adds `user_accounts`, `user_profiles`, and `auth_sessions`. Session tokens are stored as SHA-256 hashes. Accounts and their profiles/sessions use UUID keys and cascading foreign keys for account-owned data.

Phase 3 adds `topics`, `question_tags`, `questions`, and `question_tag_assignments`. Catalog entries are deactivated instead of deleted so existing question references remain valid. Question deletion is archival. MCQ choices are JSONB and the correct option is an index constrained to the stored choices; student responses omit the answer key and explanation.
