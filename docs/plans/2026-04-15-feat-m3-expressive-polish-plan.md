---
title: "feat: M3 Expressive UI Polish — Contact Creation"
type: feat
status: active
date: 2026-04-15
deepened: 2026-04-15
origin: docs/brainstorms/2026-04-15-m3-expressive-polish-brainstorm.md
---

# feat: M3 Expressive UI Polish — Contact Creation

## Enhancement Summary

**Deepened on:** 2026-04-15
**Research agents used:** M3 Expressive skill, Compose test patterns, SDD workflow, best-practices, performance-oracle, architecture-strategist, code-simplicity-reviewer

### Key Improvements from Deepening
1. **Simplified state model:** Replaced `OptionalSection` enum + `Set` with 4 booleans + existing `Add*` actions (~60 LOC saved)
2. **Fixed framework-fighting:** Type-in-label replaced with trailing icon dropdown (standard M3 pattern)
3. **Performance fixes:** Removed `derivedStateOf` overhead, use `fadeOut()` only for chips, `focusManager.moveFocus()` instead of custom manager
4. **Fixed bugs:** ShowSection `.also` bug would silently lose state; wrong icon names (`Note` → `Notes`, `MoreHoriz` needs extended dep)
5. **Use MotionScheme tokens:** Replace hardcoded spring specs with `MaterialTheme.motionScheme.*`

### Critical Fixes from Reviews
- `.also {}` on `copy()` discards the inner copy — state updates lost silently
- `Icons.AutoMirrored.Filled.Note` doesn't exist → `Icons.Filled.Notes`
- `Icons.Filled.MoreHoriz` / `CalendarMonth` / `Language` / `Chat` need `material-icons-extended` dep — verify
- `derivedStateOf` with plain param (not `State<T>`) adds overhead with zero skip benefit
- `shrinkHorizontally` on FlowRow chips causes per-frame re-measure — use `fadeOut()` only
- `ModalBottomSheet` dismiss without `SheetState.hide()` causes janky instant disappear
- Account footer CloudOff + ExpandLess icons suggest interactivity that doesn't exist — just text

## Overview

Comprehensive M3 Expressive visual polish pass on the contact creation screen. Replaces basic M3 components with expressive variants (shape morphing, tonal buttons, chip grid, bottom sheets) to match the quality bar of Google's official Contacts app.

Builds on the completed M3 layout refactor (see `docs/plans/2026-04-15-refactor-ui-redesign-m3-plan.md`).

## Scope Verification

**Fields confirmed to exist in current `ContactCreationUiState`:**

| Field | Type | Default | Chip Grid? |
|-------|------|---------|-----------|
| nameState | NameState | NameState() | N/A — always visible |
| phoneNumbers | List\<PhoneFieldState\> | [1 empty] | N/A — always visible |
| emails | List\<EmailFieldState\> | [1 empty] | N/A — always visible |
| addresses | List\<AddressFieldState\> | [] | **Top-level chip** |
| organization | OrganizationFieldState | OrganizationFieldState() | **Top-level chip** |
| note | String | "" | **Top-level chip** |
| groups | List\<GroupFieldState\> | [] | **Top-level chip** (when available) |
| events | List\<EventFieldState\> | [] | "Other" bottom sheet |
| relations | List\<RelationFieldState\> | [] | "Other" bottom sheet |
| imAccounts | List\<ImFieldState\> | [] | "Other" bottom sheet |
| websites | List\<WebsiteFieldState\> | [] | "Other" bottom sheet |
| nickname | String | "" | "Other" bottom sheet |
| sipAddress | String | "" | "Other" bottom sheet |

**No new fields are being added.** Every chip/sheet item maps to an existing UiState field.

> **Brainstorm correction:** The brainstorm's chip grid diagram shows an Email chip, but the decision "Name + Phone + Email always visible" means Email should NOT be in the chip grid. The corrected chip grid is:
>
> ```
>         Add more info
>
>   [📍 Address]  [🏢 Org]
>   [📝 Note]     [👥 Groups]
>   [⋯ Other]
> ```

## State Model Change

> **Simplified from original plan** based on architecture + simplicity reviews. The original `OptionalSection` enum + `Set<OptionalSection>` was over-engineered. Existing `Add*` actions already handle repeatable fields. Only single-field sections need visibility booleans.

**Problem:** Single non-repeatable fields (Org, Note, Nickname, SIP) default to blank strings. The "derive visibility from field lists" approach works for repeatable fields (`addresses.isEmpty()`) but not for strings that start empty.

**Solution:** 4 boolean flags + existing `Add*` actions.

```kotlin
@Immutable
@Parcelize
data class ContactCreationUiState(
    // ... existing fields ...
    val showOrganization: Boolean = false,  // NEW
    val showNote: Boolean = false,          // NEW
    val showNickname: Boolean = false,      // NEW
    val showSipAddress: Boolean = false,    // NEW
    // REMOVE: val isMoreFieldsExpanded: Boolean = false,
) : Parcelable
```

**Derivation logic (computed properties on UiState — not in composable):**
```kotlin
// On UiState — keeps logic testable without Compose runtime
val showAddressChip: Boolean get() = addresses.isEmpty()
val showOrgChip: Boolean get() = !showOrganization && organization.company.isBlank() && organization.title.isBlank()
val showNoteChip: Boolean get() = !showNote && note.isBlank()
val showGroupsChip: Boolean get() = groups.isEmpty() && availableGroups.isNotEmpty()
val hasAnyChip: Boolean get() = showAddressChip || showOrgChip || showNoteChip || showGroupsChip || showOtherChip
val showOtherChip: Boolean get() = events.isEmpty() || relations.isEmpty() || imAccounts.isEmpty() ||
    websites.isEmpty() || (!showNickname && nickname.isBlank()) || (!showSipAddress && sipAddress.isBlank())
```

**Chip tap actions — reuse existing:**
- Address chip → dispatches `AddAddress` (adds 1 empty AddressFieldState, list becomes non-empty, chip disappears)
- Organization chip → dispatches new `ShowOrganization` action (sets `showOrganization = true`)
- Note chip → dispatches new `ShowNote` action
- Groups chip → dispatches existing `ToggleGroup` or new `ShowGroups`
- "Other" sheet items → same pattern: `AddEvent`, `AddRelation`, `AddIm`, `AddWebsite`, `ShowNickname`, `ShowSipAddress`

**Remove `ToggleMoreFields` action and `isMoreFieldsExpanded` from UiState.**

**Section visibility (in EditorScreen, reading UiState properties):**
```kotlin
// Repeatable: visible when list non-empty
val addressVisible = uiState.addresses.isNotEmpty()
// Single-field: visible when flag set OR field has content
val orgVisible = uiState.showOrganization || uiState.organization.company.isNotBlank() || uiState.organization.title.isNotBlank()
val noteVisible = uiState.showNote || uiState.note.isNotBlank()
```

**Chip reappears when:** user removes last field (repeatable) or taps remove on single-field section (sets `showX = false` and clears data).

> **Note on HideSection data clearing:** Removing a single-field section (Org, Note, etc.) clears the data. This is intentional — the remove (-) button is a "discard this section" action, not just "hide." User must deliberately tap remove.

## Implementation Phases

### Phase 1: Theme + Quick Visual Wins

**Files:** `Theme.kt`, `ContactCreationEditorScreen.kt`, `SharedComponents.kt`, `PhotoSection.kt`

#### 1a. MotionScheme Integration — `Theme.kt`

**SDD note:** Config change — exempt from test-first (not easily unit-testable). Verify visually.

- Add `MotionScheme.expressive()` to `MaterialTheme` in `AppTheme`
- Requires import: `import androidx.compose.material3.MotionScheme`
- Use `@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)` on Theme.kt (file-level, not per-function)

> **Research insight:** With `MotionScheme.expressive()` set, use `MaterialTheme.motionScheme.defaultSpatialSpec()` for layout animations and `MaterialTheme.motionScheme.fastEffectsSpec()` for fade/color. Do NOT hardcode spring specs — defeats the purpose of centralized motion tokens.

#### 1b. Dead Code Cleanup — `Theme.kt`

- Remove `gentleBounce()`, `smoothExit()`, `animateItemIfMotionAllowed()` — unused since LazyColumn → Column migration

#### 1c. Save Button — `ContactCreationEditorScreen.kt`

**Test first:** UI test asserting Save button is a `FilledTonalButton` (check via testTag + semantics).

```kotlin
// Before
TextButton(onClick = { onAction(Save) }, enabled = hasPendingChanges) {
    Text("Save")
}

// After
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
FilledTonalButton(
    onClick = { onAction(Save) },
    enabled = hasPendingChanges,
    shapes = ButtonDefaults.shapes(),
    modifier = Modifier.testTag(TestTags.SAVE_BUTTON)
) {
    Text("Save")
}
```

Update `TestTags.SAVE_TEXT_BUTTON` → `TestTags.SAVE_BUTTON` (or keep, it's just a tag name).

#### 1d. Close Button Shape Morphing — `ContactCreationEditorScreen.kt`

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
IconButton(
    onClick = { onAction(NavigateBack) },
    shapes = IconButtonDefaults.shapes(),
    modifier = Modifier.testTag(TestTags.CLOSE_BUTTON)
) {
    Icon(Icons.Filled.Close, contentDescription = "Cancel")
}
```

#### 1e. Remove HorizontalDividers — `ContactCreationEditorScreen.kt`

- Delete the two `HorizontalDivider()` calls after photo section and after account chip
- Spacing (24dp) between sections provides visual separation

#### 1f. Remove Photo Background Strip — `PhotoSection.kt`

- Remove the `surfaceContainerLow` background `Box`/`Surface` behind the photo circle
- Photo circle sits directly on plain `surface` background

#### 1g. "Add Field" CTA → Text Link — `SharedComponents.kt`, all section files

**Test first:** UI test asserting "Add phone" is rendered as text (not button with icon).

```kotlin
// Before (AddFieldButton)
TextButton(onClick = onAdd) {
    Icon(Icons.Filled.Add, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(8.dp))
    Text(text, style = labelLarge)
}

// After (AddFieldTextLink) — clickable BEFORE padding for proper ripple bounds
Text(
    text = text,
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.primary,
    modifier = Modifier
        .padding(start = 56.dp)
        .clickable(onClick = onAdd) // clickable after padding so ripple covers text only
        .padding(vertical = 4.dp)
        .testTag(testTag)
)
```

**Acceptance Criteria Phase 1:**
- [ ] `MotionScheme.expressive()` in AppTheme
- [ ] Dead animation code removed from Theme.kt
- [ ] Save button is `FilledTonalButton` with shape morphing
- [ ] Close button has shape morphing
- [ ] No `HorizontalDivider` on screen
- [ ] No colored background strip behind photo
- [ ] "Add phone/email" are plain text links, no + icon
- [ ] All existing tests pass (update tags/assertions as needed)
- [ ] `./gradlew build` clean

---

### Phase 2: Remove Button + Photo Bottom Sheet

**Files:** `PhoneSection.kt`, `EmailSection.kt`, `AddressSection.kt`, `SharedComponents.kt`, `PhotoSection.kt`, plus all MoreFields section files (Event, Relation, IM, Website)

#### 2a. Remove Button Restyle — `SharedComponents.kt` + all section files

**Test first:** UI test asserting remove button has error color + outlined style.

```kotlin
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun RemoveFieldButton(
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    OutlinedIconButton(
        onClick = onClick,
        shapes = IconButtonDefaults.shapes(),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag(/* existing tag */),
    ) {
        Icon(
            Icons.Outlined.Remove,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(18.dp),
        )
    }
}
```

- Replace all `IconButton(Icons.Filled.Close)` delete buttons in: PhoneSection, EmailSection, AddressSection, EventSection, RelationSection, ImSection, WebsiteSection
- Vertically centered to its adjacent OutlinedTextField via `Alignment.CenterVertically` on the Row

#### 2b. Photo Section → Bottom Sheet — `PhotoSection.kt`

**Test first:** UI test asserting bottom sheet appears on photo tap (check for sheet content testTags).

- Replace `DropdownMenu` with `ModalBottomSheet`
- Add person silhouette (`Icons.Filled.Person`) as default empty state icon
- Add small camera badge icon at bottom-right of circle (use `Box` with `align(BottomEnd)`)
- Sheet content — **must use `rememberModalBottomSheetState()` for smooth dismiss**:
  ```kotlin
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val scope = rememberCoroutineScope()
  fun dismissAndDo(action: () -> Unit) {
      scope.launch { sheetState.hide() }.invokeOnCompletion {
          if (!sheetState.isVisible) { showSheet = false; action() }
      }
  }

  if (showSheet) {
      ModalBottomSheet(
          onDismissRequest = { showSheet = false },
          sheetState = sheetState,
      ) {
          Text("Contact photo", style = titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
          ListItem(
              headlineContent = { Text("Take photo") },
              leadingContent = { Icon(Icons.Filled.CameraAlt, ...) },
              modifier = Modifier.clickable { dismissAndDo { onAction(RequestCamera) } }
                  .testTag(TestTags.PHOTO_SHEET_CAMERA)
          )
          ListItem(
              headlineContent = { Text("Choose from gallery") },
              leadingContent = { Icon(Icons.Filled.Image, ...) },
              modifier = Modifier.clickable { dismissAndDo { onAction(RequestGallery) } }
                  .testTag(TestTags.PHOTO_SHEET_GALLERY)
          )
          if (hasPhoto) {
              ListItem(
                  headlineContent = { Text("Remove photo") },
                  leadingContent = { Icon(Icons.Filled.Delete, ...) },
                  modifier = Modifier.clickable { dismissAndDo { onAction(RemovePhoto) } }
                      .testTag(TestTags.PHOTO_SHEET_REMOVE)
              )
          }
          Spacer(Modifier.navigationBarsPadding())
      }
  }
  ```
  > **Research insight:** Without `sheetState.hide()`, dismissal is instant (janky). The `invokeOnCompletion` pattern ensures sheet animates out before leaving composition. ModalBottomSheet tests need `waitUntil` because sheet animates in asynchronously.
- New TestTags: `PHOTO_BOTTOM_SHEET`, `PHOTO_SHEET_CAMERA`, `PHOTO_SHEET_GALLERY`, `PHOTO_SHEET_REMOVE`

**Acceptance Criteria Phase 2:**
- [ ] All remove (-) buttons are red outlined circles with minus icon
- [ ] Remove buttons vertically centered to their field
- [ ] 48dp minimum touch target on all remove buttons
- [ ] Photo tap opens ModalBottomSheet (not DropdownMenu)
- [ ] Empty photo shows person icon + camera badge
- [ ] Sheet has Take/Choose/Remove options (Remove only when photo exists)
- [ ] All existing tests updated + new tests for bottom sheet
- [ ] `./gradlew build` clean

---

### Phase 3: Type-in-Label Migration

**Files:** `PhoneSection.kt`, `EmailSection.kt`, `FieldTypeSelector.kt`

#### 3a. Phone Type in Label + Trailing Dropdown — `PhoneSection.kt`

> **Architecture review correction:** Making the label itself clickable fights the Compose TextField framework (label shrinks on focus, touch targets conflict, nested interactive semantics confuse TalkBack). Instead: type goes in the label text (decorative), trailing icon opens the dropdown (standard M3 pattern).

**Test first:** UI test asserting label text includes type name + trailing icon opens dropdown.

```kotlin
OutlinedTextField(
    value = phone.number,
    onValueChange = { onAction(UpdatePhone(phone.id, it)) },
    label = { Text("Phone (${selectedType.label(context)})") }, // decorative only
    trailingIcon = {
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.testTag(TestTags.phoneType(index))
        ) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Change phone type")
        }
    },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
    modifier = Modifier.weight(1f).testTag(TestTags.phoneField(index)),
)
// DropdownMenu anchored to this TextField
DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
    PhoneType.selectorTypes.forEach { type ->
        DropdownMenuItem(
            text = { Text(type.label(context)) },
            onClick = { onAction(UpdatePhoneType(phone.id, type)); expanded = false },
        )
    }
    DropdownMenuItem(
        text = { Text("Custom...") },
        onClick = { showCustomDialog = true; expanded = false },
    )
}
```

- Remove the separate `FieldTypeSelector` FilterChip row below the phone field
- The trailing ArrowDropDown icon is the standard M3 dropdown trigger — 48dp touch target, proper a11y
- TalkBack reads "Change phone type" on the icon button — no custom semantics needed

#### 3b. Email Type in Label + Trailing Dropdown — `EmailSection.kt`

Same pattern as phone. Label: `"Email (${emailType.label(context)})"`, trailing dropdown icon.

#### 3c. Address Keeps Separate Selector

No change for AddressSection — too many sub-fields to merge type into any single field's label.

**Acceptance Criteria Phase 3:**
- [ ] Phone label shows `"Phone (Mobile)"` (or current type) — type in label text
- [ ] Email label shows `"Email (Personal)"` (or current type)
- [ ] Trailing ArrowDropDown icon opens dropdown with type options
- [ ] Selecting type updates the field label
- [ ] Custom label option still works (opens CustomLabelDialog)
- [ ] No separate FilterChip row for phone/email types
- [ ] Address type selector unchanged
- [ ] Existing FieldTypeSelectorTest.kt updated/removed for phone/email (no longer uses FilterChip)
- [ ] All phone/email tests updated
- [ ] `./gradlew build` clean

---

### Phase 4: Chip Grid — "Add More Info"

**Files:** NEW `AddMoreInfoSection.kt`, NEW `OtherFieldsBottomSheet.kt`, `ContactCreationEditorScreen.kt`, `ContactCreationUiState.kt`, `ContactCreationAction.kt`, `ContactCreationViewModel.kt`, DELETE `MoreFieldsSection.kt`

This is the biggest change. SDD: tests → stubs → impl.

#### 4a. State Model Changes — `ContactCreationUiState.kt`, `ContactCreationAction.kt`

> **Simplified per architecture + simplicity reviews.** No `OptionalSection` enum. 4 booleans + existing `Add*` actions.

**Test first (ViewModel tests — highest SDD priority):**
- `ShowOrganization` sets `showOrganization = true`
- `HideOrganization` sets `showOrganization = false` and clears org fields
- `AddAddress` when list empty adds 1 field (existing behavior, verify)
- `ShowNote` / `HideNote` same pattern
- Process death: booleans survive SavedStateHandle round-trip

**UiState changes:**
```kotlin
@Immutable
@Parcelize
data class ContactCreationUiState(
    // ... existing fields ...
    val showOrganization: Boolean = false,  // NEW
    val showNote: Boolean = false,          // NEW
    val showNickname: Boolean = false,      // NEW
    val showSipAddress: Boolean = false,    // NEW
    // REMOVE: val isMoreFieldsExpanded: Boolean = false,
) : Parcelable {
    // Computed properties for chip visibility (testable without Compose)
    val showAddressChip: Boolean get() = addresses.isEmpty()
    val showOrgChip: Boolean get() = !showOrganization && organization.company.isBlank() && organization.title.isBlank()
    val showNoteChip: Boolean get() = !showNote && note.isBlank()
    val showGroupsChip: Boolean get() = groups.isEmpty() && availableGroups.isNotEmpty()
    val showOtherChip: Boolean get() = events.isEmpty() || relations.isEmpty() || imAccounts.isEmpty() ||
        websites.isEmpty() || (!showNickname && nickname.isBlank()) || (!showSipAddress && sipAddress.isBlank())
    val hasAnyChip: Boolean get() = showAddressChip || showOrgChip || showNoteChip || showGroupsChip || showOtherChip
}
```

**New actions (only for single-field sections):**
```kotlin
sealed interface ContactCreationAction {
    // ... existing Add*/Remove* actions unchanged ...
    data object ShowOrganization : ContactCreationAction  // NEW
    data object HideOrganization : ContactCreationAction  // NEW
    data object ShowNote : ContactCreationAction           // NEW
    data object HideNote : ContactCreationAction           // NEW
    data object ShowNickname : ContactCreationAction       // NEW
    data object HideNickname : ContactCreationAction       // NEW
    data object ShowSipAddress : ContactCreationAction     // NEW
    data object HideSipAddress : ContactCreationAction     // NEW
    // REMOVE: data object ToggleMoreFields : ContactCreationAction
}
```

**ViewModel handling:**
```kotlin
// Chip taps for repeatable fields → reuse existing Add* actions
// Chip taps for single-field sections → Show* actions
is ShowOrganization -> updateState { copy(showOrganization = true) }
is HideOrganization -> updateState { copy(showOrganization = false, organization = OrganizationFieldState()) }
is ShowNote -> updateState { copy(showNote = true) }
is HideNote -> updateState { copy(showNote = false, note = "") }
is ShowNickname -> updateState { copy(showNickname = true) }
is HideNickname -> updateState { copy(showNickname = false, nickname = "") }
is ShowSipAddress -> updateState { copy(showSipAddress = true) }
is HideSipAddress -> updateState { copy(showSipAddress = false, sipAddress = "") }
```

> **Why not `.also {}`:** The original plan used `copy(...).also { copy(...) }` which discards the inner copy. This simplified approach avoids the bug entirely — each action is a single `copy()` call.

#### 4b. Chip Visibility — `ContactCreationEditorScreen.kt`

> **Performance fix:** Removed `derivedStateOf`. With a plain `uiState` param (not `State<T>`), `derivedStateOf` adds subscription overhead with zero skip benefit. Plain property access on `@Immutable` UiState is sufficient.

```kotlin
// Simply read computed properties from UiState — no derivedStateOf needed
AddMoreInfoSection(
    showAddressChip = uiState.showAddressChip,
    showOrgChip = uiState.showOrgChip,
    showNoteChip = uiState.showNoteChip,
    showGroupsChip = uiState.showGroupsChip,
    showOtherChip = uiState.showOtherChip,
    // ...
)
```

#### 4c. AddMoreInfoSection — NEW `component/AddMoreInfoSection.kt`

**Test first:** `AddMoreInfoSectionTest.kt`
- Test: chip grid renders only when sections are hidden
- Test: tapping chip dispatches `ShowSection`
- Test: chip disappears when section is shown
- Test: "Other" chip opens bottom sheet
- Test: chip grid disappears when all sections shown
- Test: chip has correct `contentDescription`

```kotlin
@Composable
internal fun AddMoreInfoSection(
    showAddressChip: Boolean,
    showOrgChip: Boolean,
    showNoteChip: Boolean,
    showGroupsChip: Boolean,
    showOtherChip: Boolean,
    onAddAddress: () -> Unit,      // dispatches existing AddAddress action
    onShowOrganization: () -> Unit, // dispatches ShowOrganization
    onShowNote: () -> Unit,         // dispatches ShowNote
    onShowGroups: () -> Unit,
    onShowOtherSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Add more info",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            // key() is CRITICAL — without it, removal animates the wrong chip
            key("address") {
                ChipItem(visible = showAddressChip, label = "Address", icon = Icons.Filled.LocationOn,
                    contentDescription = "Add address section", onClick = onAddAddress)
            }
            key("org") {
                ChipItem(visible = showOrgChip, label = "Organization", icon = Icons.Filled.Business,
                    contentDescription = "Add organization section", onClick = onShowOrganization)
            }
            key("note") {
                // FIX: Icons.AutoMirrored.Filled.Note doesn't exist → use Icons.Filled.Notes
                ChipItem(visible = showNoteChip, label = "Note", icon = Icons.Filled.Notes,
                    contentDescription = "Add note section", onClick = onShowNote)
            }
            key("groups") {
                ChipItem(visible = showGroupsChip, label = "Groups", icon = Icons.Filled.Group,
                    contentDescription = "Add groups section", onClick = onShowGroups)
            }
            key("other") {
                // FIX: Icons.Filled.MoreHoriz needs material-icons-extended — verify dep or use MoreVert
                ChipItem(visible = showOtherChip, label = "Other", icon = Icons.Filled.MoreVert,
                    contentDescription = "Add other fields", onClick = onShowOtherSheet)
            }
        }
    }
}

@Composable
private fun ChipItem(
    visible: Boolean,
    label: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        // FIX: fadeOut() only — shrinkHorizontally causes per-frame FlowRow re-measure jank
        exit = fadeOut(MaterialTheme.motionScheme.fastEffectsSpec()),
    ) {
        AssistChip(
            onClick = onClick,
            label = { Text(label) },
            leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize)) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                labelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                leadingIconContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
            modifier = Modifier
                .testTag(TestTags.addMoreInfoChip(label.lowercase())) // use TestTags factory
                .semantics { this.contentDescription = contentDescription },
        )
    }
}
```

> **Icon fixes:** `Icons.AutoMirrored.Filled.Note` → `Icons.Filled.Notes`. `Icons.Filled.MoreHoriz` needs `material-icons-extended` dep — use `Icons.Filled.MoreVert` as fallback if not available.
>
> **Animation fix:** `shrinkHorizontally` on FlowRow chips causes per-frame re-measure of all chips during animation. `fadeOut()` only avoids this and looks just as good for small chips. Use `motionScheme.fastEffectsSpec()` instead of hardcoded spring.
>
> **key() fix:** Without `key()` on each chip, FlowRow may animate the wrong chip on removal.
>
> **TestTag fix:** Inline `"add_more_info_chip_..."` strings violate project convention — all tags must be in `TestTags.kt`. Add `fun addMoreInfoChip(section: String): String` factory.

#### 4d. OtherFieldsBottomSheet — NEW `component/OtherFieldsBottomSheet.kt`

**Test first:** `OtherFieldsBottomSheetTest.kt`
- Test: sheet shows only sections not yet visible
- Test: tapping item dispatches `ShowSection` and closes sheet

```kotlin
// Data class for sheet items — cleaner than Triple
private data class OtherFieldItem(
    val label: String,
    val icon: ImageVector,
    val testTag: String,
    val onAdd: () -> Unit,
)

@Composable
internal fun OtherFieldsBottomSheet(
    uiState: ContactCreationUiState,
    onAction: (ContactCreationAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    fun dismissAndDo(action: ContactCreationAction) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) { onDismiss(); onAction(action) }
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        val items = buildList {
            if (uiState.events.isEmpty())
                add(OtherFieldItem("Significant date", Icons.Filled.DateRange,
                    TestTags.otherSheetItem("event")) { dismissAndDo(AddEvent) })
            if (uiState.relations.isEmpty())
                add(OtherFieldItem("Relationship", Icons.Filled.People,
                    TestTags.otherSheetItem("relation")) { dismissAndDo(AddRelation) })
            if (uiState.imAccounts.isEmpty())
                add(OtherFieldItem("Instant messaging", Icons.Filled.Message,
                    TestTags.otherSheetItem("im")) { dismissAndDo(AddIm) })
            if (uiState.websites.isEmpty())
                add(OtherFieldItem("Website", Icons.Filled.Public,
                    TestTags.otherSheetItem("website")) { dismissAndDo(AddWebsite) })
            if (!uiState.showSipAddress && uiState.sipAddress.isBlank() && uiState.showSipField)
                add(OtherFieldItem("SIP address", Icons.Filled.Phone,
                    TestTags.otherSheetItem("sip")) { dismissAndDo(ShowSipAddress) })
            if (!uiState.showNickname && uiState.nickname.isBlank())
                add(OtherFieldItem("Nickname", Icons.Filled.Person,
                    TestTags.otherSheetItem("nickname")) { dismissAndDo(ShowNickname) })
        }
        items.forEach { item ->
            ListItem(
                headlineContent = { Text(item.label) },
                leadingContent = { Icon(item.icon, contentDescription = null) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent), // sheet provides tonal elevation
                modifier = Modifier
                    .clickable(onClick = item.onAdd)
                    .testTag(item.testTag)
            )
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}
```

> **Icon fixes:** Replaced `CalendarMonth`, `Chat`, `Language` (all need `material-icons-extended`) with `DateRange`, `Message`, `Public` (available in core). Verify at compile time.
>
> **Sheet state fix:** Uses `rememberModalBottomSheetState` + `hide()` for smooth dismiss animation.
>
> **TestTag fix:** Uses `TestTags.otherSheetItem(section)` factory instead of inline strings.
>
> **ListItem colors:** `containerColor = Color.Transparent` prevents double-tinting (sheet already provides tonal elevation).

#### 4e. Section Visibility in EditorScreen — `ContactCreationEditorScreen.kt`

Replace the current `MoreFieldsSection` with conditional sections + chip grid. Use `MotionScheme` tokens for animations:

```kotlin
// Animation specs — use motionScheme tokens, NOT hardcoded springs
val enterSpec = expandVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
    fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec())
val exitSpec = shrinkVertically(MaterialTheme.motionScheme.defaultSpatialSpec()) +
    fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())

// Current order preserved:
// 1. Name (always)
// 2. Phone (always)
// 3. Email (always)

// 4. Address (chip-driven — visible when list non-empty)
AnimatedVisibility(visible = uiState.addresses.isNotEmpty(), enter = enterSpec, exit = exitSpec) {
    AddressSectionContent(...)
}

// 5. Organization (boolean-driven)
val orgVisible = uiState.showOrganization || uiState.organization.company.isNotBlank() || uiState.organization.title.isNotBlank()
AnimatedVisibility(visible = orgVisible, enter = enterSpec, exit = exitSpec) {
    OrganizationSectionContent(...)
}

// 6-11. Nickname, SIP, IM, Website, Events, Relations
// Repeatable: visible when list.isNotEmpty()
// Single-field: visible when showX || field.isNotBlank()

// 12. Note (no section header, just field + remove button)
val noteVisible = uiState.showNote || uiState.note.isNotBlank()
AnimatedVisibility(visible = noteVisible, enter = enterSpec, exit = exitSpec) { ... }

// 13. Chip grid
AnimatedVisibility(visible = uiState.hasAnyChip) {
    AddMoreInfoSection(
        showAddressChip = uiState.showAddressChip,
        showOrgChip = uiState.showOrgChip,
        showNoteChip = uiState.showNoteChip,
        showGroupsChip = uiState.showGroupsChip,
        showOtherChip = uiState.showOtherChip,
        onAddAddress = { onAction(AddAddress) },
        onShowOrganization = { onAction(ShowOrganization) },
        onShowNote = { onAction(ShowNote) },
        onShowGroups = { /* TODO */ },
        onShowOtherSheet = { showOtherSheet = true },
    )
}

// 14. Groups (when available)
// 15. Account footer bar
```

> **Sub-composable relocation:** `MoreFieldsSection.kt` contains `NicknameField`, `NoteField`, `SipField`, `OrganizationSectionContent`, etc. as private composables. These must be relocated to individual files (`NicknameSection.kt`, `NoteSection.kt`, `SipSection.kt`) matching the existing pattern (`PhoneSection.kt`, `EmailSection.kt`). Do this BEFORE deleting `MoreFieldsSection.kt`.

#### 4f. Auto-scroll + Focus on Section Add

> **Simplified per architecture review.** Auto-scroll is a UI concern — use composable-local state diffing, not ViewModel effects. The system automatically scrolls focused fields into view.

```kotlin
// Track previous visible sections to detect additions
val previousSections = remember { mutableStateOf(emptySet<String>()) }

// Detect newly visible sections — composable-local, no ViewModel effect needed
LaunchedEffect(
    uiState.addresses.isNotEmpty(),
    uiState.showOrganization,
    uiState.showNote,
    // ... other visibility flags
) {
    val currentSections = buildSet {
        if (uiState.addresses.isNotEmpty()) add("address")
        if (uiState.showOrganization) add("org")
        if (uiState.showNote) add("note")
        // ...
    }
    val added = currentSections - previousSections.value
    previousSections.value = currentSections
    added.firstOrNull()?.let { section ->
        // FocusRequester on the first field of the new section
        // Compose automatically scrolls focused fields into view
        sectionFocusRequesters[section]?.requestFocus()
    }
}
```

> **Why not ViewModel effect:** Scrolling is a UI concern. The ViewModel doesn't know layout positions. A `delay(100)` hack is fragile. Composable-local state diffing reacts to actual state changes after composition settles.
>
> **Why not `onGloballyPositioned` on every section:** It fires N callbacks per layout pass. Only attach it to the scroll target, if needed at all — `FocusRequester.requestFocus()` usually triggers automatic scroll-to-focused.

**Acceptance Criteria Phase 4:**
- [ ] `MoreFieldsSection.kt` deleted (sub-composables relocated to individual files first)
- [ ] `isMoreFieldsExpanded` and `ToggleMoreFields` removed from state/actions
- [ ] 4 boolean flags (`showOrganization`, `showNote`, `showNickname`, `showSipAddress`) in UiState
- [ ] `Show*` / `Hide*` actions for single-field sections
- [ ] Chip visibility as computed properties on UiState (testable without Compose)
- [ ] Chip grid renders: Address, Org, Note, Groups, Other
- [ ] Chips are tonal (secondaryContainer background)
- [ ] Chip exit animation: shrinkHorizontally + fadeOut with spring
- [ ] Section enter animation: expandVertically + fadeIn with spring
- [ ] Tapping chip adds section + auto-scrolls + focuses first field + keyboard opens
- [ ] Tapping "Other" opens ModalBottomSheet with: Event, Relation, IM, Website, SIP, Nickname
- [ ] Bottom sheet items close sheet and add section
- [ ] Chip reappears when section is removed (last field deleted or single-field remove)
- [ ] Chip grid disappears when all sections are shown
- [ ] Each chip has correct `contentDescription`
- [ ] All tests pass, new tests for chip grid + bottom sheet
- [ ] `./gradlew build` clean

---

### Phase 5: Account Footer Bar

**Files:** `ContactCreationEditorScreen.kt`

**Test first:** UI test asserting footer text renders with correct account info.

```kotlin
@Composable
private fun AccountFooterBar(
    accountName: String?,
    modifier: Modifier = Modifier,
) {
    // Visual only — no tap interaction until Phase 2 account picker
    // No expand chevron or cloud icon — they suggest interactivity that doesn't exist yet
    Text(
        text = "Saving to ${accountName ?: "Device only"}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(TestTags.ACCOUNT_FOOTER),
    )
}
```

> **Simplicity fix:** Removed CloudOff + ExpandLess icons. They suggest interactivity (expandable account picker) that doesn't exist yet. Just text. Add icons when the actual picker is built (Phase 2).

Placed at the very bottom of the scrollable content, before the bottom spacer.

**Acceptance Criteria Phase 5:**
- [ ] Footer bar shows "Saving to Device only" (or account name)
- [ ] Styled with onSurfaceVariant text, bodySmall
- [ ] Cloud-off icon + chevron icon
- [ ] Tapping does nothing (Phase 2 deferred)
- [ ] Test verifying footer content
- [ ] `./gradlew build` clean

---

### Phase 6: IME Keyboard Chaining

**Files:** All section component files, `ContactCreationEditorScreen.kt`

#### 6a. Keyboard Types — `PhoneSection.kt`, `EmailSection.kt`

**Test first:** UI test verifying keyboard options on phone/email fields.

```kotlin
// PhoneSection
OutlinedTextField(
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Phone,
        imeAction = if (isLastField) ImeAction.Done else ImeAction.Next,
    ),
)

// EmailSection
OutlinedTextField(
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        imeAction = if (isLastField) ImeAction.Done else ImeAction.Next,
    ),
)
```

#### 6b. Focus Chain — `ContactCreationEditorScreen.kt`

> **Simplified per reviews.** No `FocusRequesterManager` class needed. Use `focusManager.moveFocus(FocusDirection.Down)` which follows composition order. This is the standard Compose pattern for vertical forms.

```kotlin
val focusManager = LocalFocusManager.current

// In each section's OutlinedTextField:
keyboardActions = KeyboardActions(
    onNext = { focusManager.moveFocus(FocusDirection.Down) },
    onDone = { focusManager.clearFocus() },
)
```

- Each field gets `ImeAction.Next` except the last visible field which gets `ImeAction.Done`
- Note field: always `ImeAction.Done` (multiline)
- No explicit `FocusRequester` wiring per field — Compose's focus traversal follows composition order naturally
- When fields are added/removed, traversal order updates automatically

> **Research insight:** `FocusManager.moveFocus(FocusDirection.Down)` handles the chain automatically. Custom `FocusRequester` chains are only needed for non-linear navigation (e.g., skipping fields, jumping between sections). For a vertical form, the platform does the right thing.

**Acceptance Criteria Phase 6:**
- [ ] Phone fields use `KeyboardType.Phone`
- [ ] Email fields use `KeyboardType.Email`
- [ ] Pressing Next moves focus to the next visible field (via `moveFocus(Down)`)
- [ ] Last visible field shows Done and clears focus
- [ ] Note field always shows Done
- [ ] Focus chain updates automatically when fields are added/removed
- [ ] Tests: `performImeAction()` + `assertIsFocused()` on next field
- [ ] Note: `KeyboardType` not testable via semantics — manual verification
- [ ] `./gradlew build` clean

---

### Phase 7: Shape Morphing + Final Polish

**Files:** All component files

#### 7a. Shape Morphing on All Interactive Elements

Add `@OptIn(ExperimentalMaterial3ExpressiveApi::class)` and `shapes` parameter to:
- Save button (done in Phase 1)
- Close button (done in Phase 1)
- All RemoveFieldButtons (done in Phase 2)
- Photo circle's clickable modifier (if using `IconButton` wrapper)
- Any remaining `IconButton` instances

#### 7b. Accessibility Pass

**Test first (SDD compliance — these ARE testable via semantics):**

```kotlin
// AccessibilityTest.kt
@Test fun photoCircle_hasButtonRole() {
    composeTestRule.onNodeWithTag(TestTags.PHOTO_AVATAR)
        .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
}

@Test fun chipGrid_chipsHaveContentDescriptions() {
    composeTestRule.onNodeWithTag(TestTags.addMoreInfoChip("address"))
        .assert(hasContentDescription("Add address section"))
}

@Test fun removeButton_has48dpMinTouchTarget() {
    composeTestRule.onNodeWithTag(TestTags.phoneDelete(0))
        .assertHeightIsAtLeast(48.dp).assertWidthIsAtLeast(48.dp)
}
```

- [ ] Photo circle: `contentDescription = "Contact photo. Double tap to change"` + `role = Role.Button`
- [ ] All chips: `contentDescription = "Add [field] section"`
- [ ] Remove buttons: 48dp touch target via `minimumInteractiveComponentSize()`
- [ ] Bottom sheet items: proper focus ordering for TalkBack
- [ ] Phone/email trailing dropdown icon: `contentDescription = "Change phone type"` (standard IconButton a11y)

#### 7c. Spacing Polish

- Verify 8dp between fields in same section
- Verify 24dp between sections
- Verify 16dp between "Add phone" CTA and next section
- Verify 8dp chip grid gaps
- Verify 12dp vertical / 16dp horizontal on account footer

**Acceptance Criteria Phase 7:**
- [ ] Shape morphing on all buttons/icon buttons
- [ ] All accessibility semantics in place
- [ ] Spacing matches design spec
- [ ] Full `./gradlew build` clean (ktlint + detekt + tests)
- [ ] Manual visual inspection on device

## System-Wide Impact

- **State model:** 4 new boolean fields in `@Parcelize` UiState + computed chip visibility properties — survives process death
- **Removed:** `isMoreFieldsExpanded`, `ToggleMoreFields` — breaking change for any code referencing these
- **New files:** `AddMoreInfoSection.kt`, `OtherFieldsBottomSheet.kt`, `NicknameSection.kt`, `NoteSection.kt`, `SipSection.kt` (relocated from MoreFieldsSection.kt)
- **Deleted file:** `MoreFieldsSection.kt`
- **Test impact:** ~13 files reference MoreFields/ToggleMoreFields — all need updating. FieldTypeSelectorTest.kt needs rework for phone/email (FilterChip → trailing icon).
- **New TestTags:** `addMoreInfoChip(section)`, `otherSheetItem(section)`, `PHOTO_SHEET_*`, `ACCOUNT_FOOTER`, `*_REMOVE` for single-field sections
- **No backend changes:** Save path via `RawContactDeltaMapper` unchanged — it already maps all field types
- **Performance:** Memoize `selectorLabels` in PhoneSection/EmailSection/AddressSection (currently allocates list per recomposition)

## What's NOT in Scope

- Country code prefix on phone fields (separate ticket)
- Account picker ModalBottomSheet (Phase 2 of main plan)
- Full-screen photo picker (Google proprietary)
- Star/favorite toggle (deferred)
- Grouped section cards (decided against)
- `MaterialExpressiveTheme` (alpha only)
- Overflow menu (⋮) in TopAppBar (not needed)
- New field types not in current UiState

## Edge Cases (from testing review)

| Category | Case | Mitigation |
|----------|------|-----------|
| Rapid taps | Tap chip twice → double `AddAddress` | ViewModel: check `addresses.isNotEmpty()` before adding |
| Rapid taps | Tap remove twice on same field | Second tap on stale index — guard with ID lookup |
| Concurrent animations | Show section while chip exit in progress | `AnimatedVisibility` handles this — test outcome only |
| Round-trip | All chips tapped, all sections removed → chips reappear | Derivation from field state handles this |
| Bottom sheet | Swipe-dismiss without selecting | `onDismiss` only, no action dispatched |
| Process death | Show 3 sections, kill, restore | Boolean flags in `@Parcelize` UiState survive |
| Focus | Remove focused field | Focus clears or moves to adjacent — test explicitly |
| Focus | Add field while typing | New field added, focus stays on current — don't jump |
| IME | Done on note (multiline) | `ImeAction.Done` clears focus, doesn't add newline |
| Layout | All 5+ chips on narrow screen | `FlowRow` wraps naturally |
| Custom label | Trailing icon → dropdown → Custom → dialog → OK | Full flow must work end-to-end |

## Testing Strategy (from reviews)

**Key patterns:**
- `ModalBottomSheet` tests need `waitUntil` — sheet animates in asynchronously
- `AnimatedVisibility` exit: test outcome (node gone via `waitUntil`), not animation spec
- `KeyboardType` is NOT in Compose semantics — only `ImeAction` is testable; phone/email keyboard = manual verification
- Focus chain: `performImeAction()` + `assertIsFocused()` on next field
- All new testTags must go in `TestTags.kt` as factory functions, not inline strings

**New test files needed:**
- `AddMoreInfoSectionTest.kt`
- `OtherFieldsBottomSheetTest.kt`
- `AccessibilityTest.kt` (or add to existing screen test)

**Existing tests to update:**
- `PhotoSectionTest.kt` — DropdownMenu → ModalBottomSheet assertions
- `PhoneSectionTest.kt` / `EmailSectionTest.kt` — FilterChip → trailing icon dropdown
- `ContactCreationViewModelTest.kt` — Show*/Hide* actions, process death round-trip
- `ContactCreationEditorScreenTest.kt` — remove MoreFields references, add chip grid + visibility tests
- `FieldTypeSelectorTest.kt` — rework or delete for phone/email (still used by Address)

## Sources & References

### Origin

- **Brainstorm:** [docs/brainstorms/2026-04-15-m3-expressive-polish-brainstorm.md](docs/brainstorms/2026-04-15-m3-expressive-polish-brainstorm.md)
  - Key decisions: FilledTonalButton save, red outlined remove, chip grid for more fields, type-in-label, photo bottom sheet, account footer, shape morphing on all elements, MotionScheme in theme, IME chaining, plain surface background

### Internal References

- Completed M3 layout: `docs/plans/2026-04-15-refactor-ui-redesign-m3-plan.md`
- Architecture: `docs/plans/2026-04-14-feat-contact-creation-compose-rewrite-plan.md`
- Theme: `src/com/android/contacts/ui/core/Theme.kt`
- Main screen: `src/com/android/contacts/ui/contactcreation/ContactCreationEditorScreen.kt`
- Shared components: `src/com/android/contacts/ui/contactcreation/component/SharedComponents.kt`
- State model: `src/com/android/contacts/ui/contactcreation/model/ContactCreationUiState.kt`
- Actions: `src/com/android/contacts/ui/contactcreation/model/ContactCreationAction.kt`

### External References

- M3 Expressive catalog: `github.com/emertozd/Compose-Material-3-Expressive-Catalog`
- WikiReader grouped shapes pattern: `github.com/nsh07/WikiReader`
- Compose BOM 2026.03.01 (material3 ~1.4.x)
- Android Developers — Animation composables: `developer.android.com/develop/ui/compose/animation/composables-modifiers`
- Android Developers — Bottom sheets: `developer.android.com/develop/ui/compose/components/bottom-sheets`
- Ben Trengrove — When to use derivedStateOf: `medium.com/androiddevelopers/jetpack-compose-when-should-i-use-derivedstateof`
- ModalBottomSheet + nav bar padding: `medium.com/@gpimenoff/modalbottomsheet-and-the-system-navigation-bar-jetpack-compose`
- ExposedDropdownMenuBox pattern: `composables.com/material3/exposeddropdownmenubox`
