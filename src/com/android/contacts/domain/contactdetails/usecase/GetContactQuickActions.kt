package com.android.contacts.domain.contactdetails.usecase

import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.domain.contactdetails.model.CONTACT_DATA_ITEM_PRIORITY
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType
import com.android.contacts.domain.telecom.usecase.CanVideoCall
import javax.inject.Inject

internal fun interface GetContactQuickActions {
    operator fun invoke(details: ContactDetails): List<ContactQuickAction>
}

internal class GetContactQuickActionsImpl @Inject constructor(
    private val isEntryActionAvailable: IsEntryActionAvailable,
    private val canVideoCall: CanVideoCall,
) : GetContactQuickActions {

    override operator fun invoke(details: ContactDetails): List<ContactQuickAction> {
        val phone = primaryPhone(details)
        val number = phone?.number
        val address = primaryEmailAddress(details)

        return listOf(
            quickAction(
                type = ContactQuickActionType.CALL,
                action = number?.let { value -> ContactEntryAction.Call(value) },
            ),
            quickAction(
                type = ContactQuickActionType.MESSAGE,
                action = number?.let { value -> ContactEntryAction.Sms(value) },
            ),
            quickAction(
                type = ContactQuickActionType.VIDEO_CALL,
                action = videoCallAction(phone, number),
            ),
            quickAction(
                type = ContactQuickActionType.EMAIL,
                action = address?.let { value -> ContactEntryAction.SendEmail(value) },
            ),
        )
    }

    private fun quickAction(
        type: ContactQuickActionType,
        action: ContactEntryAction?,
    ): ContactQuickAction {
        return ContactQuickAction(
            type = type,
            action = action?.takeIf { candidate -> isEntryActionAvailable(candidate) },
        )
    }

    private fun primaryPhone(details: ContactDetails): ContactDataItem.Phone? {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Phone>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .firstOrNull { dataItem -> !dataItem.number.isNullOrBlank() }
    }

    private fun primaryEmailAddress(details: ContactDetails): String? {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Email>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .firstNotNullOfOrNull { dataItem ->
                dataItem.address?.takeIf { address -> address.isNotBlank() }
            }
    }

    private fun videoCallAction(
        phone: ContactDataItem.Phone?,
        number: String?,
    ): ContactEntryAction? {
        if (phone == null || number == null || !canVideoCall(phone.isCarrierVideoCallCapable)) {
            return null
        }

        return ContactEntryAction.VideoCall(number)
    }
}
