package com.android.contacts.domain.telecom.usecase

import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.telecom.repository.PhoneAccountsRepository
import com.android.contacts.domain.telecom.model.CallingSimChoice
import com.android.contacts.domain.telecom.model.CallingSimOptions
import javax.inject.Inject

internal fun interface GetCallingSimOptions {
    suspend operator fun invoke(details: ContactDetails): CallingSimOptions?
}

internal class GetCallingSimOptionsImpl @Inject constructor(
    private val phoneAccountsRepository: PhoneAccountsRepository,
) : GetCallingSimOptions {

    override suspend operator fun invoke(details: ContactDetails): CallingSimOptions? {
        val choices = choices(details)
        if (choices.isEmpty()) {
            return null
        }

        val sims = phoneAccountsRepository.getCallCapableSims()
        if (sims.size < MINIMUM_SIM_COUNT) {
            return null
        }

        return CallingSimOptions(
            sims = sims,
            choices = choices,
        )
    }

    private fun choices(details: ContactDetails): List<CallingSimChoice> {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Phone>()
            .mapNotNull { phone -> toChoice(phone) }
    }

    private fun toChoice(phone: ContactDataItem.Phone): CallingSimChoice? {
        val number = phone.displayString?.takeIf { value ->
            value.isNotBlank()
        } ?: return null

        return CallingSimChoice(
            dataId = phone.id,
            number = number,
            numberLabel = phone.typeLabel,
            selectedAccountId = phone.preferredPhoneAccount,
        )
    }

    private companion object {
        const val MINIMUM_SIM_COUNT = 2
    }
}
