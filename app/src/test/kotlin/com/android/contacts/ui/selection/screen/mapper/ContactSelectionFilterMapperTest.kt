package com.android.contacts.ui.selection.screen.mapper

import com.android.contacts.domain.contacts.model.ContactSelectionFilter as Filter
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import org.junit.Assert.assertEquals
import org.junit.Test

internal class ContactSelectionFilterMapperTest {

    private val subject: ContactSelectionFilterMapper = ContactSelectionFilterMapperImpl()

    @Test
    fun pick_returnsContactsWithDirectories() {
        assertEquals(
            Filter.Contacts(includesDirectories = true),
            subject.map(Mode.Pick()),
        )
    }

    @Test
    fun pickMultiple_returnsContactsWithoutDirectories() {
        assertEquals(
            Filter.Contacts(includesDirectories = false),
            subject.map(Mode.Pick(multiple = true)),
        )
    }

    @Test
    fun pickLegacy_returnsLegacyPeople() {
        assertEquals(Filter.Legacy.People, subject.map(Mode.Pick(legacy = true)))
    }

    @Test
    fun insertOrEdit_returnsContacts() {
        assertEquals(Filter.Contacts(), subject.map(Mode.InsertOrEdit))
    }

    @Test
    fun createShortcut_mapsTargetToFilter() {
        assertEquals(
            Filter.Contacts(includesDirectories = true),
            subject.map(Mode.CreateShortcut(ShortcutTarget.CONTACT)),
        )
        assertEquals(
            Filter.PhoneNumbers(callableOnly = true),
            subject.map(Mode.CreateShortcut(ShortcutTarget.CALL)),
        )
        assertEquals(
            Filter.PhoneNumbers(callableOnly = false),
            subject.map(Mode.CreateShortcut(ShortcutTarget.MESSAGE)),
        )
    }

    @Test
    fun pickPhoneNumber_returnsPhoneNumbers() {
        assertEquals(Filter.PhoneNumbers(), subject.map(Mode.PickPhoneNumber()))
        assertEquals(Filter.PhoneNumbers(), subject.map(Mode.PickPhoneNumber(multiple = true)))
    }

    @Test
    fun pickPhoneNumberLegacy_returnsLegacyPhones() {
        assertEquals(Filter.Legacy.Phones, subject.map(Mode.PickPhoneNumber(legacy = true)))
    }

    @Test
    fun pickEmail_returnsEmails() {
        assertEquals(Filter.Emails(), subject.map(Mode.PickEmail()))
        assertEquals(Filter.Emails(), subject.map(Mode.PickEmail(multiple = true)))
    }

    @Test
    fun pickPostal_returnsPostalAddresses() {
        assertEquals(Filter.PostalAddresses, subject.map(Mode.PickPostal()))
    }

    @Test
    fun pickPostalLegacy_returnsLegacyPostalAddresses() {
        assertEquals(Filter.Legacy.PostalAddresses, subject.map(Mode.PickPostal(legacy = true)))
    }

    @Test
    fun join_returnsJoinCandidatesForTarget() {
        assertEquals(Filter.JoinCandidates(targetContactId = 7L), subject.map(Mode.Join(7L)))
    }

    @Test
    fun addGroupMembers_returnsAccountContactsExcludingExistingMembers() {
        val account = AccountModelFactory.build(name = "Work")

        val result = subject.map(
            Mode.AddGroupMembers(account = account, existingMemberIds = setOf(1L, 2L)),
        )

        assertEquals(Filter.Contacts(account = account, excludeIds = setOf(1L, 2L)), result)
    }

    @Test
    fun sendToGroupSms_returnsPhoneNumbersRestrictedToItems() {
        val result = subject.map(sendToGroup(SentToGroupTarget.SMS))

        assertEquals(Filter.PhoneNumbers(restrictToContactIds = setOf(1L, 2L)), result)
    }

    @Test
    fun sendToGroupEmail_returnsEmailsRestrictedToItems() {
        val result = subject.map(sendToGroup(SentToGroupTarget.EMAIL))

        assertEquals(Filter.Emails(restrictToContactIds = setOf(1L, 2L)), result)
    }

    private fun sendToGroup(target: SentToGroupTarget): Mode.SendToGroup {
        return Mode.SendToGroup(
            target = target,
            itemIds = listOf(1L, 2L, 2L),
            defaultSelection = emptyList(),
            chooserTitle = null,
        )
    }
}
