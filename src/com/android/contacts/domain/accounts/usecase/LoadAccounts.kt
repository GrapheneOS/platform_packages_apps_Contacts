package com.android.contacts.domain.accounts.usecase

import android.content.Context
import android.content.IntentFilter
import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.util.BuildBroadcastReceiverFlow
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.google.common.base.Predicate
import dagger.hilt.android.qualifiers.ApplicationContext
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

internal fun interface LoadAccounts {
    operator fun invoke(
        filter: AccountTypeManager.AccountFilter?,
    ): Flow<ImmutableList<AccountDisplayModel>>
}

internal class LoadAccountsImpl @Inject constructor(
    private val buildBroadcastReceiverFlow: BuildBroadcastReceiverFlow,
    @param:ApplicationContext private val context: Context,
    private val accountTypeManager: AccountTypeManager,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : LoadAccounts {

    override operator fun invoke(
        filter: AccountTypeManager.AccountFilter?,
    ): Flow<ImmutableList<AccountDisplayModel>> =
        buildBroadcastReceiverFlow(IntentFilter(AccountTypeManager.BROADCAST_ACCOUNTS_CHANGED))
            .onStart { emit(Unit) }
            .map { load(filter) }
            .flowOn(coroutineDispatcher)

    private fun load(filter: AccountTypeManager.AccountFilter?) =
        try {
            accountTypeManager
                .filterAccountsAsync(prepareFilter(filter))
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

    private fun prepareFilter(filter: AccountTypeManager.AccountFilter?): Predicate<AccountInfo> {
        return when {
            filter == null ->
                AccountTypeManager.AccountFilter.ALL
            filter === AccountTypeManager.AccountFilter.CONTACTS_INSERTABLE ->
                AccountTypeManager.insertableFilter(context)
            else ->
                filter
        }
    }

    companion object {
        private const val TAG = "LoadAccounts"
    }
}
