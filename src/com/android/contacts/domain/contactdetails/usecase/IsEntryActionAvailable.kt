package com.android.contacts.domain.contactdetails.usecase

import com.android.contacts.data.contactdetails.intent.ContactEntryIntentFactory
import com.android.contacts.data.contactdetails.intent.IsIntentRegistered
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import javax.inject.Inject

internal fun interface IsEntryActionAvailable {
    operator fun invoke(action: ContactEntryAction): Boolean
}

internal class IsEntryActionAvailableImpl @Inject constructor(
    private val contactEntryIntentFactory: ContactEntryIntentFactory,
    private val isIntentRegistered: IsIntentRegistered,
) : IsEntryActionAvailable {

    override operator fun invoke(action: ContactEntryAction): Boolean {
        val intent = contactEntryIntentFactory.create(action) ?: return false

        return isIntentRegistered(intent)
    }
}
