# Database strategy

PostgreSQL is the primary database. Flyway SQL migrations under `src/main/resources/db/migration` are the source of truth for schema changes. Hibernate uses `ddl-auto: validate` and must not create or update tables automatically. Store timestamps in UTC. Add tables only as their owning product phase is implemented.

Phase 1 adds no domain tables. Add tables only as their owning product phase is implemented.
