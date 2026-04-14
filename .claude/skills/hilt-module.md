# Hilt Module Generator

Generate Hilt DI modules following this project's conventions.

## When to Use

When adding new injectable dependencies (especially bridging Java singletons to Hilt graph).

## @Provides Module (for Java singletons, external objects)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
internal object XxxProvidesModule {

    @Provides
    @Singleton
    fun provideAccountTypeManager(
        @ApplicationContext context: Context,
    ): AccountTypeManager = AccountTypeManager.getInstance(context)

    @Provides
    fun provideContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver = context.contentResolver
}
```

## Existing Modules

### CoreProvidesModule (already exists)
```kotlin
// di/core/CoreProvidesModule.kt
@DefaultDispatcher → Dispatchers.Default
@IoDispatcher     → Dispatchers.IO
@MainDispatcher   → Dispatchers.Main
```

### ContactCreationProvidesModule (to create)
```kotlin
// ui/contactcreation/di/ContactCreationProvidesModule.kt
@Module
@InstallIn(SingletonComponent::class)
internal object ContactCreationProvidesModule {

    @Provides
    @Singleton
    fun provideAccountTypeManager(
        @ApplicationContext context: Context,
    ): AccountTypeManager = AccountTypeManager.getInstance(context)
}
```

## Qualifier Usage

```kotlin
class MyClass @Inject constructor(
    @DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
)
```

## Rules

- Use `@Provides` for Java singletons and external objects (NOT `@Binds`)
- `@Binds` only when you have a Kotlin interface + implementation pair
- `@InstallIn(SingletonComponent::class)` for app-scoped singletons
- `@InstallIn(ViewModelComponent::class)` if scoped to ViewModel lifecycle
- Module classes are `internal object` (not abstract class)
- Activity must have `@AndroidEntryPoint`
- ViewModel must have `@HiltViewModel` + `@Inject constructor`
- Use `hiltViewModel()` from `androidx.hilt:hilt-navigation-compose` in composables
