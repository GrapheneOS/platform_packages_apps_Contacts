---
title: Account Selector Bottom Sheet
type: feat
status: active
date: 2026-04-15
deepened: 2026-04-15
origin: docs/brainstorms/2026-04-15-account-selector-brainstorm.md
---

# Account Selector Bottom Sheet

## Enhancement Summary

**Deepened on:** 2026-04-15
**Agents used:** best-practices-researcher, architecture-strategist, security-sentinel, code-simplicity-reviewer

### Key Simplifications (from deepening)
1. **Drop `AccountDisplayData`** — use `AccountWithDataSet` directly, resolve labels at composition time
2. **Skip icons for v1** — name + type label is sufficient differentiation
3. **Don't parcel account list** — reload from `AccountTypeManager` on restore, only persist `selectedAccount`
4. **Validate `SelectAccount`** — check account exists in writable list (security finding)
5. **Add timeout** on `Future.get()` to prevent hangs

## Overview

Make the "Saving to..." footer interactive. When >1 writable account exists, tapping it opens a `ModalBottomSheet` listing accounts. User picks one, sheet dismisses, footer updates.

## Key Decisions (from brainstorm)

- Trigger: footer + `^` icon, tappable when >1 account
- Single account: static text, no icon, not tappable
- Default: first writable account on init
- Rows: name + type label (icons deferred to v2)
- Selection: checkmark on selected

## Technical Approach

### No New Data Class

Use `AccountWithDataSet` directly (already `Parcelable`). Resolve `nameLabel` and `typeLabel` at composition time via `AccountTypeManager.getAccountInfoForAccount()`. Avoids duplicating fields into a wrapper class.

### Account List as Separate Flow

Accounts are system-derived state, not user input. Don't put in `@Parcelize` UiState — the list can become stale after process death (user adds/removes account in Settings). Instead:
- ViewModel holds `private val _accounts = MutableStateFlow<List<AccountWithDataSet>>(emptyList())`
- Exposed as `val accounts: StateFlow<List<AccountWithDataSet>>`
- Reloaded on init (including after process death restore)
- `selectedAccount` stays in UiState/SavedStateHandle (it IS user input)

### ListenableFuture

`withContext(Dispatchers.IO) { future.get(5, TimeUnit.SECONDS) }` — simple, no extra deps, timeout prevents hangs.

### Sheet Pattern

State-driven `var showAccountSheet` in Screen composable (matches `OtherFieldsBottomSheet`). Remove `LaunchAccountPicker` effect.

### Security: Account Validation

Validate `SelectAccount` — ensure the account exists in the writable accounts list before accepting it. If `EXTRA_ACCOUNT` is ever parsed from intents, validate against writable list too.

### Zero Accounts

Defensive: if empty list, show static footer "Saving to Device only", don't crash.

## Files to Modify

| File | Change |
|------|--------|
| `ContactCreationViewModel.kt` | Inject `AccountTypeManager`, add `accounts` StateFlow, load on init, validate `SelectAccount` |
| `ContactCreationEditorScreen.kt` | Make `AccountFooterBar` tappable, add inline `AccountBottomSheet`, collect `accounts` flow |
| `model/ContactCreationEffect.kt` | Remove `LaunchAccountPicker` |
| `model/ContactCreationAction.kt` | Remove `RequestAccountPicker` |
| `ContactCreationActivity.kt` | Remove `LaunchAccountPicker` handler |
| `TestTags.kt` | Add `ACCOUNT_SHEET`, `accountSheetItem(index)` |
| `component/AccountChip.kt` | **Delete** (unused) |

**No new files** — bottom sheet is small enough to inline in EditorScreen or at most a private composable in it.

## Implementation Phases

### Phase 1: ViewModel — Load Accounts

```kotlin
// Add to constructor
private val accountTypeManager: AccountTypeManager,

// New flow (NOT in UiState)
private val _accounts = MutableStateFlow<List<AccountWithDataSet>>(emptyList())
val accounts: StateFlow<List<AccountWithDataSet>> = _accounts.asStateFlow()

// In init{}
loadWritableAccounts()

private fun loadWritableAccounts() {
    viewModelScope.launch(defaultDispatcher) {
        try {
            val filter = AccountTypeManager.insertableFilter(appContext)
            val loaded = accountTypeManager.filterAccountsAsync(filter)
                .get(5, TimeUnit.SECONDS)
                .map { it.account }
            _accounts.value = loaded
            // Auto-select first if nothing selected
            if (_uiState.value.selectedAccount == null) {
                loaded.firstOrNull()?.let { first ->
                    updateState {
                        copy(selectedAccount = first, accountName = first.name)
                    }
                }
            }
        } catch (_: Exception) {
            // Fallback: device-only, empty list → footer shows "Device only"
        }
    }
}
```

Validate `SelectAccount`:
```kotlin
is ContactCreationAction.SelectAccount -> {
    val writable = _accounts.value
    if (writable.isEmpty() || action.account in writable) {
        updateState {
            copy(selectedAccount = action.account, accountName = action.account.name, groups = emptyList())
        }
    }
}
```

### Phase 2: Screen — Footer + Sheet

**AccountFooterBar** becomes interactive:
```kotlin
@Composable
private fun AccountFooterBar(
    accountName: String?,
    showPicker: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (showPicker) Modifier.clickable(onClick = onTap) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("Saving to ${accountName ?: "Device only"}", ...)
        if (showPicker) {
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Filled.KeyboardArrowUp, null, Modifier.size(16.dp))
        }
    }
}
```

**Bottom sheet** inline in `ContactCreationFieldsColumn`:
```kotlin
var showAccountSheet by remember { mutableStateOf(false) }
val accounts by viewModel.accounts.collectAsState()

// ... in footer:
AccountFooterBar(
    accountName = uiState.accountName,
    showPicker = accounts.size > 1,
    onTap = { showAccountSheet = true },
)

if (showAccountSheet) {
    ModalBottomSheet(
        onDismissRequest = { showAccountSheet = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        modifier = Modifier.testTag(TestTags.ACCOUNT_SHEET),
    ) {
        accounts.forEachIndexed { index, account ->
            val isSelected = account == uiState.selectedAccount
            val info = remember(account) {
                accountTypeManager.getAccountInfoForAccount(account)
            }
            ListItem(
                headlineContent = { Text(info?.nameLabel?.toString() ?: account.name ?: "Device") },
                supportingContent = { Text(info?.typeLabel?.toString() ?: "") },
                trailingContent = {
                    if (isSelected) Icon(Icons.Filled.Check, null, tint = primary)
                },
                modifier = Modifier
                    .clickable {
                        onAction(ContactCreationAction.SelectAccount(account))
                        showAccountSheet = false
                    }
                    .semantics { role = Role.RadioButton; selected = isSelected }
                    .testTag(TestTags.accountSheetItem(index)),
            )
        }
        Spacer(Modifier.navigationBarsPadding())
    }
}
```

### Phase 3: Cleanup

- Remove `ContactCreationEffect.LaunchAccountPicker`
- Remove `ContactCreationAction.RequestAccountPicker` + ViewModel handler
- Remove `LaunchAccountPicker` case in `ContactCreationActivity.handleEffect()`
- Delete `component/AccountChip.kt`

## Accessibility

- Each sheet row: `semantics { role = Role.RadioButton; selected = isSelected }`
- Sheet title: consider adding `semantics { heading() }` on a "Save to" header text
- Checkmark `contentDescription = null` — row semantics covers it
- Footer: when tappable, announce as button via `semantics { role = Role.Button }`

## TestTags

```kotlin
const val ACCOUNT_SHEET = "contact_creation_account_sheet"
fun accountSheetItem(index: Int): String = "contact_creation_account_sheet_item_$index"
```

## Edge Cases

| Case | Behavior |
|------|----------|
| 0 accounts | Show "Saving to Device only", static, don't crash |
| 1 account | Show "Saving to {name}", static, no `^` icon |
| Account removed mid-session | `ContactSaveService` handles error, toast shown |
| Process death | `selectedAccount` restored, account list reloaded fresh |
| `Future.get()` timeout | Catch exception, fallback to device-only |
| Invalid `SelectAccount` | Validated against writable list, rejected if not found |

## Verification

1. `./gradlew app:ktlintFormat && ./gradlew build` — compiles
2. `./gradlew test` — all tests pass
3. Install on emulator with single account → static footer, no icon
4. (If possible) add second account → footer gets `^`, tap opens sheet, selection works
5. Kill app, reopen → selected account preserved, list reloaded

## Sources

- **Origin brainstorm:** [docs/brainstorms/2026-04-15-account-selector-brainstorm.md](docs/brainstorms/2026-04-15-account-selector-brainstorm.md)
- **Pattern reference:** `OtherFieldsBottomSheet.kt` — state-driven ModalBottomSheet
- **Account API:** `AccountTypeManager.java:346` — `insertableFilter()`
- **Existing DI:** `ContactCreationProvidesModule.kt` — `AccountTypeManager` already provided
- **Security:** Validate `SelectAccount` against writable accounts list
