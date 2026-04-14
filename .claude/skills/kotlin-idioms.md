# Kotlin Idiomatic Review

Review Kotlin code for idiomatic patterns per official conventions (kotlinlang.org/docs/coding-conventions.html).

## When to Use

After implementing features — review all new/modified .kt files.

## Checklist

### 1. val vs var + Backing Properties
- [ ] Every `var` justified — could it be `val`?
- [ ] **Backing property pattern** for mutable state:
  ```kotlin
  // Private mutable, public read-only
  private val _uiState = MutableStateFlow(UiState())
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  // For collections:
  private val _phones = mutableListOf<Phone>()
  val phones: List<Phone> get() = _phones
  ```
- [ ] Property access syntax: `val phones: List<Phone>` not `fun getPhones(): List<Phone>`
- [ ] `var` in data classes = code smell (use `copy()`)
- [ ] Mutable state only via `MutableStateFlow` / `MutableState` — never bare `var` for observable state

### 2. Immutability
- [ ] Return `List`, not `MutableList` in public APIs
- [ ] `PersistentList` for hot-path structural sharing (delegate internals)
- [ ] Data classes: all `val` properties
- [ ] `buildList { }` when conditional additions needed (not `mutableListOf()` + manual adds)

### 3. Expression Body Functions
```kotlin
// Block body — unnecessary for single expression
fun double(x: Int): Int { return x * 2 }

// Expression body — idiomatic
fun double(x: Int) = x * 2

// When as expression — very idiomatic
fun label(type: PhoneType) = when (type) {
    PhoneType.Mobile -> "Mobile"
    PhoneType.Home -> "Home"
    PhoneType.Work -> "Work"
    is PhoneType.Custom -> type.label
}
```
- [ ] Single-expression functions use `= expr`
- [ ] Multi-statement functions use block body
- [ ] `when` as expression wherever possible

### 4. Scope Functions
```kotlin
// let — null-safe transform
uri?.let { viewModel.setPhoto(it) }

// apply — configure an object (builder pattern)
ContentValues().apply {
    put(Data.MIMETYPE, mimeType)
    put(Phone.NUMBER, number)
}

// also — side effects
result.also { log("Saved: $it") }

// run — compute + return on receiver
account.run { "$name ($type)" }
```
- [ ] No nested scope functions (`.let { it.also { ... } }`)
- [ ] `apply` for builders, `let` for transforms, `also` for side effects
- [ ] `with` sparingly — prefer `run` in most cases

### 5. Null Safety
- [ ] **Never `!!`** — use `?.`, `?:`, `requireNotNull()`, `checkNotNull()`
- [ ] Elvis for early exit: `val x = y ?: return`
- [ ] Elvis with error: `val x = y ?: error("expected y")`
- [ ] Prefer non-null types in function signatures
- [ ] `require()` for argument validation, `check()` for state validation

### 6. Sealed Types
- [ ] `sealed interface` over `sealed class` (unless shared state)
- [ ] `data object` for parameterless variants (singleton, proper equals/hashCode)
- [ ] `data class` for parameterized variants
- [ ] `when` exhaustive — no `else` on sealed types
- [ ] `error("Unknown: $x")` for truly unreachable else branches

### 7. Collection Operations
```kotlin
// GOOD — functional chain
val names = contacts.filter { it.isActive }.map { it.name }

// BAD — imperative loop
val names = mutableListOf<String>()
for (c in contacts) { if (c.isActive) names.add(c.name) }
```
- [ ] `map`/`filter`/`fold` over imperative loops
- [ ] `firstOrNull` over manual find
- [ ] `associateBy`/`groupBy` for lookups
- [ ] `buildList { }` for conditional list building
- [ ] `asSequence()` for 3+ chained ops on large collections

### 8. Named Arguments + Trailing Lambdas
- [ ] Named args for >2 params of same type
- [ ] Named args for all booleans: `setVisible(visible = true)`
- [ ] Trailing lambda: `items(key = { it.id }) { item -> ... }`

### 9. Kotlin Stdlib Helpers
```kotlin
// buildList instead of mutableListOf + manual adds
val ops = buildList {
    add(insertRawContact)
    if (hasName) add(insertName)
}

// buildString instead of StringBuilder
val label = buildString {
    append(firstName)
    if (lastName.isNotBlank()) append(" $lastName")
}
```
- [ ] `require(condition) { msg }` for argument checks
- [ ] `check(condition) { msg }` for state checks
- [ ] `error(msg)` for unreachable branches

### 10. Coroutine Idioms
- [ ] Backing property: `private val _effects = Channel<Effect>(BUFFERED)` / `val effects = _effects.receiveAsFlow()`
- [ ] `withContext(dispatcher)` for switching, `launch` for fire-and-forget
- [ ] Inject dispatchers (never hardcode `Dispatchers.IO`)
- [ ] Suspend functions must be main-safe
- [ ] Never catch `CancellationException`

### 11. Compose-Specific
- [ ] `remember { }` only for expensive computations
- [ ] Lambdas in params should be stable (avoid creating new instances)
- [ ] `Modifier` always last param, always default `Modifier`
- [ ] `derivedStateOf` for computed values changing less often than inputs
- [ ] No business logic in composables — delegate to ViewModel

### 12. Property Delegates
```kotlin
// Lazy initialization
val adapter: MyAdapter by lazy { MyAdapter() }

// SavedStateHandle delegate
var pendingUri: Uri?
    get() = savedStateHandle.get<Uri>(KEY)
    set(value) { savedStateHandle[KEY] = value }
```
- [ ] `by lazy` for expensive one-time init
- [ ] Custom get/set for SavedStateHandle-backed properties
- [ ] Companion object only for factory methods or constants needed by Java interop

## How to Apply

```bash
# Find all changed Kotlin files
git diff upstream/main --name-only -- '*.kt'

# For each: check backing properties, var usage, scope functions, null safety
```
