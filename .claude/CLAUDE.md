# GrapheneOS Contacts — Compose Rewrite

## Build Commands

```bash
./gradlew build                    # Full build (includes ktlint + detekt)
./gradlew test                     # Unit tests (Robolectric)
./gradlew connectedAndroidTest     # Instrumented/Compose UI tests
./gradlew app:ktlintCheck          # Kotlin lint check
./gradlew app:ktlintFormat         # Kotlin lint auto-fix
./gradlew app:detekt               # Static analysis
```

## Development Workflow: Spec-Driven Development (SDD)

**Every feature follows this strict order:**

```
1. SPEC   — Read the plan phase requirements
2. TYPES  — Define interfaces, data classes, sealed types (the contract)
3. STUBS  — Create source files with TODO() bodies + fake implementations
4. TEST   — Write ALL tests. They compile against stubs but FAIL (red)
5. IMPL   — Write minimum implementation to make tests pass (green)
6. LINT   — ./gradlew app:ktlintFormat && ./gradlew build
```

### Rules
- **Never write implementation before tests.** The plan IS the spec.
- **Tests define the contract.** If it's not tested, it's not a requirement.
- **Minimum implementation.** Write the simplest code that makes tests pass. Refactor after green.
- **Each phase produces**: failing tests first → then passing implementation → then green build.
- **Test files are created BEFORE source files** for each new component.

### SDD per component type

| Component | Write first (red) | Then implement (green) |
|-----------|-------------------|----------------------|
| Mapper | `RawContactDeltaMapperTest.kt` | `RawContactDeltaMapper.kt` |
| ViewModel | `ContactCreationViewModelTest.kt` | `ContactCreationViewModel.kt` |
| Delegate | `ContactFieldsDelegateTest.kt` | `ContactFieldsDelegate.kt` |
| UI Screen | `ContactCreationEditorScreenTest.kt` | `ContactCreationEditorScreen.kt` |
| UI Section | `PhoneSectionTest.kt` | `PhoneSection.kt` |

### What "test first" means concretely

```kotlin
// 1. Write this FIRST — it won't compile yet
class RawContactDeltaMapperTest {
    @Test fun mapsName_toStructuredNameDelta() { ... }
    @Test fun emptyPhone_notIncluded() { ... }
    @Test fun customLabel_setsBothTypeAndLabel() { ... }
}

// 2. Create stub class — just enough to compile
class RawContactDeltaMapper @Inject constructor() {
    fun map(uiState: ContactCreationUiState, account: AccountWithDataSet?): DeltaMapperResult =
        TODO("Not yet implemented")
}

// 3. Run tests — they fail (red). Good.
// 4. Implement map() — tests pass (green). Done.
```

## Architecture

### Pattern: State-down, Events-up MVI

```
Activity (@AndroidEntryPoint)
  └─ setContent { AppTheme { Screen(uiState, onAction) } }

Screen composable receives:
  - uiState: UiState           (data class with List fields, @Parcelize for SavedStateHandle)
  - onAction: (Action) -> Unit  (event callback)

ViewModel (@HiltViewModel):
  - Single source of truth for state (MutableStateFlow)
  - Dispatches to ContactFieldsDelegate for field CRUD
  - Effects via Channel<Effect> collected in LaunchedEffect
  - SavedStateHandle for process death persistence

No ScreenModel interface. No Jetpack Navigation. No separate EffectHandler class.
```

### Save Callback Mechanism

```
ContactSaveService (fire-and-forget IntentService)
  → On completion, sends callback Intent to ContactCreationActivity
  → Activity receives via onNewIntent()
  → Routes to viewModel.onSaveResult(success, contactUri)

Key: callbackActivity = ContactCreationActivity::class.java
     callbackAction = SAVE_COMPLETED_ACTION (custom constant)
     Activity must set android:launchMode="singleTop" for onNewIntent to work
```

### Key Decisions
- **Composables accept `(uiState, onAction)`** — not a ScreenModel interface
- **One delegate** — `ContactFieldsDelegate` for complex field state. Photo/account state lives in ViewModel directly.
- **Effects inline** — `LaunchedEffect` collects from `ViewModel.effects` channel
- **Per-section state slices** — each `LazyListScope` extension receives only its data (e.g., `phones: List<PhoneFieldState>`)
- **Reuse existing Java** — `ContactSaveService`, `RawContactDelta`, `ValuesDelta`, `AccountTypeManager` consumed from Kotlin
- **UUID stable keys** — every repeatable field row has a `val id: String = UUID.randomUUID().toString()`. LazyColumn `key = { it.id }`. Never use list index as key.
- **contentType on items()** — all LazyColumn `items()` calls include `contentType` for Compose recycling

### PersistentList + Parcelize Strategy

`PersistentList` is NOT `Parcelable`. Our approach:
- **Runtime state** uses `PersistentList` in the delegate for efficient structural sharing
- **UiState** (which is `@Immutable @Parcelize`) uses regular `List<T>` for SavedStateHandle compatibility
- **Upcast** at ViewModel boundary: PersistentList IS-A List, assign directly (zero-cost, no `.toList()`)
- **On restore** from SavedStateHandle: call `.toPersistentList()` once to re-enter the PersistentList world
- This avoids custom Parcelers and keeps both concerns clean

### Package Structure

```
src/com/android/contacts/ui/contactcreation/
├── ContactCreationActivity.kt
├── ContactCreationEditorScreen.kt
├── ContactCreationViewModel.kt
├── TestTags.kt
├── model/
│   ├── ContactCreationAction.kt
│   ├── ContactCreationEffect.kt
│   ├── ContactCreationUiState.kt
│   └── NameState.kt                    # Grouped name fields sub-state
├── delegate/
│   └── ContactFieldsDelegate.kt
├── component/
│   ├── NameSection.kt
│   ├── PhoneSection.kt
│   ├── EmailSection.kt
│   ├── AddressSection.kt
│   ├── OrganizationSection.kt          # Org + title (single, not repeatable)
│   ├── MoreFieldsSection.kt            # Events, relations, website, note, IM, SIP, nickname
│   ├── GroupSection.kt
│   ├── PhotoSection.kt
│   ├── AccountChip.kt
│   └── FieldType.kt
├── mapper/
│   └── RawContactDeltaMapper.kt
└── di/
    └── ContactCreationProvidesModule.kt
```

## Conventions

### Compose
- All composables `internal` visibility
- UiState: `@Immutable @Parcelize` with regular `List<T>` fields (SavedStateHandle compatible)
- Delegate: uses `PersistentList` internally for efficient updates
- UUID as stable key for every repeatable field row — never list index
- `contentType` on all `items()` calls: `items(items = phones, key = { it.id }, contentType = { "phone_field" }) { ... }`
- Use Coil `AsyncImage` for all image loading (never decode bitmaps on main thread)
- Use `animateItem()` on LazyColumn items for add/remove animations
- Respect `isReduceMotionEnabled` — skip spring animations when set

### Coil (Photo Loading)
```kotlin
// Always use AsyncImage — never BitmapFactory or contentResolver on main thread
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(photoUri)
        .size(288)  // 96dp * 3 (xxxhdpi) — downsample to display size
        .crossfade(true)
        .build(),
    contentDescription = stringResource(R.string.contact_photo),
    modifier = Modifier.size(96.dp).clip(CircleShape).testTag(TestTags.PHOTO_AVATAR),
)
```

### M3 Expressive
- Use `MaterialTheme` + `MotionScheme.expressive()` (NOT `MaterialExpressiveTheme` — alpha only)
- `LargeTopAppBar` with `exitUntilCollapsedScrollBehavior()`
- Spring-based animations via `spring()` with `DampingRatioLowBouncy` / `StiffnessMediumLow`
- No `ExpressiveTopAppBar` exists — don't search for it

### Testing
- **testTag()** on all interactive elements — zero `onNodeWithText` in tests
- TestTags in `TestTags.kt` — flat constants + helper functions for indexed fields
- UI tests: lambda capture `onAction = { capturedActions.add(it) }` — no MockK in UI tests
- ViewModel tests: fake delegate + Turbine for effects + `MainDispatcherRule`
- Mapper tests: highest priority — test all 13 field types
- Tag naming: `contact_creation_{section}_{element}_{index?}`

### Security (GrapheneOS Context)
- Sanitize all intent extras in `onCreate()` with max-length caps
- Never leak PII in error messages — generic strings only
- Delete photo temp files on discard/cancel (`ViewModel.onCleared()`)
- Photo temp files in `getCacheDir()/contact_photos/` subdirectory only
- Do NOT support `Insert.DATA` (arbitrary ContentValues from external apps)
- Validate `EXTRA_ACCOUNT` / `EXTRA_DATA_SET` against actual writable accounts list

### Intent Extras Sanitization Pattern
```kotlin
// In ContactCreationActivity.onCreate()
private fun sanitizeExtras(intent: Intent): SanitizedExtras {
    val maxNameLen = 500
    val maxPhoneLen = 100
    val maxEmailLen = 320
    return SanitizedExtras(
        name = intent.getStringExtra(Insert.NAME)?.take(maxNameLen),
        phone = intent.getStringExtra(Insert.PHONE)?.take(maxPhoneLen),
        email = intent.getStringExtra(Insert.EMAIL)?.take(maxEmailLen),
        // ... other known Insert.* constants
        // EXPLICITLY IGNORE Insert.DATA — arbitrary ContentValues not supported
    )
}
```

### DI (Hilt)
- `@AndroidEntryPoint` on Activity
- `@HiltViewModel` on ViewModel
- `@Inject constructor` on delegate, mapper
- `@Provides` module for `AccountTypeManager` (Java singleton)
- Dispatcher qualifiers: `@DefaultDispatcher`, `@IoDispatcher`, `@MainDispatcher` (existing)

## Reference

- **Plan:** `docs/plans/2026-04-14-feat-contact-creation-compose-rewrite-plan.md`
- **Brainstorm:** `docs/brainstorms/2026-04-14-contact-creation-compose-rewrite-brainstorm.md`
- **Reference PR:** [GrapheneOS Messaging PR #101](https://github.com/GrapheneOS/Messaging/pull/101)
- **Existing theme:** `src/com/android/contacts/ui/core/Theme.kt`
- **Save service:** `src/com/android/contacts/ContactSaveService.java:463`
- **Delta model:** `src/com/android/contacts/model/RawContactDelta.java`, `ValuesDelta.java`
