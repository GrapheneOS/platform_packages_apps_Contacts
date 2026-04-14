# Brainstorm: Contact Creation Screen — Kotlin/Compose Rewrite

**Date:** 2026-04-14
**Status:** Ready for planning

## What We're Building

Rewrite the contact creation screen from Java/XML (1892-line `ContactEditorFragment` + 30 supporting classes) to Kotlin + Jetpack Compose with Material 3 Expressive. Tests use stable `testTag()` IDs exclusively.

**Scope:** Create-only flow. The existing edit/update flows via `ContactEditorActivity` remain untouched.

**Fields:** Full parity with current editor — name, phone(s), email(s), photo, organization, address, notes, website, events, relations, IM, nickname, groups, custom fields, SIP.

## Why This Approach

The GrapheneOS Messaging app has established patterns (PR #101) for Java/XML → Kotlin/Compose migrations. We follow those conventions for consistency across the GrapheneOS app suite, adapting where the Contacts domain differs.

## Key Decisions

### Architecture
| Decision | Choice | Rationale |
|----------|--------|-----------|
| Navigation | AnimatedContent + sealed routes | Match Messaging PR pattern; future-proofs for edit/detail screens |
| State management | ScreenModel interface → ViewModel → Delegates | Messaging PR pattern; testable, separates concerns |
| Data persistence | Reuse existing `ContactSaveService` | Battle-tested save path; avoids duplicating ContentProviderOperation logic |
| DI | Hilt (@Binds modules) | Already set up in build.gradle.kts; matches Messaging |
| Testing | `testTag()` on all interactive elements | Task requirement: no text reliance. Constants object for tag IDs |
| Material | M3 Expressive (full) | Use ExpressiveTopAppBar, animated buttons, shape morphing, spring motion |
| Activity | New `ContactCreationActivity` (ComponentActivity) | Hosts Compose content; keeps existing editor untouched |

### Package Structure
```
com.android.contacts.ui.contactcreation/
  ContactCreationActivity.kt
  common/
    ContactFieldComponents.kt         # Reusable field editors (phone row, email row, etc.)
    TestTags.kt                        # All testTag constants
  screen/
    ContactCreationScreen.kt           # NavHost + AnimatedContent routing
    ContactCreationViewModel.kt        # ScreenModel impl
    ContactCreationEffectHandler.kt    # Side effects (save, photo pick, finish)
    model/
      ContactCreationAction.kt         # Sealed interface
      ContactCreationNavRoute.kt       # Sealed interface with depth
      ContactCreationEffect.kt         # Sealed interface
      ContactCreationUiState.kt        # @Immutable data class
  delegate/
    ContactFieldsDelegate.kt           # Manages field state (add/remove/edit rows)
    PhotoDelegate.kt                   # Photo selection state
    AccountDelegate.kt                 # Account type selection
  mapper/
    ContactCreationUiStateMapper.kt    # Maps delegate states → UiState
    RawContactDeltaMapper.kt           # Maps UiState → RawContactDeltaList for save
  di/
    ContactCreationModule.kt           # Hilt @Binds module
```

### State Model (sketch)
```kotlin
@Immutable
data class ContactCreationUiState(
    // Name
    val prefix: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val suffix: String = "",
    // Photo
    val photoUri: Uri? = null,
    // Repeatable fields
    val phoneNumbers: List<PhoneFieldState> = listOf(PhoneFieldState()),
    val emails: List<EmailFieldState> = listOf(EmailFieldState()),
    val addresses: List<AddressFieldState> = emptyList(),
    val events: List<EventFieldState> = emptyList(),
    val ims: List<ImFieldState> = emptyList(),
    val relations: List<RelationFieldState> = emptyList(),
    val websites: List<WebsiteFieldState> = emptyList(),
    // Single fields
    val organization: String = "",
    val title: String = "",
    val nickname: String = "",
    val notes: String = "",
    val sipAddress: String = "",
    // Groups
    val groups: List<GroupState> = emptyList(),
    // UI state
    val showAllFields: Boolean = false,
    val isSaving: Boolean = false,
    val selectedAccount: AccountInfo? = null,
    val availableAccounts: List<AccountInfo> = emptyList(),
)
```

### Test Strategy
| Layer | Tool | What |
|-------|------|------|
| UI (screen) | Compose test + MockK | Render screen with fake state, assert nodes by testTag, verify actions dispatched |
| ViewModel | JUnit + Turbine + Robolectric | Fake delegates, test action→state and action→effect flows |
| Delegates | JUnit + MockK | Unit test field manipulation logic |
| Mapper | JUnit | RawContactDelta mapping correctness |

### Skill Suite
| Skill | Purpose |
|-------|---------|
| `android-build` | Run gradle build, ktlint, detekt, tests with error parsing |
| `compose-screen` | Generate Compose screen following Messaging PR patterns |
| `compose-test` | Generate Compose UI tests with testTag pattern |
| `viewmodel-pattern` | Generate ScreenModel/Delegate/Action/Effect/UiState skeleton |
| `hilt-module` | Generate @Module/@Binds boilerplate |

## Guiding Principle

**Write new Kotlin/Compose code; don't add tech debt; don't increase risk unnecessarily.** If we're writing new code for this screen, do it properly in Kotlin with modern APIs. But don't rewrite shared dependencies or infrastructure that the rest of the app relies on — that increases blast radius for no gain.

## Resolved Questions

1. **Account selection UI** → Inline header chip. Tapping opens bottom sheet with account list.
2. **Photo** → Full photo support (camera + gallery + remove). Use `ActivityResultContracts.PickVisualMedia` for gallery, `TakePicture` for camera. Modern APIs, no permissions needed on 13+.
3. **Field type labels** → Kotlin rewrite. New sealed class/enum for field types with label resolution. The existing `EditorUiUtils` is View-coupled — writing new code anyway, so do it cleanly. Reuse the same string resources.
4. **Manifest registration** → Replace `ACTION_INSERT`. New `ContactCreationActivity` owns contact creation. Old `ContactEditorActivity` keeps `ACTION_EDIT` only. Clean cut, no feature flags.

## Open Questions

None — ready for planning.
