---
name: coding-standards
description: >
  Code quality and architectural standards reference. Provides pre-implementation
  verification checklists and pre-commit quality gates. Load this skill whenever
  writing production code — the task-implementation skill references it automatically.
  Also use when asked to "check code quality", "review architecture", or "run the
  quality checklist".
---

# Coding Standards Skill

This skill defines the quality gates that all production code must pass before it is
committed. It is designed to be loaded by the `task-implementation` skill at two points:
**before writing code** (to set up guard rails) and **before committing** (to verify
compliance). It can also be loaded independently when the user asks for a quality review.

---

## Pre-Implementation Checklist

Before writing any production code, complete these steps:

### 1. Understand the Existing Shape

Read the files you are about to modify or extend. For each file, answer:

- What layer is this? (controller / service / repository / domain / config / utility)
- What does it depend on? (only the layer below, or does it reach across?)
- How does it handle errors? (what exception types, what response format)
- How does it handle mapping? (inline, dedicated mapper class, static utility)

If you cannot answer these questions, you have not read enough code yet.

### 2. Identify the Established Patterns

Before introducing any structural choice, grep the codebase for how the same
concern is already handled:

```bash
# Examples — adapt to the concern at hand:
grep -r "APIResponse" --include="*.java" -l        # response wrapping
grep -r "APIException" --include="*.java" -l        # exception handling
grep -r "LocalDateTime" --include="*.java" -l       # timestamp types
grep -r "@Transactional" --include="*.java" -l      # transaction boundaries
```

Follow the dominant pattern. If patterns conflict, stop and flag it to the user.

### 3. Plan Test Coverage Alongside Implementation

For each method or branch you are about to write, note the tests you will write
*in the same step*:

```
Method: createOrder(request)
  → Test: happy path with valid input
  → Test: null userId → expected exception
  → Test: empty cart → expected exception
  → Test: restaurant closed → expected exception
```

Do not write the implementation without this test plan. Do not defer tests to
"a later step."

---

## Pre-Commit Quality Gate

Before presenting code for review or committing, verify **every item** below.
If any item fails, fix it before proceeding.

### Architecture

- [ ] **Layer discipline**: Every new dependency follows the direction
      `controller → service → repository`. No layer reaches upward or skips a level.
- [ ] **No circular references**: No service imports a controller. No repository
      imports a service.
- [ ] **Single responsibility**: Each new class has one job. Business logic is not
      in controllers. Mapping logic is not in controllers. Auth logic is not duplicated
      across endpoints.
- [ ] **Utility placement**: Shared helpers, mappers, and converters live in dedicated
      classes — not as static methods on controllers or as private methods duplicated
      across services.

### Consistency

- [ ] **Response format**: Every controller endpoint returns the project's standard
      response wrapper (check the existing pattern — e.g., `APIResponse<T>`).
- [ ] **Error handling**: Every thrown exception uses the project's standard exception
      hierarchy (check the existing pattern — e.g., `APIException` subclasses). No raw
      `ResponseStatusException`, `IllegalStateException`, or `IllegalArgumentException`
      as API error responses.
- [ ] **Type consistency**: Date/time fields, ID types, and enum usage match the
      conventions used by existing entities.
- [ ] **Naming**: New files, classes, methods, and test methods follow the naming
      conventions already present in the codebase.

### Test Quality

- [ ] **Coverage verified**: Run the project's coverage tool and confirm that new code
      has ≥95% instruction/branch coverage. Do not guess — read the report.
- [ ] **Tests written alongside code**: Tests were created in the same step as the
      implementation, not bolted on afterward.
- [ ] **Edge cases covered**: Null inputs, empty collections, boundary values, permission
      failures, and not-found scenarios are all tested where applicable.
- [ ] **Assertions are specific**: Tests assert on the *correct* expected value, not
      just "no exception thrown." If a method should throw `ErrorCode.NOT_FOUND`, the
      test asserts on that specific error code.

### Hygiene

- [ ] **No dead code**: Every new class, method, field, and enum value is actually
      referenced. Remove anything introduced but unused.
- [ ] **No assumption-driven code**: Every method signature, return type, and field
      type was verified by reading the actual source — not assumed from memory or naming
      conventions.
- [ ] **No duplicated logic**: If the same 3+ lines of logic appear in multiple places,
      extract them into a shared method or class.
- [ ] **Comments justify *why*, not *what***: Inline comments explain non-obvious
      decisions, not obvious code.

---

## How to Use This Skill

### When loaded by `task-implementation`:

1. **Before Step 2 (Implement)** — run the Pre-Implementation Checklist silently.
   Do not present it to the user unless issues are found.
2. **After Step 4 (Run Tests), before Step 5 (Present for Review)** — run the full
   Pre-Commit Quality Gate. If any item fails, fix it before presenting.

### When loaded independently:

Walk through the Pre-Commit Quality Gate against the current working tree or a
specific set of files. Report findings in the same format as the `find-bugs` skill.

---

## Principles Behind These Rules

These rules exist because of real failures observed in this project:

| Principle | What goes wrong without it |
|---|---|
| Test alongside code | Tests written after the fact discover bugs that have already been committed and deployed to other developers' branches. Fixing them retroactively is 5–10× more expensive. |
| Layer discipline | Controllers that access repositories directly become untestable, accumulate business logic, and create coupling that blocks future modularisation. |
| Consistency over cleverness | Three competing exception strategies means the global error handler catches some errors and misses others, producing inconsistent API responses for the same class of failure. |
| Verify, don't assume | Assuming `Optional<T>` when the method returns `T` causes compilation failures that waste cycles. Reading the source takes 10 seconds. |
| No dead weight | Unused enum values, DTOs, and config classes confuse future developers (and future AI sessions) about what is intentional versus abandoned. |
