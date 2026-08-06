package com.android.contacts.ui.settings.screen.model

internal sealed interface SettingsEffect {

    data object CreateProfile : SettingsEffect
    data object OpenAddAccount : SettingsEffect
    data object OpenDefaultAccountPicker : SettingsEffect
    data object OpenContactsFilter : SettingsEffect
    data object ShowImportDialog : SettingsEffect
    data object ShowExportDialog : SettingsEffect
    data object OpenBlockedNumbers : SettingsEffect
    data object OpenLicenses : SettingsEffect
    data object ShowSimImportFailure : SettingsEffect

    data class OpenProfile(
        val contactId: Long,
    ) : SettingsEffect

    data class ShowSimImportSuccess(
        val importedCount: Int,
    ) : SettingsEffect
}
