# Brainstorm: Contact Creation UI Redesign — M3 Polish

**Date:** 2026-04-15
**Status:** Ready for planning

## What We're Building

Redesign the contact creation screen from "functional but ugly" to "polished, Google Contacts-quality" following Material 3 guidelines. The current UI has no section headers, no dividers, flat hierarchy, wrong top bar icons, and inconsistent spacing.

## Current Problems (from audit)

| Problem | Impact |
|---------|--------|
| No section headers or dividers | Zero visual hierarchy — everything is a flat list |
| Back arrow + Check icon in top bar | Wrong pattern — should be Close (X) + "Save" text |
| LargeTopAppBar (collapsing) | Over-designed — flat TopAppBar is standard for editors |
| 96dp photo circle | Too small, no visual impact |
| TextFields stacked with zero spacing | Fields blur together |
| No spacing between sections | No breathing room |
| Inconsistent icon alignment (Top vs Center) | Visual jank |
| No empty states or hints | Confusing when sections are empty |
| "More fields" uses AnimatedVisibility card | Should be simple text button at 72dp |
| AOSP field sort order not matched | Unexpected field arrangement |

## Key Decisions

| Decision | Choice | Reference |
|----------|--------|-----------|
| Photo style | 120dp centered circle with `surfaceContainerLow` background strip | Samsung Contacts pattern |
| Section grouping | `titleSmall` headers in `primary` color + `HorizontalDivider` between sections | Google Contacts + M3 form spec |
| Top bar | Flat `TopAppBar` with Close (X) + text "Save" button | Google Contacts, M3 editor standard |
| AppBar style | Flat `TopAppBar` (not collapsing `LargeTopAppBar`) | AOSP pattern |
| More fields | Text button at section boundary, 72dp start, binary toggle | Google Contacts |
| Field sort order | Match AOSP: Name→Nickname→Org→Phone→SIP→Email→Address→IM→Website→Event→Relation→Note→Groups | AOSP `MimeTypeComparator` |
| Type selector | Keep `FilterChip` with dropdown | M3-native, current impl works |
| Field variant | `OutlinedTextField` (keep current) | M3 form spec |
| Icon column | 40dp (24dp icon + 16dp gap). First field only shows icon. | M3 form spec |
| Field spacing | 8dp between fields in same section, 24dp between sections | M3 form spec |
| Keyboard | `imePadding()`, `ImeAction.Next` chain, auto-focus first field | M3 form best practice |

## Design Spec

### Layout Structure (top to bottom)

```
TopAppBar (flat, 64dp)
  ├─ Close (X) icon
  ├─ "Create contact" title
  └─ "Save" TextButton

Column(verticalScroll) + imePadding
  ├─ Photo Section (120dp circle, surfaceContainerLow bg, 24dp vertical padding)
  ├─ HorizontalDivider(outlineVariant)
  ├─ Account Chip (16dp horizontal padding, 12dp vertical)
  ├─ HorizontalDivider(outlineVariant)
  │
  ├─ SectionHeader("Name") — titleSmall, primary, 56dp start
  ├─ FieldRow(Person icon) → First name OutlinedTextField
  ├─ FieldRow(null)        → Last name OutlinedTextField
  ├─ 24dp spacer
  │
  ├─ SectionHeader("Phone")
  ├─ FieldRow(Phone icon) → phone + TypeChip + delete
  ├─ AddFieldButton("Add phone") — 56dp start
  ├─ 24dp spacer
  │
  ├─ SectionHeader("Email")
  ├─ FieldRow(Email icon) → email + TypeChip + delete
  ├─ AddFieldButton("Add email")
  ├─ 24dp spacer
  │
  ├─ SectionHeader("Address") [if visible]
  ├─ ... address fields
  ├─ 24dp spacer
  │
  ├─ "More fields" TextButton (72dp start, primary color)
  │   [expands to show: Nickname, Org, SIP, IM, Website, Event, Relation, Note]
  │
  ├─ SectionHeader("Groups") [if groups available]
  ├─ ... group checkboxes
  │
  └─ 48dp bottom padding
```

### Reusable Components

**SectionHeader:**
```
titleSmall typography, primary color
Padding: start=56dp (aligned with field text past icon), top=24dp, bottom=8dp
```

**FieldRow:**
```
Row(16dp horizontal padding, 4dp vertical padding)
  ├─ Box(40dp width) → Icon 24dp or empty
  ├─ OutlinedTextField(weight=1)
  └─ Optional trailing (TypeChip, delete IconButton)
Only first field in section shows icon. Subsequent fields: empty icon slot.
```

**AddFieldButton:**
```
TextButton, padding start=56dp (aligned with field text)
Icon(Add, 18dp) + 8dp spacer + Text(labelLarge)
Color: primary
```

### Color Token Mapping

| Element | Token |
|---------|-------|
| Section header text | `primary` |
| Field label (focused) | `primary` |
| Field outline (focused) | `primary`, 2dp |
| Field outline (resting) | `outline`, 1dp |
| Leading icon | `onSurfaceVariant` |
| Field text | `onSurface` |
| Placeholder | `onSurfaceVariant` |
| Dividers | `outlineVariant` |
| "Add field" text | `primary` |
| Delete icon | `onSurfaceVariant` |
| Photo bg strip | `surfaceContainerLow` |

### Animation Spec

| Animation | Spec |
|-----------|------|
| Field add/remove | `animateItemIfMotionAllowed()` with spring (existing) |
| More fields expand | `AnimatedVisibility(expandVertically + fadeIn)` with spring |
| Photo shape morph | Existing spring animation on press (keep) |
| Keyboard push | `imePadding()` on Column |
| Section header appear | None — static |
| Discard dialog | Default M3 AlertDialog animation |

## What Changes from Current Code

| Component | Current | New |
|-----------|---------|-----|
| TopAppBar | `LargeTopAppBar`, back arrow, check icon | `TopAppBar` (flat), Close (X), "Save" text |
| Photo | 96dp circle, plain | 120dp circle, surfaceContainerLow bg strip |
| Sections | No headers, no dividers | `SectionHeader` + `HorizontalDivider` |
| Field layout | Direct Row with icon | `FieldRow` composable with 40dp icon column |
| Icon alignment | Inconsistent (Top/Center) | Always `CenterVertically` in FieldRow |
| Spacing | None between sections | 24dp between sections, 8dp between fields |
| Field sort | Random-ish | Match AOSP MimeTypeComparator order |
| More fields | AnimatedVisibility card | TextButton at 72dp, binary expand |
| Keyboard | No imePadding | `imePadding()` on scroll container |
| Scroll | LazyColumn | `Column(verticalScroll)` — simpler for form |

### LazyColumn → Column Decision

The M3 form spec and research suggest `Column(verticalScroll)` is better for forms with <30 fields because:
- TextFields maintain focus state correctly
- No recomposition issues with state hoisting
- IME padding works more reliably
- Simpler code

We have ~15 visible fields (more with expanded). `Column` is the right choice.

## Open Questions

None — ready for planning.
