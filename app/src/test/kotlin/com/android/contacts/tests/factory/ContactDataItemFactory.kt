package com.android.contacts.tests.factory

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.Im
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.provider.ContactsContract.CommonDataKinds.Note
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.Relation
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.provider.ContactsContract.CommonDataKinds.Website
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.model.dataitem.CustomDataItem

internal fun phone(
    id: Long = 1L,
    rawContactId: Long = 1L,
    number: String? = "4155551212",
    displayString: String? = number,
    formattedNumber: String? = null,
    typeLabel: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
    isCarrierVideoCallCapable: Boolean = false,
    preferredPhoneAccount: PhoneAccountId? = null,
): ContactDataItem.Phone {
    return ContactDataItem.Phone(
        id = id,
        rawContactId = rawContactId,
        mimeType = Phone.CONTENT_ITEM_TYPE,
        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,
        displayString = displayString,
        number = number,
        formattedNumber = formattedNumber,
        typeLabel = typeLabel,
        isCarrierVideoCallCapable = isCarrierVideoCallCapable,
        preferredPhoneAccount = preferredPhoneAccount,
    )
}

internal fun email(
    id: Long = 1L,
    rawContactId: Long = 1L,
    address: String? = "alex@example.org",
    data: String? = address,
    typeLabel: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): ContactDataItem.Email {
    return ContactDataItem.Email(
        id = id,
        rawContactId = rawContactId,
        mimeType = Email.CONTENT_ITEM_TYPE,
        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,
        displayString = data,
        address = address,
        data = data,
        typeLabel = typeLabel,
    )
}

internal fun postal(
    id: Long = 1L,
    rawContactId: Long = 1L,
    formattedAddress: String? = "1 Main St",
    typeLabel: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): ContactDataItem.Postal {
    return ContactDataItem.Postal(
        id = id,
        rawContactId = rawContactId,
        mimeType = StructuredPostal.CONTENT_ITEM_TYPE,
        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,
        displayString = formattedAddress,
        formattedAddress = formattedAddress,
        typeLabel = typeLabel,
    )
}

internal fun sipAddress(
    id: Long = 1L,
    rawContactId: Long = 1L,
    address: String? = "alex@sip.example.org",
    typeLabel: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): ContactDataItem.SipAddress {
    return ContactDataItem.SipAddress(
        id = id,
        rawContactId = rawContactId,
        mimeType = SipAddress.CONTENT_ITEM_TYPE,
        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,
        displayString = address,
        address = address,
        typeLabel = typeLabel,
    )
}

internal fun im(
    id: Long = 1L,
    rawContactId: Long = 1L,
    data: String? = "alex@example.org",
    protocol: Int = 0,
    customProtocol: String? = null,
    protocolLabel: String? = "AIM",
    isCustomProtocol: Boolean = false,
    chatCapability: Int = 0,
): ContactDataItem.Im {
    return ContactDataItem.Im(
        id = id,
        rawContactId = rawContactId,
        mimeType = Im.CONTENT_ITEM_TYPE,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = data,
        data = data,
        protocol = protocol,
        customProtocol = customProtocol,
        protocolLabel = protocolLabel,
        isCustomProtocol = isCustomProtocol,
        chatCapability = chatCapability,
    )
}

internal fun organization(
    id: Long = 1L,
    rawContactId: Long = 1L,
    formattedCompany: String? = "Acme",
    company: String? = "Acme",
    department: String? = null,
    title: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): ContactDataItem.Organization {
    return ContactDataItem.Organization(
        id = id,
        rawContactId = rawContactId,
        mimeType = Organization.CONTENT_ITEM_TYPE,
        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,
        displayString = formattedCompany,
        formattedCompany = formattedCompany,
        company = company,
        department = department,
        title = title,
    )
}

internal fun nickname(
    id: Long = 1L,
    rawContactId: Long = 1L,
    name: String? = "Al",
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
): ContactDataItem.Nickname {
    return ContactDataItem.Nickname(
        id = id,
        rawContactId = rawContactId,
        mimeType = Nickname.CONTENT_ITEM_TYPE,
        isPrimary = isPrimary,
        isSuperPrimary = isSuperPrimary,
        displayString = name,
        name = name,
    )
}

internal fun note(
    id: Long = 1L,
    rawContactId: Long = 1L,
    note: String? = "Met at the airport",
): ContactDataItem.Note {
    return ContactDataItem.Note(
        id = id,
        rawContactId = rawContactId,
        mimeType = Note.CONTENT_ITEM_TYPE,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = note,
        note = note,
    )
}

internal fun website(
    id: Long = 1L,
    rawContactId: Long = 1L,
    url: String? = "example.org",
): ContactDataItem.Website {
    return ContactDataItem.Website(
        id = id,
        rawContactId = rawContactId,
        mimeType = Website.CONTENT_ITEM_TYPE,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = url,
        url = url,
    )
}

internal fun event(
    isBirthday: Boolean = false,
    id: Long = 1L,
    rawContactId: Long = 1L,
    formattedDate: String? = "May 20, 1980",
    typeLabel: String? = "Birthday",
    isRecurringAnnually: Boolean = true,
): ContactDataItem.Event {
    return ContactDataItem.Event(
        id = id,
        rawContactId = rawContactId,
        mimeType = Event.CONTENT_ITEM_TYPE,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = formattedDate,
        formattedDate = formattedDate,
        typeLabel = typeLabel,
        isRecurringAnnually = isRecurringAnnually,
        isBirthday = isBirthday,
    )
}

internal fun relation(
    id: Long = 1L,
    rawContactId: Long = 1L,
    name: String? = "Sam",
    typeLabel: String? = "Father",
): ContactDataItem.Relation {
    return ContactDataItem.Relation(
        id = id,
        rawContactId = rawContactId,
        mimeType = Relation.CONTENT_ITEM_TYPE,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = name,
        name = name,
        typeLabel = typeLabel,
    )
}

internal fun custom(
    id: Long = 1L,
    rawContactId: Long = 1L,
    summary: String? = "Blood type",
    content: String? = "0",
): ContactDataItem.Custom {
    return ContactDataItem.Custom(
        id = id,
        rawContactId = rawContactId,
        mimeType = CustomDataItem.MIMETYPE_CUSTOM_FIELD,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = content,
        summary = summary,
        content = content,
    )
}

internal fun structuredName(
    id: Long = 1L,
    rawContactId: Long = 1L,
    givenName: String? = "Alex",
    isSuperPrimary: Boolean = false,
): ContactDataItem.StructuredName {
    return ContactDataItem.StructuredName(
        id = id,
        rawContactId = rawContactId,
        mimeType = StructuredName.CONTENT_ITEM_TYPE,
        isPrimary = false,
        isSuperPrimary = isSuperPrimary,
        displayString = givenName,
        givenName = givenName,
    )
}

internal fun generic(
    id: Long = 1L,
    rawContactId: Long = 1L,
    mimeType: String = "vnd.example/thing",
    displayString: String? = "Thing",
    typeColumn: String? = null,
): ContactDataItem.Generic {
    return ContactDataItem.Generic(
        id = id,
        rawContactId = rawContactId,
        mimeType = mimeType,
        isPrimary = false,
        isSuperPrimary = false,
        displayString = displayString,
        typeColumn = typeColumn,
    )
}
