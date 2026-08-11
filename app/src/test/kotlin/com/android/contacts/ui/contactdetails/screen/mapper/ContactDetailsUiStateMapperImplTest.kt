package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.tests.factory.contactCapabilities
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactDetailsUiStateMapperImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val mapper = ContactDetailsUiStateMapperImpl(context = context)

    @Test
    fun map_withADisplayName_showsIt() {
        val state = mapOf(contactDetails(displayName = "Alex Doe"))

        assertEquals("Alex Doe", state.header.displayName)
    }

    @Test
    fun map_withoutADisplayName_fallsBackToTheMissingNameLabel() {
        val state = mapOf(contactDetails(displayName = null))

        assertEquals(context.getString(R.string.missing_name), state.header.displayName)
    }

    @Test
    fun map_withABlankDisplayName_fallsBackToTheMissingNameLabel() {
        val state = mapOf(contactDetails(displayName = "  "))

        assertEquals(context.getString(R.string.missing_name), state.header.displayName)
    }

    @Test
    fun map_withAPhoneticNameThatDiffers_showsIt() {
        val state = mapOf(contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou"))

        assertEquals("Alek Dou", state.header.phoneticName)
    }

    @Test
    fun map_withAPhoneticNameEqualToTheDisplayName_hidesIt() {
        val state = mapOf(contactDetails(displayName = "Alex Doe", phoneticName = "Alex Doe"))

        assertNull(state.header.phoneticName)
    }

    @Test
    fun map_forAnOrganizationName_marksTheHeaderAsABusiness() {
        val details = contactDetails(displayNameSource = ContactDisplayNameSource.ORGANIZATION)

        assertTrue(mapOf(details).header.isBusiness)
    }

    @Test
    fun map_withAPhoto_passesItAndTheAvatarSeed() {
        val photo = ContactPhoto.Uri("content://photo/7")
        val details = contactDetails(photo = photo, lookupKey = "lookup-key")

        val header = mapOf(details).header

        assertEquals(photo, header.photo)
        assertEquals("lookup-key", header.avatarSeed)
    }

    @Test
    fun map_withAnAboutCardName_buildsTheAboutCardTitle() {
        val state = mapOf(cards = cardsOf(aboutCardGivenName = "Alex"))

        assertEquals("${context.getString(R.string.about_card_title)} Alex", state.aboutCardTitle)
    }

    @Test
    fun map_withoutAnAboutCardName_usesThePlainAboutCardTitle() {
        val state = mapOf(cards = cardsOf(aboutCardGivenName = null))

        assertEquals(context.getString(R.string.about_card_title), state.aboutCardTitle)
    }

    @Test
    fun map_forALabelledHeader_resolvesTheLabel() {
        val entry = entry(header = ContactEntryText.Label(ContactEntryLabel.WEBSITE))
        val state = mapOf(cards = cardsOf(aboutCard = groupOf(entry)))

        assertEquals(
            context.getString(R.string.header_website_entry),
            firstAboutEntry(state).header,
        )
    }

    @Test
    fun map_forAValueHeader_keepsTheValue() {
        val entry = entry(header = ContactEntryText.Value("example.org"))
        val state = mapOf(cards = cardsOf(aboutCard = groupOf(entry)))

        assertEquals("example.org", firstAboutEntry(state).header)
    }

    @Test
    fun map_forALabelledCopyAction_resolvesTheLabel() {
        val entry = entry(
            header = ContactEntryText.Value("4155551212"),
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
        )
        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(
            context.getString(R.string.phoneLabelsGroup),
            firstContactEntry(state).copyLabel,
        )
    }

    @Test
    fun map_forAPhoneGroup_picksTheCallIcon() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(), Phone.CONTENT_ITEM_TYPE)),
        )

        assertEquals(ContactEntryIcon.CALL, firstContactEntry(state).icon)
    }

    @Test
    fun map_forAPostalGroup_picksThePlaceIcon() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(), StructuredPostal.CONTENT_ITEM_TYPE)),
        )

        assertEquals(ContactEntryIcon.PLACE, firstContactEntry(state).icon)
    }

    @Test
    fun map_forAnAboutCardGroup_picksNoIcon() {
        val state = mapOf(cards = cardsOf(aboutCard = groupOf(entry())))

        assertNull(firstAboutEntry(state).icon)
    }

    @Test
    fun map_forAMessagingAction_describesItWithTheNumber() {
        val entry = entry(
            actions = ContactEntryActions(alternate = ContactEntryAction.Sms("4155551212")),
        )
        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        val alternate = firstContactEntry(state).alternateAction

        assertEquals(ContactEntryIcon.MESSAGE, alternate?.icon)
        assertEquals(
            context.getString(R.string.sms_custom, "4155551212"),
            alternate?.contentDescription,
        )
    }

    @Test
    fun map_forADirectionsAction_picksTheDirectionsIcon() {
        val entry = entry(
            actions = ContactEntryActions(
                alternate = ContactEntryAction.ShowDirections("1 Main St"),
            ),
        )
        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.DIRECTIONS, firstContactEntry(state).alternateAction?.icon)
    }

    @Test
    fun map_forAVideoCallAction_picksTheVideoCallIcon() {
        val entry = entry(
            actions = ContactEntryActions(third = ContactEntryAction.VideoCall("4155551212")),
        )
        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.VIDEO_CALL, firstContactEntry(state).thirdAction?.icon)
    }

    @Test
    fun map_forACallWithNoteAction_picksTheNoteIcon() {
        val action = ContactEntryAction.CallWithNote("4155551212", null, null)
        val entry = entry(actions = ContactEntryActions(third = action))
        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.CALL_WITH_NOTE, firstContactEntry(state).thirdAction?.icon)
    }

    @Test
    fun map_forThePrimaryAction_passesItThrough() {
        val action = ContactEntryAction.Call("4155551212")
        val entry = entry(actions = ContactEntryActions(primary = action))
        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(action, firstContactEntry(state).action)
    }

    @Test
    fun map_withoutAnyEntries_offersAddingAPhoneNumberAndAnEmail() {
        val state = mapOf(contactDetails())

        assertEquals(
            listOf(
                context.getString(R.string.quickcontact_add_phone_number),
                context.getString(R.string.quickcontact_add_email),
            ),
            state.emptyPrompt?.entries?.map(ContactEntryUiModel::header),
        )
    }

    @Test
    fun map_withoutAnyEntriesOnASimContact_offersAddingAPhoneNumberOnly() {
        val details = contactDetails(
            capabilities = contactCapabilities(areAllRawContactsSimAccounts = true),
        )

        val state = mapOf(details)

        assertEquals(
            listOf(context.getString(R.string.quickcontact_add_phone_number)),
            state.emptyPrompt?.entries?.map(ContactEntryUiModel::header),
        )
    }

    @Test
    fun map_withEntries_offersNoPrompt() {
        val state = mapOf(cards = cardsOf(aboutCard = groupOf(entry())))

        assertNull(state.emptyPrompt)
    }

    @Test
    fun map_passesTheMenuAndTheStarredStateThrough() {
        val menu = contactDetailsMenu(isDeleteVisible = false)

        val state = mapOf(contactDetails(isStarred = true), menu = menu)

        assertEquals(menu, state.menu)
        assertTrue(state.isStarred)
    }

    private fun mapOf(
        details: ContactDetails = contactDetails(),
        cards: ContactDetailsCards = cardsOf(),
        menu: ContactDetailsMenu = contactDetailsMenu(),
    ): ContactDetailsContent.Loaded {
        return mapper.map(details, cards, menu) as ContactDetailsContent.Loaded
    }

    private fun cardsOf(
        contactCard: List<ContactEntryGroup> = emptyList(),
        aboutCard: List<ContactEntryGroup> = emptyList(),
        aboutCardGivenName: String? = null,
    ): ContactDetailsCards {
        return ContactDetailsCards(
            contactCard = contactCard,
            aboutCard = aboutCard,
            aboutCardGivenName = aboutCardGivenName,
        )
    }

    private fun groupOf(
        entry: ContactEntry,
        mimeType: String? = Email.CONTENT_ITEM_TYPE,
    ): List<ContactEntryGroup> {
        return listOf(ContactEntryGroup(mimeType = mimeType, entries = listOf(entry)))
    }

    private fun entry(
        header: ContactEntryText? = ContactEntryText.Value("value"),
        copyLabel: ContactEntryText? = null,
        actions: ContactEntryActions = ContactEntryActions(),
    ): ContactEntry {
        return ContactEntry(
            id = 1L,
            mimeType = null,
            isSuperPrimary = false,
            header = header,
            subHeader = null,
            text = null,
            copyText = null,
            copyLabel = copyLabel,
            actions = actions,
        )
    }

    private fun firstContactEntry(state: ContactDetailsContent.Loaded): ContactEntryUiModel {
        return state.contactCard.first().entries.first()
    }

    private fun firstAboutEntry(state: ContactDetailsContent.Loaded): ContactEntryUiModel {
        return state.aboutCard.first().entries.first()
    }
}
