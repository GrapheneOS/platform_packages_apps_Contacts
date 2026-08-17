package com.android.contacts.domain.contactdetails.usecase

import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.domain.contactdetails.model.CONTACT_DATA_ITEM_PRIORITY
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.model.ContactQuickActionType
import javax.inject.Inject

internal fun interface GetContactQuickActions {
    operator fun invoke(details: ContactDetails): List<ContactQuickAction>
}

internal class GetContactQuickActionsImpl @Inject constructor(
    private val isEntryActionAvailable: IsEntryActionAvailable,
) : GetContactQuickActions {

    override operator fun invoke(details: ContactDetails): List<ContactQuickAction> {
        val number = primaryPhoneNumber(details)
        val address = primaryEmailAddress(details)

        return listOf(
            quickAction(
                ContactQuickActionType.CALL,
                number?.let(ContactEntryAction::Call),
            ),
            quickAction(
                ContactQuickActionType.MESSAGE,
                number?.let(ContactEntryAction::Sms),
            ),
            quickAction(
                type = ContactQuickActionType.VIDEO_CALL,
                action = number?.let(ContactEntryAction::VideoCall),
            ),
            quickAction(
                type = ContactQuickActionType.EMAIL,
                action = address?.let(ContactEntryAction::SendEmail),
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

    private fun primaryPhoneNumber(details: ContactDetails): String? {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Phone>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .firstNotNullOfOrNull { dataItem ->
                dataItem.number?.takeIf { number -> number.isNotBlank() }
            }
    }

    private fun primaryEmailAddress(details: ContactDetails): String? {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Email>()
            .sortedWith(CONTACT_DATA_ITEM_PRIORITY)
            .firstNotNullOfOrNull { dataItem ->
                dataItem.address?.takeIf { address -> address.isNotBlank() }
            }
    }
}
