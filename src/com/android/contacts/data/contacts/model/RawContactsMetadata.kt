package com.android.contacts.data.contacts.model

internal data class RawContactsMetadata(
    val contactId: Long,
    val isUserProfile: Boolean,
    val rawContacts: List<RawContact>,
)
