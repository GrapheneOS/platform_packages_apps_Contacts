package com.android.contacts.domain.sim.usecase

import android.content.IntentFilter
import com.android.contacts.database.SimContactDao
import com.android.contacts.di.core.SimReadDispatcher
import com.android.contacts.domain.common.BuildBroadcastReceiverFlow
import com.android.contacts.domain.sim.model.SimContactsResult
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.SimCard
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.util.concurrent.ContactsExecutors
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun interface LoadSimContacts {
    operator fun invoke(subscriptionId: Int): Flow<SimContactsResult>
}

internal class LoadSimContactsImpl @Inject constructor(
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    private val simContactDao: SimContactDao,
    @param:SimReadDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadSimContacts {

    override operator fun invoke(subscriptionId: Int): Flow<SimContactsResult> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load(subscriptionId) }
            .flowOn(coroutineDispatcher)

    private fun load(subscriptionId: Int): SimContactsResult {
        val sim = simContactDao.getSimBySubscriptionId(subscriptionId) ?: return SimContactsResult()
        val contacts = simContactDao.loadContactsForSim(sim).orEmpty()
        val accountsMap = simContactDao.findAccountsOfExistingSimContacts(contacts).orEmpty()
        return SimContactsResult(
            contacts = contacts,
            existingContactsInAccounts = accountsMap,
        )
    }
}
