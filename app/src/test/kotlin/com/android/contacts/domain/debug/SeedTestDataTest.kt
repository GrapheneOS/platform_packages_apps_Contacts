package com.android.contacts.domain.debug

import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.debug.usecase.ClearSeededTestData
import com.android.contacts.domain.debug.usecase.CreateTestGroups
import com.android.contacts.domain.debug.usecase.GenerateTestContact
import com.android.contacts.domain.debug.usecase.SaveTestContact
import com.android.contacts.domain.debug.usecase.SeedTestData
import com.android.contacts.domain.debug.usecase.SeedTestDataImpl
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.TestContactFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.random.Random
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SeedTestDataTest {

    private var loadAccounts: (AccountFilter?) -> Flow<List<AccountDisplayModel>> = { emptyFlow() }
    private val getDefaultAccount = mockk<GetDefaultAccount>(relaxed = true)
    private val clearSeededTestData = mockk<ClearSeededTestData>(relaxed = true)
    private val generateTestContact = mockk<GenerateTestContact>(relaxed = true) {
        every { this@mockk.invoke() } returns TestContactFactory.build()
    }
    private val createTestGroups = mockk<CreateTestGroups>(relaxed = true) {
        every { this@mockk.invoke(any(), any()) } returns emptyList()
    }
    private val saveTestContact = mockk<SaveTestContact>(relaxed = true)

    @Test
    fun usesDeviceAccount_ifAvailable() = runTest {
        val deviceAccount = buildAccount(isDeviceAccount = true)
        val otherAccount = buildAccount(isDeviceAccount = false)
        loadAccounts = { flowOf(persistentListOf(otherAccount, deviceAccount)) }
        every { getDefaultAccount() } returns otherAccount.account

        buildSubject()()

        verify(exactly = DEFAULT_TEST_CONTACTS_COUNT) {
            saveTestContact(deviceAccount.account, any(), any())
        }
    }

    @Test
    fun usesDefaultAccount_ifThereIsNoDeviceAccount() = runTest {
        val defaultAccount = buildAccount(isDeviceAccount = false)
        val otherAccount = buildAccount(isDeviceAccount = false)
        loadAccounts = { flowOf(persistentListOf(otherAccount, defaultAccount)) }
        every { getDefaultAccount() } returns defaultAccount.account

        buildSubject()()

        verify(exactly = DEFAULT_TEST_CONTACTS_COUNT) {
            saveTestContact(defaultAccount.account, any(), any())
        }
    }

    @Test
    fun usesFirstAccount_ifThereIsNoDeviceOrDefaultAccount() = runTest {
        val firstAccount = buildAccount(name = "1", isDeviceAccount = false)
        val secondAccount = buildAccount(name = "2", isDeviceAccount = false)
        loadAccounts = { flowOf(persistentListOf(firstAccount, secondAccount)) }
        every { getDefaultAccount() } returns null

        buildSubject()()

        verify(exactly = DEFAULT_TEST_CONTACTS_COUNT) {
            saveTestContact(firstAccount.account, any(), any())
        }
    }

    @Test
    fun doesNothing_ifThereIsNoAccount() = runTest {
        loadAccounts = { flowOf(persistentListOf()) }
        every { getDefaultAccount() } returns null

        buildSubject()()

        verify(exactly = 0) { saveTestContact(any(), any(), any()) }
        verify(exactly = 0) { createTestGroups(any(), any()) }
    }

    private fun buildAccount(
        name: String = "Name",
        isDeviceAccount: Boolean = false,
    ): AccountDisplayModel {
        val account = AccountModelFactory.build(name = name)
        return AccountDisplayModelFactory.build(
            account = account,
            name = name,
            type = account.type,
            isDeviceAccount = isDeviceAccount,
        )
    }

    private fun buildSubject(
        testContactsCount: Int = DEFAULT_TEST_CONTACTS_COUNT,
        testGroupsCount: Int = DEFAULT_TEST_GROUPS_COUNT,
    ): SeedTestData {
        return SeedTestDataImpl(
            loadAccounts = loadAccounts,
            getDefaultAccount = getDefaultAccount,
            clearSeededTestData = clearSeededTestData,
            generateTestContact = generateTestContact,
            createTestGroups = createTestGroups,
            saveTestContact = saveTestContact,
            random = Random,
            testContactsCount = testContactsCount,
            testGroupsCount = testGroupsCount,
            coroutineDispatcher = UnconfinedTestDispatcher(),
        )
    }

    companion object {
        private const val DEFAULT_TEST_CONTACTS_COUNT = 3
        private const val DEFAULT_TEST_GROUPS_COUNT = 1
    }
}
