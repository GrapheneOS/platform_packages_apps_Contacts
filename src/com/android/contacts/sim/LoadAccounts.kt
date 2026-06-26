package com.android.contacts.sim

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import java.util.concurrent.CancellationException
import java.util.concurrent.ExecutionException
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class LoadAccounts(
    private val loadCoroutineContext: CoroutineContext,
    private val buildBroadcastReceiverFlow: (IntentFilter) -> Flow<Unit>,
    private val context: Context,
    private val accountTypeManager: AccountTypeManager,
) {
    operator fun invoke(): Flow<List<AccountInfo>> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load() }
            .flowOn(loadCoroutineContext)

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
