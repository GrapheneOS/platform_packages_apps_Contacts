package com.android.contacts.domain.photo.usecase

import androidx.core.net.toUri
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.accounts.usecase.GetDefaultAccount
import com.android.contacts.domain.accounts.usecase.LoadAccounts
import com.android.contacts.domain.accounts.usecase.ShouldPromptForDefaultAccount
import com.android.contacts.domain.contacts.model.RawContactWithAccount
import com.android.contacts.domain.contacts.model.RawContactsResult
import com.android.contacts.domain.contacts.usecase.LoadRawContacts
import com.android.contacts.domain.photo.model.AttachPhotoTarget
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.RawContactWithAccountFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ResolveAttachPhotoTargetTest {

    private val loadRawContacts = mockk<LoadRawContacts>()
    private val loadAccounts = mockk<LoadAccounts>()
    private val shouldPromptForDefaultAccount = mockk<ShouldPromptForDefaultAccount>()
    private val getDefaultAccount = mockk<GetDefaultAccount>()

    private val subject: ResolveAttachPhotoTarget = ResolveAttachPhotoTargetImpl(
        loadRawContacts = loadRawContacts,
        loadAccounts = loadAccounts,
        shouldPromptForDefaultAccount = shouldPromptForDefaultAccount,
        getDefaultAccount = getDefaultAccount,
    )

    @Test
    fun whenContactIsNotFound_returnsNotFound() = runTest {
        every { loadRawContacts(CONTACT_URI, onlyWritable = true) } returns flowOf(null)

        assertEquals(AttachPhotoTarget.NotFound, subject(CONTACT_URI))
    }

    @Test
    fun whenWritableRawContactExists_returnsFirstOne() = runTest {
        givenWritableRawContacts(
            RawContactWithAccountFactory.build(id = 10L),
            RawContactWithAccountFactory.build(id = 11L),
        )

        assertEquals(
            AttachPhotoTarget.Ready.ExistingRawContact(rawContactId = 10L),
            subject(CONTACT_URI),
        )
        verify { loadRawContacts(CONTACT_URI, onlyWritable = true) }
        verify(exactly = 0) { loadAccounts(any()) }
    }

    @Test
    fun whenNoWritableRawContact_andPromptIsNeeded_returnsNeedsAccountSelection() = runTest {
        givenWritableRawContacts()
        givenInsertableAccounts(ACCOUNT, OTHER_ACCOUNT)
        every { shouldPromptForDefaultAccount(listOf(ACCOUNT, OTHER_ACCOUNT)) } returns true

        assertEquals(AttachPhotoTarget.NeedsAccountSelection, subject(CONTACT_URI))
        verify { loadAccounts(AccountFilter.CONTACTS_INSERTABLE) }
    }

    @Test
    fun whenNoWritableRawContact_andSingleAccount_returnsNewRawContactInThatAccount() = runTest {
        givenWritableRawContacts()
        givenInsertableAccounts(ACCOUNT)
        every { shouldPromptForDefaultAccount(any()) } returns false

        assertEquals(AttachPhotoTarget.Ready.NewRawContact(ACCOUNT), subject(CONTACT_URI))
        verify(exactly = 0) { getDefaultAccount() }
    }

    @Test
    fun whenNoWritableRawContact_andMultipleAccounts_returnsNewRawContactInDefault() = runTest {
        givenWritableRawContacts()
        givenInsertableAccounts(ACCOUNT, OTHER_ACCOUNT)
        every { shouldPromptForDefaultAccount(any()) } returns false
        every { getDefaultAccount() } returns OTHER_ACCOUNT

        assertEquals(AttachPhotoTarget.Ready.NewRawContact(OTHER_ACCOUNT), subject(CONTACT_URI))
    }

    @Test
    fun whenNoWritableRawContact_andNoDefaultAccount_returnsNewRawContactInNullAccount() =
        runTest {
            givenWritableRawContacts()
            givenInsertableAccounts()
            every { shouldPromptForDefaultAccount(any()) } returns false
            every { getDefaultAccount() } returns null

            assertEquals(
                AttachPhotoTarget.Ready.NewRawContact(AccountModel.NULL),
                subject(CONTACT_URI),
            )
        }

    private fun givenWritableRawContacts(vararg rawContacts: RawContactWithAccount) {
        every { loadRawContacts(CONTACT_URI, onlyWritable = true) } returns flowOf(
            RawContactsResult(
                contactId = 1L,
                isUserProfile = false,
                rawContacts = rawContacts.toList(),
            ),
        )
    }

    private fun givenInsertableAccounts(vararg accounts: AccountModel) {
        every { loadAccounts(AccountFilter.CONTACTS_INSERTABLE) } returns flowOf(
            accounts.map { AccountDisplayModelFactory.build(account = it) },
        )
    }

    private companion object {
        val CONTACT_URI = "content://com.android.contacts/contacts/lookup/abc/1".toUri()
        val ACCOUNT = AccountModelFactory.build(name = "user@example.org", type = "com.example")
        val OTHER_ACCOUNT = AccountModelFactory.build(
            name = "other@example.org",
            type = "com.example",
        )
    }
}
