package com.android.contacts.tests

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.contacts.model.RawContactWithAccount

internal object RawContactWithAccountFactory {
    fun build(
        id: Long = 1L,
        photoUri: String? = null,
        displayName: String? = null,
        displayNameAlt: String? = null,
        account: AccountDisplayModel = AccountDisplayModelFactory.build(),
    ): RawContactWithAccount {
        return RawContactWithAccount(
            id = id,
            photoUri = photoUri,
            displayName = displayName,
            displayNameAlt = displayNameAlt,
            account = account,
        )
    }
}
