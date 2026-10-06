package com.android.contacts.domain.contactdetails.usecase

import com.android.contacts.data.contactdetails.repository.ContactShortcutRepository
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapable
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.tests.factory.contactCapabilities
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class GetContactDetailsMenuImplTest {

    private val contactShortcutRepository = mockk<ContactShortcutRepository>()
    private val isDeviceVoiceCapable = mockk<IsDeviceVoiceCapable>()

    private val getContactDetailsMenu = GetContactDetailsMenuImpl(
        contactShortcutRepository = contactShortcutRepository,
        isDeviceVoiceCapable = isDeviceVoiceCapable,
    )

    @Before
    fun setUp() {
        every { contactShortcutRepository.isPinShortcutSupported() } returns true
        every { isDeviceVoiceCapable() } returns true
    }

    @Test
    fun invoke_forAnEditableContact_showsEveryItem() {
        val menu = getContactDetailsMenu(contactCapabilities())

        assertEquals(
            ContactDetailsMenu(
                isStarVisible = true,
                editAction = ContactDetailsEditAction.EDIT,
                isJoinVisible = true,
                isLinkedContactsVisible = false,
                isDeleteVisible = true,
                isShareVisible = true,
                isShortcutVisible = true,
                isRingtoneVisible = true,
                isSendToVoicemailVisible = true,
            ),
            menu,
        )
    }

    @Test
    fun invoke_forADirectoryContact_hidesTheStar() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertFalse(menu.isStarVisible)
    }

    @Test
    fun invoke_forTheUserProfile_hidesTheStar() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertFalse(menu.isStarVisible)
    }

    @Test
    fun invoke_forAnAddableDirectoryContact_offersAddingIt() {
        val menu = getContactDetailsMenu(
            contactCapabilities(isDirectoryEntry = true, isAddableDirectoryContact = true),
        )

        assertEquals(ContactDetailsEditAction.ADD, menu.editAction)
    }

    @Test
    fun invoke_forAnInvisibleAndAddableContact_offersAddingIt() {
        val menu = getContactDetailsMenu(contactCapabilities(isInvisibleAndAddable = true))

        assertEquals(ContactDetailsEditAction.ADD, menu.editAction)
    }

    @Test
    fun invoke_forAReadOnlyDirectoryContact_hidesEditing() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertEquals(ContactDetailsEditAction.HIDDEN, menu.editAction)
    }

    @Test
    fun invoke_forTheUserProfile_offersEditing() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertEquals(ContactDetailsEditAction.EDIT, menu.editAction)
    }

    @Test
    fun invoke_forAnInvisibleAndAddableContact_hidesLinking() {
        val menu = getContactDetailsMenu(contactCapabilities(isInvisibleAndAddable = true))

        assertFalse(menu.isJoinVisible)
    }

    @Test
    fun invoke_forADirectoryContact_hidesLinking() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertFalse(menu.isJoinVisible)
    }

    @Test
    fun invoke_forTheUserProfile_hidesLinking() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertFalse(menu.isJoinVisible)
    }

    @Test
    fun invoke_forAContactWithSeveralRawContacts_offersLinkedContactsInsteadOfLinking() {
        val menu = getContactDetailsMenu(contactCapabilities(hasMultipleRawContacts = true))

        assertFalse(menu.isJoinVisible)
        assertTrue(menu.isLinkedContactsVisible)
    }

    @Test
    fun invoke_forADirectoryContactWithSeveralRawContacts_offersLinkedContacts() {
        val menu = getContactDetailsMenu(
            contactCapabilities(isDirectoryEntry = true, hasMultipleRawContacts = true),
        )

        assertTrue(menu.isLinkedContactsVisible)
    }

    @Test
    fun invoke_forAContactWithOneRawContact_hidesLinkedContacts() {
        val menu = getContactDetailsMenu(contactCapabilities())

        assertFalse(menu.isLinkedContactsVisible)
    }

    @Test
    fun invoke_forADirectoryContact_hidesDeleting() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertFalse(menu.isDeleteVisible)
    }

    @Test
    fun invoke_forTheUserProfile_hidesDeleting() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertFalse(menu.isDeleteVisible)
    }

    @Test
    fun invoke_forADirectoryContact_hidesSharing() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertFalse(menu.isShareVisible)
    }

    @Test
    fun invoke_forTheUserProfile_offersSharing() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertTrue(menu.isShareVisible)
    }

    @Test
    fun invoke_forADirectoryContact_hidesTheShortcut() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertFalse(menu.isShortcutVisible)
    }

    @Test
    fun invoke_forTheUserProfile_hidesTheShortcut() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertFalse(menu.isShortcutVisible)
    }

    @Test
    fun invoke_whenTheLauncherDoesNotPinShortcuts_hidesTheShortcut() {
        every { contactShortcutRepository.isPinShortcutSupported() } returns false

        val menu = getContactDetailsMenu(contactCapabilities())

        assertFalse(menu.isShortcutVisible)
    }

    @Test
    fun invoke_withoutTelephony_hidesTheRingtone() {
        every { isDeviceVoiceCapable() } returns false

        val menu = getContactDetailsMenu(contactCapabilities())

        assertFalse(menu.isRingtoneVisible)
    }

    @Test
    fun invoke_forADirectoryContact_hidesTheRingtone() {
        val menu = getContactDetailsMenu(contactCapabilities(isDirectoryEntry = true))

        assertFalse(menu.isRingtoneVisible)
    }

    @Test
    fun invoke_forTheUserProfile_hidesTheRingtone() {
        val menu = getContactDetailsMenu(contactCapabilities(isUserProfile = true))

        assertFalse(menu.isRingtoneVisible)
    }
}
