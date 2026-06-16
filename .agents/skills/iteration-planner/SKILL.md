---
name: iteration-planner
description: >
  Plans a complete software iteration from scratch — reads source files and
  folder structure to extract current project state, suggests what to build next,
  generates full Agile story cards, and writes each story as a markdown file. Use
  this skill whenever the user says "plan the next iteration", "write stories for
  iteration N", "what should we build next", "start iteration planning", "generate
  story cards", or anything resembling sprint/iteration kickoff. Trigger even if
  they just say "let's plan" or "next iteration" in a project context. This skill
  handles the full planning lifecycle: context extraction → suggestion → alignment
  → story generation → file writing.
---

# Iteration Planner Skill

Plans a full software iteration by reading the project, surfacing options,
aligning with the user, then generating and writing complete story card files —
one markdown file per story.

Do not write any story files until Phase 5 is complete and the user has confirmed
the assumption list.

---

## Phase 1 — Context Extraction

Before suggesting anything, read the project deeply. The goal is to understand:
what exists, what works, what is inconsistent, and what the natural next layer is.

### 1.1 Check for existing context

```bash
cat docs/context/project-context.md    # if it exists, read it first
```

If `project-context.md` exists and appears recent (last-updated within the last
few commits), use it as the primary source for project understanding. Skip
re-reading files already covered there, and focus on what may have changed since.

If it does not exist or is stale, run a full survey:

### 1.2 Discover project structure

List the root directory tree (2 levels deep), then recursively read:

- All source files under `src/` (or equivalent)
- Any existing `docs/`, `planning/`, or `iterations/` folders
- `README.md` or any `CONTEXT.md` if present
- CI config files (`.github/workflows/`, `Makefile`, etc.)
- Build/dependency files (`pom.xml`, `build.gradle`, `package.json`, etc.)
- Migration files (if Flyway/Liquibase: `src/main/resources/db/migration/`)
- Test files to understand what is already covered

### 1.3 Determine next iteration number

Look for existing story folders to determine the correct iteration number:

```bash
ls docs/iterations/        # preferred location
ls planning/               # fallback
ls .planning/              # fallback
```

If none exist, this is **Iteration 1**.
If folders exist, count them and set N = highest existing iteration + 1.
If ambiguous, ask the user.

### 1.4 Build the Current State Summary

After reading, produce a structured **Current State** block:

```
## Current State — [Project Name]

### What exists
- [List all implemented features / endpoints / modules]
- [Auth, DB, migrations, CI, test coverage — whatever is present]

### Architecture observations
- [Bounded contexts / packages / modules]
- [Key patterns in use: layered arch, bounded contexts, etc.]
- [Notable dependencies]

### Test coverage
- [What is tested at unit / integration / e2e level]
- [Coverage tool and approximate % if visible]

### Known issues / tech debt
- [Antipatterns, dead code, naming inconsistencies, TODOs]
- [Anything that would cause friction before building the next layer]

### What is missing or empty
- [Seeded data, unimplemented endpoints, stub modules]
```

Present this to the user and ask:

> "Does this capture the current state correctly? Anything to add or correct
> before we plan?"

Wait for confirmation before proceeding to Phase 2.

---

## Phase 2 — Suggestions

Once current state is confirmed, generate **2–4 candidate directions** for the
next iteration. Each option must be scoped to fit a single iteration — not a
roadmap item.

Format each option as:

```
### Option [A/B/C/D] — [Short name]
**What it delivers:** [1–2 sentences on user/system value]
**Scope:** [What is in, what is explicitly out]
**Bounded context(s) touched:** [e.g. Menu, Order, Auth]
**Prerequisite cleanup needed:** [Yes / No — if yes, describe briefly]
**Risk / complexity:** [Low / Medium / High + one-line reason]
**Why now:** [Why this is the logical next step given the current state]
```

Always include at minimum:

- One option that **adds a net-new feature or domain**
- One option that **cleans up / hardens what exists** before building more
- One option that is a **smaller, safer version** of the main feature option

### Cleanup as a standalone option

If the project has meaningful architectural inconsistencies, inconsistent
layering, or missing seeded data, surface a **Pre-Iteration Cleanup** option
explicitly. Frame it as:

> "Building the next feature on top of the current inconsistencies will make
> the code harder to reason about. Here is what a one-iteration cleanup sprint
> would address: [list]."

Never silently fold cleanup into a feature iteration — surface the decision
explicitly and let the user choose.

---

## Phase 3 — Alignment

After presenting the options, ask:

> 1. Which option do you want to build? (or a mix?)
> 2. Do you have ideas or constraints I should know before writing stories?
>    (e.g. "I want to try CQRS here", "skip the UI", "keep it backend-only")
> 3. Any assumptions you want me to call out before I finalise stories?

Wait for the user's response. Do not generate stories until alignment is
confirmed.

---

## Phase 4 — Assumption Review (Gate)

Before writing stories, explicitly list all assumptions:

```
## Assumptions before I write stories

- [ ] The story ID prefix will be [PREFIX]-NNN (e.g. BR-001)
- [ ] The iteration folder will be: [path]
- [ ] [Feature assumption — e.g. "Menu items are owned by restaurants, not users"]
- [ ] [Architecture assumption — e.g. "Cart will be a new bounded context"]
- [ ] [Tech assumption — e.g. "Flyway migration will be included for new tables"]
- [ ] [Testing assumption — e.g. "Each story includes unit + integration test tasks"]
```

Then ask:

> "Do any of these need changing before I write the stories?"

Wait for the user to confirm or correct. Do not write any story files until this
gate is cleared.

---

## Phase 5 — Story Generation

Generate stories that are:

- **Vertically sliced** — each story delivers end-to-end value (DB → service →
  API → test), not a horizontal layer
- **Independently completable** — a story should not require another story to be
  merged first, unless explicitly sequenced
- **Bounded context-aware** — do not reach across bounded contexts without
  calling it out

### Story sizing guidance

| Size | Effort | What fits |
|------|--------|-----------|
| XS | < 2h | Config change, adding a field, tiny refactor |
| S | ~half day | Single endpoint with tests |
| M | ~1 day | New service + repository + endpoint + tests |
| L | ~2 days | New bounded context stub, complex query, multi-step flow |
| XL | > 2 days | **Must be split — never generate an XL story** |

### Iteration story count guidance

| Iteration type | Story count |
|----------------|-------------|
| Cleanup sprint | 3–6 stories |
| Single feature/domain | 4–8 stories |
| Combined feature + cleanup | 5–9 stories |

Do not pad. Fewer well-scoped stories beat a bloated backlog.

---

## Phase 6 — Story File Format

### Output folder

Determine the output folder in this order:

1. If `docs/iterations/` exists → write to `docs/iterations/iteration-N/`
2. If `planning/` exists → write to `planning/iteration-N/`
3. If `.planning/` exists → write to `.planning/iteration-N/`
4. Otherwise → create `docs/iterations/iteration-N/` at the project root

Where N is the iteration number determined in Phase 1.3.

### File naming

```
[PREFIX]-[NNN]-[kebab-case-title].md
```

Examples:

- `BR-001-pre-iteration-cleanup.md`
- `BR-002-create-menu-item-entity.md`
- `BR-007-list-menu-items-by-restaurant.md`

Story IDs are **sequential and never recycled**. If Iteration 1 ended at BR-007,
Iteration 2 starts at BR-008.

### Story file template

Write one `.md` file per story using this exact template:

```markdown
# [PREFIX]-[NNN] — [Story Title]

**Iteration:** [N]
**Type:** Feature | Cleanup | Chore | Spike
**Size:** XS | S | M | L
**Bounded Context:** [e.g. Menu, Order, Auth, Infrastructure]
**Priority:** Must-have | Should-have | Nice-to-have

---

## User Story

> As a [role], I want to [action], so that [benefit].

*(For Cleanup/Chore stories: "As a developer, I want to [clean up X], so that
[the codebase is easier to reason about / future stories are easier to build
on top of].)*

---

## Context

[2–4 sentences explaining why this story exists, what problem it solves, and how
it relates to the current project state. Reference specific files, classes, or
antipatterns by name where relevant.]

---

## Acceptance Criteria

- [ ] [Concrete, testable criterion — describe observable behaviour, not implementation]
- [ ] [Each AC maps to something you can verify manually or via a test]
- [ ] [Include at least one AC about test coverage for feature stories]
- [ ] [Include at least one AC about CI passing for every story]

---

## Technical Tasks

- [ ] [Concrete implementation step — name the class, method, file, or migration]
- [ ] [Keep tasks at the level of: "Create X", "Add Y to Z", "Write migration for W"]
- [ ] [Include task for unit test(s)]
- [ ] [Include task for integration test(s) where applicable]
- [ ] [Include task to verify CI passes]

---

## Out of Scope

- [Explicitly list what this story does NOT include, to prevent scope creep]
- [Reference future stories or iteration for deferred work]

---

## Notes / Design Decisions

[Optional. Record any architecture choices, tradeoffs, or open questions. If this
story introduces a new pattern — e.g. a new bounded context, a new exception type
— document the intent here so future stories stay consistent.]
```

---

## Phase 7 — Draft Review

After generating all story files, produce a summary table in the conversation:

```
## Iteration N — Story Summary

| ID | Title | Type | Size | Bounded Context | Priority |
|----|-------|------|------|-----------------|----------|
| BR-001 | Pre-Iteration Cleanup | Cleanup | M | Infrastructure | Must-have |
| BR-002 | Create MenuItem Entity | Feature | S | Menu | Must-have |
| ...  | ... | ... | ... | ... | ... |

**Total stories:** N
**Files written to:** docs/iterations/iteration-N/
```

Then ask:

> 1. Any stories to add, remove, or resize?
> 2. Any acceptance criteria that feel wrong or missing?
> 3. Ready to start, or do you want to adjust first?

Do not close the planning session until the user confirms the iteration plan is
ready.

---

## Key Principles

| Principle | Rule |
|-----------|------|
| Surface decisions explicitly | Never silently choose between cleanup-first vs feature-first — make the user decide |
| Bounded context discipline | If a story causes a service to reach into another context's repository, call it out |
| Cleanup is never optional to mention | If structural issues exist, always surface them as an explicit option |
| Tests are not optional | Every feature story must include tasks for unit and integration tests — "write tests" is not a task, name what you are testing |
| IDs are sequential | Never recycle or reuse a story ID |
| Assumptions before stories | Never assume silently on bounded context ownership, table naming, service boundaries, or story prefix |
| Review gate before writing | Never write story files before Phase 4 assumption confirmation |

---

## Edge Cases

**`project-context.md` is stale:**
If the last-updated date is several commits behind, note it and do a targeted
re-read of the files most likely to have changed (new migrations, new controllers,
new frontend pages) before building the Current State summary.

**User wants a mix of options:**
Accepted. Blend the selected options into a single coherent iteration scope.
Adjust story count to fit the combined scope (cap at 9 stories).

**Story count would exceed 9:**
Split into two iterations and present both outlines. Ask the user which one to
write first.

**No existing story IDs found:**
Confirm the prefix with the user before generating. Default: infer from the
project name (e.g. `Burito` → `BR-`).

**User disagrees with the current state summary:**
Update the summary before generating options. Never plan from a state the user
has not confirmed.
