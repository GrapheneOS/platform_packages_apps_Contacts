package com.android.contacts.tests.factory

import android.net.Uri
import com.android.contacts.data.contactdetails.model.ContactCapabilities
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactPhoto

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
    customRingtone: String? = null,
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
        customRingtone = customRingtone,
        dataItems = dataItems,
        capabilities = capabilities,
    )
}
