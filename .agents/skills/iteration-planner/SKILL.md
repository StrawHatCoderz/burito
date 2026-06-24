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
  API → frontend), not a horizontal layer
- **Independently completable** — a story should not require another story to be
  merged first, unless explicitly sequenced via priority and dependencies
- **Bounded context-aware** — do not reach across bounded contexts without
  calling it out
- **Behaviour-focused, not implementation-focused** — acceptance criteria describe
  WHAT the system does, not HOW it is built. Technical implementation details
  belong in Technical Notes, not in acceptance criteria or task lists.

### Story sizing guidance

| Complexity | Effort | What fits |
|------------|--------|-----------|
| XS | < 2h | Config change, adding a field, tiny refactor |
| S | ~half day | Single endpoint with tests, simple UI component |
| M | ~1 day | New service + repository + endpoint + tests |
| L | ~2 days | New bounded context stub, complex multi-step flow |
| XL | > 2 days | **Must be split — never generate an XL story** |

### Priority system

Priority is NOT "must-have vs nice-to-have" — every story in an iteration is
intentional. Priority indicates **dependency ordering** within the iteration:

| Priority | Meaning |
|----------|---------|
| P1 — critical | Foundation. No dependencies on other stories in this iteration. Can start immediately. |
| P2 — high | Depends on one or more P1 stories. Start after foundations are merged. |
| P3 — medium | Depends on P2 stories. Builds on top of established features. |
| P4 — low | Final layer. Depends on most other stories. Polish, integration, or UX. |

Assign priority based on the **dependency graph**, not perceived importance.
Every story in an accepted iteration is worth building.

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

- `BR-001-user-registration-and-login.md`
- `BR-002-restaurant-listing-and-detail.md`
- `BR-007-ci-pipeline.md`

Story IDs are **sequential and never recycled**. If Iteration 7 ended at BR-702,
Iteration 8 starts at BR-801.

### Story file template

Write one `.md` file per story using this exact template:

```markdown
# [PREFIX]-[NNN]
## [Story Title — concise, descriptive, business-readable]

Complexity: [XS | S | M | L]  ·  Module: [e.g. Identity | Catalog | Ordering | Delivery | Infra] <span style="float: right;">**[P1 | P2 | P3 | P4] — [critical | high | medium | low]**</span>

---

**BUSINESS GOAL**

[2–4 sentences explaining WHY this story exists and what value it delivers. Written from a product perspective, not a technical one. Answer: "What breaks or is impossible if we don't build this?"]

---

**DEPENDENCIES**

[List of story IDs this depends on with a brief reason, or "None — this is a foundation story."]

---

**ACCEPTANCE CRITERIA**

✓ [Criterion 1 — observable behavior]
✓ [Criterion 2 — observable behavior]

---

**SCENARIOS**

> **Given** [preconditions]
> **When** [action / event]
> **Then** [expected outcome]

> **Given** [preconditions]
> **When** [action / event]
> **Then** [expected outcome]

---

**TECHNICAL NOTES**

[Implementation guidance for the developer using `code` formatting for technical terms. No task checklists; the implementer decides the exact steps.]

---

**EDGE CASES**

⚠ [Edge Case 1 — description of what happens and expected decision]
⚠ [Edge Case 2 — description of what happens and expected decision]

---

**TESTING REQUIREMENTS**

✓ [Requirement 1 — unit, integration, frontend, or security]
✓ [Requirement 2 — unit, integration, frontend, or security]

---

**OBSERVABILITY**

[What should be logged, monitored, or alerted on. If nothing new, "No new observability requirements — existing logging is sufficient."]

---

**RISKS**

⚠ Risk: [Description of risk and its mitigation]

---

**REFACTOR TRIGGERS**

⚠ Refactor when: [Description of trigger condition to pay off debt or scale up]
```

### Writing guidance for each section

**Business goal:** Write as if explaining to a non-technical product owner. No
jargon. No class names. "Users need to save a delivery address so orders can
be delivered" — not "Add address fields to User entity."

**Acceptance criteria:** Each criterion should be verifiable without reading the
source code. "Expired tokens return 401, not 500" is good. "Use `@PreAuthorize`
on the controller" is not — that's an implementation detail for Technical Notes.

**Scenarios:** Use Given/When/Then rigorously. Each scenario should test ONE
behaviour. Cover at least: the happy path, one invalid-input path, one
auth/permissions path.

**Technical notes:** Provide the schema, the key architectural decisions, the
library choices. Reference existing patterns in the codebase. Do NOT write a
step-by-step task list — the planning and task-creation skills handle that.

**Edge cases:** Think adversarially. What happens with null input? Concurrent
requests? Missing foreign keys? Network failures? Stale data? State each case
and the expected outcome.

**Testing requirements:** Name the categories and what matters, not the test
methods. "Integration: full HTTP cycle with auth, assert response shape" is
useful. "Write `AdminOrderControllerTest.testAssignDriver()`" is too
prescriptive.

**Risks:** Be honest. If you're accepting technical debt, name it. If there's a
scalability concern, document it. If there's a security gap, flag it. This
section is the future engineer's safety net.

---

## Phase 7 — Draft Review

After generating all story files, produce a summary table in the conversation:

```
## Iteration N — Story Summary

| ID | Title | Complexity | Module | Priority |
|----|-------|-----------|--------|----------|
| BR-801 | User profile and address management | M | Identity | P1 — critical |
| BR-802 | Checkout address gate | S | Ordering | P2 — high |
| ...  | ... | ... | ... | ... |

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
| Behaviour over implementation | Acceptance criteria describe what the system does, never how it's built |
| Surface decisions explicitly | Never silently choose between cleanup-first vs feature-first — make the user decide |
| Bounded context discipline | If a story causes a service to reach into another context's repository, call it out |
| Cleanup is never optional to mention | If structural issues exist, always surface them as an explicit option |
| Tests are requirements, not tasks | Testing requirements describe what coverage is needed; the implementer decides how |
| IDs are sequential | Never recycle or reuse a story ID |
| Priority = dependency order | P1 stories have no deps; P2 depends on P1; P3 on P2; P4 on P3 |
| Assumptions before stories | Never assume silently on bounded context ownership, table naming, service boundaries, or story prefix |
| Review gate before writing | Never write story files before Phase 4 assumption confirmation |
| Technical notes, not task lists | Stories provide implementation guidance, not step-by-step checklists |
| Edge cases are first-class | Every story must consider failure modes, boundary conditions, and limitations |

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
