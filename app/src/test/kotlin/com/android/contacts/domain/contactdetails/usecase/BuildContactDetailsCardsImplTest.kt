package com.android.contacts.domain.contactdetails.usecase

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.data.connectedapps.model.ConnectedApp
import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepository
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.domain.contactdetails.model.ContactConnectedApp
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.util.CanVideoCall
import com.android.contacts.domain.util.IsCallWithNoteSupported
import com.android.contacts.domain.util.IsDeviceVoiceCapable
import com.android.contacts.domain.util.IsSipCallingSupported
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.custom
import com.android.contacts.tests.factory.email
import com.android.contacts.tests.factory.event
import com.android.contacts.tests.factory.generic
import com.android.contacts.tests.factory.im
import com.android.contacts.tests.factory.nickname
import com.android.contacts.tests.factory.note
import com.android.contacts.tests.factory.organization
import com.android.contacts.tests.factory.phone
import com.android.contacts.tests.factory.postal
import com.android.contacts.tests.factory.relation
import com.android.contacts.tests.factory.sipAddress
import com.android.contacts.tests.factory.structuredName
import com.android.contacts.tests.factory.website
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BuildContactDetailsCardsImplTest {

    private val isDeviceVoiceCapable = mockk<IsDeviceVoiceCapable>()
    private val isCallWithNoteSupported = mockk<IsCallWithNoteSupported>()
    private val canVideoCall = mockk<CanVideoCall>()
    private val isSipCallingSupported = mockk<IsSipCallingSupported>()
    private val connectedAppsRepository = mockk<ConnectedAppsRepository>()

    private val buildContactDetailsCards = BuildContactDetailsCardsImpl(
        isDeviceVoiceCapable = isDeviceVoiceCapable,
        isCallWithNoteSupported = isCallWithNoteSupported,
        canVideoCall = canVideoCall,
        isSipCallingSupported = isSipCallingSupported,
        connectedAppsRepository = connectedAppsRepository,
    )

    @Before
    fun setUp() {
        every { isDeviceVoiceCapable() } returns true
        every { isCallWithNoteSupported() } returns false
        every { canVideoCall(any()) } returns false
        every { isSipCallingSupported() } returns true
        every { connectedAppsRepository.getConnectedApp(any(), any()) } returns null
    }

    @Test
    fun invoke_ordersTheContactCardByTheLeadingMimeTypes() {
        val details = detailsOf(
            postal(formattedAddress = "1 Main St"),
            email(address = "alex@example.org"),
            sipAddress(address = "alex@sip.example.org"),
            phone(number = "4155551212"),
        )
        val mimeTypes = mimeTypesOf(buildContactDetailsCards(details, null).contactCard)

        assertEquals(
            listOf(
                Phone.CONTENT_ITEM_TYPE,
                Email.CONTENT_ITEM_TYPE,
                StructuredPostal.CONTENT_ITEM_TYPE,
                SipAddress.CONTENT_ITEM_TYPE,
            ),
            mimeTypes,
        )
    }

    @Test
    fun invoke_withAPrioritizedMimeType_putsItFirst() {
        val details = detailsOf(
            phone(number = "4155551212"),
            email(address = "alex@example.org"),
        )
        val cards = buildContactDetailsCards(details, Email.CONTENT_ITEM_TYPE)

        assertEquals(
            listOf(Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE),
            mimeTypesOf(cards.contactCard),
        )
    }

    @Test
    fun invoke_withAnUnknownMimeType_keepsItAfterTheLeadingOnes() {
        val details = detailsOf(
            generic(),
            phone(number = "4155551212"),
        )

        assertEquals(
            listOf(Phone.CONTENT_ITEM_TYPE, "vnd.example/thing"),
            mimeTypesOf(buildContactDetailsCards(details, null).contactCard),
        )
    }

    @Test
    fun invoke_withinAMimeType_putsTheSuperPrimaryItemFirst() {
        val details = detailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isSuperPrimary = true),
            phone(id = 3L, number = "4155553333", isPrimary = true),
        )
        val entries = buildContactDetailsCards(details, null).contactCard.first().entries

        assertEquals(listOf(2L, 3L, 1L), entries.map(ContactEntry::id))
    }

    @Test
    fun invoke_withinAMimeType_keepsTheStoredOrderOfPlainItems() {
        val details = detailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222"),
        )
        val entries = buildContactDetailsCards(details, null).contactCard.first().entries

        assertEquals(listOf(1L, 2L), entries.map(ContactEntry::id))
    }

    @Test
    fun invoke_withinAMimeType_marksTheSuperPrimaryItemAsTheDefault() {
        val details = detailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isSuperPrimary = true),
        )
        val entries = buildContactDetailsCards(details, null).contactCard.first().entries

        assertEquals(listOf(2L), entries.filter(ContactEntry::isDefault).map(ContactEntry::id))
    }

    @Test
    fun invoke_withoutASuperPrimaryItem_marksThePrimaryItemAsTheDefault() {
        val details = detailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isPrimary = true),
        )
        val entries = buildContactDetailsCards(details, null).contactCard.first().entries

        assertEquals(listOf(2L), entries.filter(ContactEntry::isDefault).map(ContactEntry::id))
    }

    @Test
    fun invoke_withoutAnyPrimaryItem_marksNoDefault() {
        val details = detailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222"),
        )
        val entries = buildContactDetailsCards(details, null).contactCard.first().entries

        assertTrue(entries.none(ContactEntry::isDefault))
    }

    @Test
    fun invoke_keepsTheNoteOutOfTheContactCard() {
        val details = detailsOf(
            note(note = "Met at the airport"),
            event(formattedDate = "May 20, 1980"),
            website(url = "example.org"),
        )
        val cards = buildContactDetailsCards(details, null)

        assertEquals(
            listOf(
                ContactEntryText.Value("May 20, 1980"),
                ContactEntryText.Value("example.org"),
            ),
            cards.contactCard.map { group -> group.entries.first().header },
        )
        assertEquals(
            listOf(ContactEntryText.Value("Met at the airport")),
            cards.notes.map { group -> group.entries.first().header },
        )
    }

    @Test
    fun invoke_reportsTheNicknameAndTheOrganizationForTheHeader() {
        val details = detailsOf(
            nickname(name = "Al"),
            organization(company = "Acme", department = "R&D", title = "Engineer"),
        )
        val cards = buildContactDetailsCards(details, null)

        assertEquals(listOf("Al"), cards.headerNicknames)
        assertEquals(listOf(listOf("Engineer", "R&D", "Acme")), cards.headerOrganizations)
    }

    @Test
    fun invoke_forTheHeaderNicknameAndOrganization_buildsNoCardEntries() {
        val details = detailsOf(
            nickname(name = "Al"),
            organization(formattedCompany = "Acme", company = "Acme"),
        )

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withSeveralNicknames_promotesThemAllInPriorityOrder() {
        val details = detailsOf(
            nickname(id = 1L, name = "Al"),
            nickname(id = 2L, name = "Ally", isSuperPrimary = true),
        )
        val cards = buildContactDetailsCards(details, null)

        assertEquals(listOf("Ally", "Al"), cards.headerNicknames)
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withSeveralOrganizations_promotesThemAllInPriorityOrder() {
        val details = detailsOf(
            organization(id = 1L, formattedCompany = "Acme", company = "Acme"),
            organization(
                id = 2L,
                formattedCompany = "Globex",
                company = "Globex",
                isPrimary = true,
            ),
        )
        val cards = buildContactDetailsCards(details, null)

        assertEquals(listOf(listOf("Globex"), listOf("Acme")), cards.headerOrganizations)
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withAnOrganizationWithoutACompany_promotesItsTitleToTheHeader() {
        val details = detailsOf(
            organization(
                formattedCompany = null,
                company = null,
                title = "Test Data Custodian",
            ),
        )
        val cards = buildContactDetailsCards(details, null)

        assertEquals(listOf(listOf("Test Data Custodian")), cards.headerOrganizations)
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withANicknameThatIsTheDisplayName_leavesTheHeaderNicknameEmpty() {
        val details = detailsOf(nickname(name = "Al", rawContactId = 7L))
            .copy(nameRawContactId = 7L, displayNameSource = ContactDisplayNameSource.NICKNAME)

        assertTrue(buildContactDetailsCards(details, null).headerNicknames.isEmpty())
    }

    @Test
    fun invoke_withABlankNickname_leavesTheHeaderNicknameEmpty() {
        val details = detailsOf(nickname(name = " "))

        assertTrue(buildContactDetailsCards(details, null).headerNicknames.isEmpty())
    }

    @Test
    fun invoke_withAPhoneticName_keepsItOutOfTheContactCard() {
        val details = detailsOf(website(url = "example.org")).copy(phoneticName = "Alek")
        val cards = buildContactDetailsCards(details, null)

        assertEquals(
            listOf(ContactEntryText.Value("example.org")),
            cards.contactCard.map { group -> group.entries.first().header },
        )
    }

    @Test
    fun invoke_withAPhoneticName_addsNoEntry() {
        val details = detailsOf().copy(phoneticName = "Alek")

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withoutAPhoneticName_addsNoEntry() {
        val details = detailsOf(website(url = "example.org"))
        val cards = buildContactDetailsCards(details, null)

        assertEquals(1, cards.contactCard.size)
    }

    @Test
    fun invoke_withAStructuredName_buildsNoEntryForIt() {
        val details = detailsOf(structuredName(givenName = "Alex"))
        val cards = buildContactDetailsCards(details, null)

        assertTrue(cards.contactCard.isEmpty())
        assertTrue(cards.notes.isEmpty())
    }

    @Test
    fun invoke_withANicknameFromAnotherRawContact_promotesItToTheHeader() {
        val details = detailsOf(nickname(name = "Al", rawContactId = 11L))
            .copy(nameRawContactId = 7L, displayNameSource = ContactDisplayNameSource.NICKNAME)

        assertEquals(
            listOf("Al"),
            buildContactDetailsCards(details, null).headerNicknames,
        )
    }

    @Test
    fun invoke_withAPhoneWithoutANumber_dropsTheEntry() {
        val details = detailsOf(phone(number = null, displayString = "4155551212"))

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAnEmailWithoutData_dropsTheEntry() {
        val details = detailsOf(email(data = null, address = "alex@example.org"))

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAPostalAddressWithoutAnAddress_dropsTheEntry() {
        val details = detailsOf(postal(formattedAddress = null))

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withASipAddressWithoutAnAddress_dropsTheEntry() {
        val details = detailsOf(sipAddress(address = null))

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAGenericItemWithoutAnyText_dropsTheEntry() {
        val details = detailsOf(generic(displayString = null, typeColumn = null))

        assertTrue(buildContactDetailsCards(details, null).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAGenericItemOwnedByAnotherApp_movesItToConnectedApps() {
        val details = detailsOf(
            phone(number = "4155551212"),
            generic(id = 7L, mimeType = CHAT_MESSAGE, displayString = "Message 088 525 7470"),
        )
        every { connectedAppsRepository.getConnectedApp(7L, CHAT_MESSAGE) } returns CHAT_APP

        val cards = buildContactDetailsCards(details, null)

        assertEquals(listOf(CHAT_APP), cards.connectedApps.map(ContactConnectedApp::app))
        assertEquals(listOf(7L), cards.connectedApps.single().entries.map(ContactEntry::id))
        assertEquals(listOf(Phone.CONTENT_ITEM_TYPE), mimeTypesOf(cards.contactCard))
    }

    @Test
    fun invoke_withSeveralMimeTypesOfOneApp_groupsThemUnderThatApp() {
        val details = detailsOf(
            generic(id = 7L, mimeType = CHAT_MESSAGE, displayString = "Message 088 525 7470"),
            generic(id = 8L, mimeType = CHAT_CALL, displayString = "Voice call 088 525 7470"),
        )
        every { connectedAppsRepository.getConnectedApp(7L, CHAT_MESSAGE) } returns CHAT_APP
        every { connectedAppsRepository.getConnectedApp(8L, CHAT_CALL) } returns CHAT_APP

        val cards = buildContactDetailsCards(details, null)

        assertEquals(listOf(7L, 8L), cards.connectedApps.single().entries.map(ContactEntry::id))
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withAGenericItemNoAppHandles_keepsItInTheContactCard() {
        val details = detailsOf(generic(id = 7L))
        val cards = buildContactDetailsCards(details, null)

        assertTrue(cards.connectedApps.isEmpty())
        assertEquals(listOf(7L), cards.contactCard.single().entries.map(ContactEntry::id))
    }

    @Test
    fun invoke_forAKnownKind_neverAsksForAConnectedApp() {
        val details = detailsOf(phone(number = "4155551212"))
        val cards = buildContactDetailsCards(details, null)

        assertTrue(cards.connectedApps.isEmpty())
        verify(exactly = 0) { connectedAppsRepository.getConnectedApp(any(), any()) }
    }

    @Test
    fun invoke_withAnEmptyNote_dropsTheEntry() {
        val details = detailsOf(note(note = null))

        assertTrue(buildContactDetailsCards(details, null).notes.isEmpty())
    }

    @Test
    fun invoke_forANote_putsTheNoteInTheHeader() {
        val details = detailsOf(note(note = "Met at the airport"))
        val entry = buildContactDetailsCards(details, null).notes.first().entries.first()

        assertEquals(ContactEntryText.Value("Met at the airport"), entry.header)
        assertNull(entry.subHeader)
        assertEquals("Met at the airport", entry.copyText)
    }

    @Test
    fun invoke_forAPhone_buildsTheHeaderAndTypeFromTheDataItem() {
        val details = detailsOf(
            phone(number = "4155551212", displayString = "(415) 555-1212", typeLabel = "Mobile"),
        )
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("(415) 555-1212"), entry.header)
        assertEquals("Mobile", entry.text)
        assertEquals("(415) 555-1212", entry.copyText)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.PHONE), entry.copyLabel)
    }

    @Test
    fun invoke_forAnEmail_labelsTheCopyActionAsEmail() {
        val details = detailsOf(email(address = "alex@example.org", typeLabel = "Work"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("alex@example.org"), entry.header)
        assertEquals("Work", entry.text)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.EMAIL), entry.copyLabel)
    }

    @Test
    fun invoke_forASipAddress_labelsTheCopyActionAsPhone() {
        val details = detailsOf(sipAddress(address = "alex@sip.example.org"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Label(ContactEntryLabel.PHONE), entry.copyLabel)
    }

    @Test
    fun invoke_forAnImProtocol_putsTheAddressInTheHeader() {
        val details = detailsOf(
            im(protocolLabel = "AIM", isCustomProtocol = false),
        )
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("alex@example.org"), entry.header)
        assertEquals(ContactEntryText.Value("AIM"), entry.subHeader)
        assertNull(entry.text)
    }

    @Test
    fun invoke_forAnImWithoutAProtocolLabel_fallsBackToTheImLabel() {
        val details = detailsOf(
            im(protocolLabel = null, isCustomProtocol = true),
        )
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("alex@example.org"), entry.header)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.IM), entry.subHeader)
    }

    @Test
    fun invoke_forACustomFieldWithoutASummary_labelsItAsACustomField() {
        val details = detailsOf(custom(summary = null, content = "Blood type: 0"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("Blood type: 0"), entry.header)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD), entry.subHeader)
    }

    @Test
    fun invoke_forACustomFieldWithASummary_usesTheSummaryAsTheSubHeader() {
        val details = detailsOf(custom(summary = "Blood type", content = "0"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("0"), entry.header)
        assertEquals(ContactEntryText.Value("Blood type"), entry.subHeader)
    }

    @Test
    fun invoke_forAGenericItem_labelsTheCopyActionWithTheMimeType() {
        val details = detailsOf(generic(typeColumn = "data2"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("Thing"), entry.header)
        assertEquals("data2", entry.text)
        assertEquals(ContactEntryText.Value("vnd.example/thing"), entry.copyLabel)
    }

    @Test
    fun invoke_withoutDataItems_buildsEmptyCards() {
        val cards = buildContactDetailsCards(detailsOf(), null)

        assertTrue(cards.contactCard.isEmpty())
        assertTrue(cards.notes.isEmpty())
        assertTrue(cards.headerNicknames.isEmpty())
        assertTrue(cards.headerOrganizations.isEmpty())
        assertTrue(cards.groups.isEmpty())
    }

    @Test
    fun invoke_forAPhone_offersCallingAndMessaging() {
        val details = detailsOf(phone(number = "4155551212"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.Call("4155551212"), entry.actions.primary)
        assertEquals(ContactEntryAction.Sms("4155551212"), entry.actions.alternate)
        assertNull(entry.actions.third)
    }

    @Test
    fun invoke_forAPhoneWithoutTelephony_offersMessagingOnly() {
        every { isDeviceVoiceCapable() } returns false

        val details = detailsOf(phone(number = "4155551212"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertNull(entry.actions.primary)
        assertEquals(ContactEntryAction.Sms("4155551212"), entry.actions.alternate)
    }

    @Test
    fun invoke_whenCallingWithANoteIsSupported_offersItAsTheThirdAction() {
        every { isCallWithNoteSupported() } returns true
        every { canVideoCall(any()) } returns true

        val details = detailsOf(
            phone(number = "4155551212", formattedNumber = "(415) 555-1212", typeLabel = "Mobile"),
        )
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(
            ContactEntryAction.CallWithNote(
                number = "4155551212",
                formattedNumber = "(415) 555-1212",
                numberLabel = "Mobile",
            ),
            entry.actions.third,
        )
    }

    @Test
    fun invoke_whenOnlyVideoCallingIsAvailable_offersItAsTheThirdAction() {
        every { canVideoCall(any()) } returns true

        val details = detailsOf(phone(number = "4155551212"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.VideoCall("4155551212"), entry.actions.third)
    }

    @Test
    fun invoke_forAPhone_asksVideoCallingAboutTheCarrierCapability() {
        every { canVideoCall(true) } returns true
        every { canVideoCall(false) } returns false

        val details = detailsOf(
            phone(id = 1L, number = "4155551111", isCarrierVideoCallCapable = true),
            phone(id = 2L, number = "4155552222", isCarrierVideoCallCapable = false),
        )
        val entries = buildContactDetailsCards(details, null).contactCard.first().entries

        assertEquals(ContactEntryAction.VideoCall("4155551111"), entries.first().actions.third)
        assertNull(entries.last().actions.third)
    }

    @Test
    fun invoke_forAnEmail_offersSendingAnEmail() {
        val details = detailsOf(email(address = "alex@example.org"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.SendEmail("alex@example.org"), entry.actions.primary)
        assertNull(entry.actions.alternate)
    }

    @Test
    fun invoke_forAPostalAddress_offersTheMapAndDirections() {
        val details = detailsOf(postal(formattedAddress = "1 Main St"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.ShowOnMap("1 Main St"), entry.actions.primary)
        assertEquals(ContactEntryAction.ShowDirections("1 Main St"), entry.actions.alternate)
    }

    @Test
    fun invoke_forASipAddress_offersASipCall() {
        val details = detailsOf(sipAddress(address = "alex@sip.example.org"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.SipCall("alex@sip.example.org"), entry.actions.primary)
    }

    @Test
    fun invoke_withoutSipCalling_offersNoSipAction() {
        every { isSipCallingSupported() } returns false

        val details = detailsOf(sipAddress(address = "alex@sip.example.org"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertNull(entry.actions.primary)
    }

    @Test
    fun invoke_forAWebsite_offersOpeningTheUrl() {
        val details = detailsOf(website(url = "example.org"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.OpenUrl("example.org"), entry.actions.primary)
    }

    @Test
    fun invoke_forAnEvent_offersTheDateInTheCalendar() {
        val details = detailsOf(event(formattedDate = "May 20", isRecurringAnnually = true))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(
            ContactEntryAction.ShowEventDate(date = "May 20", isRecurringAnnually = true),
            entry.actions.primary,
        )
    }

    @Test
    fun invoke_forARelation_offersSearchingForTheName() {
        val details = detailsOf(relation(name = "Sam"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryAction.SearchContacts("Sam"), entry.actions.primary)
    }

    @Test
    fun invoke_forAnIm_offersOpeningTheChat() {
        val details = detailsOf(im(data = "alex@example.org", protocol = 5))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(
            ContactEntryAction.OpenChat(
                data = "alex@example.org",
                protocol = 5,
                customProtocol = null,
            ),
            entry.actions.primary,
        )
    }

    @Test
    fun invoke_forAGenericItem_offersViewingTheDataRow() {
        val details = detailsOf(generic(id = 42L, mimeType = "vnd.example/thing"))
        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(
            ContactEntryAction.ViewDataItem(dataId = 42L, mimeType = "vnd.example/thing"),
            entry.actions.primary,
        )
    }

    @Test
    fun invoke_forANote_offersNoAction() {
        val details = detailsOf(note(note = "Met at the airport"))
        val entry = buildContactDetailsCards(details, null).notes.first().entries.first()

        assertNull(entry.actions.primary)
        assertNull(entry.actions.alternate)
        assertNull(entry.actions.third)
    }

    private fun detailsOf(vararg dataItems: ContactDataItem): ContactDetails {
        return contactDetails(dataItems = dataItems.toList())
    }

    private fun mimeTypesOf(card: List<ContactEntryGroup>): List<String?> {
        return card.map { group -> group.mimeType }
    }

    private companion object {
        const val CHAT_MESSAGE = "vnd.android.cursor.item/vnd.com.example.chat.profile"
        const val CHAT_CALL = "vnd.android.cursor.item/vnd.com.example.chat.call"

        val CHAT_APP = ConnectedApp(
            packageName = "com.example.chat",
            label = "Chat",
            iconUri = "android.resource://com.example.chat/1",
        )
    }
}
