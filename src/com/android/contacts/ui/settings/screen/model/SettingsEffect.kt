package com.android.contacts.ui.settings.screen.model

internal sealed interface SettingsEffect {

    sealed interface Host : SettingsEffect
    sealed interface Message : SettingsEffect

    data object CreateProfile : Host
    data object OpenAddAccount : Host
    data object OpenDefaultAccountPicker : Host
    data object OpenContactsFilter : Host
    data object ShowImportDialog : Host
    data object ShowExportDialog : Host
    data object OpenBlockedNumbers : Host
    data object OpenLicenses : Host
    data object ShowSimImportFailure : Message

    data class OpenProfile(
        val contactId: Long,
    ) : Host

    data class ShowSimImportSuccess(
        val importedCount: Int,
    ) : Message
}
