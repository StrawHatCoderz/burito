# Burito

## Running locally

```bash
cp .env.example .env
docker compose up --build
```

## Developer Setup & Pre-Commit Hooks

This project uses **Husky** to enforce pre-commit checks at the local workspace level before code can be committed or pushed.
Every commit triggers:
1. Backend checkstyle checks (`checkstyleMain` - warnings only).
2. Backend full test suite (`./gradlew test`).
3. Frontend full test suite (`npm run test`).

If any test fails, the commit will be aborted.

### Setup Instructions
To set up the hooks locally, install the root dependencies from the workspace root directory:
```bash
npm install
```
This registers the local git hooks in `.husky/` so they run automatically on `git commit`.

## Required ports

| Port | Service      |
|------|--------------|
| 80   | NGINX (entry)|
| 3000 | Frontend     |
| 8080 | Backend      |
| 5432 | PostgreSQL   |

## CI & Branch Protection

Every PR to `main` runs three parallel GitHub Actions jobs:

| Job | What it does |
|-----|-------------|
| **Backend** | Builds the JAR, runs all tests (Testcontainers + PostgreSQL), uploads JUnit results as an artifact |
| **Frontend** | Validates `frontend/index.html` exists (stub — full React pipeline added later) |
| **Lint** | Runs `./gradlew checkstyleMain` (Google style, violations reported but not enforced yet) |

### Enable branch protection (one-time setup)

To prevent merging when CI fails, configure branch protection in GitHub:

1. Go to **Settings → Branches → Add branch ruleset**
2. Target branch: `main`
3. Enable **Require status checks to pass** and add:
   - `Backend — build & test`
   - `Frontend — validate`
   - `Lint — Checkstyle`
4. Enable **Require branches to be up to date before merging**

### Running tests locally with Colima (macOS)

Testcontainers requires a Docker daemon. If you use Colima, export these variables before running `./gradlew test`:

```bash
export DOCKER_HOST=unix://${DOCKER_HOST}
export TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE=/var/run/docker.sock
```

Or add them to your shell profile to apply automatically.

## Endpoints

| Method | Path                        | Auth     |
|--------|-----------------------------|----------|
| POST   | /api/auth/register          | None     |
| POST   | /api/auth/login             | None     |
| GET    | /api/me                     | Bearer   |
| GET    | /api/restaurants/           | None     |
| GET    | /api/restaurants/{id}       | None     |
| GET    | /api/health                 | None     |
