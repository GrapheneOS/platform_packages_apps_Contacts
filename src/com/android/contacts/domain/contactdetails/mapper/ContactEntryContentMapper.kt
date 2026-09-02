package com.android.contacts.domain.contactdetails.mapper

import android.provider.ContactsContract.CommonDataKinds.GroupMembership
import android.provider.ContactsContract.CommonDataKinds.Identity
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.telecom.source.IsCallWithNoteSupported
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapable
import com.android.contacts.data.telecom.source.IsSipCallingSupported
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryContent
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.contactdetails.model.duplicatesDisplayName
import com.android.contacts.domain.telecom.usecase.CanVideoCall
import javax.inject.Inject

internal interface ContactEntryContentMapper {

    fun mapKind(dataItem: ContactDataItem): ContactEntryKind

    fun mapContent(
        dataItem: ContactDataItem,
        details: ContactDetails,
    ): ContactEntryContent?
}

internal class ContactEntryContentMapperImpl @Inject constructor(
    private val isDeviceVoiceCapable: IsDeviceVoiceCapable,
    private val isCallWithNoteSupported: IsCallWithNoteSupported,
    private val canVideoCall: CanVideoCall,
    private val isSipCallingSupported: IsSipCallingSupported,
) : ContactEntryContentMapper {

    @Suppress("CyclomaticComplexMethod")
    override fun mapKind(dataItem: ContactDataItem): ContactEntryKind {
        return when (dataItem) {
            is ContactDataItem.Phone -> ContactEntryKind.PHONE
            is ContactDataItem.SipAddress -> ContactEntryKind.SIP_ADDRESS
            is ContactDataItem.Email -> ContactEntryKind.EMAIL
            is ContactDataItem.Postal -> ContactEntryKind.POSTAL
            is ContactDataItem.Im -> ContactEntryKind.IM
            is ContactDataItem.Organization -> ContactEntryKind.ORGANIZATION
            is ContactDataItem.Nickname -> ContactEntryKind.NICKNAME
            is ContactDataItem.Note -> ContactEntryKind.NOTE
            is ContactDataItem.Website -> ContactEntryKind.WEBSITE
            is ContactDataItem.Relation -> ContactEntryKind.RELATION
            is ContactDataItem.Custom -> ContactEntryKind.CUSTOM_FIELD
            is ContactDataItem.StructuredName -> ContactEntryKind.OTHER

            is ContactDataItem.Event -> when {
                dataItem.isBirthday -> ContactEntryKind.BIRTHDAY
                else -> ContactEntryKind.EVENT
            }

            is ContactDataItem.Generic -> when (dataItem.mimeType) {
                GroupMembership.CONTENT_ITEM_TYPE -> ContactEntryKind.GROUP
                Identity.CONTENT_ITEM_TYPE -> ContactEntryKind.IDENTITY
                else -> ContactEntryKind.OTHER
            }
        }
    }

    @Suppress("CyclomaticComplexMethod")
    override fun mapContent(
        dataItem: ContactDataItem,
        details: ContactDetails,
    ): ContactEntryContent? {
        return when (dataItem) {
            is ContactDataItem.Phone -> phoneContent(dataItem)
            is ContactDataItem.Email -> emailContent(dataItem)
            is ContactDataItem.Postal -> postalContent(dataItem)
            is ContactDataItem.SipAddress -> sipAddressContent(dataItem)
            is ContactDataItem.Im -> imContent(dataItem)
            is ContactDataItem.Organization -> organizationContent(dataItem)
            is ContactDataItem.Nickname -> nicknameContent(dataItem, details)
            is ContactDataItem.Note -> noteContent(dataItem)
            is ContactDataItem.Website -> websiteContent(dataItem)
            is ContactDataItem.Event -> eventContent(dataItem)
            is ContactDataItem.Relation -> relationContent(dataItem)
            is ContactDataItem.Custom -> customContent(dataItem)
            is ContactDataItem.Generic -> genericContent(dataItem)
            is ContactDataItem.StructuredName -> null
        }
    }

    private fun phoneContent(dataItem: ContactDataItem.Phone): ContactEntryContent? {
        val number = dataItem.number
        if (number.isNullOrEmpty()) {
            return null
        }

        val header = dataItem.displayString.orEmpty()

        return ContactEntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeLabel,
            copyText = header,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
            primaryAction = callAction(number),
            alternateAction = ContactEntryAction.Sms(number),
            enhancedCallAction = enhancedPhoneCallAction(dataItem, number),
            editBeforeCallAction = ContactEntryAction.EditNumberBeforeCall(number),
        )
    }

    private fun callAction(number: String): ContactEntryAction? {
        return when {
            isDeviceVoiceCapable() -> ContactEntryAction.Call(number)
            else -> null
        }
    }

    private fun enhancedPhoneCallAction(
        dataItem: ContactDataItem.Phone,
        number: String,
    ): ContactEntryAction? {
        return when {
            isCallWithNoteSupported() -> ContactEntryAction.CallWithNote(
                number = number,
                formattedNumber = dataItem.formattedNumber,
                numberLabel = dataItem.typeLabel,
            )

            canVideoCall(dataItem.isCarrierVideoCallCapable) -> {
                ContactEntryAction.VideoCall(number)
            }

            else -> null
        }
    }

    private fun emailContent(dataItem: ContactDataItem.Email): ContactEntryContent? {
        if (dataItem.data.isNullOrEmpty()) {
            return null
        }

        val header = dataItem.address.orEmpty()

        return ContactEntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeLabel,
            copyText = header,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.EMAIL),
            primaryAction = ContactEntryAction.SendEmail(header),
        )
    }

    private fun postalContent(dataItem: ContactDataItem.Postal): ContactEntryContent? {
        if (dataItem.formattedAddress.isNullOrEmpty()) {
            return null
        }

        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.formattedAddress),
            text = dataItem.typeLabel,
            copyText = dataItem.formattedAddress,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.POSTAL),
            primaryAction = ContactEntryAction.ShowOnMap(dataItem.formattedAddress),
            alternateAction = ContactEntryAction.ShowDirections(dataItem.formattedAddress),
        )
    }

    private fun sipAddressContent(dataItem: ContactDataItem.SipAddress): ContactEntryContent? {
        if (dataItem.address.isNullOrEmpty()) {
            return null
        }

        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.address),
            text = dataItem.typeLabel,
            copyText = dataItem.address,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
            primaryAction = sipCallAction(dataItem.address),
        )
    }

    private fun sipCallAction(address: String): ContactEntryAction? {
        return when {
            isSipCallingSupported() -> ContactEntryAction.SipCall(address)
            else -> null
        }
    }

    private fun imContent(dataItem: ContactDataItem.Im): ContactEntryContent {
        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.data.orEmpty()),
            subHeader = imLabel(dataItem),
            copyText = dataItem.data,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.IM),
            primaryAction = chatAction(dataItem),
        )
    }

    private fun imLabel(dataItem: ContactDataItem.Im): ContactEntryText {
        val protocolLabel = dataItem.protocolLabel

        return when {
            protocolLabel.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.IM)
            else -> ContactEntryText.Value(protocolLabel)
        }
    }

    private fun chatAction(dataItem: ContactDataItem.Im): ContactEntryAction? {
        val data = dataItem.data
        if (data.isNullOrEmpty()) {
            return null
        }

        return ContactEntryAction.OpenChat(
            data = data,
            protocol = dataItem.protocol,
            customProtocol = dataItem.customProtocol,
        )
    }

    private fun organizationContent(dataItem: ContactDataItem.Organization): ContactEntryContent {
        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.formattedCompany.orEmpty()),
            subHeader = ContactEntryText.Label(ContactEntryLabel.ORGANIZATION),
            copyText = dataItem.formattedCompany,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.ORGANIZATION),
        )
    }

    private fun nicknameContent(
        dataItem: ContactDataItem.Nickname,
        details: ContactDetails,
    ): ContactEntryContent? {
        if (dataItem.duplicatesDisplayName(details)) {
            return null
        }

        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.name.orEmpty()),
            subHeader = ContactEntryText.Label(ContactEntryLabel.NICKNAME),
            copyText = dataItem.name,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.NICKNAME),
        )
    }

    private fun noteContent(dataItem: ContactDataItem.Note): ContactEntryContent {
        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.note.orEmpty()),
            copyText = dataItem.note,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.NOTE),
        )
    }

    private fun websiteContent(dataItem: ContactDataItem.Website): ContactEntryContent {
        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.url.orEmpty()),
            subHeader = ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            copyText = dataItem.url,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            primaryAction = dataItem.url?.let { url -> ContactEntryAction.OpenUrl(url) },
        )
    }

    private fun eventContent(dataItem: ContactDataItem.Event): ContactEntryContent {
        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.formattedDate.orEmpty()),
            subHeader = eventLabel(dataItem),
            copyText = dataItem.formattedDate,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.EVENT),
            primaryAction = eventDateAction(dataItem),
        )
    }

    private fun eventLabel(dataItem: ContactDataItem.Event): ContactEntryText {
        val typeLabel = dataItem.typeLabel

        return when {
            typeLabel.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.EVENT)
            else -> ContactEntryText.Value(typeLabel)
        }
    }

    private fun eventDateAction(dataItem: ContactDataItem.Event): ContactEntryAction? {
        val date = dataItem.displayString
        if (date.isNullOrEmpty()) {
            return null
        }

        return ContactEntryAction.ShowEventDate(
            date = date,
            isRecurringAnnually = dataItem.isRecurringAnnually,
        )
    }

    private fun relationContent(dataItem: ContactDataItem.Relation): ContactEntryContent {
        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.name.orEmpty()),
            subHeader = relationLabel(dataItem),
            copyText = dataItem.name,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.RELATION),
            primaryAction = dataItem.displayString
                ?.takeIf { name -> name.isNotEmpty() }
                ?.let { name -> ContactEntryAction.SearchContacts(name) },
        )
    }

    private fun relationLabel(dataItem: ContactDataItem.Relation): ContactEntryText {
        val typeLabel = dataItem.typeLabel

        return when {
            typeLabel.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.RELATION)
            else -> ContactEntryText.Value(typeLabel)
        }
    }

    private fun customContent(dataItem: ContactDataItem.Custom): ContactEntryContent {
        val summary = dataItem.summary

        return ContactEntryContent(
            header = ContactEntryText.Value(dataItem.content.orEmpty()),
            subHeader = when {
                summary.isNullOrEmpty() -> ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD)
                else -> ContactEntryText.Value(summary)
            },
            copyText = dataItem.content,
            copyLabel = ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD),
        )
    }

    private fun genericContent(dataItem: ContactDataItem.Generic): ContactEntryContent {
        val header = dataItem.displayString.orEmpty()

        return ContactEntryContent(
            header = ContactEntryText.Value(header),
            text = dataItem.typeColumn,
            copyText = header,
            copyLabel = ContactEntryText.Value(dataItem.mimeType),
            primaryAction = ContactEntryAction.ViewDataItem(
                dataId = dataItem.id,
                mimeType = dataItem.mimeType,
            ),
        )
    }
}
