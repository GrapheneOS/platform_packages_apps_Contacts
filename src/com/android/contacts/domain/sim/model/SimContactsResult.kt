package com.android.contacts.domain.sim.model

import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountWithDataSet

internal data class SimContactsResult(
    val contacts: List<SimContact> = emptyList(),
    val existingContactsInAccounts: Map<AccountWithDataSet, Set<SimContact>> = emptyMap(),
)
