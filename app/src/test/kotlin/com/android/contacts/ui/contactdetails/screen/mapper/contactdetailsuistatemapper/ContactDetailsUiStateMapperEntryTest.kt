package com.android.contacts.ui.contactdetails.screen.mapper.contactdetailsuistatemapper

import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.SipAddress
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.R
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.domain.contactdetails.model.ContactEntryLabel
import com.android.contacts.domain.contactdetails.model.ContactEntryText
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ContactDetailsUiStateMapperEntryTest : BaseContactDetailsUiStateMapperTest() {

    @Test
    fun map_forALabelledHeader_resolvesTheLabel() {
        val entry = entry(header = ContactEntryText.Label(ContactEntryLabel.WEBSITE))
        val state = mapState(cards = cardsOf(notes = groupOf(entry)))

        assertEquals(
            context.getString(R.string.header_website_entry),
            firstNoteEntry(state).header,
        )
    }

    @Test
    fun map_forAValueHeader_keepsTheValue() {
        val entry = entry(header = ContactEntryText.Value("example.org"))
        val state = mapState(cards = cardsOf(notes = groupOf(entry)))

        assertEquals("example.org", firstNoteEntry(state).header)
    }

    @Test
    fun map_forALabelledCopyAction_resolvesTheLabel() {
        val entry = entry(
            header = ContactEntryText.Value("4155551212"),
            copyLabel = ContactEntryText.Label(ContactEntryLabel.PHONE),
        )
        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(
            context.getString(R.string.phoneLabelsGroup),
            firstContactEntry(state).copyLabel,
        )
    }

    @Test
    fun map_forAPhoneEntry_picksTheCallIcon() {
        val state = mapState(
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
        val state = mapState(
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
        val state = mapState(
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
        val state = mapState(
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
        val state = mapState(
            cards = cardsOf(contactCard = groupOf(entry(), SipAddress.CONTENT_ITEM_TYPE)),
        )

        assertTrue(firstContactEntry(state).isHeaderLtr)
    }

    @Test
    fun map_forAnActionWithoutAnIcon_buildsNoAlternateAction() {
        val entry = entry(
            actions = ContactEntryActions(alternateAction = ContactEntryAction.Call("4155551212")),
        )

        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertNull(firstContactEntry(state).alternateAction)
    }

    @Test
    fun map_forABirthdayEntry_picksTheCakeIcon() {
        val state = mapState(
            cards = cardsOf(contactCard = groupOf(entry(kind = ContactEntryKind.BIRTHDAY))),
        )

        assertEquals(ContactEntryIcon.BIRTHDAY, firstContactEntry(state).icon)
    }

    @Test
    fun map_forACustomFieldEntry_picksNoIcon() {
        val state = mapState(
            cards = cardsOf(contactCard = groupOf(entry(kind = ContactEntryKind.CUSTOM_FIELD))),
        )

        assertNull(firstContactEntry(state).icon)
    }

    @Test
    fun map_forAnUnknownEntry_picksNoIcon() {
        val state = mapState(
            cards = cardsOf(contactCard = groupOf(entry(kind = ContactEntryKind.OTHER))),
        )

        assertNull(firstContactEntry(state).icon)
    }

    @Test
    fun map_forAMessagingAction_describesItWithTheNumber() {
        val entry = entry(
            actions = ContactEntryActions(alternateAction = ContactEntryAction.Sms("4155551212")),
        )
        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

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
                alternateAction = ContactEntryAction.ShowDirections("1 Main St"),
            ),
        )
        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.DIRECTIONS, firstContactEntry(state).alternateAction?.icon)
    }

    @Test
    fun map_forAVideoCallAction_picksTheVideoCallIcon() {
        val entry = entry(
            actions = ContactEntryActions(
                enhancedCallAction = ContactEntryAction.VideoCall("4155551212"),
            ),
        )
        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.VIDEO_CALL, firstContactEntry(state).enhancedCallAction?.icon)
    }

    @Test
    fun map_forACallWithNoteAction_picksTheNoteIcon() {
        val action = ContactEntryAction.CallWithNote(
            number = "4155551212",
            formattedNumber = null,
            numberLabel = null,
        )
        val entry = entry(actions = ContactEntryActions(enhancedCallAction = action))
        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(
            ContactEntryIcon.CALL_WITH_NOTE,
            firstContactEntry(state).enhancedCallAction?.icon,
        )
    }

    @Test
    fun map_forThePrimaryAction_passesItThrough() {
        val action = ContactEntryAction.Call("4155551212")
        val entry = entry(actions = ContactEntryActions(primaryAction = action))
        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(action, firstContactEntry(state).action)
    }

    @Test
    fun map_forAPhoneGroup_marksTheHeaderAsLeftToRight() {
        val state = mapState(
            cards = cardsOf(contactCard = groupOf(entry(), Phone.CONTENT_ITEM_TYPE)),
        )

        assertTrue(firstContactEntry(state).isHeaderLtr)
    }

    @Test
    fun map_forAnAboutCardGroup_leavesTheHeaderDirectionAlone() {
        val state = mapState(cards = cardsOf(notes = groupOf(entry())))

        assertFalse(firstNoteEntry(state).isHeaderLtr)
    }

    @Test
    fun map_whenNothingResolvesThePrimaryAction_dropsIt() {
        val action = ContactEntryAction.Call("4155551212")
        every { isEntryActionAvailable(action) } returns false
        val entry = entry(actions = ContactEntryActions(primaryAction = action))

        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertNull(firstContactEntry(state).action)
    }

    @Test
    fun map_whenNothingResolvesTheAlternateAction_dropsIt() {
        val action = ContactEntryAction.Sms("4155551212")
        every { isEntryActionAvailable(action) } returns false
        val entry = entry(actions = ContactEntryActions(alternateAction = action))

        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertNull(firstContactEntry(state).alternateAction)
    }

    @Test
    fun map_whenNothingResolvesTheEnhancedCallAction_keepsIt() {
        val action = ContactEntryAction.VideoCall("4155551212")
        every { isEntryActionAvailable(action) } returns false
        val entry = entry(actions = ContactEntryActions(enhancedCallAction = action))

        val state = mapState(cards = cardsOf(contactCard = groupOf(entry, Phone.CONTENT_ITEM_TYPE)))

        assertEquals(ContactEntryIcon.VIDEO_CALL, firstContactEntry(state).enhancedCallAction?.icon)
    }
}
