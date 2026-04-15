---
title: "refactor: UI redesign — M3 polish, section headers, proper spacing"
type: refactor
status: done
date: 2026-04-15
origin: docs/brainstorms/2026-04-15-ui-redesign-m3-brainstorm.md
---

# UI Redesign Plan

## File Changes (SDD Order)

### 1. String resources
- **`res/values/strings.xml`** — Add section header strings: Name, Phone, Email, Address, Organization, Groups, Save

### 2. TestTags
- **`TestTags.kt`** — Add tags for: `CLOSE_BUTTON` (replaces `BACK_BUTTON`), `SAVE_TEXT_BUTTON`, section headers, dividers, `PHOTO_BG_STRIP`

### 3. Tests (update first)
- **`ContactCreationEditorScreenTest.kt`** — Update: Close icon tag, Save TextButton tag, assertions for section headers existence

### 4. Reusable components (new file)
- **`component/SharedComponents.kt`** — `SectionHeader`, `FieldRow`, `AddFieldButton` composables

### 5. ContactCreationEditorScreen.kt
- `LargeTopAppBar` -> flat `TopAppBar`
- Back arrow -> Close (X) icon
- Check icon -> `TextButton("Save")`
- `LazyColumn` -> `Column(verticalScroll)` + `imePadding()`
- Add `SectionHeader` before each section
- Add `HorizontalDivider` between photo/account and fields
- Add 24dp spacing between sections
- Reorder: Name->Phone->Email->Address->(more fields)->Groups

### 6. PhotoSection.kt
- 96dp -> 120dp circle
- Add `surfaceContainerLow` background strip (full-width, 168dp)
- Center circle in strip
- Update downsample size

### 7. NameSection.kt
- Use `FieldRow` with Person icon on first field only
- 8dp spacing between fields

### 8. PhoneSection.kt
- Use `FieldRow` with Phone icon on first field only
- Use `AddFieldButton` at 56dp start
- 8dp spacing between fields

### 9. EmailSection.kt
- Same pattern as Phone

### 10. AddressSection.kt
- Use `FieldRow` with Place icon on first field only
- Use `AddFieldButton`
- Convert from LazyListScope to @Composable `AddressSectionContent`

### 11. OrganizationSection.kt
- Use `FieldRow` with Business icon on first field only
- Convert from LazyListScope to @Composable `OrganizationSectionContent`
- Moved into MoreFields section (AOSP pattern)

### 12. MoreFieldsSection.kt
- TextButton at 56dp start, primary color
- Convert from LazyListScope to @Composable `MoreFieldsSectionContent`
- Includes: Nickname, Note, SIP, Organization, Events, Relations, IM, Website

### 13. GroupSection.kt
- Use `SectionHeader("Groups")`
- Remove inline header row
- Convert from LazyListScope to @Composable `GroupSectionContent`
- Use `FieldRow` with Label icon on first group

### 14. EventSection.kt, RelationSection.kt, ImSection.kt, WebsiteSection.kt
- Convert from LazyListScope to @Composable `*SectionContent`
- Use `FieldRow` with section-appropriate icon on first field only
- Use `AddFieldButton` at 56dp start

### 15. Preview file
- Update `ContactCreationPreviews.kt` for new signatures (LazyColumn -> Column where needed)

### 16. All tests
- Convert section tests from LazyColumn wrappers to direct @Composable calls
- Update EditorScreenTest for Close/Save tags and new section header/divider assertions
- Update FlowTest for new Save button tag
- Fix AddressSectionTest delete test (needs 2 addresses for delete button visibility)

## Acceptance Criteria

- [x] Flat `TopAppBar` with Close (X) + "Save" TextButton
- [x] 120dp photo circle with `surfaceContainerLow` strip
- [x] `SectionHeader` before Name, Phone, Email, Address, Groups
- [x] `HorizontalDivider` after photo and after account chip
- [x] 40dp icon column, first-field-only icon in every section
- [x] 8dp between fields, 24dp between sections
- [x] `Column(verticalScroll)` + `imePadding()` instead of `LazyColumn`
- [x] "More fields" as TextButton at 56dp start
- [x] AOSP field order: Name->Phone->Email->Address->(more)->Groups
- [x] All existing tests pass
- [x] ktlint + detekt clean
- [x] Build passes
