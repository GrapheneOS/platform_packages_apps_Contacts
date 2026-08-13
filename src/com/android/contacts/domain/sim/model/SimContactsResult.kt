package com.android.contacts.domain.sim.model

import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.SimContact

internal data class SimContactsResult(
    val contacts: List<SimContact> = emptyList(),
    val existingContactsInAccounts: Map<AccountModel, Set<SimContact>> = emptyMap(),
)
