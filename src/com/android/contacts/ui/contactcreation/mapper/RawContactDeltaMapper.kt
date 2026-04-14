package com.android.contacts.ui.contactcreation.mapper

import android.content.ContentValues
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Event
import android.provider.ContactsContract.CommonDataKinds.GroupMembership
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
import android.provider.ContactsContract.Data
import com.android.contacts.model.RawContact
import com.android.contacts.model.RawContactDelta
import com.android.contacts.model.RawContactDeltaList
import com.android.contacts.model.ValuesDelta
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import javax.inject.Inject

internal data class DeltaMapperResult(val state: RawContactDeltaList, val updatedPhotos: Bundle)

@Suppress("TooManyFunctions")
internal class RawContactDeltaMapper @Inject constructor() {

    fun map(
        uiState: ContactCreationUiState,
        account: AccountWithDataSet?,
    ): DeltaMapperResult {
        val rawContact = RawContact().apply {
            if (account != null) setAccount(account) else setAccountToLocal()
        }
        val delta = RawContactDelta(ValuesDelta.fromAfter(rawContact.values))
        val updatedPhotos = Bundle()

        mapName(delta, uiState)
        mapPhones(delta, uiState)
        mapEmails(delta, uiState)
        mapAddresses(delta, uiState)
        mapOrganization(delta, uiState)
        mapEvents(delta, uiState)
        mapRelations(delta, uiState)
        mapImAccounts(delta, uiState)
        mapWebsites(delta, uiState)
        mapNote(delta, uiState)
        mapNickname(delta, uiState)
        mapSipAddress(delta, uiState)
        mapGroups(delta, uiState)
        mapPhoto(delta, uiState, updatedPhotos)

        val state = RawContactDeltaList().apply { add(delta) }
        return DeltaMapperResult(state = state, updatedPhotos = updatedPhotos)
    }

    private fun mapName(delta: RawContactDelta, uiState: ContactCreationUiState) {
        val name = uiState.nameState
        if (!name.hasData()) return

        delta.addEntry(
            ValuesDelta.fromAfter(
                contentValues(StructuredName.CONTENT_ITEM_TYPE) {
                    putIfNotBlank(StructuredName.PREFIX, name.prefix)
                    putIfNotBlank(StructuredName.GIVEN_NAME, name.first)
                    putIfNotBlank(StructuredName.MIDDLE_NAME, name.middle)
                    putIfNotBlank(StructuredName.FAMILY_NAME, name.last)
                    putIfNotBlank(StructuredName.SUFFIX, name.suffix)
                },
            ),
        )
    }

    private fun mapPhones(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (phone in uiState.phoneNumbers) {
            if (phone.number.isBlank()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(Phone.CONTENT_ITEM_TYPE) {
                        put(Phone.NUMBER, phone.number)
                        put(Phone.TYPE, phone.type.rawValue)
                        if (phone.type is PhoneType.Custom) {
                            put(Phone.LABEL, phone.type.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapEmails(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (email in uiState.emails) {
            if (email.address.isBlank()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(Email.CONTENT_ITEM_TYPE) {
                        put(Email.DATA, email.address)
                        put(Email.TYPE, email.type.rawValue)
                        if (email.type is EmailType.Custom) {
                            put(Email.LABEL, email.type.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapAddresses(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (address in uiState.addresses) {
            if (!address.hasData()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(StructuredPostal.CONTENT_ITEM_TYPE) {
                        putIfNotBlank(StructuredPostal.STREET, address.street)
                        putIfNotBlank(StructuredPostal.CITY, address.city)
                        putIfNotBlank(StructuredPostal.REGION, address.region)
                        putIfNotBlank(StructuredPostal.POSTCODE, address.postcode)
                        putIfNotBlank(StructuredPostal.COUNTRY, address.country)
                        put(StructuredPostal.TYPE, address.type.rawValue)
                        if (address.type is AddressType.Custom) {
                            put(StructuredPostal.LABEL, address.type.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapOrganization(delta: RawContactDelta, uiState: ContactCreationUiState) {
        val org = uiState.organization
        if (!org.hasData()) return

        delta.addEntry(
            ValuesDelta.fromAfter(
                contentValues(Organization.CONTENT_ITEM_TYPE) {
                    putIfNotBlank(Organization.COMPANY, org.company)
                    putIfNotBlank(Organization.TITLE, org.title)
                },
            ),
        )
    }

    private fun mapEvents(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (event in uiState.events) {
            if (event.startDate.isBlank()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(Event.CONTENT_ITEM_TYPE) {
                        put(Event.START_DATE, event.startDate)
                        put(Event.TYPE, event.type.rawValue)
                        if (event.type is EventType.Custom) {
                            put(Event.LABEL, event.type.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapRelations(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (relation in uiState.relations) {
            if (relation.name.isBlank()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(Relation.CONTENT_ITEM_TYPE) {
                        put(Relation.NAME, relation.name)
                        put(Relation.TYPE, relation.type.rawValue)
                        if (relation.type is RelationType.Custom) {
                            put(Relation.LABEL, relation.type.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapImAccounts(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (im in uiState.imAccounts) {
            if (im.data.isBlank()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(Im.CONTENT_ITEM_TYPE) {
                        put(Im.DATA, im.data)
                        put(Im.PROTOCOL, im.protocol.rawValue)
                        if (im.protocol is ImProtocol.Custom) {
                            put(Im.CUSTOM_PROTOCOL, im.protocol.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapWebsites(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (website in uiState.websites) {
            if (website.url.isBlank()) continue
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(Website.CONTENT_ITEM_TYPE) {
                        put(Website.URL, website.url)
                        put(Website.TYPE, website.type.rawValue)
                        if (website.type is WebsiteType.Custom) {
                            put(Website.LABEL, website.type.label)
                        }
                    },
                ),
            )
        }
    }

    private fun mapNote(delta: RawContactDelta, uiState: ContactCreationUiState) {
        if (uiState.note.isBlank()) return
        delta.addEntry(
            ValuesDelta.fromAfter(
                contentValues(Note.CONTENT_ITEM_TYPE) {
                    put(Note.NOTE, uiState.note)
                },
            ),
        )
    }

    private fun mapNickname(delta: RawContactDelta, uiState: ContactCreationUiState) {
        if (uiState.nickname.isBlank()) return
        delta.addEntry(
            ValuesDelta.fromAfter(
                contentValues(Nickname.CONTENT_ITEM_TYPE) {
                    put(Nickname.NAME, uiState.nickname)
                },
            ),
        )
    }

    private fun mapSipAddress(delta: RawContactDelta, uiState: ContactCreationUiState) {
        if (uiState.sipAddress.isBlank()) return
        delta.addEntry(
            ValuesDelta.fromAfter(
                contentValues(SipAddress.CONTENT_ITEM_TYPE) {
                    put(SipAddress.SIP_ADDRESS, uiState.sipAddress)
                },
            ),
        )
    }

    private fun mapGroups(delta: RawContactDelta, uiState: ContactCreationUiState) {
        for (group in uiState.groups) {
            delta.addEntry(
                ValuesDelta.fromAfter(
                    contentValues(GroupMembership.CONTENT_ITEM_TYPE) {
                        put(GroupMembership.GROUP_ROW_ID, group.groupId)
                    },
                ),
            )
        }
    }

    private fun mapPhoto(
        delta: RawContactDelta,
        uiState: ContactCreationUiState,
        updatedPhotos: Bundle,
    ) {
        val photoUri = uiState.photoUri ?: return
        val tempId = delta.values.id
        updatedPhotos.putParcelable(tempId.toString(), photoUri)
    }

    private inline fun contentValues(
        mimeType: String,
        block: ContentValues.() -> Unit,
    ): ContentValues = ContentValues().apply {
        put(Data.MIMETYPE, mimeType)
        block()
    }

    private fun ContentValues.putIfNotBlank(key: String, value: String) {
        if (value.isNotBlank()) put(key, value)
    }
}
