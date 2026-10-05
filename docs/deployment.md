# Deployment and operations

InterviewForge runs as a Java 21+ Spring Boot JAR and uses PostgreSQL. Docker is not required. A deployment needs a Java runtime, a reachable PostgreSQL database, and environment variables supplied through the hosting platform's secret/configuration manager.

## Build and run

```powershell
mvn -DskipTests package
$env:SPRING_PROFILES_ACTIVE = 'prod'
$env:DB_URL = 'jdbc:postgresql://HOST:5432/DATABASE?sslmode=require'
$env:DB_USERNAME = 'APPLICATION_DATABASE_USER'
$env:DB_PASSWORD = 'SET_THIS_IN_THE_HOST_SECRET_MANAGER'
$env:APP_CORS_ALLOWED_ORIGINS = 'https://YOUR-LOVABLE-APP.example'
$env:PORT = '8080'
java -jar target/interviewforge-api-0.1.0-SNAPSHOT.jar
```

Do not use the example placeholders in a deployed environment. Use a dedicated least-privilege database account, require TLS for database connections, and keep the database inaccessible from the public internet. Set CORS to the exact production frontend origin(s); do not use `*` for this authenticated API. The app runs Flyway migrations on startup, so deploy schema-compatible app versions and avoid starting several app instances simultaneously during a migration rollout.

## Health and monitoring

- `/actuator/health` gives a sanitized aggregate status.
- `/actuator/health/liveness` indicates whether the process is alive and does not depend on PostgreSQL.
- `/actuator/health/readiness` includes PostgreSQL and application readiness; use it to decide whether an instance should receive traffic.
- `/actuator/info` reports build metadata and does not expose environment properties.

Health routes are public for platform probes, but component details remain hidden. Put the service behind HTTPS and a trusted reverse proxy. The app honors forwarded headers for TLS detection; configure the proxy to overwrite forwarded headers supplied by clients. Capture application stdout/stderr in the hosting platform's log service. Avoid logging authorization headers, passwords, or request bodies.

Configure automated PostgreSQL backups and periodically verify restore procedures. Alert on failed health checks, repeated startup/migration failures, elevated 5xx rates, and database capacity. The current API does not expose a Prometheus scrape endpoint.

## CI

GitHub Actions compiles and packages on pushes to `main` and pull requests targeting `main`. It does not deploy automatically. A manual or separately approved release process should set the production secrets, migrate the database, deploy the JAR, and verify readiness.

## Remaining launch controls

Before a public launch, configure edge rate limits for registration and login, TLS-only ingress, database firewalling, secret rotation, backup/restore checks, and an administrator provisioning process. Review the current security limitations in [security.md](security.md). This repository does not configure a specific cloud provider or create cloud resources.
