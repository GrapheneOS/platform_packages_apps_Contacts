package com.android.contacts.ui.settings.screen.model

import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder

internal sealed interface SettingsAction {

    data object LicensesClicked : SettingsAction

    data class ItemClicked(
        val id: SettingsItemId,
    ) : SettingsAction

    data class SortOrderSelected(
        val sortOrder: SortOrder,
    ) : SettingsAction

    data class DisplayOrderSelected(
        val displayOrder: DisplayOrder,
    ) : SettingsAction

    data class PhoneticNameDisplaySelected(
        val phoneticNameDisplay: PhoneticNameDisplay,
    ) : SettingsAction
}
