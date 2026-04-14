---
title: "feat: Rewrite contact creation screen in Kotlin/Compose"
type: feat
status: active
date: 2026-04-14
deepened: 2026-04-14
origin: docs/brainstorms/2026-04-14-contact-creation-compose-rewrite-brainstorm.md
---

# feat: Rewrite Contact Creation Screen in Kotlin/Compose

## Enhancement Summary

**Deepened on:** 2026-04-14 (round 1), 2026-04-14 (round 2)
**Agents used:** Architecture strategist, Security sentinel, Performance oracle, Code simplicity reviewer, Best practices researcher, Framework docs researcher, RawContactDelta bridging researcher, SpecFlow analyzer

### Key Improvements from Deepening

**Round 1:**
1. **Simplified architecture** — dropped ScreenModel interface, NavRoute, UiStateMapper, EffectHandler class (6 files eliminated, ~25% LOC reduction)
2. **Concrete RawContactDeltaMapper** — full implementation with all 13 field types from source code analysis
3. **Security hardening** — intent extras sanitization, photo temp file cleanup, PII-safe error messages
4. **Performance optimizations** — Coil for async photos, state slices per section, PersistentList for repeatable fields, stable UUIDs as keys
5. **Missing dependencies identified** — Coil, hilt-navigation-compose, kotlinx-collections-immutable
6. **Save callback mechanism defined** — `onNewIntent()` handler for ContactSaveService result
7. **Phases consolidated** — 8 → 6 phases (merged fields+save, merged polish+edge cases)

**Round 2:**
8. **SDD cycle refined** — added TYPES + STUBS steps before TEST for compile-first stubs
9. **Phase 1a+1b merged** — single Phase 1 with bottom-up SDD order (mapper → delegate → VM → sections → screen)
10. **Test paths fixed** — explicit `app/src/test/` vs `app/src/androidTest/` paths
11. **PersistentList strategy clarified** — `@Immutable` on UiState, zero-cost upcast (PersistentList IS-A List)
12. **Missing acceptance criteria tests added** — type change, account selection, custom label, SIP filtering, name section
13. **M3 Expressive specifics** — named spring constants, `contentType` on items(), reduce-motion guard, icon mapping
14. **IM special handling** — PROTOCOL + CUSTOM_PROTOCOL (not TYPE + LABEL)

### Development Methodology: Spec-Driven Development (SDD)

Every phase follows a strict **red → green → refactor** cycle driven by this plan as the spec:

1. **SPEC** — Read the plan phase requirements
2. **TYPES** — Define interfaces, data classes, sealed types (the contract)
3. **STUBS** — Create source files with TODO() bodies + fake implementations
4. **TEST** — Write ALL tests. They compile against stubs but FAIL (red)
5. **IMPL** — Write minimum implementation to make tests pass (green)
6. **LINT** — `./gradlew app:ktlintFormat && ./gradlew build`

Test files are created BEFORE source files. The plan's acceptance criteria ARE the test specifications.

### Architecture Decision: Simplify vs Match Messaging PR

Multiple reviewers flagged the Messaging PR #101 patterns (ScreenModel interface, AnimatedContent routing, 3 delegates) as over-engineered for a single-screen form. **Decision: simplify.** Rationale:
- Single screen = no routing needed. Bottom sheets handle pickers.
- State-down/events-up `(uiState, onAction)` is more idiomatic Compose than a ScreenModel interface.
- Photo and account state are trivial (~20 LOC each) — fold into ViewModel.
- If a future edit screen rewrite needs these patterns, extract then. YAGNI now.

This departs from the brainstorm's "match Messaging pattern" decision. The Messaging PR had multiple screens (Settings → AppSettings → SubscriptionSettings) that justified routing. We don't.

---

## Overview

Rewrite the contact creation screen from Java/XML (`ContactEditorFragment` — 1892 lines + 30 supporting classes) to Kotlin + Jetpack Compose with Material 3 Expressive. Full field parity. Tests use `testTag()` exclusively. Simplified MVI architecture (ViewModel + single delegate + sealed Actions/Effects).

## Problem Statement / Motivation

The current contact editor is a monolithic Java Fragment with tightly coupled custom Views, deprecated APIs (`android.app.Fragment`, `LoaderManager`), and no ViewModel layer. The GrapheneOS project is migrating apps to Kotlin/Compose (Messaging app already done). The Contacts app needs the same treatment, starting with the creation screen.

## Proposed Solution

New `ContactCreationActivity` (Compose-based, `@AndroidEntryPoint`) replaces `ACTION_INSERT` handling. Existing `ContactEditorActivity` retains `ACTION_EDIT`. Save path reuses the proven `ContactSaveService` via `RawContactDeltaList` — no changes to data layer.

## Technical Approach

### Architecture

```
ContactCreationActivity (@AndroidEntryPoint, ComponentActivity)
  └─ setContent { AppTheme { ContactCreationEditorScreen(viewModel) } }

ContactCreationEditorScreen (uiState: UiState, onAction: (Action) -> Unit)
  ├─ Scaffold + LargeTopAppBar + MotionScheme.expressive()
  ├─ LazyColumn with section-scoped state slices
  └─ TopAppBar save action

ContactCreationViewModel (@HiltViewModel)
  ├─ ContactFieldsDelegate (manages all field state — single MutableStateFlow)
  ├─ Photo state (Uri? — directly in ViewModel)
  ├─ Account state (selection — directly in ViewModel)
  ├─ SavedStateHandle (process death persistence via @Parcelize)
  └─ Effects via Channel<Effect>

RawContactDeltaMapper (@Inject)
  └─ Converts UiState → RawContactDeltaList → ContactSaveService
```

> **Research insight (Architecture):** Composables accept `(uiState, onAction)` directly instead of a ScreenModel interface. This eliminates a Hilt @Binds module and is more idiomatic Compose. UI tests mock via lambda capture instead of MockK interface.

> **Research insight (Performance):** Each LazyListScope section receives only its state slice (e.g., `phones: List<PhoneFieldState>`) not the full UiState. This prevents unnecessary recomposition when unrelated fields change.

> **Convention:** All LazyColumn `items()` calls must include `contentType` for Compose recycling: `items(items = phones, key = { it.id }, contentType = { "phone_field" }) { ... }`

### Package Structure

```
src/com/android/contacts/
├── ui/
│   ├── core/
│   │   └── Theme.kt                          # EXISTS — add MotionScheme.expressive()
│   └── contactcreation/
│       ├── ContactCreationActivity.kt         # @AndroidEntryPoint host
│       ├── ContactCreationEditorScreen.kt     # Main editor composable
│       ├── ContactCreationViewModel.kt        # @HiltViewModel + state
│       ├── model/
│       │   ├── ContactCreationAction.kt       # Sealed interface
│       │   ├── ContactCreationEffect.kt       # Sealed interface
│       │   ├── ContactCreationUiState.kt      # @Parcelize data class (List<T> fields)
│       │   └── NameState.kt                   # @Parcelize sub-state for name fields
│       ├── delegate/
│       │   └── ContactFieldsDelegate.kt       # Field CRUD (PersistentList internally)
│       ├── component/
│       │   ├── NameSection.kt                 # Name fields composable
│       │   ├── PhoneSection.kt                # Phone fields composable
│       │   ├── EmailSection.kt                # Email fields composable
│       │   ├── AddressSection.kt              # Address fields
│       │   ├── OrganizationSection.kt         # Org + title (single, not repeatable)
│       │   ├── MoreFieldsSection.kt           # Events, relations, website, note, IM, SIP, nickname
│       │   ├── GroupSection.kt                # Group membership
│       │   ├── PhotoSection.kt                # Photo avatar + picker
│       │   ├── AccountChip.kt                 # Account selection chip + sheet
│       │   └── FieldType.kt                   # Sealed classes for type labels
│       ├── mapper/
│       │   └── RawContactDeltaMapper.kt       # UiState → RawContactDeltaList
│       ├── TestTags.kt                        # All testTag constants
│       └── di/
│           └── ContactCreationProvidesModule.kt # @Provides for AccountTypeManager
├── di/core/
│   ├── CoreProvidesModule.kt                  # EXISTS — dispatchers
│   └── Qualifiers.kt                          # EXISTS
```

> **Research insight (Architecture):** Split `ContactFieldComponents.kt` into per-section files from the start. With 13 field types, a single file would exceed 1000 lines. Each `LazyListScope` extension is a natural file boundary.

> **Research insight (Architecture):** New `ContactCreationProvidesModule` needed to expose `AccountTypeManager` (Java singleton) to Hilt graph via `@Provides`.

### New Dependencies Required

| Dependency | Purpose | Version catalog entry |
|------------|---------|----------------------|
| `io.coil-kt.coil3:coil-compose` | Async photo loading (off-thread decode, LRU cache) | `coil-compose` |
| `androidx.hilt:hilt-navigation-compose` | `hiltViewModel()` in composables | `hilt-navigation-compose` |
| `org.jetbrains.kotlinx:kotlinx-collections-immutable` | `PersistentList` for delegate internals | `kotlinx-collections-immutable` |

> **Research insight (Performance):** Photo display MUST use async image loader. A 12MP camera photo = ~48MB bitmap at full resolution. Without Coil, decoding on main thread causes 200-500ms ANR. Hold only `Uri` in state, never `Bitmap`.

> **Research insight (Performance):** `PersistentList` gives O(log32 n) structural sharing on updates vs O(n) list copies. Used inside `ContactFieldsDelegate` for efficient field CRUD.

### PersistentList + @Parcelize Resolution

`PersistentList` is NOT `Parcelable`. Strategy:
- **UiState** (`@Parcelize` + `@Immutable`) uses regular `List<T>` — SavedStateHandle compatible
- **ContactFieldsDelegate** uses `PersistentList` internally for efficient structural sharing on updates
- **ViewModel** bridges: `PersistentList` IS-A `List`, so assign directly to UiState `List<T>` fields (zero-cost upcast, no `.toList()` needed)
- **On restore** from SavedStateHandle: call `.toPersistentList()` once to re-enter the PersistentList world
- No custom Parcelers. No compatibility hacks. Clean separation.

### NameState Sub-object

Name fields grouped into a dedicated data class for clean section-scoped state passing:
```kotlin
@Parcelize
data class NameState(
    val prefix: String = "",
    val first: String = "",
    val middle: String = "",
    val last: String = "",
    val suffix: String = "",
) : Parcelable {
    fun hasData() = prefix.isNotBlank() || first.isNotBlank() ||
        middle.isNotBlank() || last.isNotBlank() || suffix.isNotBlank()
}
```
`ContactCreationUiState.nameState: NameState` — passed directly to `nameSection()`.

### Implementation Phases

#### Phase 1: Core Fields + Save — End-to-End

**Goal:** App compiles, new activity launches via `ACTION_INSERT`, create contact with name + phone + email → appears in contacts list.

**SDD order (bottom-up: mapper → delegate → VM → sections → screen):**
1. Scaffold setup: create stubs for Activity, ViewModel (TODO), UiState, Action, Effect, Screen, TestTags, Hilt module. Add deps to build.gradle.kts + libs.versions.toml. Register activity in manifest. `./gradlew build` to verify compilation.
2. Write `RawContactDeltaMapperTest.kt` — test name/phone/email mapping, empty field exclusion, custom labels. Red.
3. Write `ContactFieldsDelegateTest.kt` — test add/remove/update phone, email, `updatePhoneType_changesTypeInState()`. Red.
4. Write `ContactCreationViewModelTest.kt` — test save action → effect, add phone → state update, process death restore. Red.
5. Write `NameSectionTest.kt`, `PhoneSectionTest.kt`, `EmailSectionTest.kt` — test field rendering + action dispatch. Red.
6. Write `ContactCreationEditorScreenTest.kt` — test empty scaffold renders (SAVE_BUTTON visible, BACK_BUTTON visible), name/phone/email sections visible, save dispatches, `selectAccount_dispatchesAction()`. Red.
7. Implement: FieldType → Mapper → Delegate → ViewModel → Sections → Screen wiring → Activity. Green.
8. `./gradlew build`

**Deliverables:**
- `ContactCreationActivity.kt` — `@AndroidEntryPoint`, `enableEdgeToEdge()`, `setContent`, `android:launchMode="singleTop"` in manifest
- `ContactCreationEditorScreen.kt` — `Scaffold` + `LargeTopAppBar` + save action + name/phone/email sections
- `ContactCreationViewModel.kt` — full wiring: SavedStateHandle, actions, effects, account loading
- `ContactCreationUiState.kt` — `@Parcelize` data class (name + phone + email fields)
- `NameState.kt` — `@Parcelize` sub-state for name fields
- `ContactCreationAction.kt` / `ContactCreationEffect.kt` — sealed interfaces
- `TestTags.kt` — constants
- `ContactCreationProvidesModule.kt` — Hilt `@Provides` for `AccountTypeManager`
- `RawContactDeltaMapper.kt` — maps UiState → RawContactDeltaList (name, phone, email)
- `ContactFieldsDelegate.kt` — field CRUD with `PersistentList` internally
- `FieldType.kt` — `PhoneType`, `EmailType` sealed classes
- `NameSection.kt`, `PhoneSection.kt`, `EmailSection.kt` — composables
- `AccountChip.kt` — account selection chip + bottom sheet
- Intent extras sanitization in Activity `onCreate()`
- Save callback via `onNewIntent()`
- `AndroidManifest.xml` — register new activity with `ACTION_INSERT`, remove from `ContactEditorActivity`
- `app/build.gradle.kts` + `libs.versions.toml` — add Coil, hilt-navigation-compose, kotlinx-collections-immutable

**Files:**
| File | Action |
|------|--------|
| `app/src/test/java/com/android/contacts/ui/contactcreation/RawContactDeltaMapperTest.kt` | Create FIRST (red) |
| `app/src/test/java/com/android/contacts/ui/contactcreation/ContactFieldsDelegateTest.kt` | Create FIRST (red) |
| `app/src/test/java/com/android/contacts/ui/contactcreation/ContactCreationViewModelTest.kt` | Create FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/NameSectionTest.kt` | Create FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/PhoneSectionTest.kt` | Create FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/EmailSectionTest.kt` | Create FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/ContactCreationEditorScreenTest.kt` | Create FIRST (red) |
| `src/.../ui/contactcreation/ContactCreationActivity.kt` | Create |
| `src/.../ui/contactcreation/ContactCreationEditorScreen.kt` | Create |
| `src/.../ui/contactcreation/ContactCreationViewModel.kt` | Create |
| `src/.../ui/contactcreation/model/ContactCreationAction.kt` | Create |
| `src/.../ui/contactcreation/model/ContactCreationEffect.kt` | Create |
| `src/.../ui/contactcreation/model/ContactCreationUiState.kt` | Create |
| `src/.../ui/contactcreation/model/NameState.kt` | Create |
| `src/.../ui/contactcreation/TestTags.kt` | Create |
| `src/.../ui/contactcreation/di/ContactCreationProvidesModule.kt` | Create |
| `src/.../ui/contactcreation/delegate/ContactFieldsDelegate.kt` | Create |
| `src/.../ui/contactcreation/component/NameSection.kt` | Create |
| `src/.../ui/contactcreation/component/PhoneSection.kt` | Create |
| `src/.../ui/contactcreation/component/EmailSection.kt` | Create |
| `src/.../ui/contactcreation/component/AccountChip.kt` | Create |
| `src/.../ui/contactcreation/component/FieldType.kt` | Create |
| `src/.../ui/contactcreation/mapper/RawContactDeltaMapper.kt` | Create |
| `AndroidManifest.xml` | Modify |
| `app/build.gradle.kts` | Add deps |
| `gradle/libs.versions.toml` | Add entries |

**Success criteria:** `./gradlew build` passes. Create contact with name + phone + email → appears in contacts list. All Phase 1 tests green. Process death restores form state.

#### Phase 2: Extended Fields — Full Parity

**SDD order:**
1. Expand `RawContactDeltaMapperTest.kt` — tests for all remaining 10 field types (address, org, note, website, event, relation, IM, nickname, SIP, group). Red.
2. Expand `ContactFieldsDelegateTest.kt` — tests for add/remove/update address, events, etc. Add `accountWithoutSip_hidesSipField()`. Red.
3. Write section tests: `AddressSectionTest.kt`, `MoreFieldsSectionTest.kt` (include `customType_opensLabelDialog()`), `GroupSectionTest.kt`. Red.
4. Implement: FieldType expansion → Delegate expansion → Mapper expansion → Sections → Screen wiring. Green.

**Deliverables:**
- All remaining field types: organization, address, notes, website, events, relations, IM, nickname, SIP, groups
- "More fields" expand/collapse with `AnimatedVisibility`
- Per-field-type composable files
- Group membership picker (account-scoped)
- Custom label dialog for TYPE_CUSTOM

**Field types and their files:**
| MIME Type | File | Repeatable |
|-----------|------|-----------|
| `StructuredPostal` | `AddressSection.kt` | Yes |
| `Organization` | `OrganizationSection.kt` | No |
| `Event` | `MoreFieldsSection.kt` | Yes |
| `Relation` | `MoreFieldsSection.kt` | Yes |
| `Im` | `MoreFieldsSection.kt` | Yes |
| `Website` | `MoreFieldsSection.kt` | Yes |
| `Note` | `MoreFieldsSection.kt` | No |
| `Nickname` | `MoreFieldsSection.kt` | No |
| `SipAddress` | `MoreFieldsSection.kt` | No |
| `GroupMembership` | `GroupSection.kt` | N/A |

> **Research insight (SpecFlow):** Account-specific field filtering — some accounts don't support all field types (e.g., SIP, IM). The "more fields" section should hide unsupported types based on the selected account's `DataKind` list via `AccountType.getKindForMimetype()`.

> **Research insight (SpecFlow):** Groups are account-scoped. Changing the account must clear/refresh the group list. Default group ("My Contacts") may auto-assign on some accounts.

**Files:**
| File | Action |
|------|--------|
| `app/src/test/java/com/android/contacts/ui/contactcreation/RawContactDeltaMapperTest.kt` | Expand FIRST (red) |
| `app/src/test/java/com/android/contacts/ui/contactcreation/ContactFieldsDelegateTest.kt` | Expand FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/AddressSectionTest.kt` | Create FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/MoreFieldsSectionTest.kt` | Create FIRST (red) |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/GroupSectionTest.kt` | Create FIRST (red) |
| `src/.../ui/contactcreation/component/AddressSection.kt` | Create |
| `src/.../ui/contactcreation/component/OrganizationSection.kt` | Create |
| `src/.../ui/contactcreation/component/MoreFieldsSection.kt` | Create |
| `src/.../ui/contactcreation/component/GroupSection.kt` | Create |
| `src/.../ui/contactcreation/model/ContactCreationUiState.kt` | Expand |
| `src/.../ui/contactcreation/model/ContactCreationAction.kt` | Expand |
| `src/.../ui/contactcreation/component/FieldType.kt` | Expand |
| `src/.../ui/contactcreation/delegate/ContactFieldsDelegate.kt` | Expand |
| `src/.../ui/contactcreation/mapper/RawContactDeltaMapper.kt` | Expand |

**Success criteria:** All Phase 2 tests green. All field types render, accept input, save correctly. "More fields" expands/collapses. Groups selectable.

#### Phase 3: Photo Support

**SDD order:**
1. Expand `RawContactDeltaMapperTest.kt` — test photo URI in updatedPhotos bundle. Red.
2. Expand `ContactCreationViewModelTest.kt` — test SetPhoto/RemovePhoto actions, cleanup on clear. Red.
3. Write `PhotoSectionTest.kt` — test avatar renders, menu opens, actions dispatched. Red.
4. Implement: Mapper photo bundle → ViewModel photo state → PhotoSection → cleanup. Green.

**Deliverables:**
- Photo avatar composable — tappable circle with camera/gallery/remove dropdown
- `ActivityResultContracts.PickVisualMedia` for gallery (no permissions needed — minSdk 36)
- `ACTION_IMAGE_CAPTURE` implicit intent for camera (no CAMERA permission needed from caller)
- Photo URI passed to save service via `EXTRA_UPDATED_PHOTOS` bundle
- Coil `AsyncImage` for off-thread display with downsampling to avatar size
- Temp file cleanup on discard/cancel/activity finish

> **Research insight (Security):** Create temp photos in `getCacheDir()/contact_photos/` subdirectory. Delete on discard/cancel in ViewModel `onCleared()`. Scope `file_paths.xml` to `contact_photos/` path only.

> **Research insight (Security):** Use `ACTION_IMAGE_CAPTURE` implicit intent — does NOT require CAMERA permission from the caller. The system camera app handles it. Pass FileProvider URI via `EXTRA_OUTPUT`.

> **Research insight (Performance):** Never hold `Bitmap` in state. `AsyncImage(model = photoUri)` with Coil handles off-thread decode, downsampling to display size (96dp = ~288px on xxxhdpi), and LRU caching.

**Files:**
| File | Action |
|------|--------|
| `src/.../ui/contactcreation/component/PhotoSection.kt` | Create |
| `src/.../ui/contactcreation/ContactCreationEditorScreen.kt` | Wire photo |
| `src/.../ui/contactcreation/ContactCreationViewModel.kt` | Photo state + cleanup |
| `app/src/androidTest/java/com/android/contacts/ui/contactcreation/PhotoSectionTest.kt` | Create FIRST (red) |
| `res/xml/file_paths.xml` | Scope to `contact_photos/` subdirectory |

**Success criteria:** All Phase 3 tests green. Pick photo from gallery, take with camera, remove. Photo saves with contact. Temp files cleaned on discard.

#### Phase 4: M3 Expressive + Edge Cases + Polish (merge of original 6-7)

**SDD order:**
1. Expand `ContactCreationViewModelTest.kt` — test back-with-changes → discard effect, zero-account → local-only, intent extras sanitization. Red.
2. Expand `ContactCreationEditorScreenTest.kt` — test discard dialog renders, more-fields toggle, animations respect reduce-motion. Red.
3. Implement: Theme + animations + dialogs + predictive back + edge cases. Green.

**Deliverables:**
- `MotionScheme.expressive()` on `AppTheme` (physics-based spring animations)
- Named spring constants: `GentleBounce = spring(DampingRatioLowBouncy, StiffnessMediumLow)`, `SmoothExit = spring(DampingRatioNoBouncy, StiffnessMedium)`
- `animateItem(fadeInSpec = GentleBounce, fadeOutSpec = SmoothExit)` on all LazyColumn items
- `LocalReduceMotion.current` check: `val animSpec = if (reduceMotion) snap() else spring(...)`
- All composables use `MaterialTheme.colorScheme.*` and `MaterialTheme.typography.*` roles
- Icon mapping per field type (reference m3-expressive skill)
- Shape morphing on photo avatar tap
- Animated save button
- Predictive back gesture via `PredictiveBackHandler` (Android 14+)
- Back/cancel with unsaved changes → confirmation dialog
- Keyboard management (focus first field, dismiss on save)
- Zero-account / local-only contact support (critical for GrapheneOS)
- Error handling — generic snackbar messages (never leak PII)

> **Research insight (Best practices):** No `ExpressiveTopAppBar` exists. Use `LargeTopAppBar` + `MotionScheme.expressive()` on the theme. `MaterialExpressiveTheme` is alpha-only; stick with `MaterialTheme` + motionScheme parameter.

> **Research insight (SpecFlow):** GrapheneOS users frequently have no Google account. MUST support device-local contacts (`setAccountToLocal()`). Zero-account = device-only, not an error state.

> **Research insight (Security):** Error messages must be generic: "Could not save contact. Please try again." Never include field values or account names in user-visible messages.

> **Research insight (Performance):** Use `animateItem()` (LazyColumn built-in) not per-item `AnimatedVisibility`. Profile on Pixel 3a-class device. Skip spring animations when `isReduceMotionEnabled`.

**Files:**
| File | Action |
|------|--------|
| `src/.../ui/core/Theme.kt` | Add MotionScheme.expressive() |
| `src/.../ui/contactcreation/ContactCreationEditorScreen.kt` | Dialogs, back handling, animations |
| `src/.../ui/contactcreation/ContactCreationViewModel.kt` | Edge case logic |
| `src/.../ui/contactcreation/component/*.kt` | Add animateItem(), spring motion |

#### Phase 5: Test Hardening & Coverage Audit

**Note:** This phase is coverage hardening, not SDD — tests here catch gaps, not drive new implementation. Tests are written BEFORE implementation in each prior phase (SDD). This phase is for hardening: filling coverage gaps, adding edge case tests, and verifying the full test suite runs end-to-end.

**SDD order:**
1. Run `./gradlew test` + `./gradlew connectedAndroidTest` — identify any gaps.
2. Add missing edge case tests (e.g., max field count, concurrent save, rapid add/remove).
3. Add integration tests for intent extras → pre-fill → save flow.
4. Verify all ~75 tests pass.

**UI Tests (androidTest) — state-down/events-up pattern:**
```kotlin
class ContactCreationEditorScreenTest {
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<ContactCreationAction>()

    @Test fun initialState_showsNameAndPhoneFields() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.NAME_FIRST).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.phoneField(0)).assertIsDisplayed()
    }

    @Test fun tapSave_dispatchesSaveAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performClick()
        assertEquals(ContactCreationAction.Save, capturedActions.last())
    }

    private fun setContent(state: ContactCreationUiState = ContactCreationUiState()) {
        composeTestRule.setContent {
            AppTheme {
                ContactCreationEditorScreen(
                    uiState = state,
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
```

> **Research insight (Simplicity):** No MockK needed for UI tests. Lambda capture `onAction = { capturedActions.add(it) }` replaces `mockk(relaxed = true)` + `verify()`. Simpler, faster, no mock framework dependency in androidTest.

**ViewModel Tests (test):**
```kotlin
@RunWith(RobolectricTestRunner::class)
class ContactCreationViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    @Test fun saveAction_emitsSaveEffect() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(initialState = stateWithData())
        vm.effects.test {
            vm.onAction(ContactCreationAction.Save)
            assertIs<ContactCreationEffect.Save>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun addPhoneAction_addsEmptyPhoneRow() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel()
        vm.onAction(ContactCreationAction.AddPhone)
        assertEquals(2, vm.uiState.value.phoneNumbers.size)
    }
}
```

**Mapper Tests (test) — highest priority, most risk:**
```kotlin
class RawContactDeltaMapperTest {
    private val mapper = RawContactDeltaMapper()

    @Test fun mapsNameFields_toStructuredNameDelta() {
        val state = ContactCreationUiState(firstName = "John", lastName = "Doe")
        val result = mapper.map(state, account = null)
        val nameDelta = result.state[0].getMimeEntries(StructuredName.CONTENT_ITEM_TYPE)
        assertEquals("John", nameDelta[0].getAsString(StructuredName.GIVEN_NAME))
        assertEquals("Doe", nameDelta[0].getAsString(StructuredName.FAMILY_NAME))
    }

    @Test fun emptyFields_notIncludedInDelta() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(PhoneFieldState(number = "", type = PhoneType.MOBILE))
        )
        val result = mapper.map(state, account = null)
        val phoneDelta = result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)
        assertTrue(phoneDelta.isNullOrEmpty())
    }

    @Test fun customTypeLabel_setsBothTypeAndLabel() {
        val state = ContactCreationUiState(
            phoneNumbers = listOf(
                PhoneFieldState(number = "555", type = PhoneType.Custom("Work cell"))
            )
        )
        val result = mapper.map(state, account = null)
        val phone = result.state[0].getMimeEntries(Phone.CONTENT_ITEM_TYPE)!![0]
        assertEquals(Phone.TYPE_CUSTOM, phone.getAsInteger(Phone.TYPE))
        assertEquals("Work cell", phone.getAsString(Phone.LABEL))
    }

    @Test fun photoUri_addedToUpdatedPhotosBundle() {
        val photoUri = Uri.parse("content://test/photo.jpg")
        val state = ContactCreationUiState(photoUri = photoUri)
        val result = mapper.map(state, account = null)
        val rawContactId = result.state[0].values.id.toString()
        assertEquals(photoUri, result.updatedPhotos.getParcelable(rawContactId, Uri::class.java))
    }

    // Test ALL 13 field types...
}
```

**Test coverage targets:**
| Layer | Files | Tests |
|-------|-------|-------|
| UI - Editor screen | `ContactCreationEditorScreenTest.kt` | ~20 tests |
| UI - Sections | `PhoneSectionTest.kt`, `EmailSectionTest.kt`, etc. | ~15 tests |
| ViewModel | `ContactCreationViewModelTest.kt` | ~15 tests |
| Delegate | `ContactFieldsDelegateTest.kt` | ~10 tests |
| Mapper | `RawContactDeltaMapperTest.kt` | ~15 tests (highest priority) |
| **Total** | **~7 test files** | **~75 tests** |

**TestTags — flat constants with helper functions for indexed fields:**
```kotlin
internal object TestTags {
    const val SCREEN = "contact_creation_screen"
    const val SAVE_BUTTON = "contact_creation_save"
    const val BACK_BUTTON = "contact_creation_back"
    const val ACCOUNT_CHIP = "contact_creation_account_chip"
    const val PHOTO_AVATAR = "contact_creation_photo"
    const val MORE_FIELDS = "contact_creation_more_fields"

    // Name
    const val NAME_PREFIX = "contact_creation_name_prefix"
    const val NAME_FIRST = "contact_creation_name_first"
    const val NAME_MIDDLE = "contact_creation_name_middle"
    const val NAME_LAST = "contact_creation_name_last"
    const val NAME_SUFFIX = "contact_creation_name_suffix"

    // Indexed field helpers
    fun phoneField(index: Int) = "contact_creation_phone_$index"
    fun phoneType(index: Int) = "contact_creation_phone_type_$index"
    fun phoneDelete(index: Int) = "contact_creation_phone_delete_$index"
    const val PHONE_ADD = "contact_creation_phone_add"

    fun emailField(index: Int) = "contact_creation_email_$index"
    fun emailType(index: Int) = "contact_creation_email_type_$index"
    fun emailDelete(index: Int) = "contact_creation_email_delete_$index"
    const val EMAIL_ADD = "contact_creation_email_add"

    // Same pattern for address, event, im, relation, website...

    const val ORG_COMPANY = "contact_creation_org_company"
    const val ORG_TITLE = "contact_creation_org_title"
    const val NICKNAME = "contact_creation_nickname"
    const val NOTES = "contact_creation_notes"
    const val SIP = "contact_creation_sip"
    const val GROUPS = "contact_creation_groups"

    // Dialogs
    const val DISCARD_DIALOG = "contact_creation_discard_dialog"
    const val DISCARD_YES = "contact_creation_discard_yes"
    const val DISCARD_NO = "contact_creation_discard_no"
    const val CUSTOM_LABEL_DIALOG = "contact_creation_custom_label_dialog"
    const val CUSTOM_LABEL_INPUT = "contact_creation_custom_label_input"
    const val ACCOUNT_SHEET = "contact_creation_account_sheet"

    // Photo
    const val PHOTO_MENU = "contact_creation_photo_menu"
    const val PHOTO_GALLERY = "contact_creation_photo_gallery"
    const val PHOTO_CAMERA = "contact_creation_photo_camera"
    const val PHOTO_REMOVE = "contact_creation_photo_remove"
}
```

#### Phase 6: CLAUDE.md & Skills Setup

**Project CLAUDE.md** at `.claude/CLAUDE.md`:
- Build commands, architecture conventions, test patterns
- Reference to this plan and brainstorm
- TestTag naming conventions

**Skills** (8 skills covering every aspect of the implementation):

| Skill | File | Purpose |
|-------|------|---------|
| `sdd-workflow` | `.claude/skills/sdd-workflow.md` | **Start here.** Spec-driven dev cycle: plan → tests (red) → stubs → impl (green) → lint |
| `android-build` | `.claude/skills/android-build.md` | Run build, lint, test commands with error parsing |
| `compose-screen` | `.claude/skills/compose-screen.md` | Generate Compose screen following state-down/events-up pattern |
| `compose-test` | `.claude/skills/compose-test.md` | Generate UI/ViewModel/Mapper tests with testTag + lambda capture |
| `m3-expressive` | `.claude/skills/m3-expressive.md` | M3 Expressive components, animations, theme, icon mapping |
| `viewmodel-pattern` | `.claude/skills/viewmodel-pattern.md` | Generate ViewModel + Action/Effect/UiState MVI skeleton |
| `hilt-module` | `.claude/skills/hilt-module.md` | Generate Hilt @Provides/@Binds modules |
| `delta-mapper` | `.claude/skills/delta-mapper.md` | RawContactDelta construction, column reference, save service contract |

---

## Concrete RawContactDeltaMapper Implementation

> **Research insight (RawContactDelta bridging):** Full implementation derived from source code analysis of `ValuesDelta.fromAfter()`, `RawContactDelta.addEntry()`, `ContactSaveService.createSaveContactIntent()`, and `RawContactModifier.trimEmpty()`.

```kotlin
data class DeltaMapperResult(
    val state: RawContactDeltaList,
    val updatedPhotos: Bundle,
)

class RawContactDeltaMapper @Inject constructor() {
    fun map(uiState: ContactCreationUiState, account: AccountWithDataSet?): DeltaMapperResult {
        val rawContact = RawContact().apply {
            if (account != null) setAccount(account) else setAccountToLocal()
        }
        val delta = RawContactDelta(ValuesDelta.fromAfter(rawContact.values))
        val rawContactId = delta.values.id  // negative temp ID from sNextInsertId--

        // Name
        if (uiState.hasNameData()) {
            delta.addEntry(ValuesDelta.fromAfter(contentValues(StructuredName.CONTENT_ITEM_TYPE) {
                put(StructuredName.GIVEN_NAME, uiState.firstName)
                put(StructuredName.FAMILY_NAME, uiState.lastName)
                put(StructuredName.PREFIX, uiState.namePrefix)
                put(StructuredName.MIDDLE_NAME, uiState.middleName)
                put(StructuredName.SUFFIX, uiState.nameSuffix)
            }))
        }
        // Phones — skip blank entries (trimEmpty handles it, but save hasPendingChanges check)
        for (phone in uiState.phoneNumbers) {
            if (phone.number.isBlank()) continue
            delta.addEntry(ValuesDelta.fromAfter(contentValues(Phone.CONTENT_ITEM_TYPE) {
                put(Phone.NUMBER, phone.number)
                put(Phone.TYPE, phone.type.rawValue)
                if (phone.type is PhoneType.Custom) put(Phone.LABEL, phone.type.label)
            }))
        }
        // ... same pattern for all 13 field types (emails, addresses, org, notes, etc.)

        val state = RawContactDeltaList().apply { add(delta) }
        val updatedPhotos = Bundle()
        uiState.photoUri?.let { updatedPhotos.putParcelable(rawContactId.toString(), it) }

        return DeltaMapperResult(state, updatedPhotos)
    }

    private inline fun contentValues(mimeType: String, block: ContentValues.() -> Unit) =
        ContentValues().apply { put(Data.MIMETYPE, mimeType); block() }
}
```

Key edge cases from source analysis:
- `ValuesDelta.fromAfter()` assigns negative temp IDs via `sNextInsertId--`
- `ContactSaveService.saveContact()` calls `RawContactModifier.trimEmpty()` before building diff — empty entries are auto-cleaned
- Photos are separate from delta list — passed via `EXTRA_UPDATED_PHOTOS` bundle keyed by String of rawContactId
- For `TYPE_CUSTOM`, must set BOTH the type column AND the label column
- **IMPORTANT: IM uses PROTOCOL + CUSTOM_PROTOCOL (not TYPE + LABEL like other field types)**

---

## System-Wide Impact

### Interaction Graph

```
User taps "+" (FAB/menu in PeopleActivity)
  → Intent(ACTION_INSERT, Contacts.CONTENT_URI)
  → ContactCreationActivity.onCreate()                    # NEW
    → Sanitize intent extras (cap lengths, validate accounts)
    → setContent { AppTheme { ContactCreationEditorScreen(...) } }
    → ContactCreationViewModel.init()
      → Load writable accounts via AccountTypeManager
      → If zero accounts → show local-only prompt
      → If single → auto-select
      → If multiple → show account chip
    → User fills form, dispatches Actions
    → Action.Save → ViewModel
      → RawContactDeltaMapper.map(uiState, account)       # on Dispatchers.Default
      → Effect.Save(deltaList, photos)
      → LaunchedEffect → ContactSaveService.createSaveContactIntent(
            context, state, "saveMode", SaveMode.CLOSE,
            false, ContactCreationActivity::class.java,
            SAVE_COMPLETED_ACTION, updatedPhotos, null, null
        )
      → context.startService(intent)
    → ContactSaveService.saveContact()                    # EXISTING (Java)
      → RawContactModifier.trimEmpty()                    # auto-cleans empty fields
      → ContentResolver.applyBatch()                      # SYSTEM
      → Callback Intent(SAVE_COMPLETED_ACTION)
    → ContactCreationActivity.onNewIntent()               # receive callback
      → viewModel.onSaveResult(success, contactUri)
      → finish() or show error snackbar
```

### Error Propagation

| Error | Source | Handling |
|-------|--------|----------|
| Save failure | ContactSaveService callback (null URI) | Generic snackbar: "Could not save contact" |
| No writable accounts | AccountTypeManager | UiState → local-only prompt |
| Photo temp file creation fails | IOException in cache dir | Snackbar, photo section disabled |
| Permission revoked mid-save | SecurityException in ContentProvider | Caught in save service, null URI callback |
| Intent extras too large | External app sends oversized strings | Truncated in onCreate() sanitization |

### State Lifecycle Risks

- **Partial save**: `applyBatch()` is atomic per batch. No orphan risk.
- **Process death**: `SavedStateHandle` with `@Parcelize` UiState. All field data persisted. Restored transparently by ViewModel.
- **Photo temp file**: Created in `getCacheDir()/contact_photos/`. Deleted in `ViewModel.onCleared()` if not saved. Subdirectory wiped on activity start as safety net.

> **Research insight (Security):** PII in SavedStateHandle is serialized to disk by ActivityManager. This matches existing behavior (current editor uses Parcelable RawContactDeltaList). Document as explicit privacy tradeoff. GrapheneOS per-profile encryption provides defense-in-depth.

### Security Considerations

| Finding | Severity | Mitigation |
|---------|----------|------------|
| Intent extras injection via `Insert.DATA` | HIGH | Drop `Insert.DATA` support. Only accept known extras (`Insert.NAME`, `Insert.PHONE`, `Insert.EMAIL`, etc.) with max-length caps |
| PII in SavedStateHandle | MEDIUM | Matches existing behavior. Document tradeoff. Clear in `onDestroy(isFinishing=true)` |
| Photo temp files on discard | MEDIUM | Delete in `ViewModel.onCleared()`. Wipe subdirectory on activity start |
| Exported activity without validation | MEDIUM | Sanitize all extras in `onCreate()`. Validate `EXTRA_ACCOUNT` against writable accounts |
| Error messages leak PII | LOW | Generic error strings only. Debug-level logging for details |

---

## Acceptance Criteria

### Functional Requirements

- [ ] Create contact with all field types (name, phone, email, address, org, notes, website, events, relations, IM, nickname, SIP, groups)
- [ ] Add/remove multiple instances of repeatable fields
- [ ] Change field type labels (Home/Work/Mobile/Custom)
- [ ] Custom label dialog for TYPE_CUSTOM
- [ ] Select account when multiple writable accounts exist
- [ ] Device-local contact creation when zero accounts (critical for GrapheneOS)
- [ ] Add photo from gallery (PickVisualMedia) or camera (ACTION_IMAGE_CAPTURE)
- [ ] Remove photo
- [ ] Expand/collapse "more fields" section
- [ ] Account-specific field filtering (hide unsupported types)
- [ ] Back with unsaved changes shows confirmation dialog (including predictive back gesture)
- [ ] Handle `ACTION_INSERT` intent with extras (pre-fill fields, sanitized)
- [ ] Save creates contact visible in contacts list
- [ ] Empty form save does nothing

### Non-Functional Requirements

- [ ] M3 with `MotionScheme.expressive()` — spring animations, `animateItem()` on field add/remove
- [ ] Dynamic color theme (Material You) via existing `AppTheme`
- [ ] Edge-to-edge display
- [ ] Keyboard focus management
- [ ] All interactive elements have `testTag()`
- [ ] No hardcoded strings — all from `R.string.*`
- [ ] Process death restores form state via `SavedStateHandle`
- [ ] Photo temp files cleaned on discard/cancel
- [ ] Intent extras sanitized with max-length caps
- [ ] Respect `isReduceMotionEnabled` accessibility setting

### Testing Requirements

- [ ] ~75 tests across ~7 test files
- [ ] UI tests use `testTag()` exclusively (zero `onNodeWithText`)
- [ ] UI tests use lambda capture (no MockK for UI layer)
- [ ] ViewModel tests use fake delegate + Turbine
- [ ] Mapper tests cover ALL 13 field types + edge cases (highest priority)
- [ ] All tests pass: `./gradlew test` and `./gradlew connectedAndroidTest`

### Quality Gates

- [ ] `./gradlew build` passes (includes ktlint + detekt)
- [ ] No `any` types or suppressed warnings
- [ ] All composables `internal` visibility
- [ ] All state classes `@Parcelize`
- [ ] Zero View/Fragment dependencies in new code
- [ ] Coil for all image loading (no main-thread bitmap decode)

---

## Dependencies & Prerequisites

| Dependency | Status |
|------------|--------|
| Gradle + version catalog | Done (main branch) |
| Compose BOM 2026.03.01 | Done (app/build.gradle.kts) |
| Hilt setup | Done (`@HiltAndroidApp`, dispatchers module) |
| ktlint + detekt | Done (build.gradle.kts) |
| M3 theme (`AppTheme`) | Done (ui/core/Theme.kt) — add MotionScheme |
| `ContactSaveService` | Existing Java — no changes needed |
| `RawContactDelta` / `ValuesDelta` | Existing Java — consumed from Kotlin |
| **Coil Compose** | **TODO** — add to version catalog + build.gradle.kts |
| **hilt-navigation-compose** | **TODO** — needed for `hiltViewModel()` |
| **kotlinx-collections-immutable** | **TODO** — needed for `PersistentList` |

## Risk Analysis & Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|-----------|--------|------------|
| `RawContactDeltaMapper` incorrectly builds delta | Medium | High | 15 dedicated mapper tests; concrete implementation from source analysis; compare output to legacy editor |
| M3 Expressive APIs unstable | Medium | Low | Use `MotionScheme.expressive()` on stable `MaterialTheme` only. No alpha-only components |
| Process death loses form state | Low | Medium | `SavedStateHandle` + `@Parcelize` from Phase 1 |
| `ContactSaveService` callback not received | Low | Medium | `onNewIntent()` + matching `callbackAction` string; test with real save |
| Photo temp file leak | Low | Low | Cleanup in `onCleared()` + subdirectory wipe on start |
| Intent extras injection | Medium | Medium | Strict allowlist + length caps in `onCreate()` |
| Large form recomposition overhead | Low | Medium | State slices per section + `PersistentList` + stable keys |

---

## Files Eliminated (vs Original Plan)

| Eliminated File | Reason |
|----------------|--------|
| `ContactCreationScreen.kt` (routing) | Single screen — no routing needed |
| `ContactCreationNavRoute.kt` | No navigation routes |
| `ContactCreationEffectHandler.kt` | Effects handled inline via `LaunchedEffect` |
| `ContactCreationUiStateMapper.kt` | ViewModel produces UiState directly |
| `ContactCreationModule.kt` (@Binds) | No interfaces to bind — use `@Provides` module instead |
| `PhotoDelegate.kt` | Trivial state folded into ViewModel |
| `AccountDelegate.kt` | Trivial state folded into ViewModel |

**Net: 7 files eliminated, ~400-500 LOC saved.**

---

## Sources & References

### Origin

- **Brainstorm:** [docs/brainstorms/2026-04-14-contact-creation-compose-rewrite-brainstorm.md](docs/brainstorms/2026-04-14-contact-creation-compose-rewrite-brainstorm.md)
- Decisions carried forward: reuse ContactSaveService, testTag-only testing, M3 Expressive, Kotlin rewrite for field types
- Decision revised: simplified architecture (dropped ScreenModel, NavRoute, extra delegates)

### Internal References

- `src/com/android/contacts/editor/ContactEditorFragment.java` — current implementation (1892 lines)
- `src/com/android/contacts/ContactSaveService.java:463` — `createSaveContactIntent()` signature
- `src/com/android/contacts/model/RawContactDelta.java` — `addEntry()`, `buildDiff()`
- `src/com/android/contacts/model/ValuesDelta.java:72` — `fromAfter()`, temp ID assignment at line 78
- `src/com/android/contacts/model/RawContact.java:298` — `setAccount()`, `setAccountToLocal()`
- `src/com/android/contacts/model/RawContactModifier.java:413` — `trimEmpty()` behavior
- `src/com/android/contacts/editor/EditorUiUtils.java` — field type icons (reference for Compose Material Icons mapping)
- `src/com/android/contacts/ui/core/Theme.kt` — existing M3 Compose theme
- `src/com/android/contacts/di/core/CoreProvidesModule.kt` — existing Hilt dispatchers
- `app/build.gradle.kts` — Compose + Hilt + test dependencies
- `gradle/libs.versions.toml` — version catalog

### External References

- [GrapheneOS Messaging PR #101](https://github.com/GrapheneOS/Messaging/pull/101) — reference patterns (adapted, not copied)
- [Material 3 Expressive](https://developer.android.com/develop/ui/compose/designsystems/material3-expressive) — MotionScheme docs
- [Compose Testing](https://developer.android.com/develop/ui/compose/testing) — testTag patterns
- [Android Photo Picker](https://developer.android.com/training/data-storage/shared/photo-picker) — PickVisualMedia (guaranteed on minSdk 36)
- [Hilt 2.59.2 Release](https://github.com/google/dagger/releases/tag/dagger-2.59.2) — AGP 9 compatibility
- [Turbine 1.2.1](https://github.com/cashapp/turbine/releases/tag/1.2.1) — Flow testing
- [kotlinx-collections-immutable](https://github.com/Kotlin/kotlinx.collections.immutable) — PersistentList
