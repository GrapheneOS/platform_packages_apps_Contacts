# Spec-Driven Development Workflow

Enforce test-first development driven by the plan as specification.

## When to Use

At the START of every implementation phase. This skill defines the execution order.

## The Cycle

```
PLAN (spec) → TESTS (red) → STUBS (compile) → IMPL (green) → LINT → COMMIT
```

### Step 1: Read Spec

Read the current phase from the plan:
```bash
cat docs/plans/2026-04-14-feat-contact-creation-compose-rewrite-plan.md
```
Extract:
- Phase deliverables
- SDD order (test-first sequence)
- Files to create
- Success criteria

### Step 2: Write Tests (Red)

For each component in the phase, write tests FIRST:

| Component type | Test file location | Write before |
|---------------|-------------------|--------------|
| Mapper | `app/src/test/.../mapper/RawContactDeltaMapperTest.kt` | `RawContactDeltaMapper.kt` |
| ViewModel | `app/src/test/.../ContactCreationViewModelTest.kt` | `ContactCreationViewModel.kt` |
| Delegate | `app/src/test/.../delegate/ContactFieldsDelegateTest.kt` | `ContactFieldsDelegate.kt` |
| UI Screen | `app/src/androidTest/.../ContactCreationEditorScreenTest.kt` | `ContactCreationEditorScreen.kt` |
| UI Section | `app/src/androidTest/.../component/PhoneSectionTest.kt` | `PhoneSection.kt` |

Tests reference classes that don't exist yet — they won't compile.

### Step 3: Create Stubs (Compiles, Fails)

Create minimal source files with `TODO()` bodies so tests compile:

```kotlin
// Stub — just enough to compile
class RawContactDeltaMapper @Inject constructor() {
    fun map(uiState: ContactCreationUiState, account: AccountWithDataSet?): DeltaMapperResult =
        TODO("Phase 1b: implement mapper")
}
```

Run tests: they compile but FAIL. This is correct.

```bash
./gradlew test 2>&1 | tail -20  # Expect failures
```

### Step 4: Implement (Green)

Now write the real implementation. Replace each `TODO()` with working code.

After each component:
```bash
./gradlew test 2>&1 | grep -E "(PASSED|FAILED|Tests)"
```

Continue until ALL tests pass.

### Step 5: Lint + Build

```bash
./gradlew app:ktlintFormat && ./gradlew build
```

Fix any lint/detekt issues.

### Step 6: Commit

```
feat(contacts): Phase Xb - [description]

- Tests written first (SDD)
- [key deliverables]
```

## Test Priority Order (within a phase)

1. **Mapper tests** — highest risk, data correctness
2. **Delegate tests** — business logic
3. **ViewModel tests** — state management + effects
4. **UI section tests** — component rendering
5. **Screen tests** — integration of sections

This order ensures the deepest layers are tested first. Each layer builds on the previous.

## What Makes a Good SDD Test

```kotlin
// GOOD — tests the SPEC, not the implementation
@Test fun saveAction_withNoChanges_doesNotEmitSaveEffect() {
    // Spec: "Empty form save does nothing"
    val vm = createViewModel()
    vm.effects.test {
        vm.onAction(ContactCreationAction.Save)
        expectNoEvents()
    }
}

// BAD — tests implementation details
@Test fun saveAction_callsDelegateGetState() {
    // This couples the test to HOW, not WHAT
}
```

## Phase Checklist

Before moving to the next phase:
- [ ] All tests for this phase written
- [ ] All tests pass (`./gradlew test`)
- [ ] `./gradlew build` passes (lint + detekt clean)
- [ ] Phase success criteria met
- [ ] Committed
