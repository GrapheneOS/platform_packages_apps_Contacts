package com.android.contacts.domain.accounts.usecase

import android.content.IntentFilter
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.model.AccountTypeManager
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun interface LoadWritableAccounts {
    operator fun invoke(): Flow<ImmutableList<AccountDisplayModel>>
}

internal class LoadWritableAccountsImpl @Inject constructor(
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    private val accountTypeManager: AccountTypeManager,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadWritableAccounts {

    override operator fun invoke(): Flow<ImmutableList<AccountDisplayModel>> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load() }
            .flowOn(coroutineDispatcher)

    private fun load() =
        try {
            accountTypeManager
                .filterAccountsAsync(AccountTypeManager.writableFilter())
                .get()
                .orEmpty()
                .map(accountDisplayModelMapper::map)
                .toImmutableList()
        } catch (e: InterruptedException) {
            Log.w(TAG, "Could not load writable accounts", e)
            persistentListOf()
        } catch (e: ExecutionException) {
            Log.w(TAG, "Could not load writable accounts", e)
            persistentListOf()
        }

    companion object {
        private const val TAG = "LoadWritableAccounts"
    }
}
