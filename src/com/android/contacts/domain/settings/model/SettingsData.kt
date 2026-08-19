package com.android.contacts.domain.settings.model

import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.SettingsAvailability

internal data class SettingsData(
    val availability: SettingsAvailability,
    val displaySettings: DisplaySettings,
    val defaultAccountLabel: String?,
    val buildVersion: String?,
    val isCallLogPermissionGranted: Boolean,
)
