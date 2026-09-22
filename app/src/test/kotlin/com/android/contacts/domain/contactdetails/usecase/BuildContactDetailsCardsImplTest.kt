package com.android.contacts.domain.contactdetails.usecase

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.data.connectedapps.model.ConnectedApp
import com.android.contacts.data.connectedapps.repository.ConnectedAppsRepository
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.telecom.source.IsCallWithNoteSupported
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapable
import com.android.contacts.data.telecom.source.IsSipCallingSupported
import com.android.contacts.domain.contactdetails.mapper.ContactEntryContentMapperImpl
import com.android.contacts.domain.contactdetails.model.ContactConnectedApp
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.telecom.usecase.CanVideoCall
import com.android.contacts.tests.factory.contactDetailsOf
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

internal class BuildContactDetailsCardsImplTest {

    private val isDeviceVoiceCapable = mockk<IsDeviceVoiceCapable>()
    private val isCallWithNoteSupported = mockk<IsCallWithNoteSupported>()
    private val canVideoCall = mockk<CanVideoCall>()
    private val isSipCallingSupported = mockk<IsSipCallingSupported>()
    private val connectedAppsRepository = mockk<ConnectedAppsRepository>()

    private val contactEntryContentMapper = ContactEntryContentMapperImpl(
        isDeviceVoiceCapable = isDeviceVoiceCapable,
        isCallWithNoteSupported = isCallWithNoteSupported,
        canVideoCall = canVideoCall,
        isSipCallingSupported = isSipCallingSupported,
    )

    private val buildContactDetailsCards = BuildContactDetailsCardsImpl(
        contactEntryContentMapper = contactEntryContentMapper,
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
        val details = contactDetailsOf(
            postal(formattedAddress = "1 Main St"),
            email(address = "alex@example.org"),
            sipAddress(address = "alex@sip.example.org"),
            phone(number = "4155551212"),
        )
        val mimeTypes = mimeTypesOf(cardsOf(details).contactCard)

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
        val details = contactDetailsOf(
            phone(number = "4155551212"),
            email(address = "alex@example.org"),
        )
        val cards = cardsOf(details, prioritizedMimeType = Email.CONTENT_ITEM_TYPE)

        assertEquals(
            listOf(Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE),
            mimeTypesOf(cards.contactCard),
        )
    }

    @Test
    fun invoke_withAnUnknownMimeType_keepsItAfterTheLeadingOnes() {
        val details = contactDetailsOf(
            generic(),
            phone(number = "4155551212"),
        )

        assertEquals(
            listOf(Phone.CONTENT_ITEM_TYPE, "vnd.example/thing"),
            mimeTypesOf(cardsOf(details).contactCard),
        )
    }

    @Test
    fun invoke_withinAMimeType_putsTheSuperPrimaryItemFirst() {
        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isSuperPrimary = true),
            phone(id = 3L, number = "4155553333", isPrimary = true),
        )
        val entries = firstCardGroupEntries(details)

        assertEquals(listOf(2L, 3L, 1L), entries.map(ContactEntry::id))
    }

    @Test
    fun invoke_withinAMimeType_keepsTheStoredOrderOfPlainItems() {
        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222"),
        )
        val entries = firstCardGroupEntries(details)

        assertEquals(listOf(1L, 2L), entries.map(ContactEntry::id))
    }

    @Test
    fun invoke_withinAMimeType_marksTheSuperPrimaryItemAsTheDefault() {
        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isSuperPrimary = true),
        )
        val entries = firstCardGroupEntries(details)

        assertEquals(listOf(2L), entries.filter(ContactEntry::isDefault).map(ContactEntry::id))
    }

    @Test
    fun invoke_withoutASuperPrimaryItem_marksThePrimaryItemAsTheDefault() {
        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222", isPrimary = true),
        )
        val entries = firstCardGroupEntries(details)

        assertEquals(listOf(2L), entries.filter(ContactEntry::isDefault).map(ContactEntry::id))
    }

    @Test
    fun invoke_withoutAnyPrimaryItem_marksNoDefault() {
        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551111"),
            phone(id = 2L, number = "4155552222"),
        )
        val entries = firstCardGroupEntries(details)

        assertTrue(entries.none(ContactEntry::isDefault))
    }

    @Test
    fun invoke_keepsTheNoteOutOfTheContactCard() {
        val details = contactDetailsOf(
            note(note = "Met at the airport"),
            event(formattedDate = "May 20, 1980"),
            website(url = "example.org"),
        )
        val cards = cardsOf(details)

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
        val details = contactDetailsOf(
            nickname(name = "Al"),
            organization(company = "Acme", department = "R&D", title = "Engineer"),
        )
        val cards = cardsOf(details)

        assertEquals(listOf("Al"), cards.headerNicknames)
        assertEquals(listOf(listOf("Engineer", "R&D", "Acme")), cards.headerOrganizations)
    }

    @Test
    fun invoke_forTheHeaderNicknameAndOrganization_buildsNoCardEntries() {
        val details = contactDetailsOf(
            nickname(name = "Al"),
            organization(formattedCompany = "Acme", company = "Acme"),
        )

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withSeveralNicknames_promotesThemAllInPriorityOrder() {
        val details = contactDetailsOf(
            nickname(id = 1L, name = "Al"),
            nickname(id = 2L, name = "Ally", isSuperPrimary = true),
        )
        val cards = cardsOf(details)

        assertEquals(listOf("Ally", "Al"), cards.headerNicknames)
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withSeveralOrganizations_promotesThemAllInPriorityOrder() {
        val details = contactDetailsOf(
            organization(id = 1L, formattedCompany = "Acme", company = "Acme"),
            organization(
                id = 2L,
                formattedCompany = "Globex",
                company = "Globex",
                isPrimary = true,
            ),
        )
        val cards = cardsOf(details)

        assertEquals(listOf(listOf("Globex"), listOf("Acme")), cards.headerOrganizations)
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withAnOrganizationWithoutACompany_promotesItsTitleToTheHeader() {
        val details = contactDetailsOf(
            organization(
                formattedCompany = null,
                company = null,
                title = "Test Data Custodian",
            ),
        )
        val cards = cardsOf(details)

        assertEquals(listOf(listOf("Test Data Custodian")), cards.headerOrganizations)
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withANicknameThatIsTheDisplayName_leavesTheHeaderNicknameEmpty() {
        val details = contactDetailsOf(nickname(name = "Al", rawContactId = 7L))
            .copy(nameRawContactId = 7L, displayNameSource = ContactDisplayNameSource.NICKNAME)

        assertTrue(cardsOf(details).headerNicknames.isEmpty())
    }

    @Test
    fun invoke_withABlankNickname_leavesTheHeaderNicknameEmpty() {
        val details = contactDetailsOf(nickname(name = " "))

        assertTrue(cardsOf(details).headerNicknames.isEmpty())
    }

    @Test
    fun invoke_withAPhoneticName_keepsItOutOfTheContactCard() {
        val details = contactDetailsOf(website(url = "example.org")).copy(phoneticName = "Alek")
        val cards = cardsOf(details)

        assertEquals(
            listOf(ContactEntryText.Value("example.org")),
            cards.contactCard.map { group -> group.entries.first().header },
        )
    }

    @Test
    fun invoke_withAPhoneticName_addsNoEntry() {
        val details = contactDetailsOf().copy(phoneticName = "Alek")

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withoutAPhoneticName_addsNoEntry() {
        val details = contactDetailsOf(website(url = "example.org"))
        val cards = cardsOf(details)

        assertEquals(1, cards.contactCard.size)
    }

    @Test
    fun invoke_withAStructuredName_buildsNoEntryForIt() {
        val details = contactDetailsOf(structuredName(displayName = "Alex"))
        val cards = cardsOf(details)

        assertTrue(cards.contactCard.isEmpty())
        assertTrue(cards.notes.isEmpty())
    }

    @Test
    fun invoke_withANicknameFromAnotherRawContact_promotesItToTheHeader() {
        val details = contactDetailsOf(nickname(name = "Al", rawContactId = 11L))
            .copy(nameRawContactId = 7L, displayNameSource = ContactDisplayNameSource.NICKNAME)

        assertEquals(
            listOf("Al"),
            cardsOf(details).headerNicknames,
        )
    }

    @Test
    fun invoke_withAPhoneWithoutANumber_dropsTheEntry() {
        val details = contactDetailsOf(phone(number = null, displayString = "4155551212"))

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAnEmailWithoutData_dropsTheEntry() {
        val details = contactDetailsOf(email(data = null, address = "alex@example.org"))

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAPostalAddressWithoutAnAddress_dropsTheEntry() {
        val details = contactDetailsOf(postal(formattedAddress = null))

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withASipAddressWithoutAnAddress_dropsTheEntry() {
        val details = contactDetailsOf(sipAddress(address = null))

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAGenericItemWithoutAnyText_dropsTheEntry() {
        val details = contactDetailsOf(generic(displayString = null, typeColumn = null))

        assertTrue(cardsOf(details).contactCard.isEmpty())
    }

    @Test
    fun invoke_withAGenericItemOwnedByAnotherApp_movesItToConnectedApps() {
        val details = contactDetailsOf(
            phone(number = "4155551212"),
            generic(id = 7L, mimeType = CHAT_MESSAGE, displayString = "Message 088 525 7470"),
        )
        every { connectedAppsRepository.getConnectedApp(7L, CHAT_MESSAGE) } returns CHAT_APP

        val cards = cardsOf(details)

        assertEquals(listOf(CHAT_APP), cards.connectedApps.map(ContactConnectedApp::app))
        assertEquals(listOf(7L), cards.connectedApps.single().entries.map(ContactEntry::id))
        assertEquals(listOf(Phone.CONTENT_ITEM_TYPE), mimeTypesOf(cards.contactCard))
    }

    @Test
    fun invoke_withSeveralMimeTypesOfOneApp_groupsThemUnderThatApp() {
        val details = contactDetailsOf(
            generic(id = 7L, mimeType = CHAT_MESSAGE, displayString = "Message 088 525 7470"),
            generic(id = 8L, mimeType = CHAT_CALL, displayString = "Voice call 088 525 7470"),
        )
        every { connectedAppsRepository.getConnectedApp(7L, CHAT_MESSAGE) } returns CHAT_APP
        every { connectedAppsRepository.getConnectedApp(8L, CHAT_CALL) } returns CHAT_APP

        val cards = cardsOf(details)

        assertEquals(listOf(7L, 8L), cards.connectedApps.single().entries.map(ContactEntry::id))
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withAGenericItemNoAppHandles_keepsItInTheContactCard() {
        val details = contactDetailsOf(generic(id = 7L))
        val cards = cardsOf(details)

        assertTrue(cards.connectedApps.isEmpty())
        assertEquals(listOf(7L), cards.contactCard.single().entries.map(ContactEntry::id))
    }

    @Test
    fun invoke_forAKnownKind_neverAsksForAConnectedApp() {
        val details = contactDetailsOf(phone(number = "4155551212"))
        val cards = cardsOf(details)

        assertTrue(cards.connectedApps.isEmpty())
        verify(exactly = 0) { connectedAppsRepository.getConnectedApp(any(), any()) }
    }

    @Test
    fun invoke_withAnEmptyNote_dropsTheEntry() {
        val details = contactDetailsOf(note(note = null))

        assertTrue(cardsOf(details).notes.isEmpty())
    }

    @Test
    fun invoke_forANote_putsTheNoteInTheHeader() {
        val details = contactDetailsOf(note(note = "Met at the airport"))
        val entry = firstNoteEntry(details)

        assertEquals(ContactEntryText.Value("Met at the airport"), entry.header)
        assertNull(entry.subHeader)
        assertEquals("Met at the airport", entry.copyText)
    }

    @Test
    fun invoke_forAPhone_buildsTheHeaderAndTypeFromTheDataItem() {
        val details = contactDetailsOf(
            phone(number = "4155551212", displayString = "(415) 555-1212", typeLabel = "Mobile"),
        )
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("(415) 555-1212"), entry.header)
        assertEquals("Mobile", entry.text)
        assertEquals("(415) 555-1212", entry.copyText)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.PHONE), entry.copyLabel)
    }

    @Test
    fun invoke_forAnEmail_labelsTheCopyActionAsEmail() {
        val details = contactDetailsOf(email(address = "alex@example.org", typeLabel = "Work"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("alex@example.org"), entry.header)
        assertEquals("Work", entry.text)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.EMAIL), entry.copyLabel)
    }

    @Test
    fun invoke_forASipAddress_labelsTheCopyActionAsPhone() {
        val details = contactDetailsOf(sipAddress(address = "alex@sip.example.org"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Label(ContactEntryLabel.PHONE), entry.copyLabel)
    }

    @Test
    fun invoke_forAnImProtocol_putsTheAddressInTheHeader() {
        val details = contactDetailsOf(im(protocolLabel = "AIM"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("alex@example.org"), entry.header)
        assertEquals(ContactEntryText.Value("AIM"), entry.subHeader)
        assertNull(entry.text)
    }

    @Test
    fun invoke_forAnImWithoutAProtocolLabel_fallsBackToTheImLabel() {
        val details = contactDetailsOf(im(protocolLabel = null))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("alex@example.org"), entry.header)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.IM), entry.subHeader)
    }

    @Test
    fun invoke_forACustomFieldWithoutASummary_labelsItAsACustomField() {
        val details = contactDetailsOf(custom(summary = null, content = "Blood type: 0"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("Blood type: 0"), entry.header)
        assertEquals(ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD), entry.subHeader)
    }

    @Test
    fun invoke_forACustomFieldWithASummary_usesTheSummaryAsTheSubHeader() {
        val details = contactDetailsOf(custom(summary = "Blood type", content = "0"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("0"), entry.header)
        assertEquals(ContactEntryText.Value("Blood type"), entry.subHeader)
    }

    @Test
    fun invoke_forAGenericItem_labelsTheCopyActionWithTheMimeType() {
        val details = contactDetailsOf(generic(typeColumn = "data2"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryText.Value("Thing"), entry.header)
        assertEquals("data2", entry.text)
        assertEquals(ContactEntryText.Value("vnd.example/thing"), entry.copyLabel)
    }

    @Test
    fun invoke_withoutDataItems_buildsEmptyCards() {
        val cards = cardsOf(contactDetailsOf())

        assertTrue(cards.contactCard.isEmpty())
        assertTrue(cards.notes.isEmpty())
        assertTrue(cards.headerNicknames.isEmpty())
        assertTrue(cards.headerOrganizations.isEmpty())
        assertTrue(cards.groups.isEmpty())
    }

    @Test
    fun invoke_forAPhone_offersCallingAndMessaging() {
        val details = contactDetailsOf(phone(number = "4155551212"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryAction.Call("4155551212"), entry.actions.primaryAction)
        assertEquals(ContactEntryAction.Sms("4155551212"), entry.actions.alternateAction)
        assertNull(entry.actions.enhancedCallAction)
    }

    @Test
    fun invoke_forAPhone_offersEditingTheNumberBeforeCalling() {
        val details = contactDetailsOf(phone(number = "4155551212"))
        val entry = firstCardEntry(details)

        assertEquals(
            ContactEntryAction.EditNumberBeforeCall("4155551212"),
            entry.actions.editBeforeCallAction,
        )
    }

    @Test
    fun invoke_forAnEmail_offersNoEditingBeforeCalling() {
        val details = contactDetailsOf(email(address = "alex@example.org"))
        val entry = firstCardEntry(details)

        assertNull(entry.actions.editBeforeCallAction)
    }

    @Test
    fun invoke_forAPhoneWithoutTelephony_offersMessagingOnly() {
        every { isDeviceVoiceCapable() } returns false

        val details = contactDetailsOf(phone(number = "4155551212"))
        val entry = firstCardEntry(details)

        assertNull(entry.actions.primaryAction)
        assertEquals(ContactEntryAction.Sms("4155551212"), entry.actions.alternateAction)
    }

    @Test
    fun invoke_whenCallingWithANoteIsSupported_offersItAsTheEnhancedCallAction() {
        every { isCallWithNoteSupported() } returns true
        every { canVideoCall(any()) } returns true

        val details = contactDetailsOf(
            phone(number = "4155551212", formattedNumber = "(415) 555-1212", typeLabel = "Mobile"),
        )
        val entry = firstCardEntry(details)

        assertEquals(
            ContactEntryAction.CallWithNote(
                number = "4155551212",
                formattedNumber = "(415) 555-1212",
                numberLabel = "Mobile",
            ),
            entry.actions.enhancedCallAction,
        )
    }

    @Test
    fun invoke_whenOnlyVideoCallingIsAvailable_offersItAsTheEnhancedCallAction() {
        every { canVideoCall(any()) } returns true

        val details = contactDetailsOf(phone(number = "4155551212"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryAction.VideoCall("4155551212"), entry.actions.enhancedCallAction)
    }

    @Test
    fun invoke_forAPhone_asksVideoCallingAboutTheCarrierCapability() {
        every { canVideoCall(true) } returns true
        every { canVideoCall(false) } returns false

        val details = contactDetailsOf(
            phone(id = 1L, number = "4155551111", isCarrierVideoCallCapable = true),
            phone(id = 2L, number = "4155552222", isCarrierVideoCallCapable = false),
        )
        val entries = firstCardGroupEntries(details)

        assertEquals(
            ContactEntryAction.VideoCall("4155551111"),
            entries.first().actions.enhancedCallAction,
        )
        assertNull(entries.last().actions.enhancedCallAction)
    }

    @Test
    fun invoke_forAnEmail_offersSendingAnEmail() {
        val details = contactDetailsOf(email(address = "alex@example.org"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryAction.SendEmail("alex@example.org"), entry.actions.primaryAction)
        assertNull(entry.actions.alternateAction)
    }

    @Test
    fun invoke_forAPostalAddress_offersTheMapAndDirections() {
        val details = contactDetailsOf(postal(formattedAddress = "1 Main St"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryAction.ShowOnMap("1 Main St"), entry.actions.primaryAction)
        assertEquals(ContactEntryAction.ShowDirections("1 Main St"), entry.actions.alternateAction)
    }

    @Test
    fun invoke_forASipAddress_offersASipCall() {
        val details = contactDetailsOf(sipAddress(address = "alex@sip.example.org"))
        val entry = firstCardEntry(details)

        assertEquals(
            ContactEntryAction.SipCall("alex@sip.example.org"),
            entry.actions.primaryAction,
        )
    }

    @Test
    fun invoke_withoutSipCalling_offersNoSipAction() {
        every { isSipCallingSupported() } returns false

        val details = contactDetailsOf(sipAddress(address = "alex@sip.example.org"))
        val entry = firstCardEntry(details)

        assertNull(entry.actions.primaryAction)
    }

    @Test
    fun invoke_forAWebsite_offersOpeningTheUrl() {
        val details = contactDetailsOf(website(url = "example.org"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryAction.OpenUrl("example.org"), entry.actions.primaryAction)
    }

    @Test
    fun invoke_forAnEvent_offersTheDateInTheCalendar() {
        val details = contactDetailsOf(event(formattedDate = "May 20", isRecurringAnnually = true))
        val entry = firstCardEntry(details)

        assertEquals(
            ContactEntryAction.ShowEventDate(date = "May 20", isRecurringAnnually = true),
            entry.actions.primaryAction,
        )
    }

    @Test
    fun invoke_forARelation_offersSearchingForTheName() {
        val details = contactDetailsOf(relation(name = "Sam"))
        val entry = firstCardEntry(details)

        assertEquals(ContactEntryAction.SearchContacts("Sam"), entry.actions.primaryAction)
    }

    @Test
    fun invoke_forAnIm_offersOpeningTheChat() {
        val details = contactDetailsOf(im(data = "alex@example.org", protocol = 5))
        val entry = firstCardEntry(details)

        assertEquals(
            ContactEntryAction.OpenChat(
                data = "alex@example.org",
                protocol = 5,
                customProtocol = null,
            ),
            entry.actions.primaryAction,
        )
    }

    @Test
    fun invoke_forAGenericItem_offersViewingTheDataRow() {
        val details = contactDetailsOf(generic(id = 42L, mimeType = "vnd.example/thing"))
        val entry = firstCardEntry(details)

        assertEquals(
            ContactEntryAction.ViewDataItem(dataId = 42L, mimeType = "vnd.example/thing"),
            entry.actions.primaryAction,
        )
    }

    @Test
    fun invoke_forANote_offersNoAction() {
        val details = contactDetailsOf(note(note = "Met at the airport"))
        val entry = firstNoteEntry(details)

        assertNull(entry.actions.primaryAction)
        assertNull(entry.actions.alternateAction)
        assertNull(entry.actions.enhancedCallAction)
    }

    private fun cardsOf(
        details: ContactDetails,
        prioritizedMimeType: String? = null,
    ): ContactDetailsCards {
        return buildContactDetailsCards(details, prioritizedMimeType)
    }

    private fun firstCardGroupEntries(details: ContactDetails): List<ContactEntry> {
        return cardsOf(details).contactCard.first().entries
    }

    private fun firstCardEntry(details: ContactDetails): ContactEntry {
        return firstCardGroupEntries(details).first()
    }

    private fun firstNoteEntry(details: ContactDetails): ContactEntry {
        return cardsOf(details).notes.first().entries.first()
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
