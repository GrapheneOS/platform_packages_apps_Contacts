package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Organization
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactEntry
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.domain.contactdetails.usecase.IsEntryActionAvailable
import com.android.contacts.tests.factory.contactCapabilities
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactQuickActionUiModel
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactDetailsUiStateMapperImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val isEntryActionAvailable = mockk<IsEntryActionAvailable>()

    private val contactQuickActionsMapper = mockk<ContactQuickActionsMapper>()

    private val mapper = ContactDetailsUiStateMapperImpl(
        context = context,
        isEntryActionAvailable = isEntryActionAvailable,
        contactQuickActionsMapper = contactQuickActionsMapper,
    )

    @Before
    fun setUp() {
        every { isEntryActionAvailable(any()) } returns true
        every { contactQuickActionsMapper.map(any()) } returns QUICK_ACTIONS
    }

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
    fun map_withTheFamilyNameFirstDisplayOrder_showsTheAlternativeName() {
        val details = contactDetails(
            displayName = "Alex Doe",
            alternativeDisplayName = "Doe, Alex",
        )

        val state = mapOf(details, displayOrder = DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("Doe, Alex", state.header.displayName)
    }

    @Test
    fun map_withTheFamilyNameFirstOrderAndNoAlternativeName_fallsBackToMissingName() {
        val details = contactDetails(
            displayName = "Alex Doe",
            alternativeDisplayName = null,
        )

        val state = mapOf(details, displayOrder = DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals(context.getString(R.string.missing_name), state.header.displayName)
    }

    @Test
    fun map_forAPhoneNumberName_marksTheDisplayNameAsLeftToRight() {
        val details = contactDetails(displayNameSource = ContactDisplayNameSource.PHONE)

        assertTrue(mapOf(details).header.isDisplayNameLtr)
    }

    @Test
    fun map_forAStructuredName_leavesTheDisplayNameDirectionAlone() {
        val details = contactDetails(displayNameSource = ContactDisplayNameSource.STRUCTURED_NAME)

        assertFalse(mapOf(details).header.isDisplayNameLtr)
    }

    @Test
    fun map_withAPhoneticNameThatDiffers_showsItQuoted() {
        val state = mapOf(contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou"))

        assertEquals(listOf("“Alek Dou”"), state.header.subtitles)
    }

    @Test
    fun map_withAPhoneticNameEqualToTheDisplayName_hidesIt() {
        val state = mapOf(contactDetails(displayName = "Alex Doe", phoneticName = "Alex Doe"))

        assertEquals(emptyList<String>(), state.header.subtitles)
    }

    @Test
    fun map_joinsTheNicknameAndTheQuotedPhoneticNameIntoTheFirstSubtitle() {
        val details = contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou")
        val cards = cardsOf(
            headerNickname = "Al",
            headerOrganizationParts = listOf("Engineer", "R&D", "Acme"),
        )

        val header = mapOf(details, cards).header

        assertEquals(listOf("Al • “Alek Dou”", "Engineer • R&D • Acme"), header.subtitles)
    }

    @Test
    fun map_withoutANickname_stillQuotesThePhoneticName() {
        val details = contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou")

        assertEquals(listOf("“Alek Dou”"), mapOf(details).header.subtitles)
    }

    @Test
    fun map_withoutAnySubtitleSources_leavesTheSubtitlesEmpty() {
        val header = mapOf(cards = cardsOf()).header

        assertEquals(emptyList<String>(), header.subtitles)
    }

    @Test
    fun map_passesTheQuickActionsThrough() {
        val state = mapOf(contactDetails())

        assertEquals(QUICK_ACTIONS, state.quickActions)
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

        assertEquals(ContactAvatarImage.Uri("content://photo/7"), header.photo)
        assertEquals("lookup-key", header.avatarSeed)
    }

    @Test
    fun map_forALabelledHeader_resolvesTheLabel() {
        val entry = entry(header = ContactEntryText.Label(ContactEntryLabel.WEBSITE))
        val state = mapOf(cards = cardsOf(notes = groupOf(entry)))

        assertEquals(
            context.getString(R.string.header_website_entry),
            firstNoteEntry(state).header,
        )
    }

    @Test
    fun map_forAValueHeader_keepsTheValue() {
        val entry = entry(header = ContactEntryText.Value("example.org"))
        val state = mapOf(cards = cardsOf(notes = groupOf(entry)))

        assertEquals("example.org", firstNoteEntry(state).header)
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
    fun map_forAPhoneEntry_picksTheCallIcon() {
        val state = mapOf(
            cards = cardsOf(
                contactCard = groupOf(
                    entry(kind = ContactEntryKind.PHONE),
                    Phone.CONTENT_ITEM_TYPE,
                ),
            ),
        )

        assertEquals(ContactEntryIcon.CALL, firstContactEntry(state).icon)
    }

    @Test
    fun map_forAPostalEntry_picksThePlaceIcon() {
        val state = mapOf(
            cards = cardsOf(
                contactCard = groupOf(
                    entry(kind = ContactEntryKind.POSTAL),
                    StructuredPostal.CONTENT_ITEM_TYPE,
                ),
            ),
        )

        assertEquals(ContactEntryIcon.PLACE, firstContactEntry(state).icon)
    }

    @Test
    fun map_forAnEmailEntry_picksTheEmailIcon() {
        val state = mapOf(
            cards = cardsOf(
                contactCard = groupOf(
                    entry(kind = ContactEntryKind.EMAIL),
                    Email.CONTENT_ITEM_TYPE,
                ),
            ),
        )

        assertEquals(ContactEntryIcon.EMAIL, firstContactEntry(state).icon)
    }

    @Test
    fun map_forASipEntry_picksTheSipIcon() {
        val state = mapOf(
            cards = cardsOf(
                contactCard = groupOf(
                    entry(kind = ContactEntryKind.SIP_ADDRESS),
                    SipAddress.CONTENT_ITEM_TYPE,
                ),
            ),
        )

        assertEquals(ContactEntryIcon.SIP_CALL, firstContactEntry(state).icon)
    }

    @Test
    fun map_forASipGroup_marksTheHeaderAsLeftToRight() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(), SipAddress.CONTENT_ITEM_TYPE)),
        )

        assertTrue(firstContactEntry(state).isHeaderLtr)
    }

    @Test
    fun map_forAnActionWithoutAnIcon_buildsNoAlternateAction() {
        val entry = entry(
            actions = ContactEntryActions(alternate = ContactEntryAction.Call("4155551212")),
        )

        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertNull(firstContactEntry(state).alternateAction)
    }

    @Test
    fun map_forABirthdayEntry_picksTheCakeIcon() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(kind = ContactEntryKind.BIRTHDAY))),
        )

        assertEquals(ContactEntryIcon.BIRTHDAY, firstContactEntry(state).icon)
    }

    @Test
    fun map_forACustomFieldEntry_picksNoIcon() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(kind = ContactEntryKind.CUSTOM_FIELD))),
        )

        assertNull(firstContactEntry(state).icon)
    }

    @Test
    fun map_forAnUnknownEntry_picksNoIcon() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(kind = ContactEntryKind.OTHER))),
        )

        assertNull(firstContactEntry(state).icon)
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
    fun map_forAPhoneGroup_marksTheHeaderAsLeftToRight() {
        val state = mapOf(
            cards = cardsOf(contactCard = groupOf(entry(), Phone.CONTENT_ITEM_TYPE)),
        )

        assertTrue(firstContactEntry(state).isHeaderLtr)
    }

    @Test
    fun map_forAnAboutCardGroup_leavesTheHeaderDirectionAlone() {
        val state = mapOf(cards = cardsOf(notes = groupOf(entry())))

        assertFalse(firstNoteEntry(state).isHeaderLtr)
    }

    @Test
    fun map_whenNothingResolvesThePrimaryAction_dropsIt() {
        val action = ContactEntryAction.Call("4155551212")
        every { isEntryActionAvailable(action) } returns false
        val entry = entry(actions = ContactEntryActions(primary = action))

        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertNull(firstContactEntry(state).action)
    }

    @Test
    fun map_whenNothingResolvesTheAlternateAction_dropsIt() {
        val action = ContactEntryAction.Sms("4155551212")
        every { isEntryActionAvailable(action) } returns false
        val entry = entry(actions = ContactEntryActions(alternate = action))

        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertNull(firstContactEntry(state).alternateAction)
    }

    @Test
    fun map_whenNothingResolvesTheThirdAction_keepsIt() {
        val action = ContactEntryAction.VideoCall("4155551212")
        every { isEntryActionAvailable(action) } returns false
        val entry = entry(actions = ContactEntryActions(third = action))

        val state = mapOf(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.VIDEO_CALL, firstContactEntry(state).thirdAction?.icon)
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
        val state = mapOf(cards = cardsOf(notes = groupOf(entry())))

        assertNull(state.emptyPrompt)
    }

    @Test
    fun map_withTheOnlyPhoneNumber_doesNotOfferChangingTheDefault() {
        val cards = cardsOf(contactCard = groupOf(entry(), mimeType = Phone.CONTENT_ITEM_TYPE))

        val state = mapOf(cards = cards)

        assertFalse(firstContactEntry(state).isDefaultChangeable)
    }

    @Test
    fun map_withSeveralPhoneNumbers_offersChangingTheDefault() {
        val cards = cardsOf(
            contactCard = listOf(
                ContactEntryGroup(
                    mimeType = Phone.CONTENT_ITEM_TYPE,
                    entries = listOf(entry(), entry()),
                ),
            ),
        )

        val state = mapOf(cards = cards)

        assertTrue(firstContactEntry(state).isDefaultChangeable)
    }

    @Test
    fun map_forASuperPrimaryEntry_offersChangingTheDefault() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(isSuperPrimary = true),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapOf(cards = cards)

        assertTrue(firstContactEntry(state).isDefaultChangeable)
    }

    @Test
    fun map_forADirectoryContact_doesNotOfferChangingTheDefault() {
        val details = contactDetails(
            capabilities = contactCapabilities(isDirectoryEntry = true),
        )
        val cards = cardsOf(
            contactCard = groupOf(
                entry(isSuperPrimary = true),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapOf(details, cards)

        assertFalse(firstContactEntry(state).isDefaultChangeable)
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
        displayOrder: DisplayOrder = DisplayOrder.GIVEN_NAME_FIRST,
    ): ContactDetailsContent.Loaded {
        return mapper.map(details, cards, emptyList(), menu, displayOrder)
            as ContactDetailsContent.Loaded
    }

    private fun cardsOf(
        contactCard: List<ContactEntryGroup> = emptyList(),
        notes: List<ContactEntryGroup> = emptyList(),
        headerNickname: String? = null,
        headerOrganizationParts: List<String> = emptyList(),
        groups: List<String> = emptyList(),
    ): ContactDetailsCards {
        return ContactDetailsCards(
            contactCard = contactCard,
            notes = notes,
            headerNickname = headerNickname,
            headerOrganizationParts = headerOrganizationParts,
            groups = groups,
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
        isSuperPrimary: Boolean = false,
        kind: ContactEntryKind = ContactEntryKind.OTHER,
    ): ContactEntry {
        return ContactEntry(
            id = 1L,
            mimeType = null,
            kind = kind,
            isSuperPrimary = isSuperPrimary,
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

    private fun firstNoteEntry(state: ContactDetailsContent.Loaded): ContactEntryUiModel {
        return state.notes.first().entries.first()
    }

    private companion object {
        val QUICK_ACTIONS = persistentListOf(contactQuickActionUiModel())
    }
}
