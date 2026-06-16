# Burito Design System

This document outlines the core design tokens and constraints for Burito. Every frontend component must strictly adhere to these values. Do not introduce new colors, spacing, or arbitrary values without updating this system.

## 1. Color Tokens
Burito's palette is warm, appetizing, and solid. We avoid cliché fast-food red/yellow and clinical SaaS blue/grey.

- `--color-bg-primary`: `#FFF9F5` (Warm cream, appetizing base)
- `--color-bg-surface`: `#FFFFFF` (Clean white for cards, high contrast)
- `--color-text-primary`: `#2D2327` (Deep aubergine/charcoal; warmer than pure black)
- `--color-text-muted`: `#8C7C83` (Muted earth tone for secondary text/placeholders)
- `--color-accent`: `#D34A24` (Harissa Red — deep, spicy red-orange for actions/badges)
- `--color-accent-subtle`: `#FDF2EE` (Very light tint of Harissa Red for hover states)
- `--color-border`: `#EBE3DE` (Soft, warm border color)

## 2. Type Scale
- **Display/Headings:** `Outfit` (Friendly, modern, slightly geometric)
- **Body/UI:** `DM Sans` (Highly legible, professional, warm)

**Scale (Variable classes):**
- **Display**: 48px | Line-height: 1.1 | Weight: 700 (`text-5xl font-display font-bold leading-tight`)
- **H1**: 32px | Line-height: 1.2 | Weight: 600 (`text-3xl font-display font-semibold leading-tight`)
- **H2**: 24px | Line-height: 1.3 | Weight: 600 (`text-2xl font-display font-semibold leading-snug`)
- **Body**: 16px | Line-height: 1.5 | Weight: 400 (`text-base font-body leading-relaxed`)
- **Small**: 14px | Line-height: 1.5 | Weight: 400 (`text-sm font-body leading-relaxed`)
- **Label**: 12px | Line-height: 1.2 | Weight: 600 | Uppercase (`text-xs font-body font-semibold uppercase tracking-wider`)
- **Caption**: 12px | Line-height: 1.4 | Weight: 400 (`text-xs font-body leading-normal`)

## 3. Spacing & Radius
A strict 4px base grid.
- `4px`, `8px`, `12px`, `16px`, `24px`, `32px`, `48px`, `64px`

**Border Radius:**
- `--radius-sm`: `4px` (Inputs, small buttons, tags)
- `--radius-md`: `8px` (Standard buttons, dropdowns)
- `--radius-lg`: `16px` (Restaurant cards, modals)
- `--radius-xl`: `24px` (Large promotional banners, bottom sheets)

## 4. Signature Element
**"The Thermal Sizzle Loading State"**
Instead of using standard grey skeleton loaders, Burito uses a "Thermal Sizzle" effect. It is a subtle, slow-moving gradient mesh of warm oranges and reds (`#D34A24` blending into `#FDF2EE`) suggesting heat and freshness.

## Component Constraints
1. **No inline styles.** Use Tailwind utilities mapped to these variables.
2. **Interactive states are mandatory** (`hover:`, `focus-visible:`, `active:`).
3. **Empty states are real UI**, guiding users to the next action.
4. **Error messages** must explain the issue and the fix clearly.
5. **Phosphor Icons only** (or the project's agreed single icon library).
