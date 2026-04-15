---
title: "test: Comprehensive test coverage — components, integration, E2E"
type: test
status: active
date: 2026-04-15
origin: docs/brainstorms/2026-04-14-test-coverage-strategy-brainstorm.md
---

# test: Comprehensive Test Coverage — Components, Integration, E2E

## Overview

Close the test gaps identified in PR review #12. Add 3 layers: missing component tests (20), integration tests with real mapper (10), and E2E flow tests (5). Target: ~35 new tests, bringing total from 181 to ~216.

## Problem Statement

Current 181 tests cover mapper (excellent) and ViewModel (good) but miss:
- 5 composable components with zero tests
- No integration tests (ViewModel + real mapper end-to-end)
- No E2E flow tests (Activity launch → fill form → save)

(see brainstorm: docs/brainstorms/2026-04-14-test-coverage-strategy-brainstorm.md)

## Implementation — SDD Per Layer

Follow project SDD: write tests first (red), then any supporting code (stubs/helpers) to make them pass (green).

### Phase 1: Test Helpers (shared infrastructure)

**File:** `app/src/test/java/com/android/contacts/ui/contactcreation/TestFactory.kt` (unit tests)
**File:** `app/src/androidTest/java/com/android/contacts/ui/contactcreation/TestFactory.kt` (instrumented — duplicate or shared via testFixtures)

```kotlin
internal object TestFactory {
    fun phone(number: String = "555-1234", type: PhoneType = PhoneType.Mobile) =
        PhoneFieldState(number = number, type = type)
    fun email(address: String = "test@example.com", type: EmailType = EmailType.Home) =
        EmailFieldState(address = address, type = type)
    fun address(street: String = "123 Main St", city: String = "Springfield") =
        AddressFieldState(street = street, city = city)
    fun fullState() = ContactCreationUiState(
        nameState = NameState(first = "Jane", last = "Doe"),
        phoneNumbers = listOf(phone()),
        emails = listOf(email()),
        // ... all field types populated
    )
}
```

### Phase 2: Missing Component Tests (~20 tests)

| File (androidTest) | Component | Tests |
|---------------------|-----------|-------|
| `OrganizationSectionTest.kt` | OrganizationSection | 5: renders company+title, input dispatches UpdateCompany/UpdateJobTitle, icon visible, empty state |
| `AccountChipTest.kt` | AccountChip | 4: displays account name, shows "Device" when null, tap dispatches RequestAccountPicker, testTag |
| `CustomLabelDialogTest.kt` | CustomLabelDialog | 5: shows input field, confirm dispatches with label, cancel dismisses, empty label disables confirm, pre-fills existing label |
| `FieldTypeSelectorTest.kt` | FieldTypeSelector | 6: shows current type label, tap opens dropdown, select type dispatches callback, Custom opens dialog, menu items match type list, testTag |

**SDD order:**
1. Write all 4 test files — Red (composables exist but tests don't exercise them)
2. Fix any composable bugs found by tests — Green
3. `./gradlew build`

### Phase 3: Integration Tests (~10 tests)

**File:** `app/src/test/java/com/android/contacts/ui/contactcreation/ContactCreationIntegrationTest.kt`

Uses real ViewModel + real RawContactDeltaMapper. No mocks except `appContext` (Robolectric).

| Test | What it proves |
|------|----------------|
| `createBasicContact_producesCorrectDelta()` | Name+phone+email → delta has 3 MIME entries |
| `createAllFields_producesAllMimeTypes()` | All 13 field types → delta has 13+ entries |
| `emptyForm_save_noEffect()` | Empty state → save → no Save effect emitted |
| `customPhoneType_deltaHasTypeCustomAndLabel()` | Custom("Work cell") → TYPE_CUSTOM + LABEL in delta |
| `processDeathRoundTrip_deltaMatchesOriginal()` | Fill → kill → restore → save → delta matches |
| `photoUri_inUpdatedPhotosBundle()` | Set photo → save → bundle has URI keyed by temp ID |
| `multiplePhones_produceMultipleEntries()` | 3 phones → 3 Phone delta entries |
| `imProtocol_usesProtocolNotType()` | IM field → PROTOCOL column (not TYPE) |
| `addressPartialFill_included()` | Only city filled → address delta still created |
| `save_setsIsSavingFlag()` | Save action → isSaving=true in state before effect |

**SDD order:**
1. Write `ContactCreationIntegrationTest.kt` with all 10 tests — Red
2. These should pass immediately (real implementations exist) — Green
3. Fix any bugs discovered

### Phase 4: E2E Flow Tests (~5 tests)

**File:** `app/src/androidTest/java/com/android/contacts/ui/contactcreation/ContactCreationFlowTest.kt`

Uses `createAndroidComposeRule<ContactCreationActivity>`. Tests the full Activity lifecycle.

| Test | Flow |
|------|------|
| `createBasicContact_endToEnd()` | Launch → type first name → type phone → tap save → verify Save effect |
| `createWithAllFields_endToEnd()` | Launch → fill all sections → expand more fields → add event/relation → save → verify all MIME types |
| `cancelWithDiscard_endToEnd()` | Launch → type name → tap back → discard dialog appears → tap discard → Activity finishes |
| `intentExtras_preFill_endToEnd()` | Launch with Insert.NAME="Jane" + Insert.PHONE="555" → verify fields pre-filled → save |
| `zeroAccount_localContact_endToEnd()` | Launch with no accounts → "Device" chip shown → fill + save → account is null (local) |

**SDD order:**
1. Write `ContactCreationFlowTest.kt` — Red
2. These test the real Activity wiring — may uncover integration bugs
3. Fix any bugs — Green
4. `./gradlew build`

## Acceptance Criteria

- [ ] 4 new component test files (OrganizationSection, AccountChip, CustomLabelDialog, FieldTypeSelector)
- [ ] ~20 component tests, all green
- [ ] 1 integration test file with ~10 tests, all green
- [ ] 1 E2E flow test file with 5 tests, all green
- [ ] TestFactory shared helper created
- [ ] `./gradlew test` passes (unit + Robolectric)
- [ ] `./gradlew build` passes (ktlint + detekt clean)
- [ ] Total test count: ~216+

## Sources

- **Origin brainstorm:** [docs/brainstorms/2026-04-14-test-coverage-strategy-brainstorm.md](docs/brainstorms/2026-04-14-test-coverage-strategy-brainstorm.md)
- Key decisions: Compose test rule (no emulator), real mapper + mock save at Intent boundary, 5 E2E flows, skip screenshots
- **Test patterns:** `.claude/skills/compose-test.md`
- **Existing tests:** `app/src/test/` and `app/src/androidTest/` for contactcreation
