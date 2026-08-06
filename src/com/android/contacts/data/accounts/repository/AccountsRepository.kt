package com.android.contacts.data.accounts.repository

import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountInfo
import com.android.contacts.preference.ContactsPreferences
import java.util.concurrent.ExecutionException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface AccountsRepository {
    suspend fun getDefaultAccountLabel(): String?
}

internal class AccountsRepositoryImpl @Inject constructor(
    private val contactsPreferences: ContactsPreferences,
    private val accountTypeManager: AccountTypeManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AccountsRepository {

    override suspend fun getDefaultAccountLabel(): String? {
        return withContext(ioDispatcher) {
            val defaultAccount = contactsPreferences.defaultAccount ?: return@withContext null

            loadWritableAccounts()
                .firstOrNull { it.account == defaultAccount }
                ?.nameLabel
                ?.toString()
        }
    }

    private fun loadWritableAccounts(): List<AccountInfo> {
        return try {
            accountTypeManager
                .filterAccountsAsync(AccountTypeManager.writableFilter())
                .get()
                .orEmpty()
        } catch (e: InterruptedException) {
            Log.w(TAG, "Could not load writable accounts", e)
            emptyList()
        } catch (e: ExecutionException) {
            Log.w(TAG, "Could not load writable accounts", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "AccountsRepository"
    }
}
