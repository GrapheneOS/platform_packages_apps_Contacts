package com.android.contacts.data.profile.model

internal data class ProfileData(
    val hasProfile: Boolean = false,
    val contactId: Long? = null,
    val displayName: String? = null,
    val isDisplayNameFromPhoneNumber: Boolean = false,
)
