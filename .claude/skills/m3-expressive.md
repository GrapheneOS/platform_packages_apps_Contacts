# Material 3 Expressive

Apply M3 Expressive design patterns in this project.

## When to Use

When adding UI components, animations, or theming to Compose screens.

## Theme Setup

```kotlin
// In AppTheme (ui/core/Theme.kt)
@Composable
fun AppTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        dynamicLightColorScheme(LocalContext.current)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),  // <-- enables spring-based motion
        shapes = Shapes,
        content = content,
    )
}
```

**IMPORTANT:** Do NOT use `MaterialExpressiveTheme` — it's alpha-only and unstable. Use `MaterialTheme` with `MotionScheme.expressive()` parameter.

## Available Components

### Stable (use freely)
- `LargeTopAppBar` with `exitUntilCollapsedScrollBehavior()`
- `Scaffold`, `Surface`, `Card`
- `OutlinedTextField`, `TextField`
- `Switch`, `Checkbox`, `RadioButton`
- `AlertDialog`
- `ModalBottomSheet`
- `HorizontalDivider`
- `Icon`, `IconButton`, `TextButton`, `FilledTonalButton`
- `DropdownMenu`, `DropdownMenuItem`
- All Material Icons (`Icons.Filled`, `Icons.Outlined`, `Icons.AutoMirrored`)

### Does NOT Exist (don't search for these)
- ~~`ExpressiveTopAppBar`~~ — use `LargeTopAppBar`
- ~~`ExpressiveButton`~~ — use standard buttons with spring animations

### Experimental (use with `@OptIn(ExperimentalMaterial3ExpressiveApi::class)`)
- `FloatingActionButtonMenu` / `ToggleFloatingActionButton` — speed-dial FAB
- `FloatingActionButtonMenuItem`
- Expressive list items
- `AppBarWithSearch` — integrated search in top bar

## Animation Patterns

### Spring Animations (default with MotionScheme.expressive())

```kotlin
// Spring constants for different feels
spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)  // Gentle bounce
spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)      // Smooth, no overshoot
```

### animateItem() on LazyColumn (field add/remove)

```kotlin
LazyColumn {
    items(fields, key = { it.id }) { field ->
        FieldRow(
            modifier = Modifier.animateItem(
                fadeInSpec = spring(stiffness = Spring.StiffnessMediumLow),
                fadeOutSpec = spring(stiffness = Spring.StiffnessMedium),
            )
        )
    }
}
```

### AnimatedVisibility (expand/collapse sections)

```kotlin
AnimatedVisibility(
    visible = expanded,
    enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
    exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMedium)) + fadeOut(),
) {
    MoreFieldsContent()
}
```

### Shape Morphing (photo avatar)

```kotlin
val shape by animateShape(
    targetValue = if (pressed) RoundedCornerShape(16.dp) else CircleShape,
    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
)
Image(
    modifier = Modifier.clip(shape).size(96.dp),
    // ...
)
```

## Accessibility

```kotlin
// ALWAYS check before applying spring animations
val reduceMotion = LocalReduceMotion.current
val animSpec = if (reduceMotion) snap() else spring(stiffness = Spring.StiffnessMediumLow)
```

## Color & Typography

```kotlin
// Use M3 color roles, not hardcoded colors
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.onSurface
MaterialTheme.colorScheme.onSurfaceVariant
MaterialTheme.colorScheme.outlineVariant
MaterialTheme.colorScheme.surfaceContainerLow

// Typography
MaterialTheme.typography.headlineMedium  // Screen title
MaterialTheme.typography.bodyLarge        // Field labels
MaterialTheme.typography.bodyMedium       // Field values
MaterialTheme.typography.labelLarge       // Section headers
MaterialTheme.typography.labelMedium      // Chips, buttons
```

## Icon Mapping (from legacy drawable → Material Icons Compose)

| Field Type | Material Icon |
|-----------|---------------|
| Name | `Icons.Filled.Person` |
| Phone | `Icons.Filled.Phone` |
| Email | `Icons.Filled.Email` |
| Address | `Icons.Filled.Place` |
| Organization | `Icons.Filled.Business` |
| Website | `Icons.Filled.Public` |
| Event | `Icons.Filled.Event` |
| Note | `Icons.Filled.Notes` |
| Relation | `Icons.Filled.People` |
| IM | `Icons.Filled.Message` |
| SIP | `Icons.Filled.DialerSip` |
| Group | `Icons.Filled.Label` |
| Photo | `Icons.Filled.CameraAlt` |
| Add field | `Icons.Filled.Add` |
| Delete field | `Icons.Filled.Close` |
| Back | `Icons.AutoMirrored.Filled.ArrowBack` |
| Expand | `Icons.Filled.ExpandMore` |
| Collapse | `Icons.Filled.ExpandLess` |
