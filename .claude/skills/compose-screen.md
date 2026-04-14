# Compose Screen Generator

Generate a new Compose screen following this project's state-down/events-up MVI pattern.

## When to Use

Creating a new screen or major section composable in the contactcreation package.

## Pattern

### Screen Composable

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun XxxScreen(
    uiState: XxxUiState,
    onAction: (XxxAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.xxx_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(XxxAction.NavigateBack) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            // Each section gets ONLY its state slice
            xxxSection(uiState.sectionData, onAction)
        }
    }
}
```

### LazyListScope Section Extensions

```kotlin
internal fun LazyListScope.xxxSection(
    items: PersistentList<XxxState>,
    onAction: (XxxAction) -> Unit,
) {
    items(
        items = items,
        key = { it.id },  // stable UUID, NOT list index
        contentType = { "xxx_field" },
    ) { item ->
        XxxFieldRow(
            state = item,
            onValueChanged = { onAction(XxxAction.UpdateXxx(item.id, it)) },
            onDelete = { onAction(XxxAction.RemoveXxx(item.id)) },
            modifier = Modifier
                .testTag(TestTags.xxxField(items.indexOf(item)))
                .animateItem(),
        )
    }
    item(key = "xxx_add") {
        AddFieldButton(
            label = stringResource(R.string.add_xxx),
            onClick = { onAction(XxxAction.AddXxx) },
            modifier = Modifier.testTag(TestTags.XXX_ADD),
        )
    }
}
```

### UiState

```kotlin
@Parcelize
internal data class XxxUiState(
    val items: PersistentList<XxxFieldState> = persistentListOf(XxxFieldState()),
    val isLoading: Boolean = false,
) : Parcelable

@Parcelize
internal data class XxxFieldState(
    val id: String = UUID.randomUUID().toString(),
    val value: String = "",
    val type: XxxType = XxxType.DEFAULT,
) : Parcelable
```

### Action / Effect

```kotlin
internal sealed interface XxxAction {
    data object NavigateBack : XxxAction
    data object Save : XxxAction
    data class UpdateXxx(val id: String, val value: String) : XxxAction
    data class RemoveXxx(val id: String) : XxxAction
    data object AddXxx : XxxAction
}

internal sealed interface XxxEffect {
    data class Save(val result: DeltaMapperResult) : XxxEffect
    data object NavigateBack : XxxEffect
    data class ShowError(val messageResId: Int) : XxxEffect
}
```

## Checklist

- [ ] All composables `internal`
- [ ] State class `@Parcelize`
- [ ] `PersistentList` for repeatable fields
- [ ] Stable `key` (UUID) on list items — never list index
- [ ] `contentType` on list items
- [ ] `animateItem()` modifier on items
- [ ] `testTag()` on all interactive elements
- [ ] Section receives only its state slice, not full UiState
- [ ] Strings from `R.string.*`, never hardcoded
