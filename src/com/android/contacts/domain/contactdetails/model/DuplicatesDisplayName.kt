package com.android.contacts.domain.contactdetails.model

import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource

internal fun ContactDataItem.Nickname.duplicatesDisplayName(details: ContactDetails): Boolean {
    val isNameRawContact = details.nameRawContactId == rawContactId

    return isNameRawContact && details.displayNameSource == ContactDisplayNameSource.NICKNAME
}
