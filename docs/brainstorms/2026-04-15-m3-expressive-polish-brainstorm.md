# M3 Expressive UI Polish — Contact Creation Screen

**Date:** 2026-04-15
**Status:** Ready for planning
**Predecessor:** `2026-04-15-ui-redesign-m3-brainstorm.md` (basic M3 layout — done)

## What We're Building

A comprehensive M3 Expressive visual polish pass on the contact creation screen to match the quality bar of Google's official Contacts app. This builds on the existing M3 layout (flat TopAppBar, 120dp photo, SectionHeader/FieldRow components) and adds expressive interactions, better field patterns, and a proper "add more info" chip grid.

## Why This Approach

The current implementation has correct M3 structure but lacks the polish that makes M3 Expressive feel premium:
- Save button is a plain TextButton (low emphasis)
- Remove buttons are generic close icons (not visually destructive)
- "More fields" is a toggle TextButton (not discoverable)
- No shape morphing on press (THE M3 Expressive signature)
- Photo picker uses a DropdownMenu (should be BottomSheet)
- No account footer bar
- Phone type selector is a separate FilterChip (should be in the field label)

Reference: Google Contacts screenshots showing FilledTonal save, red circle remove, chip grid for more info, "Saving to Device only" footer.

## Key Decisions

### 1. Save Button → FilledTonalButton
- Replace `TextButton("Save")` in TopAppBar actions with `FilledTonalButton`
- Add `shapes = ButtonDefaults.shapes()` for press shape morphing
- Disabled state when `!hasPendingChanges`

### 2. Remove Button → Red Outlined Circle
- Replace `IconButton(Icons.Filled.Close)` with `OutlinedIconButton` using `Icons.Outlined.Remove`
- Color: `error` tint on icon, `error` outline
- Vertically centered to the input field it removes
- Only shown when section has >1 field (existing behavior)

### 3. Phone Type in Field Label
- OutlinedTextField label shows `"Phone (Mobile)"` / `"Phone (Work)"` etc.
- Type is part of the label string, not a separate FilterChip
- Tapping the label area or a small dropdown indicator opens type selector
- Remove the separate FilterChip row below the phone field
- Same pattern for Email: `"Email (Personal)"` / `"Email (Work)"`
- Address keeps separate type selector (too complex to merge into label)

### 4. "Add More Info" → Chip Grid
Replace the `MoreFieldsSection` TextButton toggle with:

**Layout:**
```
        Add more info          ← centered titleSmall

  [✉ Email]    [📍 Address]   ← Tonal AssistChip in FlowRow
  [🏢 Org]     [📝 Note]       (secondaryContainer bg)
  [👥 Groups]  [⋯ Other]
```

**Behavior:**
- Tapping a chip adds that section to the form above and **animates the chip out** (shrink/fade with spring)
- "Other" chip opens a ModalBottomSheet with uncommon fields:
  - Significant date, Relationship, Instant messaging, Website, SIP address, Nickname
- Each item in the "Other" sheet is a ListItem with icon; tapping adds that section
- Chips preserve the field ordering from the original screen layout
- Once all fields are added, the entire "Add more info" section disappears
- Groups chip only shown when `availableGroups` is non-empty
- No "Labels" chip — only show chips for features that exist in our code
- **Auto-scroll + focus**: when a chip adds a section, smooth-scroll to it and request focus on the first field (opens keyboard via `FocusRequester`)
- Default visible sections: Name + Phone + Email (always visible, 1 empty field each). Chip grid for: Address, Org, Note, Groups, Other
- **Chip tap → action flow**: chip tap dispatches action → VM adds 1 empty field to list → section appears → auto-scroll + focus + keyboard
- **"Other" bottom sheet**: tapping an item closes sheet immediately and adds the section (no multi-select)
- **Organization empty check**: `company.isBlank() && title.isBlank()` — both must be blank for chip to appear
- **Note section**: just an OutlinedTextField (4-line min) with remove (-) button, no section header

### 5. Photo Section → Bottom Sheet
- Keep the 120dp tappable circle with shape morph animation
- Replace `DropdownMenu` with `ModalBottomSheet`
- Options: Take photo, Choose from gallery, Remove photo (only when photo exists)
- Title: "Contact photo"

### 6. Account Footer Bar (Visual Only)
- Bottom of the form: `Row` with "Saving to Device only" text + cloud-off icon + chevron
- Styled with `surfaceContainerLow` background, `bodySmall` text
- Tapping does nothing yet (Phase 2 will add ModalBottomSheet account picker)
- Shows `selectedAccount?.name ?: "Device only"`

### 7. Shape Morphing on All Interactive Elements
- `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` on all component files
- Add `shapes = ButtonDefaults.shapes()` to: FilledTonalButton (Save)
- Add `shapes = IconButtonDefaults.shapes()` to: Close IconButton, all Remove buttons, photo circle
- Spring animations via `MotionScheme.expressive()` in theme (currently missing from AppTheme)

### 8. "Add Phone/Email" CTA → Text Link
- Replace current TextButton with + icon → plain primary-colored `Text("Add phone")` / `Text("Add email")`
- Left-aligned below the last field in the section, matching Google Contacts style
- `clickable` modifier with ripple, no icon

### 9. Remove All HorizontalDividers
- Remove divider after photo section
- Remove divider after account chip
- Spacing alone provides visual separation (24dp between sections)
- Matches Google Contacts which uses no dividers

### 10. Plain Surface Background
- Entire screen uses `MaterialTheme.colorScheme.surface`
- Remove the `surfaceContainerLow` background strip behind photo section
- Visual hierarchy comes from OutlinedTextField borders and section spacing only

### 11. Field Spacing Refinement
- Between fields in same section: 8dp (keep)
- Between sections: 24dp (keep)
- Between "Add phone" CTA and next section: 16dp
- Chip grid horizontal gap: 8dp, vertical gap: 8dp
- Account footer: 16dp horizontal padding, 12dp vertical padding

### 12. MotionScheme Integration
- Add `MotionScheme.expressive()` to `AppTheme` `MaterialTheme` call
- Currently missing — CLAUDE.md says to use it but Theme.kt doesn't apply it
- Enables spring-based defaults for all M3 component animations

### 13. Cleanup Dead Animation Code
- Remove unused `gentleBounce()`, `smoothExit()`, `animateItemIfMotionAllowed()` from Theme.kt
- These were for LazyColumn which was replaced with Column(verticalScroll)

### 14. IME Keyboard Chaining
- Full chain across all visible fields: First → Last → Company → Phone → Email → ...
- Last visible field shows `ImeAction.Done`, all others show `ImeAction.Next`
- Implementation: `FocusRequester` per field + `focusProperties { next = ... }` + `keyboardActions { onNext = { nextRequester.requestFocus() } }`
- ViewModel maintains ordered list of active field IDs; EditorScreen maps to FocusRequesters
- When fields are added/removed dynamically, the chain updates
- Phone fields: `KeyboardType.Phone` (digits, +, *, #)
- Email fields: `KeyboardType.Email` (@ symbol, .com suggestion)
- Address fields: `KeyboardType.Text` (default)
- Note field: `ImeAction.Done` always (multiline)

### 15. Type-in-Label Interaction
- Tapping the label text area of the OutlinedTextField opens the type selector dropdown
- Small `▾` indicator appended to label: `"Phone (Mobile) ▾"`
- Dropdown anchored near the label position
- Same `DropdownMenu` + `DropdownMenuItem` pattern as current FieldTypeSelector, just triggered differently

### 17. Animation Specs
- **Chip exit**: `shrinkHorizontally() + fadeOut()` with `spring(dampingRatio = 0.7f, stiffness = StiffnessMediumLow)`. Other chips reflow via FlowRow layout.
- **Section enter**: `expandVertically(spring(StiffnessMediumLow)) + fadeIn()`. Same spec as existing MoreFields AnimatedVisibility. Consistent.
- **Section exit** (if removing via remove button): `shrinkVertically(spring(StiffnessMedium)) + fadeOut()`
- **Photo bottom sheet**: Default M3 `ModalBottomSheet` animation. With `MotionScheme.expressive()` in theme, it uses spring-based motion automatically.
- **Shape morphing**: Handled by `shapes` parameter on M3 components — no custom animation code.
- **All springs respect reduce motion**: When `ANIMATOR_DURATION_SCALE=0`, springs resolve instantly (framework behavior).

### 18. Performance
- **Chip visibility derivation**: Use `derivedStateOf` to wrap `uiState.emails.isEmpty()` etc. Only recomposes chip grid when field lists actually change.
- **FocusRequester chain**: Low concern — lightweight objects, small field count (<20). Rebuild chain on field add/remove is fine.
- **Concurrent chip animations**: Allow multiple chips to animate out simultaneously. Spring animations are GPU-accelerated. 6 chips max is trivial.
- **FlowRow reflow**: FlowRow handles layout changes efficiently. Chip removal triggers one reflow.

### 19. Accessibility
- **Reduce motion**: Let M3 framework handle it — `shapes` parameter respects `ANIMATOR_DURATION_SCALE=0` natively. No custom guard needed.
- **Type label dropdown**: Add `semantics { role = Role.DropdownList; contentDescription = "Phone type: Mobile. Double tap to change" }` to the tappable label area
- **Chip grid**: Each chip gets `contentDescription = "Add [field] section"` (e.g., "Add email section") for screen reader clarity
- **Remove button touch target**: 48dp minimum via `minimumInteractiveComponentSize()`. Visual icon is ~24dp but touch area stays accessible.
- **Photo circle**: `contentDescription = "Contact photo. Double tap to change"` with `role = Role.Button`

## Scope Summary

| Item | Complexity | Files Touched |
|------|-----------|---------------|
| Save → FilledTonalButton | Low | EditorScreen |
| Remove → red outlined circle | Low | PhoneSection, EmailSection, AddressSection, SharedComponents |
| Phone/Email type in label | Medium | PhoneSection, EmailSection, FieldTypeSelector |
| Chip grid "Add more info" | High | NEW: AddMoreInfoSection.kt, remove MoreFieldsSection.kt |
| Photo → bottom sheet | Medium | PhotoSection |
| Account footer bar | Low | EditorScreen |
| Shape morphing everywhere | Low | All component files |
| MotionScheme in theme | Low | Theme.kt |
| Dead code cleanup | Low | Theme.kt |
| "Add field" CTA → text link | Low | SharedComponents, all sections |
| Remove HorizontalDividers | Low | EditorScreen |
| Remove photo bg strip | Low | PhotoSection, EditorScreen |
| Auto-scroll + focus on chip tap | Medium | EditorScreen (ScrollState + FocusRequester) |
| IME keyboard chaining | Medium | All section components, EditorScreen (FocusRequester chain) |
| Keyboard types per field | Low | PhoneSection, EmailSection |

## What's NOT in Scope

- Country code prefix on phone fields (separate ticket — needs libphonenumber)
- Account picker ModalBottomSheet (Phase 2)
- Full-screen photo picker (Google proprietary)
- Grouped section cards (decided against — Google doesn't use them either)
- `MaterialExpressiveTheme` (alpha only, stick with `MaterialTheme`)

## Open Questions

_None — all resolved during brainstorm._

## Resolved Questions

| Question | Decision |
|----------|----------|
| Save button style | FilledTonalButton (matches Google) |
| Remove button style | Red outlined circle with minus icon, centered to field |
| More fields pattern | Chip grid with "Other" opening bottom sheet |
| "Other" chip contents | Uncommon fields only (date, relation, IM, website, SIP, nickname) |
| Top-level chips | Email, Address, Org, Note, Groups, Other (no Labels — only existing features) |
| Photo interaction | Bottom sheet (Take/Choose/Remove) |
| Country code prefix | Deferred to separate ticket |
| Grouped section cards | No — plain surface, grouping via headers + spacing |
| Account footer | Visual bar only, no picker logic yet |
| Shape morphing | Yes, all interactive elements, opt-in to Experimental API |
| Photo picker richness | Simple bottom sheet, not Google's proprietary full-screen picker |
| Chip animation on tap | Animate out (shrink/fade with spring), not instant disappear |
| Type selector interaction | Tap label text to open dropdown (not trailing icon, not separate chip) |
| Labels/Groups chip | Groups chip (when available), no Labels chip — only existing features |
| Star/favorite toggle | Deferred — not in this pass |
| Default visible sections | Name + Phone + Email always visible (1 empty field each). Chips for: Address, Org, Note, Groups, Other |
| Chip tap action | Adds 1 empty field → auto-scroll → focus → keyboard opens |
| "Other" sheet behavior | Tap item → close sheet immediately → add section |
| Org empty check | company.isBlank() && title.isBlank() |
| Note section | Just OutlinedTextField (4-line) + remove button, no section header |
| "Add field" CTA style | Plain primary text link "Add phone" — no + icon, matches Google |
| Dividers | Remove all HorizontalDividers — spacing only |
| Photo background strip | Remove surfaceContainerLow strip — plain surface throughout |
| Auto-scroll on chip tap | Yes + auto-focus first field of new section (FocusRequester + keyboard opens) |
| Screen background | Plain surface everywhere, no tinted regions |
| IME chaining | Full chain across all visible fields with FocusRequester list |
| Phone keyboard | KeyboardType.Phone |
| Email keyboard | KeyboardType.Email |
| Focus implementation | FocusRequester per field + focusProperties { next = ... } |
| Overflow menu (⋮) | Skip — Close (X) handles discard, no need for overflow |
| Reduce motion + shape morphing | Let M3 framework handle — no custom guard |
| Type label a11y | semantics { role = DropdownList } with descriptive contentDescription |
| Chip a11y | contentDescription = "Add [field] section" |
| Remove button touch target | 48dp minimum (minimumInteractiveComponentSize) |
| Photo size | 120dp (keep current) |
| Photo empty icon | Person silhouette + camera badge at bottom-right |
| Chip style | Tonal filled AssistChip (secondaryContainer bg). Disappears on tap, no checkmark. |
| "Add more info" text | Plain centered text label (titleSmall), NOT a chip |
| Account footer | Subtle text row on plain surface, onSurfaceVariant text, no tinted background |
| Chip exit animation | shrinkHorizontally + fadeOut with spring(0.7f, MediumLow) |
| Section enter animation | expandVertically + fadeIn with spring(MediumLow) |
| Photo sheet animation | Default M3 ModalBottomSheet (spring via MotionScheme) |
| M3 API availability | All needed APIs available in compose-bom 2026.03.01. @OptIn for shapes param accepted. |
| Chip reappears on section remove | Yes — removing last field in a section re-adds its chip. Symmetric. |
| Chip visibility state | Derived from field lists (no extra state). emails.isEmpty() → show Email chip. |
| Large font / long form | Photo scrolls out naturally (it's a list item, not a sticky header). No special handling. |
| RTL | Let Compose handle it — use start/end, AutoMirrored icons. No custom RTL code. |

## Implementation Order (Suggested)

1. Theme: MotionScheme + dead code cleanup
2. Save button → FilledTonalButton + Close button shape morphing
3. Remove button restyle (all sections)
4. Remove dividers + photo bg strip (plain surface throughout)
5. "Add field" CTAs → text link style (no + icon)
6. Phone/Email type-in-label migration
7. Photo section → bottom sheet + person icon with camera badge
8. Chip grid "Add more info" (biggest change — tonal chips, animations, auto-scroll+focus)
9. Account footer bar (visual only)
10. IME keyboard chaining (FocusRequester chain, keyboard types)
11. Final spacing/polish + accessibility pass
