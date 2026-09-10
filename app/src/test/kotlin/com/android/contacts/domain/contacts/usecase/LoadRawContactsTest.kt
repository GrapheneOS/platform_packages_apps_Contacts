package com.android.contacts.domain.contacts.usecase

import androidx.core.net.toUri
import com.android.contacts.data.contacts.model.RawContactsMetadata
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.domain.contacts.mapper.RawContactWithAccountMapper
import com.android.contacts.domain.contacts.model.RawContactWithAccount
import com.android.contacts.tests.AccountDisplayModelFactory
import com.android.contacts.tests.RawContactFactory
import com.android.contacts.tests.RawContactWithAccountFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal class LoadRawContactsTest {

    private val contactsRepository = mockk<ContactsRepository>(relaxed = true)
    private val rawContactWithAccountMapper = mockk<RawContactWithAccountMapper>(relaxed = true)

    private val subject: LoadRawContacts = LoadRawContactsImpl(
        contactsRepository = contactsRepository,
        rawContactWithAccountMapper = rawContactWithAccountMapper,
    )

    @Test
    fun whenLoadIsNull_returnsNull() = runTest {
        every { contactsRepository.loadRawContacts(any()) } returns flowOf(null)

        assertNull(subject("".toUri(), false).first())
    }

    @Test
    fun returnsMetadataBaseFields() = runTest {
        val metadata = RawContactsMetadata(
            contactId = 123L,
            isUserProfile = true,
            rawContacts = emptyList(),
        )
        every { contactsRepository.loadRawContacts(any()) } returns flowOf(metadata)
        val uri = "content://contact/1".toUri()

        val result = subject(uri, false).first()!!

        assertEquals(123L, result.contactId)
        assertTrue(result.isUserProfile)
        assertEquals(emptyList<RawContactWithAccount>(), result.rawContacts)
    }

    @Test
    fun whenOnlyWritableIsFalse_returnsContactsWithReadOnlyAccount() = runTest {
        val contact = RawContactFactory.build(id = 1L)
        val account = AccountDisplayModelFactory.build(areContactsWritable = false)
        val contactWithAccount = RawContactWithAccountFactory.build(account = account)
        every { rawContactWithAccountMapper.map(any()) } returns contactWithAccount
        val metadata = RawContactsMetadata(
            contactId = 123L,
            isUserProfile = true,
            rawContacts = listOf(contact),
        )
        every { contactsRepository.loadRawContacts(any()) } returns flowOf(metadata)

        val result = subject("".toUri(), false).first()!!

        assertEquals(listOf(contactWithAccount), result.rawContacts)
        verify { rawContactWithAccountMapper.map(contact) }
    }

    @Test
    fun whenOnlyWritableIsTrue_returnsOnlyContactsWithWritableAccount() = runTest {
        val contact1 = RawContactFactory.build(id = 1L)
        val contact2 = RawContactFactory.build(id = 2L)
        val accountReadOnly = AccountDisplayModelFactory.build(areContactsWritable = false)
        val contactWithAccountReadOnly = RawContactWithAccountFactory.build(
            account = accountReadOnly,
        )
        val accountWritable = AccountDisplayModelFactory.build(areContactsWritable = true)
        val contactWithAccountWritable = RawContactWithAccountFactory.build(
            account = accountWritable,
        )
        every { rawContactWithAccountMapper.map(contact1) } returns contactWithAccountReadOnly
        every { rawContactWithAccountMapper.map(contact2) } returns contactWithAccountWritable
        val metadata = RawContactsMetadata(
            contactId = 123L,
            isUserProfile = true,
            rawContacts = listOf(contact1, contact2),
        )
        every { contactsRepository.loadRawContacts(any()) } returns flowOf(metadata)

        val result = subject("".toUri(), true).first()!!

        assertEquals(listOf(contactWithAccountWritable), result.rawContacts)
    }
}
