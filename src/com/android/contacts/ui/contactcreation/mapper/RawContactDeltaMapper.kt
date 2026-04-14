package com.android.contacts.ui.contactcreation.mapper

import android.content.ContentValues
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.Data
import com.android.contacts.model.RawContact
import com.android.contacts.model.RawContactDelta
import com.android.contacts.model.RawContactDeltaList
import com.android.contacts.model.ValuesDelta
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import javax.inject.Inject

internal data class DeltaMapperResult(val state: RawContactDeltaList, val updatedPhotos: Bundle)

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
