package com.android.contacts.domain.accounts.usecase

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.common.BuildBroadcastReceiverFlow
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun interface LoadAccounts {
    operator fun invoke(): Flow<List<AccountInfo>>
}

internal class LoadAccountsImpl @Inject constructor(
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    @param:ApplicationContext private val context: Context,
    private val accountTypeManager: AccountTypeManager,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadAccounts {

    override operator fun invoke(): Flow<List<AccountInfo>> =
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
        } catch (e: InterruptedException) {
            Log.w(TAG, "Could not load accounts", e)
            emptyList()
        } catch (e: ExecutionException) {
            Log.w(TAG, "Could not load accounts", e)
            emptyList()
        } catch (e: CancellationException) {
            Log.w(TAG, "Could not load accounts", e)
            emptyList()
        }

    companion object {
        private const val TAG = "LoadAccounts"
    }
}
