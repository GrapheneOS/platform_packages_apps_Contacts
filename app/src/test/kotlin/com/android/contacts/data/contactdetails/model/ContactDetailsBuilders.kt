package com.android.contacts.data.contactdetails.model

import com.android.contacts.data.contactdetails.model.ContactGroup
import android.net.Uri

internal fun contactCapabilities(
    isDirectoryEntry: Boolean = false,
    isAddableDirectoryContact: Boolean = false,
    isInvisibleAndAddable: Boolean = false,
    isUserProfile: Boolean = false,
    hasMultipleRawContacts: Boolean = false,
    areAllRawContactsSimAccounts: Boolean = false,
): ContactCapabilities {
    return ContactCapabilities(
        isDirectoryEntry = isDirectoryEntry,
        isAddableDirectoryContact = isAddableDirectoryContact,
        isInvisibleAndAddable = isInvisibleAndAddable,
        isUserProfile = isUserProfile,
        hasMultipleRawContacts = hasMultipleRawContacts,
        areAllRawContactsSimAccounts = areAllRawContactsSimAccounts,
    )
}

internal fun contactDetails(
    contactId: Long = 7L,
    lookupKey: String? = "lookup-key",
    lookupUri: Uri? = null,
    nameRawContactId: Long = 11L,
    displayName: String? = "Alex Doe",
    alternativeDisplayName: String? = "Doe, Alex",
    phoneticName: String? = null,
    displayNameSource: ContactDisplayNameSource = ContactDisplayNameSource.STRUCTURED_NAME,
    isStarred: Boolean = false,
    photoId: Long = 0L,
    photo: ContactPhoto? = null,
    isSendToVoicemail: Boolean = false,
    customRingtone: String? = null,
    customRingtoneTitle: String? = null,
    groups: List<ContactGroup> = emptyList(),
    dataItems: List<ContactDataItem> = emptyList(),
    capabilities: ContactCapabilities = contactCapabilities(),
): ContactDetails {
    return ContactDetails(
        contactId = contactId,
        lookupKey = lookupKey,
        lookupUri = lookupUri,
        nameRawContactId = nameRawContactId,
        displayName = displayName,
        alternativeDisplayName = alternativeDisplayName,
        phoneticName = phoneticName,
        displayNameSource = displayNameSource,
        isStarred = isStarred,
        photoId = photoId,
        photo = photo,
        isSendToVoicemail = isSendToVoicemail,
        customRingtone = customRingtone,
        customRingtoneTitle = customRingtoneTitle,
        groups = groups,
        dataItems = dataItems,
        capabilities = capabilities,
    )
}
