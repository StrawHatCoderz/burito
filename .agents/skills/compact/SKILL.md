---
name: compact
description: >
  Compress the current working session into a continuation package optimised for
  future AI sessions. Use this skill whenever the user says "compact the session",
  "create a handoff", "checkpoint this session", "save context", "hand off to
  another model", or when the conversation context is becoming large and needs to
  be preserved before a major change or new iteration begins. The goal is not to
  summarise — it is to preserve decision-making context so a future session can
  continue immediately without reconstructing previous discussions.
---

# Compact Skill

Compress the current session into `docs/context/session-handoff.md` — a
continuation package optimised for future AI sessions.

The objective is **not** to summarise the conversation.
The objective is to **preserve decision-making context**.

A future session reading only the output document should be able to continue
work immediately, with minimal additional explanation from the user.

Do not write the handoff file until Phase 8 is complete and the user has
confirmed the draft.

---

## Phase 1 — Capture the User Goal

Identify and record:

- **Original objective** — what the user wanted when the session started
- **Current objective** — what the work evolved into (may differ)
- **Desired outcome** — what "done" looks like for the user

Document both long-term goals (e.g. "build a food delivery platform") and
immediate goals (e.g. "finish iteration planning for the cart feature").

---

## Phase 2 — Capture Project Understanding

Record only the information a future session needs to continue without
re-reading the whole codebase:

- Product purpose — one sentence
- Tech stack — concise, not exhaustive
- Architecture — how layers connect; request flow summary
- Relevant bounded contexts — which contexts were touched this session
- Important conventions — naming, file layout, commit style, test style

> Keep this section short. It supplements `project-context.md`, it does not
> replace it. If `docs/context/project-context.md` exists, reference it rather
> than duplicating it.

---

## Phase 3 — Capture Decisions Made

Record every significant decision reached during the session.

Include:

- Architectural decisions
- Technology choices
- Naming and numbering conventions (e.g. story ID prefix)
- Repository structure decisions
- Testing strategy decisions
- Scope decisions (what was explicitly ruled in or out)

**Do not record:**

- Exploratory brainstorming that was abandoned
- Options that were considered but rejected (unless the rejection rationale is
  important for future decisions)
- Temporary implementation details

Format each decision as:

```
**[Topic]:** [Decision taken] — [one-line rationale if non-obvious]
```

---

## Phase 4 — Capture Constraints

Document every constraint that will influence future work:

| Type | Constraint |
|------|-----------|
| Business | [e.g. "No payment integration this iteration"] |
| Technical | [e.g. "Java 25, Spring Boot 4.x — no downgrade"] |
| Convention | [e.g. "Google Checkstyle enforced on all Java"] |
| Infrastructure | [e.g. "PostgreSQL only — no additional datastores"] |
| Scope | [e.g. "Backend-only for iteration 2"] |

Constraints often make future decisions obvious.
Losing them forces future sessions to rediscover them the hard way.

---

## Phase 5 — Capture Progress

Classify all work touched this session into three buckets:

### Completed

Work that is finished, committed, and not expected to change.

### In Progress

Work that was started but not finished — include the specific stopping point.

### Blocked

Work that cannot proceed — include the blocker and what is needed to unblock it.

---

## Phase 6 — Capture Assumptions

Separate assumptions into two groups:

### Confirmed Assumptions

Assumptions the user explicitly validated during the session.

### Open Assumptions

Assumptions that were made but not yet validated.

> Future sessions **must** revisit open assumptions before acting on them.
> Flag them prominently.

---

## Phase 7 — Capture Remaining Work

Document what has not yet been done:

- Outstanding story cards (with IDs if assigned)
- Pending implementation tasks
- Missing documentation
- Decisions that still need to be made
- Next iteration candidates (features not yet planned)

Where story cards exist, list them in priority order.

---

## Phase 8 — Draft Review

Before writing the file, present the full draft handoff to the user in the
conversation and ask:

> "Here is the session handoff draft. Does this capture everything correctly?
> Any decisions, constraints, or open questions I've missed before I write the
> file?"

If the user requests corrections, update the relevant section(s) and re-present.

Once the user agrees (or says "looks good", "write it", "go ahead"), proceed to
Phase 9.

---

## Phase 9 — Write the Handoff File

### Output location

Determine the file path:

1. If `docs/context/session-handoff.md` already exists → read it first, merge
   still-valid information, remove obsolete sections, then overwrite with the
   merged result
2. If `docs/context/` exists → write `docs/context/session-handoff.md`
3. Otherwise → create `docs/context/session-handoff.md`

> Do not blindly overwrite a previous handoff. Preserve decisions and
> constraints that are still valid. Remove only what is genuinely obsolete.

### Document template

```markdown
# Session Handoff

## User Goal

[Original and current objective. What does "done" look like?]

---

## Current Objective

[The specific thing being worked on right now, if different from the long-term goal.]

---

## Project Overview

**Product:** [One-sentence product description]
**Context file:** `docs/context/project-context.md` *(read this for full detail)*
**Repo:** [Monorepo / single-service / etc.]
**Stack:** [Backend: X. Frontend: Y. DB: Z. Infra: W.]

---

## Architecture Summary

[3–5 sentences. How layers connect. Key patterns. Any bounded context boundaries
that matter for the remaining work.]

---

## Decisions Made

[List using the format: **[Topic]:** [Decision] — [rationale if non-obvious]]

---

## Constraints

| Type | Constraint |
|------|-----------|
| | |

---

## Completed Work

[List completed items — be specific: file names, story IDs, endpoints, etc.]

---

## Work In Progress

[What was started but not finished. Include the exact stopping point.]

---

## Blocked Work

[What cannot proceed. What is needed to unblock it.]

---

## Confirmed Assumptions

[Assumptions the user explicitly validated.]

---

## Open Assumptions

> ⚠️ These must be confirmed before acting on them in a future session.

[Unvalidated assumptions. Flag each one clearly.]

---

## Remaining Work

[Outstanding stories, tasks, decisions, and next iteration candidates — in
priority order where known.]

---

## Risks

[Known risks to the remaining work — ranked by impact.]

| Risk | Impact | Mitigation |
|------|--------|-----------|
| | | |

---

## Recommended Next Actions

[Ordered list of the 3–5 most important things to do next, with enough context
to act on them immediately.]

1. [Action 1]
2. [Action 2]
3. [Action 3]

---

## Recommended Next Prompt

[A ready-to-paste prompt that a future AI session can use to continue work
immediately. Include: what was last done, what to do next, any critical
constraints or open assumptions to surface first.]

```
[Paste this at the start of the next session]

We are building [product name]. The last session [brief summary of what was done].

The immediate next task is: [specific next action].

Before starting, please:
1. Read `docs/context/project-context.md` for full project context.
2. Read `docs/context/session-handoff.md` for session decisions and progress.
[Any other critical files to read first]

Key constraints for this session:
- [Constraint 1]
- [Constraint 2]

Open assumptions that need confirming before proceeding:
- [Assumption 1]
```

---

## Generated On

[ISO 8601 date — YYYY-MM-DD HH:mm]
[Git branch and commit hash at time of compaction]
```

---

## Phase 10 — Confirm and Close

After writing the file, confirm to the user:

> "Session handoff written to `docs/context/session-handoff.md`.
>
> To continue this work in a new session, paste the **Recommended Next Prompt**
> at the start of the conversation."

---

## Compaction Rules

**Keep:**

- Decisions (architectural, scope, naming, numbering)
- Constraints (business, technical, convention)
- Architecture understanding
- Confirmed and open assumptions
- Plans and story card references
- Progress (completed / in-progress / blocked)
- Open questions

**Remove:**

- Greetings and small talk
- Duplicate or repeated discussions
- Abandoned approaches and exploratory dead ends
- Repeated explanations of the same concept
- Anything a future session can re-derive in under 30 seconds

---

## Information Priority

Write the document in this priority order — highest value information first:

| Priority | Content |
|----------|---------|
| 1 — Critical | User goals, accepted decisions, constraints |
| 2 — High | Architecture, open assumptions, blocked work |
| 3 — Medium | Progress, risks, planned work |
| 4 — Low | Discussion history (omit unless a decision depends on it) |

---

## Key Principles

| Principle | Rule |
|-----------|------|
| Continuation over history | Optimise for future sessions, not historical record |
| Decisions only | Only capture resolved decisions, not brainstorming |
| Flag open assumptions | Never silently carry forward an unvalidated assumption |
| Merge, don't overwrite | Read any existing handoff before writing |
| Short and dense | Every sentence must earn its place |
| Review before writing | Never write the file before Phase 8 user confirmation |

---

## Edge Cases

**No previous session context:**
Write the handoff from the current conversation alone. Note in the document that
this is the first handoff for this project.

**Handoff file already exists:**
Read it first. Identify sections that are still accurate vs. now obsolete.
Merge valid decisions and constraints. Update progress. Remove obsolete sections.
Do not blindly overwrite.

**Session covered multiple unrelated topics:**
Create one section per topic in Completed / In Progress / Remaining Work.
Keep the Recommended Next Prompt focused on the single most important next action.

**No decisions were made this session:**
Write the handoff anyway — capture the project state, constraints, and open
assumptions. A handoff with no decisions is still useful as a checkpoint.
