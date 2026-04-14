# Compose Test Generator

Generate Compose UI tests using testTag() and lambda capture (no MockK in UI layer).

## When to Use

**BEFORE creating or modifying a Compose screen or section component.** We follow Spec-Driven Development:

1. Read the plan phase requirements — these are the test specs
2. Write ALL tests FIRST — they must fail (red)
3. Create stub source files with `TODO()` — tests compile but fail
4. Implement — tests pass (green)
5. `./gradlew build` — all green

**Test files are always created before source files.**

## UI Test Pattern (androidTest)

```kotlin
class XxxScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val capturedActions = mutableListOf<XxxAction>()

    @Before
    fun setup() {
        capturedActions.clear()
    }

    // --- Rendering tests ---

    @Test
    fun initialState_showsExpectedFields() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.XXX_FIELD).assertIsDisplayed()
    }

    @Test
    fun emptyState_hidesOptionalSection() {
        setContent(state = XxxUiState(optionalItems = persistentListOf()))
        composeTestRule.onNodeWithTag(TestTags.OPTIONAL_SECTION).assertDoesNotExist()
    }

    // --- Interaction tests ---

    @Test
    fun tapSave_dispatchesSaveAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performClick()
        assertEquals(XxxAction.Save, capturedActions.last())
    }

    @Test
    fun typeInField_dispatchesUpdateAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.xxxField(0)).performTextInput("hello")
        assertIs<XxxAction.UpdateXxx>(capturedActions.last())
    }

    @Test
    fun tapAddButton_dispatchesAddAction() {
        setContent()
        composeTestRule.onNodeWithTag(TestTags.XXX_ADD).performClick()
        assertEquals(XxxAction.AddXxx, capturedActions.last())
    }

    @Test
    fun tapDelete_dispatchesRemoveAction() {
        setContent(state = XxxUiState(
            items = persistentListOf(XxxFieldState(id = "1"), XxxFieldState(id = "2"))
        ))
        composeTestRule.onNodeWithTag(TestTags.xxxDelete(1)).performClick()
        assertIs<XxxAction.RemoveXxx>(capturedActions.last())
    }

    // --- Disabled state tests ---

    @Test
    fun savingState_disablesSaveButton() {
        setContent(state = XxxUiState(isSaving = true))
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).assertIsNotEnabled()
    }

    // --- Helper ---

    private fun setContent(state: XxxUiState = XxxUiState()) {
        composeTestRule.setContent {
            AppTheme {
                XxxScreen(
                    uiState = state,
                    onAction = { capturedActions.add(it) },
                )
            }
        }
    }
}
```

## ViewModel Test Pattern (test — Robolectric)

```kotlin
@RunWith(RobolectricTestRunner::class)
class XxxViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun saveAction_emitsSaveEffect() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(initialState = stateWithData())
        vm.effects.test {
            vm.onAction(XxxAction.Save)
            assertIs<XxxEffect.Save>(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun addAction_addsEmptyRow() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel()
        val initialCount = vm.uiState.value.items.size
        vm.onAction(XxxAction.AddXxx)
        assertEquals(initialCount + 1, vm.uiState.value.items.size)
    }

    @Test
    fun removeAction_removesRow() = runTest(mainDispatcherRule.testDispatcher) {
        val vm = createViewModel(initialState = XxxUiState(
            items = persistentListOf(XxxFieldState(id = "1"), XxxFieldState(id = "2"))
        ))
        vm.onAction(XxxAction.RemoveXxx("1"))
        assertEquals(1, vm.uiState.value.items.size)
        assertEquals("2", vm.uiState.value.items.first().id)
    }

    private fun createViewModel(
        initialState: XxxUiState = XxxUiState(),
        fieldsDelegate: ContactFieldsDelegate = FakeContactFieldsDelegate(),
    ): XxxViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("state" to initialState))
        return XxxViewModel(savedStateHandle, fieldsDelegate)
    }
}
```

## Mapper Test Pattern (test — pure JUnit, highest priority)

```kotlin
class RawContactDeltaMapperTest {
    private val mapper = RawContactDeltaMapper()

    @Test
    fun mapsFieldType_toCorrectMimeType() {
        val state = XxxUiState(/* field data */)
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(EXPECTED_MIME_TYPE)
        assertNotNull(entries)
        assertEquals(expectedValue, entries!![0].getAsString(EXPECTED_COLUMN))
    }

    @Test
    fun emptyField_notIncluded() {
        val state = XxxUiState(items = persistentListOf(XxxFieldState(value = "")))
        val result = mapper.map(state, account = null)
        val entries = result.state[0].getMimeEntries(EXPECTED_MIME_TYPE)
        assertTrue(entries.isNullOrEmpty())
    }

    @Test
    fun customTypeLabel_setsBothTypeAndLabel() {
        // TYPE_CUSTOM requires BOTH type column AND label column
        val state = XxxUiState(items = persistentListOf(
            XxxFieldState(value = "data", type = XxxType.Custom("My Label"))
        ))
        val result = mapper.map(state, account = null)
        val entry = result.state[0].getMimeEntries(EXPECTED_MIME_TYPE)!![0]
        assertEquals(TYPE_CUSTOM_VALUE, entry.getAsInteger(TYPE_COLUMN))
        assertEquals("My Label", entry.getAsString(LABEL_COLUMN))
    }
}
```

## Rules

- **NEVER** use `onNodeWithText()` — always `onNodeWithTag()`
- **NEVER** use MockK in UI tests — use lambda capture `onAction = { capturedActions.add(it) }`
- MockK is OK in ViewModel tests for dependencies (not for screenModel/onAction)
- Use Turbine `flow.test { }` for Effect assertions, always call `cancelAndIgnoreRemainingEvents()`
- Use `FakeContactFieldsDelegate` (not mockk) for ViewModel tests
- Mapper tests are highest priority — test ALL 13 field types
- Every `testTag` used in tests must exist in `TestTags.kt`
