package com.android.contacts.editornew.contact

import com.android.contacts.model.RawContactDelta
import com.android.contacts.model.account.AccountInfo

internal sealed interface ContactState {
    data object Loading : ContactState
    data class Data(
        val accounts: List<AccountInfo>,
        val rawContactDelta: RawContactDelta,
    ) : ContactState
}
