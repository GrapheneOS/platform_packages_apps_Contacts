package com.android.contacts.sim

import android.content.IntentFilter
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.SimCard
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountWithDataSet
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class LoadSimContacts(
    private val loadCoroutineContext: CoroutineContext,
    private val buildBroadcastReceiverFlow: (IntentFilter) -> Flow<Unit>,
    private val getSimBySubscriptionId: (Int) -> SimCard?,
    private val loadContactsForSim: (SimCard) -> List<SimContact>?,
    private val findAccountsOfExistingSimContacts: (
        List<SimContact>,
    ) -> Map<AccountWithDataSet, Set<SimContact>>?,
) {
    operator fun invoke(subscriptionId: Int): Flow<Result> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load(subscriptionId) }
            .flowOn(loadCoroutineContext)

    private fun load(subscriptionId: Int): Result {
        val sim = getSimBySubscriptionId(subscriptionId) ?: return Result()
        val contacts = loadContactsForSim(sim).orEmpty()
        val accountsMap = findAccountsOfExistingSimContacts(contacts).orEmpty()
        return Result(
            contacts = contacts,
            existingContactsInAccounts = accountsMap,
        )
    }

    data class Result(
        val contacts: List<SimContact> = emptyList(),
        val existingContactsInAccounts: Map<AccountWithDataSet, Set<SimContact>> = emptyMap(),
    )
}
