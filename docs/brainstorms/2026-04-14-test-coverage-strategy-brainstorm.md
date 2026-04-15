# Brainstorm: Test Coverage Strategy for Contact Creation Screen

**Date:** 2026-04-14
**Status:** Ready for planning

## What We're Building

A comprehensive test strategy to close the gaps identified in PR review comment #12. Currently at 181 tests (~65% coverage). Target: full component coverage, integration tests with real mapper, and 5 E2E flow tests.

## Current State

| Layer | Tests | Coverage |
|-------|-------|----------|
| Mapper (RawContactDeltaMapper) | 68 | Excellent — all 13 field types |
| ViewModel | 35 | Good — core actions, effects, persistence |
| UI Sections (8 composables) | 78 | Fair — main sections covered |
| UI Helpers | 0 | Missing — OrganizationSection, AccountChip, CustomLabelDialog, FieldTypeSelector |
| Integration | 0 | Missing — no ViewModel+real mapper test |
| E2E flows | 0 | Missing — no full Activity flow tests |

## Why This Approach

The reviewer's feedback is valid — unit tests prove components work in isolation but don't prove the system works end-to-end. We need three layers:

1. **Component tests** — fill the 5 untested composable gaps
2. **Integration tests** — ViewModel with real mapper, mock save service at Intent boundary
3. **E2E flow tests** — launch real Activity, fill form via testTag, verify save

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| E2E framework | `createAndroidComposeRule<ContactCreationActivity>` | Fast, no emulator, Robolectric-compatible |
| Save path realism | Real mapper, mock save service at Intent boundary | Proves Kotlin pipeline without ContentProvider |
| Screenshot tests | Skip for now | Behavioral tests first; screenshot CI complexity not justified yet |
| E2E flow count | 5 flows | Happy path + all fields + cancel + intent extras + zero-account |
| Test helpers | Create test builder/factory functions | DRY test data creation across all test files |

## Test Plan

### Layer 1: Missing Component Tests (~20 new tests)

| Component | Test File | Tests | What to verify |
|-----------|-----------|-------|----------------|
| OrganizationSection | `OrganizationSectionTest.kt` | 5 | Company/title render, input dispatch, icon |
| AccountChip | `AccountChipTest.kt` | 4 | Displays name, "Device" fallback, tap dispatches RequestAccountPicker |
| CustomLabelDialog | `CustomLabelDialogTest.kt` | 5 | Shows input, confirm dispatches with label, cancel dismisses, empty label blocked |
| FieldTypeSelector | `FieldTypeSelectorTest.kt` | 6 | Shows current type, opens dropdown, selection dispatches, Custom triggers dialog |

### Layer 2: Integration Tests (~10 new tests)

**File:** `ContactCreationIntegrationTest.kt` (unit test, Robolectric)

Tests the ViewModel → Mapper pipeline with real dependencies:
- Fill name+phone+email → save → verify DeltaMapperResult has correct RawContactDelta entries
- Fill ALL field types → save → verify all 13 MIME types present in delta
- Empty form → save → no effect emitted
- Custom phone type → save → verify TYPE_CUSTOM + LABEL in delta
- Process death → restore → save → delta matches original input
- Photo URI → save → verify updatedPhotos bundle has correct temp ID key

**No mocking except:** `appContext` (RuntimeEnvironment.getApplication())

### Layer 3: E2E Flow Tests (~5 tests)

**File:** `ContactCreationFlowTest.kt` (androidTest, Compose rule with Activity)

| Flow | Steps | Verification |
|------|-------|-------------|
| 1. Create basic contact | Launch → type name → add phone → add email → tap save | Save effect emitted with correct delta |
| 2. Create with all fields | Launch → fill all sections → expand more fields → add events/relations → tap save | All 13 field types in delta |
| 3. Cancel with discard | Launch → type name → tap back → verify discard dialog → tap discard | Activity finished, no save |
| 4. Intent extras pre-fill | Launch with Insert.NAME + Insert.PHONE extras → verify pre-filled → tap save | Pre-filled values in delta |
| 5. Zero-account local-only | Launch with no accounts configured → verify "Device" chip → fill + save | Account is null (local) in delta |

### Test Helpers to Create

**File:** `TestFactory.kt` (shared between unit + androidTest)

```kotlin
object TestFactory {
    fun uiState(
        firstName: String = "",
        phones: List<PhoneFieldState> = listOf(PhoneFieldState()),
        // ... defaults for all fields
    ) = ContactCreationUiState(nameState = NameState(first = firstName), phoneNumbers = phones, ...)

    fun phone(number: String = "555-1234", type: PhoneType = PhoneType.Mobile) =
        PhoneFieldState(number = number, type = type)

    fun email(address: String = "test@example.com", type: EmailType = EmailType.Home) =
        EmailFieldState(address = address, type = type)
    // ... factory for each field type
}
```

## Resolved Questions

1. **E2E framework** → Compose test rule with Robolectric (no emulator)
2. **Save realism** → Real mapper, mock save service at Intent boundary
3. **Screenshot tests** → Skip for now
4. **Flow count** → 5 E2E flows

## Open Questions

None — ready for planning.
