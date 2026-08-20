package com.android.contacts.domain.debug

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.ContactsContract
import com.android.contacts.domain.accounts.model.AccountDisplayModel
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.debug.usecase.ClearSeededTestData
import com.android.contacts.domain.debug.usecase.GenerateTestContact
import com.android.contacts.domain.debug.usecase.SeedTestData
import com.android.contacts.domain.debug.usecase.SeedTestDataImpl
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.TestContactFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
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
    private val operationsList = mutableListOf<List<ContentProviderOperation>>()
    private val contentResolver = mockk<ContentResolver>(relaxed = true) {
        every { this@mockk.applyBatch(any(), any()) } answers {
            val operations = secondArg<ArrayList<ContentProviderOperation>>()
            operationsList.add(operations)
            emptyArray()
        }
    }

    @Test
    fun usesDeviceAccount_ifAvailable() = runTest {
        val deviceAccount = buildAccount(isDeviceAccount = true)
        val otherAccount = buildAccount(isDeviceAccount = false)
        loadAccounts = { flowOf(persistentListOf(otherAccount, deviceAccount)) }
        every { getDefaultAccount() } returns otherAccount.account

        buildSubject()()

        verify(exactly = DEFAULT_TEST_CONTACTS_COUNT) {
            contentResolver.applyBatch(any(), any())
        }

        assertTrue(
            "A contact was inserted to the wrong non-device account",
            operationsList.all { operations ->
                val firstOperation = operations.first()
                valuesMatchAccount(firstOperation.values, deviceAccount.account)
            },
        )
    }

    @Test
    fun usesDefaultAccount_ifThereIsNoDeviceAccount() = runTest {
        val defaultAccount = buildAccount(isDeviceAccount = false)
        val otherAccount = buildAccount(isDeviceAccount = false)
        loadAccounts = { flowOf(persistentListOf(otherAccount, defaultAccount)) }
        every { getDefaultAccount() } returns defaultAccount.account

        buildSubject()()

        verify(exactly = DEFAULT_TEST_CONTACTS_COUNT) {
            contentResolver.applyBatch(any(), any())
        }

        assertTrue(
            "A contact was inserted to the wrong non-default account",
            operationsList.all { operations ->
                val firstOperation = operations.first()
                valuesMatchAccount(firstOperation.values, defaultAccount.account)
            },
        )
    }

    @Test
    fun usesFirstAccount_ifThereIsNoDeviceOrDefaultAccount() = runTest {
        val firstAccount = buildAccount(name = "1", isDeviceAccount = false)
        val secondAccount = buildAccount(name = "2", isDeviceAccount = false)
        loadAccounts = { flowOf(persistentListOf(firstAccount, secondAccount)) }
        every { getDefaultAccount() } returns null

        buildSubject()()

        verify(exactly = DEFAULT_TEST_CONTACTS_COUNT) {
            contentResolver.applyBatch(any(), any())
        }

        assertTrue(
            "A contact was inserted to the wrong non-first account",
            operationsList.all { operations ->
                val firstOperation = operations.first()
                valuesMatchAccount(firstOperation.values, firstAccount.account)
            },
        )
    }

    @Test
    fun doesNothing_ifThereIsNoAccount() = runTest {
        loadAccounts = { flowOf(persistentListOf()) }
        every { getDefaultAccount() } returns null

        buildSubject()()

        verify(exactly = 0) { contentResolver.applyBatch(any(), any()) }
    }

    @Test
    fun setsPhoneValuesCorrectly() = runTest {
        loadAccounts = { flowOf(persistentListOf(buildAccount())) }
        val contact = TestContactFactory.build()
        every { generateTestContact() } returns contact

        buildSubject(testContactsCount = 1)()

        verify(exactly = 1) { contentResolver.applyBatch(any(), any()) }

        assertEquals(
            "More than 1 contact as added",
            1,
            operationsList.size,
        )
        val operations = operationsList.first()
        val phoneOperations = operations.filter {
            it.values[ContactsContract.Data.MIMETYPE] ==
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        }
        assertEquals(
            "The phone operations amount does not match the number of phones of the contact",
            contact.phones.size,
            phoneOperations.size,
        )

        assertTrue(
            "Phone operations values do not match contact phones",
            phoneOperations.mapIndexed { index, operation ->
                val phone = contact.phones[index]
                val values = operation.values
                values[ContactsContract.CommonDataKinds.Phone.NUMBER] == phone.value &&
                    values[ContactsContract.CommonDataKinds.Phone.TYPE] == phone.type
            }.all { it },
        )
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

    private val ContentProviderOperation.values
        get(): ContentValues {
            return resolveValueBackReferences(
                arrayOf(ContentProviderResult(1)),
                1,
            )!!
        }

    private fun valuesMatchAccount(values: ContentValues, account: AccountModel): Boolean {
        return values[ContactsContract.RawContacts.ACCOUNT_NAME] == account.name &&
            values[ContactsContract.RawContacts.ACCOUNT_TYPE] == account.type &&
            values[ContactsContract.RawContacts.DATA_SET] == account.dataSet
    }

    private fun buildSubject(testContactsCount: Int = DEFAULT_TEST_CONTACTS_COUNT): SeedTestData {
        return SeedTestDataImpl(
            loadAccounts = loadAccounts,
            getDefaultAccount = getDefaultAccount,
            clearSeededTestData = clearSeededTestData,
            generateTestContact = generateTestContact,
            contentResolver = contentResolver,
            testContactsCount = testContactsCount,
            coroutineDispatcher = UnconfinedTestDispatcher(),
        )
    }

    companion object {
        private const val DEFAULT_TEST_CONTACTS_COUNT = 3
    }
}
