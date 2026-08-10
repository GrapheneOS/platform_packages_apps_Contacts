package com.android.contacts.domain.contactdetails.usecase

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.custom
import com.android.contacts.tests.factory.email
import com.android.contacts.tests.factory.generic
import com.android.contacts.tests.factory.im
import com.android.contacts.tests.factory.nickname
import com.android.contacts.tests.factory.note
import com.android.contacts.tests.factory.organization
import com.android.contacts.tests.factory.phone
import com.android.contacts.tests.factory.postal
import com.android.contacts.tests.factory.sipAddress
import com.android.contacts.tests.factory.structuredName
import com.android.contacts.tests.factory.website
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BuildContactDetailsCardsImplTest {

    private val buildContactDetailsCards = BuildContactDetailsCardsImpl()

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
                SipAddress.CONTENT_ITEM_TYPE,
                Email.CONTENT_ITEM_TYPE,
                StructuredPostal.CONTENT_ITEM_TYPE,
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
    fun invoke_ordersTheAboutCardByItsOwnMimeTypeOrder() {
        val details = detailsOf(
            note(note = "Met at the airport"),
            organization(formattedCompany = "Acme"),
            nickname(name = "Al"),
            website(url = "example.org"),
        )

        val cards = buildContactDetailsCards(details, null)

        assertEquals(
            listOf(
                ContactEntryText.Label(ContactEntryLabel.NICKNAME),
                ContactEntryText.Label(ContactEntryLabel.WEBSITE),
                ContactEntryText.Label(ContactEntryLabel.ORGANIZATION),
                ContactEntryText.Label(ContactEntryLabel.NOTE),
            ),
            cards.aboutCard.map { group -> group.entries.first().header },
        )
        assertTrue(cards.contactCard.isEmpty())
    }

    @Test
    fun invoke_withAPhoneticName_putsItAfterTheNickname() {
        val details = detailsOf(nickname(name = "Al"), website(url = "example.org"))
            .copy(phoneticName = "Alek")

        val cards = buildContactDetailsCards(details, null)

        assertEquals(
            listOf(
                ContactEntryText.Label(ContactEntryLabel.NICKNAME),
                ContactEntryText.Label(ContactEntryLabel.PHONETIC_NAME),
                ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            ),
            cards.aboutCard.map { group -> group.entries.first().header },
        )
    }

    @Test
    fun invoke_withAPhoneticNameAndNoNickname_putsItFirst() {
        val details = detailsOf(website(url = "example.org")).copy(phoneticName = "Alek")

        val cards = buildContactDetailsCards(details, null)

        assertEquals(
            listOf(
                ContactEntryText.Label(ContactEntryLabel.PHONETIC_NAME),
                ContactEntryText.Label(ContactEntryLabel.WEBSITE),
            ),
            cards.aboutCard.map { group -> group.entries.first().header },
        )
    }

    @Test
    fun invoke_withAPhoneticName_copiesTheNameFromTheEntry() {
        val details = detailsOf().copy(phoneticName = "Alek")

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals("Alek", entry.subHeader)
        assertEquals("Alek", entry.copyText)
        assertNull(entry.mimeType)
    }

    @Test
    fun invoke_withoutAPhoneticName_addsNoEntry() {
        val details = detailsOf(website(url = "example.org"))

        val cards = buildContactDetailsCards(details, null)

        assertEquals(1, cards.aboutCard.size)
    }

    @Test
    fun invoke_withAStructuredName_reportsTheGivenNameForTheAboutCardTitle() {
        val details = detailsOf(structuredName(givenName = "Alex"))

        assertEquals("Alex", buildContactDetailsCards(details, null).aboutCardGivenName)
    }

    @Test
    fun invoke_withASuperPrimaryStructuredName_prefersIt() {
        val details = detailsOf(
            structuredName(id = 1L, givenName = "Alex"),
            structuredName(id = 2L, givenName = "Alexander", isSuperPrimary = true),
        )

        assertEquals("Alexander", buildContactDetailsCards(details, null).aboutCardGivenName)
    }

    @Test
    fun invoke_withAStructuredName_buildsNoEntryForIt() {
        val details = detailsOf(structuredName(givenName = "Alex"))

        val cards = buildContactDetailsCards(details, null)

        assertTrue(cards.contactCard.isEmpty())
        assertTrue(cards.aboutCard.isEmpty())
    }

    @Test
    fun invoke_withoutAStructuredName_reportsNoAboutCardName() {
        val details = detailsOf(phone(number = "4155551212"))

        assertNull(buildContactDetailsCards(details, null).aboutCardGivenName)
    }

    @Test
    fun invoke_withANicknameThatIsTheDisplayName_dropsTheEntry() {
        val details = detailsOf(nickname(name = "Al", rawContactId = 7L))
            .copy(nameRawContactId = 7L, displayNameSource = ContactDisplayNameSource.NICKNAME)

        assertTrue(buildContactDetailsCards(details, null).aboutCard.isEmpty())
    }

    @Test
    fun invoke_withANicknameFromAnotherRawContact_keepsTheEntry() {
        val details = detailsOf(nickname(name = "Al", rawContactId = 11L))
            .copy(nameRawContactId = 7L, displayNameSource = ContactDisplayNameSource.NICKNAME)

        assertEquals(1, buildContactDetailsCards(details, null).aboutCard.size)
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
    fun invoke_withAnEmptyNote_keepsTheLabelledEntry() {
        val details = detailsOf(note(note = null))

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals(ContactEntryText.Label(ContactEntryLabel.NOTE), entry.header)
        assertNull(entry.subHeader)
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
    fun invoke_forAnOrganization_copiesTheFormattedCompany() {
        val details = detailsOf(organization(formattedCompany = "Acme, Engineer"))

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals(ContactEntryText.Label(ContactEntryLabel.ORGANIZATION), entry.header)
        assertEquals("Acme, Engineer", entry.text)
        assertEquals("Acme, Engineer", entry.copyText)
    }

    @Test
    fun invoke_forAKnownImProtocol_putsTheProtocolInTheHeader() {
        val details = detailsOf(
            im(protocolLabel = "AIM", isCustomProtocol = false),
        )

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals(ContactEntryText.Value("AIM"), entry.header)
        assertEquals("alex@example.org", entry.subHeader)
        assertNull(entry.text)
    }

    @Test
    fun invoke_forACustomImProtocol_putsTheProtocolInTheSubHeader() {
        val details = detailsOf(
            im(protocolLabel = "Matrix", isCustomProtocol = true),
        )

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals(ContactEntryText.Label(ContactEntryLabel.IM), entry.header)
        assertEquals("Matrix", entry.subHeader)
        assertEquals("alex@example.org", entry.text)
    }

    @Test
    fun invoke_forACustomFieldWithoutASummary_labelsItAsACustomField() {
        val details = detailsOf(custom(summary = null, content = "Blood type: 0"))

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals(ContactEntryText.Label(ContactEntryLabel.CUSTOM_FIELD), entry.header)
        assertEquals("Blood type: 0", entry.subHeader)
    }

    @Test
    fun invoke_forACustomFieldWithASummary_usesTheSummaryAsTheHeader() {
        val details = detailsOf(custom(summary = "Blood type", content = "0"))

        val entry = buildContactDetailsCards(details, null).aboutCard.first().entries.first()

        assertEquals(ContactEntryText.Value("Blood type"), entry.header)
        assertEquals(ContactEntryText.Value("Blood type"), entry.copyLabel)
    }

    @Test
    fun invoke_forAGenericItem_labelsTheCopyActionWithTheMimeType() {
        val details = detailsOf(
            generic(typeColumn = "data2"),
        )

        val entry = buildContactDetailsCards(details, null).contactCard.first().entries.first()

        assertEquals(ContactEntryText.Value("Thing"), entry.header)
        assertEquals("data2", entry.text)
        assertEquals(ContactEntryText.Value("vnd.example/thing"), entry.copyLabel)
    }

    @Test
    fun invoke_withoutDataItems_buildsEmptyCards() {
        val cards = buildContactDetailsCards(detailsOf(), null)

        assertTrue(cards.contactCard.isEmpty())
        assertTrue(cards.aboutCard.isEmpty())
        assertNull(cards.aboutCardGivenName)
    }

    private fun detailsOf(vararg dataItems: ContactDataItem): ContactDetails {
        return contactDetails(dataItems = dataItems.toList())
    }

    private fun mimeTypesOf(card: List<ContactEntryGroup>): List<String?> {
        return card.map { group -> group.mimeType }
    }

}
