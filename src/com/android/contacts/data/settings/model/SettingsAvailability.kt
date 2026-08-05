package com.android.contacts.data.settings.model

internal data class SettingsAvailability(
    val areContactsAvailable: Boolean,
    val areBlockedNumbersAvailable: Boolean,
    val isAboutAvailable: Boolean,
)
