package com.android.contacts.ui.interactions.account.filter.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.ContactsAccountFilter

@Immutable
internal sealed interface AccountFilterItem {
    val contactsCount: Int
    val filter: ContactsAccountFilter

    @Immutable
    data class All(
        override val contactsCount: Int,
    ) : AccountFilterItem {
        override val filter = ContactsAccountFilter.All
    }

    @Immutable
    data class One(
        val name: String,
        val description: String? = null,
        val iconData: AccountIconData? = null,
        override val contactsCount: Int = 0,
        override val filter: ContactsAccountFilter.One,
    ) : AccountFilterItem
}
