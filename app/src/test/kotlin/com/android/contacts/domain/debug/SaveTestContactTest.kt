package com.android.contacts.domain.debug

import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentResolver
import android.content.ContentValues
import android.provider.ContactsContract
import com.android.contacts.domain.debug.usecase.SaveTestContact
import com.android.contacts.domain.debug.usecase.SaveTestContactImpl
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.TestContactFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SaveTestContactTest {

    private val operationsList = mutableListOf<List<ContentProviderOperation>>()
    private val contentResolver = mockk<ContentResolver>(relaxed = true) {
        every { this@mockk.applyBatch(any(), any()) } answers {
            val operations = secondArg<ArrayList<ContentProviderOperation>>()
            operationsList.add(operations)
            emptyArray()
        }
    }

    @Test
    fun setsAccountCorrectly() = runTest {
        val account = AccountModelFactory.build(
            name = "Name",
            type = "device",
            dataSet = "data_set"
        )
        val contact = TestContactFactory.build()

        buildSubject()(account, contact, emptyList())

        verify(exactly = 1) { contentResolver.applyBatch(any(), any()) }

        val values = operationsList.first().first().values
        assertEquals(values[ContactsContract.RawContacts.ACCOUNT_NAME], account.name)
        assertEquals(values[ContactsContract.RawContacts.ACCOUNT_TYPE], account.type)
        assertEquals(values[ContactsContract.RawContacts.DATA_SET], account.dataSet)
    }

    @Test
    fun setsPhoneValuesCorrectly() = runTest {
        val account = AccountModelFactory.build()
        val contact = TestContactFactory.build()

        buildSubject()(account, contact, emptyList())

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

    private val ContentProviderOperation.values
        get(): ContentValues {
            return resolveValueBackReferences(
                arrayOf(ContentProviderResult(1)),
                1,
            )!!
        }

    private fun buildSubject(): SaveTestContact {
        return SaveTestContactImpl(
            contentResolver = contentResolver,
        )
    }
}
