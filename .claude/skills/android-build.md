# Android Build & Lint

Run build, lint, and test commands with structured error parsing.

## Usage

Trigger when: code changes need validation, before commits, after implementation phases.

## Commands

### Full Build (includes ktlint + detekt)
```bash
cd ~/Documents/development/foss/platform_packages_apps_Contacts && ./gradlew build 2>&1
```

### Unit Tests Only (fast — Robolectric)
```bash
cd ~/Documents/development/foss/platform_packages_apps_Contacts && ./gradlew test 2>&1
```

### Compose UI Tests (requires emulator/device)
```bash
cd ~/Documents/development/foss/platform_packages_apps_Contacts && ./gradlew connectedAndroidTest 2>&1
```

### Lint Only
```bash
cd ~/Documents/development/foss/platform_packages_apps_Contacts && ./gradlew app:ktlintCheck app:detekt 2>&1
```

### Auto-fix Lint
```bash
cd ~/Documents/development/foss/platform_packages_apps_Contacts && ./gradlew app:ktlintFormat 2>&1
```

## Error Parsing

When build fails:
1. Look for `> Task :app:compile*` lines — compilation errors
2. Look for `ktlint` violations — format with `ktlintFormat`, then re-check
3. Look for `detekt` findings — fix manually (detekt has no auto-fix)
4. Look for test failures — read the failure message, fix the test or source

## Pre-commit Checklist

Run in order:
1. `./gradlew app:ktlintFormat` — auto-fix formatting
2. `./gradlew build` — verify everything passes
3. If build passes → safe to commit
