---
name: context
description: >
  Build and maintain an accurate understanding of the current project. Analyses
  the repository, identifies architecture, determines current development status,
  discovers technical debt, and writes docs/context/project-context.md. Use this
  skill whenever the user says "build context", "update the project context",
  "analyse the repo", "what is the current state of the project", or is starting
  work on an unfamiliar codebase, returning after a gap, or preparing for
  iteration planning or architecture review. Also trigger when the user starts
  iteration planning and no context file exists yet.
---

# Context Builder Skill

Read the repository deeply, surface findings, get user confirmation, then write
`docs/context/project-context.md` — the single source of truth for all future
planning, story generation, and AI sessions.

Do not write any files until Phase 7 is complete and the user has confirmed the findings.

---

## Phase 1 — Repository Discovery

Read every layer of the project that reveals intent, structure, or behaviour.

### 1.1 Root structure

List the root directory (2 levels deep) to understand the monorepo shape:

```bash
find . -maxdepth 2 -not -path '*/\.*' -not -path '*/node_modules/*'
```

### 1.2 Documentation

Read, in order:

- `README.md` (and any `CONTEXT.md`, `ARCHITECTURE.md`, `CONTRIBUTING.md`)
- Any `docs/` directory — especially prior context files, ADRs, or iteration notes

If `docs/context/project-context.md` already exists, read it first and note its
last-updated date. You will update it in Phase 8, not overwrite it.

### 1.3 Source code

Recursively list and read key files:

```bash
# Backend — adapt extension to stack (*.java, *.go, *.py, *.ts, etc.)
find src/ -name "*.java" | head -60

# Frontend — if present
find frontend/src -name "*.ts" -o -name "*.tsx" | head -40
```

Read at minimum:
- All entry points (main class, `main.tsx`, `app.py`, etc.)
- All controllers / route handlers
- All service classes
- All domain/entity/model classes
- All repository interfaces
- All configuration files
- All enums and error types

### 1.4 Build and dependency files

```bash
cat build.gradle      # or pom.xml, package.json, requirements.txt, go.mod
cat frontend/package.json
```

Note every notable dependency: framework, ORM, auth library, test runner, etc.

### 1.5 Database migrations

```bash
ls src/main/resources/db/migration/    # Flyway / Liquibase
```

Read every migration file. Build a chronological picture of the schema:
tables created, columns added, seed data loaded.

### 1.6 Test suites

```bash
find . -name "*Test*.java" -o -name "*.test.ts" -o -name "*.spec.*" | head -30
```

Read representative test files to understand:
- Test runner and framework (JUnit, Vitest, pytest, etc.)
- What is tested (unit, integration, e2e)
- What is NOT tested
- Coverage tooling if present (JaCoCo, Istanbul, etc.)

### 1.7 CI and infrastructure

```bash
cat .github/workflows/*.yml
cat docker-compose.yml
cat docker-compose.override.yml
cat nginx/nginx.conf
cat .env.example
```

---

## Phase 2 — Architecture Analysis

From the files read in Phase 1, infer (do not assume) the architecture.

Identify:

- **Entry points** — what starts the application, what handles the first request
- **Application layers** — controllers / services / repositories / domain, or equivalent
- **Bounded contexts** — are domain concepts separated by package, module, or service?
- **Service boundaries** — which services call which; any cross-context coupling
- **Data flow** — from HTTP request to database and back
- **Auth model** — how authentication and authorisation work end-to-end
- **External integrations** — any third-party APIs, message queues, storage services

Produce a one-paragraph architecture narrative and a simple diagram in text or Mermaid.

> If evidence is absent, state uncertainty explicitly. Never invent structure.

---

## Phase 3 — Current State Analysis

Determine what is finished, what is partial, and what is a stub.

Walk through every controller, service, and frontend page and classify it:

| Status | Meaning |
|--------|---------|
| ✅ Complete | Implemented, tested, and wired end-to-end |
| 🔶 Partial | Implemented but missing tests, edge cases, or wiring |
| 🔲 Stub | Exists as a file/class but contains no meaningful logic |
| ❌ Missing | Referenced or expected but does not exist yet |

Search for in-code signals of unfinished work:

```bash
grep -r "TODO\|FIXME\|HACK\|XXX\|WIP" --include="*.java" --include="*.ts" --include="*.tsx" -n
```

Document every hit with its file path and line number.

---

## Phase 4 — Active Work Detection

Inspect git signals to infer what is currently in flight.

```bash
git log --oneline -25          # recent commit history
git status                     # uncommitted changes
git branch -a                  # active feature branches
```

From commit messages, infer:
- What was just completed
- What the current development focus is
- What the natural next step is

Mark all inferences as uncertain unless directly confirmed by file content.

---

## Phase 5 — Risk Assessment

Identify problems ranked by impact. For each finding, note:
- What the problem is
- Where it lives (file/class name)
- Why it matters
- Impact: **High** / **Medium** / **Low**

Categories to examine:

| Category | What to look for |
|----------|-----------------|
| Missing tests | Services, controllers, or edge cases with no test coverage |
| Dead code | Methods defined but never called; imports unused |
| Domain leakage | Entities serialised directly to the API (no DTO/projection layer) |
| Excessive coupling | Service A calling Service B's repository directly |
| Security | Hardcoded secrets, overly permissive CORS, missing validation |
| Performance | Unbounded list queries, N+1 risks, missing indices |
| Architectural drift | Code that violates the patterns the rest of the project follows |
| Stub CI | Pipeline jobs that validate trivially (e.g. "file exists") |
| Inconsistency | Mixed ID strategies, mixed naming conventions, mixed patterns |

---

## Phase 6 — Opportunity Discovery

Suggest the most valuable next work. Ground every suggestion in evidence from the code.

Organise suggestions into:

- **Product features** — what the app obviously needs next to be useful
- **Technical debt** — what will slow down future features if not addressed
- **Testing** — coverage gaps that pose real risk
- **Developer experience** — CI improvements, local dev friction, tooling gaps
- **Architecture** — refactors that would improve reasoning or extensibility

Do not pad. Suggest only what the evidence supports.

---

## Phase 7 — Assumption Review

Before writing any files, present all findings to the user.

Show:

1. The technology stack (observed, not assumed)
2. The architecture summary and diagram
3. The current state table (complete / partial / stub / missing)
4. Active work signals from git
5. Risk assessment ranked by impact
6. Suggested next work
7. Explicit list of assumptions:

```
## Assumptions

- [ ] [Assumption 1 — e.g. "Story ID prefix is BR-"]
- [ ] [Assumption 2 — e.g. "No external integrations exist — no evidence found"]
- [ ] [Assumption 3 — e.g. "HomePage is an intentional stub for a future dashboard"]
```

Then ask:

> "Does this match your understanding of the project?
> Please confirm or correct any findings before I write `project-context.md`."

Do not write any files until the user responds.
If the user requests corrections, update the relevant section and re-present.
Once the user agrees (or says "looks good", "write it", "go ahead"), proceed to Phase 8.

---

## Phase 8 — Generate Context Document

### Output location

Determine the file path:

1. If `docs/context/project-context.md` already exists → update it in place, preserve
   any historical sections not covered by fresh analysis
2. If `docs/` exists but no `context/` subdirectory → create `docs/context/project-context.md`
3. Otherwise → create `docs/context/project-context.md` at the project root

### Document format

Write the full context document using the template below.
Every section must be populated from observed evidence.
Mark anything inferred as *(inferred)*.
Mark anything uncertain as *(uncertain)*.
Never leave a section blank — write "None identified" if genuinely absent.

```markdown
# Project Context

## Executive Summary

[2–4 sentences. What the product is, its current maturity, and the most important
thing to know before working on it.]

---

## Product Purpose

[What the product does from the user's perspective. Not technical — describe the
user value.]

---

## Business Goals

[What problems this product solves. Infer from the domain if not documented.]

---

## Current State

[Paragraph summary of what is built and working end-to-end today.]

### Feature Status

| Feature | Status | Notes |
|---------|--------|-------|
| [Feature name] | ✅ Complete / 🔶 Partial / 🔲 Stub / ❌ Missing | [Detail] |

---

## Architecture Overview

[Narrative paragraph + diagram.]

```
[ASCII or Mermaid architecture diagram]
```

### Request Flow

[Trace a typical request from the browser/client through to the database and back.]

---

## Bounded Contexts

[List each bounded context, what it owns, and its service/package location.
If the project does not use bounded contexts, say so and describe the actual
package structure.]

| Context | Owns | Location |
|---------|------|----------|

---

## Technology Stack

### Backend

| Concern | Technology | Version |
|---------|-----------|---------|

### Frontend

| Concern | Technology | Version |
|---------|-----------|---------|

### Infrastructure

| Concern | Technology |
|---------|-----------|

---

## Database Design

### Schema Summary

[List tables, primary key type, and key relationships. Reference migration files.]

| Table | PK Type | Key Columns | Notes |
|-------|---------|-------------|-------|

### Migration Files

| File | What it does |
|------|-------------|

---

## External Integrations

[Any third-party APIs, payment gateways, email services, object storage, etc.
If none: "None — the application has no external service dependencies."]

---

## Testing Strategy

### Backend

| Layer | Framework | Coverage notes |
|-------|-----------|---------------|

### Frontend

| Layer | Framework | Coverage notes |
|-------|-----------|---------------|

### CI

[Describe the CI pipeline jobs, what each validates, and any known gaps.]

---

## Deployment Strategy

[How the application is run locally and in production.
Docker Compose, Kubernetes, bare metal, cloud — whatever the evidence shows.]

---

## Active Development

[What was most recently completed, based on git log.
What appears to be in progress or next, based on git signals and code state.]

---

## Known Issues

[Confirmed bugs or broken behaviours observed in the code.]

| Issue | Location | Impact |
|-------|----------|--------|

---

## Technical Debt

[Code quality problems that will slow down future development.]

| Item | Location | Impact |
|------|----------|--------|

---

## Risks

[Ranked by impact. Focus on things that could cause real problems.]

| Risk | Location | Impact | Mitigation |
|------|----------|--------|------------|

---

## Suggested Next Iteration

[Top 3–5 high-value items to tackle next, grounded in the evidence above.
Not a roadmap — just the most logical next layer given the current state.]

---

## Assumptions

[List every assumption made in producing this document. Distinguish from
observed facts.]

- [Assumption 1]
- [Assumption 2]

---

## Last Updated

[Date this document was generated or last refreshed — use ISO 8601: YYYY-MM-DD]
[Git commit hash at time of analysis]
```

---

## Phase 9 — Confirm and Close

After writing the file, confirm to the user:

> "Context document written to `docs/context/project-context.md`.
>
> Key findings worth highlighting:
> - [Top risk or architectural observation]
> - [Most significant gap]
> - [Suggested next focus]
>
> You can now run iteration planning or start a story directly from this context."

---

## Key Principles

| Principle | Rule |
|-----------|------|
| Accuracy first | Never invent project details. If unsure, say so. |
| Evidence-based | Every finding must cite a file, class, or commit. |
| Distinguish clearly | Always separate observed fact / inferred conclusion / assumption. |
| Review before writing | Never write `project-context.md` before Phase 7 user confirmation. |
| Preserve history | If a context file exists, update it — do not overwrite prior insights. |
| No padding | Write only what the evidence supports. |

---

## Edge Cases

**Context file already exists:**
Read it first. Compare its claims against the current code. Note any sections that
are now stale. Update those sections and preserve sections that are still accurate.
Add a "Last Updated" line at the bottom.

**Monorepo with multiple services:**
Produce one context document that covers all services. Add a section per service
in the Architecture and Technology Stack sections.

**No documentation anywhere:**
Proceed from code alone. Mark the entire document as *(inferred from source)* in
the Executive Summary.

**Very large codebase:**
Prioritise reading: entry points → controllers/routes → domain models → migrations →
tests. Read service implementations only for areas that are ambiguous from the above.
