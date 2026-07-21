package com.android.contacts.domain.accounts.usecase

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.model.AccountTypeManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import java.util.concurrent.ExecutionException
import javax.inject.Inject

internal fun interface LoadAccounts {
    operator fun invoke(): Flow<ImmutableList<AccountDisplayModel>>
}

internal class LoadAccountsImpl @Inject constructor(
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    @param:ApplicationContext private val context: Context,
    private val accountTypeManager: AccountTypeManager,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadAccounts {

    override operator fun invoke(): Flow<ImmutableList<AccountDisplayModel>> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load() }
            .flowOn(coroutineDispatcher)

    private fun load() =
        try {
            accountTypeManager
                .filterAccountsAsync(AccountTypeManager.insertableFilter(context))
                .get()
                .orEmpty()
                .map(accountDisplayModelMapper::map)
                .toImmutableList()
        } catch (e: InterruptedException) {
            Log.w(TAG, "Could not load accounts", e)
            persistentListOf()
        } catch (e: ExecutionException) {
            Log.w(TAG, "Could not load accounts", e)
            persistentListOf()
        }

    companion object {
        private const val TAG = "LoadAccounts"
    }
}
