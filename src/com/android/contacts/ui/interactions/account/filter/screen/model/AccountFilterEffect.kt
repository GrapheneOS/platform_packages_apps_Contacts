package com.android.contacts.ui.interactions.account.filter.screen.model

import com.android.contacts.domain.accounts.model.ContactsAccountFilter

internal sealed interface AccountFilterEffect {

    data class Close(
        val filterPicked: ContactsAccountFilter? = null,
    ) : AccountFilterEffect
}
