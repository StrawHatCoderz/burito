---
name: issue-context
description: >
  Requirements gathering and codebase context skill. Given a story card ID (e.g. BR-003),
  reads the requirements file from requirements/<story_id>.md, surveys the codebase
  structure and relevant files, and produces a structured context summary ready for
  planning. Used by the user-story-implementation orchestrator — load this skill at the
  start of Phase 0 and follow it completely before returning.
---

# Issue Context Skill

Gather everything needed to understand a story card and the codebase it lives in.
Produce a structured **Context Summary** and signal when it is ready for the planning phase.

Do not proceed to planning until all steps below are complete.

---

## Step 1 — Read the Requirements File

```bash
cat requirements/<story_id>.md
```

If the file does not exist:
- Stop immediately and tell the user:
  > "Could not find `requirements/<story_id>.md`. Please check the story ID and confirm
  > the file exists before proceeding."
- Do not guess or continue with a missing file.

The requirements file contains the full story card: title, description, acceptance
criteria, and any other context the team has written. Read it in full before doing
anything else.

### 1.1 Linked stories

Scan the requirements file for references to other story IDs (e.g. `BR-001`, `BR-002`).
If any are found, read those files too:

```bash
cat requirements/<linked_story_id>.md
```

Note the relationship (blocks / blocked-by / related / depends-on) for each.

---

## Step 2 — Check for Embedded Images

Requirements files may contain inline images or image links (Figma URLs, local asset
paths, or markdown image syntax). If any are present:

- For **local image paths** (e.g. `requirements/assets/br-003-mockup.png`), read them
  directly with the `view` tool.
- For **external URLs** (Figma, Notion, etc.), note their presence and ask the user
  to share the image content directly if it's needed for context.
- If no images are present, skip this step silently.

After reading any images, write a one-line caption for each:

```
Images:
  1. br-003-mockup.png  — Figma mockup of the new menu item card layout
  2. br-003-flow.png    — user flow diagram showing add-to-cart interaction
```

---

## Step 3 — Survey the Codebase

Survey the repository so the planning phase can reference real files and patterns rather
than speaking generically.

### 3.1 Repo structure

```bash
find . -maxdepth 3 -not -path '*/\.*' -not -path '*/node_modules/*' | head -80
```

### 3.2 Relevant files

Search for files, functions, types, and modules related to the story's domain keywords:

```bash
grep -r "<domain keyword>" --include="*.java" -l    # adapt extension to tech stack
rg "<domain keyword>" -l                             # if ripgrep is available
```

Read the most relevant files (or key sections) to understand current behaviour.

### 3.3 Test framework and conventions

```bash
find . -name "*.test.*" -o -name "*.spec.*" -o -name "*Test.java" | head -20
```

Note: test runner (JUnit / pytest / jest / etc.), file naming pattern, assertion style,
and where test files live relative to source files.

### 3.4 Tech stack

Read the project's dependency manifest(s):

```bash
cat pom.xml        # Maven
cat build.gradle   # Gradle
cat package.json   # Node
cat requirements.txt
```

### 3.5 Existing documentation

```bash
ls docs/
# Read any ADRs, architecture notes, or prior context files that touch this story's domain
```

---

## Step 4 — Produce the Context Summary

Write a structured summary that will be handed to the planning sub-skill as its starting
input. Every section must be concrete — file names, not generalities.

```
## Context Summary: <story_id> — <Title>

### Story Card
- **File:** requirements/<story_id>.md
- **Status:** (as stated in the requirements file, if present)

### What the story asks for
<2–4 sentences in your own words>

### Acceptance Criteria (from requirements file)
<copy the acceptance criteria verbatim from the requirements file>

### Linked stories
| ID | Title | Relationship |
|----|-------|-------------|

### Images
| File | What it shows |
|------|--------------|

### Codebase findings
| Area              | Detail |
|-------------------|--------|
| Repo structure    | ... |
| Relevant files    | ... |
| Test framework    | ... |
| Tech stack        | ... |
| Relevant docs     | ... |

### Initial observations
<Anything notable about the code that bears on this story:
patterns to follow, potential risk areas, existing similar implementations>
```

---

## Step 5 — User Confirmation and Save

Present the full Context Summary to the user and ask:
> "Does this context look accurate? Reply **Agreed** to proceed to planning, or let me know what needs correcting."

If the user requests corrections, update the relevant section(s) and re-present before asking again.

Once the user agrees, write the confirmed Context Summary to disk:

```bash
# docs/<story_id>/ must already exist (created by setup-story-dir.sh in Phase 0a)
# Write the full Context Summary markdown to docs/<story_id>/context.md
```

Confirm to the user:
> "Context saved to `docs/<story_id>/context.md`. Ready to begin planning."

Then return to the `user-story-implementation` orchestrator — Phase 0 is complete.
