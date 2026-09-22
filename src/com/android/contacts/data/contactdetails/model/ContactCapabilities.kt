package com.android.contacts.data.contactdetails.model

internal data class ContactCapabilities(
    val isDirectoryEntry: Boolean,
    val isAddableDirectoryContact: Boolean,
    val isInvisibleAndAddable: Boolean,
    val isUserProfile: Boolean,
    val hasMultipleRawContacts: Boolean,
    val areAllRawContactsSimAccounts: Boolean,
)
