package com.android.contacts.ui.selection.screen.mapper

import android.content.ContentUris
import android.net.Uri
import android.provider.Contacts.ContactMethods as LegacyContactMethods
import android.provider.Contacts.People as LegacyPeople
import android.provider.Contacts.Phones as LegacyPhones
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Contacts
import android.provider.ContactsContract.DIRECTORY_PARAM_KEY
import android.provider.ContactsContract.Data
import androidx.core.net.toUri
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.contacts.model.ContactLookupQuery
import com.android.contacts.data.contacts.model.ContactLookupResult
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.selection.screen.model.ContactSelectionEffect as Effect
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionResult as Result
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("DEPRECATION")
@RunWith(RobolectricTestRunner::class)
internal class ContactSelectionEffectMapperTest {

    private val contactsRepository = mockk<ContactsRepository>()

    private val subject = ContactSelectionEffectMapperImpl(contactsRepository = contactsRepository)

    @Before
    fun setUp() {
        every { contactsRepository.lookup(any()) } returns flowOf(null)
        every {
            contactsRepository.lookup(ContactLookupQuery.Id(CONTACT_ID))
        } returns flowOf(listOf(ContactLookupResult(CONTACT_ID, "lookup", LOOKUP_URI)))
    }

    @Test
    fun emptyKeys_closesCanceled() = runTest {
        assertEquals(close(Result.Canceled), subject.map(Mode.Pick(), emptySet()))
    }

    // Directory contacts

    @Test
    fun directoryKey_withPick_returnsDirectoryLookupUri() = runTest {
        val result = subject.map(Mode.Pick(), setOf(DIRECTORY_KEY))

        assertEquals(close(Result.Picked(DIRECTORY_URI)), result)
    }

    @Test
    fun directoryKey_withContactShortcut_returnsShortcut() = runTest {
        val result = subject.map(Mode.CreateShortcut(ShortcutTarget.CONTACT), setOf(DIRECTORY_KEY))

        assertEquals(close(Result.Shortcut(ShortcutTarget.CONTACT, DIRECTORY_URI)), result)
    }

    @Test
    fun directoryKey_withUnsupportedModes_closesCanceled() = runTest {
        listOf(
            Mode.CreateShortcut(ShortcutTarget.CALL),
            Mode.InsertOrEdit,
            Mode.PickPhoneNumber(),
            Mode.Join(1L),
        ).forEach { mode ->
            val result = subject.map(mode, setOf(DIRECTORY_KEY))

            assertEquals(
                mode.toString(),
                close(Result.Canceled),
                result,
            )
        }
    }

    @Test
    fun directoryAndLocalKeys_prefersDirectoryKey() = runTest {
        val result = subject.map(Mode.Pick(), setOf(LOCAL_KEY, DIRECTORY_KEY))

        assertEquals(close(Result.Picked(DIRECTORY_URI)), result)
    }

    // Pick

    @Test
    fun pick_returnsLookupUri() = runTest {
        assertEquals(
            close(Result.Picked(LOOKUP_URI)),
            subject.map(Mode.Pick(), setOf(LOCAL_KEY)),
        )
    }

    @Test
    fun pick_whenLookupFails_fallsBackToContactUri() = runTest {
        every { contactsRepository.lookup(any()) } returns flowOf(emptyList())

        val result = subject.map(Mode.Pick(), setOf(LOCAL_KEY))

        assertEquals(close(Result.Picked(Contacts.CONTENT_URI.withId(CONTACT_ID))), result)
    }

    @Test
    fun pickLegacy_returnsLegacyPeopleUri() = runTest {
        val result = subject.map(Mode.Pick(legacy = true), setOf(LOCAL_KEY))

        assertEquals(close(Result.Picked(LegacyPeople.CONTENT_URI.withId(CONTACT_ID))), result)
    }

    @Test
    fun pickMultiple_returnsContactUris() = runTest {
        val result = subject.map(Mode.Pick(multiple = true), localKeys(1L, 2L))

        assertEquals(
            close(
                Result.PickedMultiple(
                    uris = listOf(Contacts.CONTENT_URI.withId(1L), Contacts.CONTENT_URI.withId(2L)),
                    mimeType = Contacts.CONTENT_ITEM_TYPE,
                ),
            ),
            result,
        )
    }

    // Insert or edit

    @Test
    fun insertOrEdit_forwardsToEditorWithLookupUri() = runTest {
        assertEquals(
            Effect.ForwardToEditor.EditContact(LOOKUP_URI),
            subject.map(Mode.InsertOrEdit, setOf(LOCAL_KEY)),
        )
    }

    // Phone numbers

    @Test
    fun pickPhoneNumber_returnsDataUri() = runTest {
        val result = subject.map(Mode.PickPhoneNumber(), setOf(LOCAL_KEY))

        assertEquals(
            close(Result.Picked(Data.CONTENT_URI.withId(CONTACT_ID))),
            result,
        )
    }

    @Test
    fun pickPhoneNumberLegacy_returnsLegacyPhonesUri() = runTest {
        val result = subject.map(Mode.PickPhoneNumber(legacy = true), setOf(LOCAL_KEY))

        assertEquals(
            close(Result.Picked(LegacyPhones.CONTENT_URI.withId(CONTACT_ID))),
            result,
        )
    }

    @Test
    fun pickPhoneNumberMultiple_returnsPhoneUris() = runTest {
        val result = subject.map(Mode.PickPhoneNumber(multiple = true), localKeys(1L, 2L))

        assertEquals(
            close(
                Result.PickedMultiple(
                    uris = listOf(Phone.CONTENT_URI.withId(1L), Phone.CONTENT_URI.withId(2L)),
                    mimeType = Phone.CONTENT_ITEM_TYPE,
                ),
            ),
            result,
        )
    }

    // Emails

    @Test
    fun pickEmail_returnsDataUri() = runTest {
        val result = subject.map(Mode.PickEmail(), setOf(LOCAL_KEY))

        assertEquals(
            close(Result.Picked(Data.CONTENT_URI.withId(CONTACT_ID))),
            result,
        )
    }

    @Test
    fun pickEmailMultiple_returnsEmailUris() = runTest {
        val result = subject.map(Mode.PickEmail(multiple = true), localKeys(1L, 2L))

        assertEquals(
            close(
                Result.PickedMultiple(
                    uris = listOf(Email.CONTENT_URI.withId(1L), Email.CONTENT_URI.withId(2L)),
                    mimeType = Email.CONTENT_ITEM_TYPE,
                ),
            ),
            result,
        )
    }

    // Postal addresses

    @Test
    fun pickPostal_returnsDataUri() = runTest {
        val result = subject.map(Mode.PickPostal(), setOf(LOCAL_KEY))

        assertEquals(
            close(Result.Picked(Data.CONTENT_URI.withId(CONTACT_ID))),
            result,
        )
    }

    @Test
    fun pickPostalLegacy_returnsLegacyContactMethodsUri() = runTest {
        val result = subject.map(Mode.PickPostal(legacy = true), setOf(LOCAL_KEY))

        assertEquals(
            close(Result.Picked(LegacyContactMethods.CONTENT_URI.withId(CONTACT_ID))),
            result,
        )
    }

    // Shortcuts

    @Test
    fun createContactShortcut_returnsLookupUri() = runTest {
        val result = subject.map(Mode.CreateShortcut(ShortcutTarget.CONTACT), setOf(LOCAL_KEY))

        assertEquals(
            close(Result.Shortcut(ShortcutTarget.CONTACT, LOOKUP_URI)),
            result,
        )
    }

    @Test
    fun createCallAndMessageShortcuts_returnDataUri() = runTest {
        listOf(ShortcutTarget.CALL, ShortcutTarget.MESSAGE).forEach { target ->
            val result = subject.map(Mode.CreateShortcut(target), setOf(LOCAL_KEY))

            assertEquals(
                close(Result.Shortcut(target, Data.CONTENT_URI.withId(CONTACT_ID))),
                result,
            )
        }
    }

    // Join

    @Test
    fun join_returnsLookupUri() = runTest {
        assertEquals(
            close(Result.Picked(LOOKUP_URI)),
            subject.map(Mode.Join(1L), setOf(LOCAL_KEY)),
        )
    }

    // Group members

    @Test
    fun addGroupMembers_withSingleContact_returnsGroupMember() = runTest {
        val result = subject.map(ADD_GROUP_MEMBERS, setOf(LOCAL_KEY))

        assertEquals(close(Result.GroupMember(CONTACT_ID)), result)
    }

    @Test
    fun addGroupMembers_withMultipleContacts_returnsGroupMembers() = runTest {
        val result = subject.map(ADD_GROUP_MEMBERS, localKeys(1L, 2L))

        assertEquals(close(Result.GroupMembers(setOf(1L, 2L))), result)
    }

    // Send to group

    @Test
    fun sendToGroupSms_sendsToPhoneNumbers() = runTest {
        coEvery {
            contactsRepository.getContactDataValues(ContactDataKind.Phone(), setOf(1L, 2L))
        } returns listOf("+1 555 0100", "+1 555 0101")

        val result = subject.map(sendToGroup(SentToGroupTarget.SMS), localKeys(1L, 2L))

        assertEquals(
            Effect.SendTo(
                target = SentToGroupTarget.SMS,
                addresses = listOf("+1 555 0100", "+1 555 0101"),
                chooserTitle = "Send",
            ),
            result,
        )
    }

    @Test
    fun sendToGroupEmail_sendsToEmails() = runTest {
        coEvery {
            contactsRepository.getContactDataValues(ContactDataKind.Email, setOf(1L))
        } returns listOf("john@example.com")

        val result = subject.map(sendToGroup(SentToGroupTarget.EMAIL), localKeys(1L))

        assertEquals(
            Effect.SendTo(
                target = SentToGroupTarget.EMAIL,
                addresses = listOf("john@example.com"),
                chooserTitle = "Send",
            ),
            result,
        )
    }

    @Test
    fun sendToGroup_withoutAddresses_closesCanceled() = runTest {
        coEvery { contactsRepository.getContactDataValues(any(), any()) } returns emptyList()

        val result = subject.map(sendToGroup(SentToGroupTarget.SMS), localKeys(1L))

        assertEquals(close(Result.Canceled), result)
        coVerify { contactsRepository.getContactDataValues(ContactDataKind.Phone(), setOf(1L)) }
    }

    private fun close(result: Result) = Effect.Close(result)

    private fun localKeys(vararg ids: Long): Set<ContactItemKey> {
        return ids.mapTo(linkedSetOf()) { ContactItemKey.Local(it) }
    }

    private fun sendToGroup(target: SentToGroupTarget) = Mode.SendToGroup(
        target = target,
        itemIds = listOf(1L, 2L),
        defaultSelection = emptyList(),
        chooserTitle = "Send",
    )

    private fun Uri.withId(id: Long): Uri = ContentUris.withAppendedId(this, id)

    private companion object {
        const val CONTACT_ID = 42L
        val LOCAL_KEY = ContactItemKey.Local(CONTACT_ID)
        val LOOKUP_URI = "content://com.android.contacts/contacts/lookup/lookup/42".toUri()
        val DIRECTORY_KEY = ContactItemKey.Directory(
            directoryId = 10L,
            id = 7L,
            lookupKey = "remote-lookup",
        )
        val DIRECTORY_URI = Contacts.getLookupUri(7L, "remote-lookup")
            .buildUpon()
            .appendQueryParameter(DIRECTORY_PARAM_KEY, "10")
            .build()
        val ADD_GROUP_MEMBERS = Mode.AddGroupMembers(
            account = AccountModelFactory.build(),
            existingMemberIds = emptySet(),
        )
    }
}
