package com.android.contacts.domain.accounts.usecase

import android.content.IntentFilter
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.mapper.AccountFilterMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.util.core.BuildBroadcastReceiverFlow
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

internal fun interface LoadAccounts {
    operator fun invoke(filter: AccountFilter?): Flow<List<AccountDisplayModel>>
}

internal class LoadAccountsImpl @Inject constructor(
    private val accountFilterMapper: AccountFilterMapper,
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    private val accountTypeManager: AccountTypeManager,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadAccounts {

    override operator fun invoke(
        filter: AccountFilter?,
    ): Flow<List<AccountDisplayModel>> {
        return buildBroadcastReceiverFlow(
            IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED),
        )
            .map { load(filter) }
            .flowOn(coroutineDispatcher)
    }

    private fun load(filter: AccountFilter?): List<AccountDisplayModel> {
        val filterPredicate = accountFilterMapper.map(filter)
        return try {
            accountTypeManager
                .filterAccountsAsync(filterPredicate)
                .get()
                .orEmpty()
                .map(accountDisplayModelMapper::map)
        } catch (e: InterruptedException) {
            onLoadError(e)
        } catch (e: ExecutionException) {
            onLoadError(e)
        }
    }

    private fun onLoadError(e: Exception): List<AccountDisplayModel> {
        Log.w(TAG, "Could not load accounts", e)
        return emptyList()
    }

    companion object {
        private const val TAG = "LoadAccounts"
    }
}
