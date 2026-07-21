package com.android.contacts.domain.sim.model

import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.SimContact
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf

internal data class SimContactsResult(
    val contacts: ImmutableList<SimContact> = persistentListOf(),
    val existingContactsInAccounts: ImmutableMap<AccountModel, Set<SimContact>> = persistentMapOf(),
)
