# ViewModel Pattern Generator

Generate @HiltViewModel + Action/Effect/UiState following this project's MVI conventions.

## When to Use

Creating a new ViewModel or modifying the existing ContactCreationViewModel.

## Complete ViewModel Template

```kotlin
@HiltViewModel
internal class XxxViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val fieldsDelegate: ContactFieldsDelegate,
    private val deltaMapper: RawContactDeltaMapper,
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<XxxUiState>("state") ?: XxxUiState()
    )
    val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

    // Derived flows per section — prevents cross-section recomposition
    val phones: StateFlow<List<PhoneFieldState>> = _uiState
        .map { it.phoneNumbers }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val emails: StateFlow<List<EmailFieldState>> = _uiState
        .map { it.emails }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val nameState: StateFlow<NameState> = _uiState
        .map { it.nameState }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NameState())

    // Effects — one-shot events (save result, navigation, snackbar)
    private val _effects = Channel<XxxEffect>(Channel.BUFFERED)
    val effects: Flow<XxxEffect> = _effects.receiveAsFlow()

    init {
        // Persist state to SavedStateHandle on changes
        viewModelScope.launch {
            _uiState.collect { savedStateHandle["state"] = it }
        }
    }

    fun onAction(action: XxxAction) {
        when (action) {
            is XxxAction.Save -> save()
            is XxxAction.NavigateBack -> handleBack()
            is XxxAction.AddPhone -> updateState { copy(phoneNumbers = phoneNumbers + PhoneFieldState()) }
            is XxxAction.RemovePhone -> updateState {
                copy(phoneNumbers = phoneNumbers.filterNot { it.id == action.id })
            }
            is XxxAction.UpdatePhone -> updateState {
                copy(phoneNumbers = phoneNumbers.map {
                    if (it.id == action.id) it.copy(number = action.value) else it
                })
            }
            // ... other actions
        }
    }

    private fun save() {
        val state = _uiState.value
        if (!state.hasPendingChanges()) return

        viewModelScope.launch(defaultDispatcher) {
            updateState { copy(isSaving = true) }
            val result = deltaMapper.map(state, state.selectedAccount)
            _effects.send(XxxEffect.Save(result))
        }
    }

    private fun handleBack() {
        viewModelScope.launch {
            if (_uiState.value.hasPendingChanges()) {
                _effects.send(XxxEffect.ShowDiscardDialog)
            } else {
                _effects.send(XxxEffect.NavigateBack)
            }
        }
    }

    fun onSaveResult(success: Boolean, contactUri: Uri?) {
        viewModelScope.launch {
            updateState { copy(isSaving = false) }
            if (success) {
                _effects.send(XxxEffect.SaveSuccess(contactUri))
            } else {
                _effects.send(XxxEffect.ShowError(R.string.save_failed))
            }
        }
    }

    private inline fun updateState(crossinline transform: XxxUiState.() -> XxxUiState) {
        _uiState.update { it.transform() }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up photo temp files if not saved
        cleanupTempPhotos()
    }
}
```

## UiState Template

```kotlin
@Immutable
@Parcelize
internal data class XxxUiState(
    // Name — grouped sub-state
    val nameState: NameState = NameState(),
    // Repeatable fields — List (PersistentList IS-A List, zero-cost upcast from delegate)
    val phoneNumbers: List<PhoneFieldState> = listOf(PhoneFieldState()),
    val emails: List<EmailFieldState> = listOf(EmailFieldState()),
    val addresses: List<AddressFieldState> = emptyList(),
    // ... more fields
    // Photo
    val photoUri: Uri? = null,
    // Account
    val selectedAccount: AccountWithDataSet? = null,
    val availableAccounts: List<AccountInfo> = emptyList(),
    // UI state
    val showAllFields: Boolean = false,
    val isSaving: Boolean = false,
) : Parcelable {
    fun hasPendingChanges(): Boolean =
        nameState.hasData() ||
        phoneNumbers.any { it.number.isNotBlank() } ||
        emails.any { it.address.isNotBlank() } ||
        photoUri != null
        // ... check all fields
}

@Parcelize
internal data class PhoneFieldState(
    val id: String = UUID.randomUUID().toString(),
    val number: String = "",
    val type: PhoneType = PhoneType.Mobile,
) : Parcelable

@Parcelize
internal data class EmailFieldState(
    val id: String = UUID.randomUUID().toString(),
    val address: String = "",
    val type: EmailType = EmailType.Home,
) : Parcelable
```

## Action Template

```kotlin
internal sealed interface XxxAction {
    // Navigation
    data object NavigateBack : XxxAction
    data object Save : XxxAction
    data object ConfirmDiscard : XxxAction

    // Name
    data class UpdateFirstName(val value: String) : XxxAction
    data class UpdateLastName(val value: String) : XxxAction
    // ... other name fields

    // Repeatable fields — Add/Remove/Update pattern
    data object AddPhone : XxxAction
    data class RemovePhone(val id: String) : XxxAction
    data class UpdatePhone(val id: String, val value: String) : XxxAction
    data class UpdatePhoneType(val id: String, val type: PhoneType) : XxxAction
    // ... same for email, address, etc.

    // Photo
    data class SetPhoto(val uri: Uri) : XxxAction
    data object RemovePhoto : XxxAction

    // Account
    data class SelectAccount(val account: AccountWithDataSet) : XxxAction

    // More fields
    data object ToggleMoreFields : XxxAction
}
```

## Effect Template

```kotlin
internal sealed interface XxxEffect {
    data class Save(val result: DeltaMapperResult) : XxxEffect
    data class SaveSuccess(val contactUri: Uri?) : XxxEffect
    data class ShowError(val messageResId: Int) : XxxEffect
    data object ShowDiscardDialog : XxxEffect
    data object NavigateBack : XxxEffect
}
```

## Activity Effect Collection

```kotlin
// In ContactCreationActivity or a top-level composable
LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
        when (effect) {
            is Effect.Save -> {
                val intent = ContactSaveService.createSaveContactIntent(
                    context, effect.result.state,
                    "saveMode", SaveMode.CLOSE, false,
                    ContactCreationActivity::class.java,
                    SAVE_COMPLETED_ACTION,
                    effect.result.updatedPhotos, null, null,
                )
                context.startService(intent)
            }
            is Effect.SaveSuccess -> (context as? Activity)?.finish()
            is Effect.ShowError -> snackbarHostState.showSnackbar(context.getString(effect.messageResId))
            is Effect.ShowDiscardDialog -> showDiscardDialog = true
            is Effect.NavigateBack -> (context as? Activity)?.finish()
        }
    }
}
```

## Rules

- Single `MutableStateFlow<UiState>` in ViewModel — not per-field flows
- Derived `StateFlow` per section via `.map { }.distinctUntilChanged().stateIn()` (including `nameState`)
- `@Immutable` on UiState, `List<T>` fields (PersistentList IS-A List, zero-cost upcast)
- `PersistentList` used internally in delegate for efficient structural sharing
- UUID as stable ID for each field row
- `SavedStateHandle` for process death — sync via `init { collect {} }`
- Effects via `Channel(BUFFERED)` + `receiveAsFlow()`
- Mapper runs on `@DefaultDispatcher`
- Clean up temp files in `onCleared()`
