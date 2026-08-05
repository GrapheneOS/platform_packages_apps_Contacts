package com.android.contacts.data.settings.model

internal data class DisplaySettings(
    val sortOrder: SortOrder,
    val isSortOrderChangeable: Boolean,
    val displayOrder: DisplayOrder,
    val isDisplayOrderChangeable: Boolean,
    val phoneticNameDisplay: PhoneticNameDisplay,
    val isPhoneticNameDisplayChangeable: Boolean,
)
