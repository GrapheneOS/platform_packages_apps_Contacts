package com.android.contacts.domain.sim.usecase

import android.content.IntentFilter
import android.util.Log
import com.android.contacts.database.SimContactDao
import com.android.contacts.di.core.SimReadDispatcher
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.sim.model.SimContactsResult
import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.model.AccountTypeManager
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun interface LoadSimContacts {
    operator fun invoke(subscriptionId: Int): Flow<SimContactsResult>
}

internal class LoadSimContactsImpl @Inject constructor(
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    private val simContactDao: SimContactDao,
    private val accountModelMapper: AccountModelMapper,
    @param:SimReadDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadSimContacts {

    override operator fun invoke(subscriptionId: Int): Flow<SimContactsResult> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load(subscriptionId) }
            .catch {
                if (it is CancellationException) {
                    return@catch
                }

                Log.e(TAG, "Load SIM contacts failed (subscriptionId=$subscriptionId)", it)
                emit(SimContactsResult())
            }
            .flowOn(coroutineDispatcher)

    private fun load(subscriptionId: Int): SimContactsResult {
        val sim = simContactDao.getSimBySubscriptionId(subscriptionId) ?: return SimContactsResult()
        val contacts = simContactDao.loadContactsForSim(sim).orEmpty()
        val accountsMap = simContactDao.findAccountsOfExistingSimContacts(contacts).orEmpty()
        return SimContactsResult(
            contacts = contacts.toImmutableList(),
            existingContactsInAccounts = accountsMap
                .mapKeys { (accountWithDataSet, _) -> accountModelMapper.map(accountWithDataSet) }
                .toImmutableMap(),
        )
    }

    private companion object {
        const val TAG = "LoadSimContactsImpl"
    }
}
