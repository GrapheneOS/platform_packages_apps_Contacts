---
date: 2026-04-15
topic: account-selector-bottom-sheet
---

# Account Selector Bottom Sheet

## What We're Building

Interactive account selector triggered from the existing "Saving to..." footer. Tapping the footer opens a `ModalBottomSheet` listing all writable accounts. User picks one, sheet dismisses, footer updates.

## Key Decisions

- **Trigger**: Footer text "Saving to {account}" with `^` (KeyboardArrowUp) icon, 8dp padding, tappable
- **Single account**: Static text, no icon, not tappable (option a)
- **Default selection**: First writable account from system on init (option b)
- **Sheet rows**: Account name + type label + icon from AccountInfo (option c)
- **Device account**: Distinct device/phone icon, no extra "Not synced" text (option c)
- **Selection indicator**: Checkmark on currently selected account
- **Component**: M3 `ModalBottomSheet` with `ListItem` rows

## Data Flow

```
ViewModel.init() → loadWritableAccounts() → UiState.availableAccounts
Footer tap → showAccountSheet = true → ModalBottomSheet
User selects → ContactCreationAction.SelectAccount → UiState updates → footer text updates
```

## Open Questions

None — ready for planning.

## Next Steps

→ `/ce:plan` for implementation details
