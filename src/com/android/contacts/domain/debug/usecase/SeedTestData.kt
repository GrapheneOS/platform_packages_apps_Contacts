package com.android.contacts.domain.debug.usecase

import android.util.Log
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.di.debug.SeedTestContactsCount
import com.android.contacts.di.debug.SeedTestGroupsCount
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.debug.model.TestContact
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

internal fun interface SeedTestData {
    suspend operator fun invoke()
}

internal class SeedTestDataImpl @Inject constructor(
    private val loadAccounts: LoadAccounts,
    private val getDefaultAccount: GetDefaultAccount,
    private val clearSeededTestData: ClearSeededTestData,
    private val generateTestContact: GenerateTestContact,
    private val createTestGroups: CreateTestGroups,
    private val saveTestContact: SaveTestContact,
    private val random: Random,
    @param:SeedTestContactsCount val testContactsCount: Int,
    @param:SeedTestGroupsCount val testGroupsCount: Int,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : SeedTestData {
    override suspend fun invoke() {
        withContext(coroutineDispatcher) {
            val account = getDeviceAccount() ?: return@withContext
            clearSeededTestData()
            val contacts = (1..testContactsCount).map { generateTestContact() }
            val groupIds = createTestGroups(account, testGroupsCount)
            saveContacts(account, contacts, groupIds)
        }
    }

    /*
     * Choses the account based on the following order:
     *  - Device account if available
     *  - Default account if any is configured
     *  - First account from the accounts list
     */
    private suspend fun getDeviceAccount(): AccountModel? {
        val accounts = loadAccounts(AccountFilter.CONTACTS_INSERTABLE).first()
        return accounts.firstOrNull { it.isDeviceAccount }?.account
            ?: getDefaultAccount()
            ?: accounts.firstOrNull()?.account
            ?: run {
                Log.w(TAG, "No account available to save test data")
                null
            }
    }

    private fun saveContacts(
        account: AccountModel,
        contacts: List<TestContact>,
        groupIds: List<Long>,
    ) {
        contacts.forEach { contact ->
            val contactGroupIds = when {
                groupIds.isEmpty() -> emptyList()
                else -> groupIds.shuffled(random).take(random.nextInt(groupIds.size))
            }
            saveTestContact(account = account, contact = contact, groupIds = contactGroupIds)
        }
    }

    companion object {
        private const val TAG = "SeedTestData"
    }
}
