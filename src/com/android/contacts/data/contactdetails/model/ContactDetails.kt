package com.android.contacts.data.contactdetails.model

import android.net.Uri

internal data class ContactDetails(
    val contactId: Long,
    val lookupKey: String?,
    val lookupUri: Uri?,
    val nameRawContactId: Long,
    val displayName: String?,
    val alternativeDisplayName: String?,
    val phoneticName: String?,
    val displayNameSource: ContactDisplayNameSource,
    val isStarred: Boolean,
    val photoId: Long,
    val photo: ContactPhoto?,
    val isSendToVoicemail: Boolean,
    val customRingtone: String?,
    val customRingtoneTitle: String?,
    val groups: List<ContactGroup>,
    val dataItems: List<ContactDataItem>,
    val capabilities: ContactCapabilities,
)
