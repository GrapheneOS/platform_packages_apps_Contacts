package com.android.contacts.ui.contactdetails.screen.mapper.contactdetailsuistatemapper

import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.domain.contactdetails.model.ContactEntryActions
import com.android.contacts.domain.contactdetails.model.ContactEntryGroup
import com.android.contacts.domain.contactdetails.model.ContactEntryKind
import com.android.contacts.tests.factory.contactCapabilities
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactGroupUiModel
import io.mockk.every
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ContactDetailsUiStateMapperSectionsTest : BaseContactDetailsUiStateMapperTest() {

    @Test
    fun map_withoutAnyEntries_offersAddingAPhoneNumberAndAnEmail() {
        val state = mapState(contactDetails())

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

        val state = mapState(details)

        assertEquals(
            listOf(context.getString(R.string.quickcontact_add_phone_number)),
            state.emptyPrompt?.entries?.map(ContactEntryUiModel::header),
        )
    }

    @Test
    fun map_withEntries_offersNoPrompt() {
        val state = mapState(cards = cardsOf(notes = groupOf(entry())))

        assertNull(state.emptyPrompt)
    }

    @Test
    fun map_forTheDefaultPhone_marksTheTypeAsDefault() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(isDefault = true, text = "Mobile", kind = ContactEntryKind.PHONE),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertEquals("Mobile · Default", firstContactEntry(state).text)
    }

    @Test
    fun map_forTheDefaultPostalAddress_marksTheTypeAsDefault() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(isDefault = true, text = "Work", kind = ContactEntryKind.POSTAL),
                mimeType = StructuredPostal.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertEquals("Work · Default", firstContactEntry(state).text)
    }

    @Test
    fun map_forAnEntryThatIsNotTheDefault_keepsTheTypeAlone() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(text = "Mobile", kind = ContactEntryKind.PHONE),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertEquals("Mobile", firstContactEntry(state).text)
    }

    @Test
    fun map_forAPhoneWithSeveralSims_offersChangingTheCallingSim() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(kind = ContactEntryKind.PHONE),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards, callingSimOptions = CALLING_SIM_OPTIONS)

        assertTrue(firstContactEntry(state).isCallingSimChangeable)
    }

    @Test
    fun map_forAnEmailWithSeveralSims_doesNotOfferChangingTheCallingSim() {
        val cards = cardsOf(
            contactCard = groupOf(entry(kind = ContactEntryKind.EMAIL)),
        )

        val state = mapState(cards = cards, callingSimOptions = CALLING_SIM_OPTIONS)

        assertFalse(firstContactEntry(state).isCallingSimChangeable)
    }

    @Test
    fun map_forAPhoneWithASingleSim_doesNotOfferChangingTheCallingSim() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(kind = ContactEntryKind.PHONE),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertFalse(firstContactEntry(state).isCallingSimChangeable)
    }

    @Test
    fun map_forARegisteredEditBeforeCallAction_keepsIt() {
        val action = ContactEntryAction.EditNumberBeforeCall(number = "555 0001")
        val cards = cardsOf(
            contactCard = groupOf(
                entry(
                    kind = ContactEntryKind.PHONE,
                    actions = ContactEntryActions(editBeforeCallAction = action),
                ),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertEquals(action, firstContactEntry(state).editBeforeCallAction)
    }

    @Test
    fun map_forAnUnregisteredEditBeforeCallAction_dropsIt() {
        every { isEntryActionAvailable(any()) } returns false
        val cards = cardsOf(
            contactCard = groupOf(
                entry(
                    kind = ContactEntryKind.PHONE,
                    actions = ContactEntryActions(
                        editBeforeCallAction = ContactEntryAction.EditNumberBeforeCall("555 0001"),
                    ),
                ),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertNull(firstContactEntry(state).editBeforeCallAction)
    }

    @Test
    fun map_withTheOnlyPhoneNumber_doesNotOfferChangingTheDefault() {
        val cards = cardsOf(contactCard = groupOf(entry(), mimeType = Phone.CONTENT_ITEM_TYPE))

        val state = mapState(cards = cards)

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

        val state = mapState(cards = cards)

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

        val state = mapState(cards = cards)

        assertTrue(firstContactEntry(state).isDefaultChangeable)
    }

    @Test
    fun map_forTheOnlyPrimaryEntry_offersClearingTheDefault() {
        val cards = cardsOf(
            contactCard = groupOf(
                entry(isSuperPrimary = false, isDefault = true),
                mimeType = Phone.CONTENT_ITEM_TYPE,
            ),
        )

        val state = mapState(cards = cards)

        assertTrue(firstContactEntry(state).isDefaultChangeable)
    }

    @Test
    fun map_withSeveralPostalAddresses_offersChangingTheDefault() {
        val cards = cardsOf(
            contactCard = listOf(
                ContactEntryGroup(
                    mimeType = StructuredPostal.CONTENT_ITEM_TYPE,
                    entries = listOf(entry(), entry()),
                ),
            ),
        )

        val state = mapState(cards = cards)

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

        val state = mapState(details, cards)

        assertFalse(firstContactEntry(state).isDefaultChangeable)
    }

    @Test
    fun map_forAContactSentToVoicemail_checksTheToggle() {
        val details = contactDetails(isSendToVoicemail = true)

        assertEquals(true, sendToVoicemailSetting(mapState(details))?.isChecked)
    }

    @Test
    fun map_forAContactNotSentToVoicemail_leavesTheToggleUnchecked() {
        val details = contactDetails(isSendToVoicemail = false)

        assertEquals(false, sendToVoicemailSetting(mapState(details))?.isChecked)
    }

    @Test
    fun map_whenSendToVoicemailIsHidden_dropsTheRow() {
        val menu = contactDetailsMenu(isSendToVoicemailVisible = false)

        assertNull(sendToVoicemailSetting(mapState(menu = menu)))
    }

    @Test
    fun map_passesTheMenuAndTheStarredStateThrough() {
        val menu = contactDetailsMenu(isDeleteVisible = false)

        val state = mapState(contactDetails(isStarred = true), menu = menu)

        assertEquals(menu, state.menu)
        assertTrue(state.isStarred)
    }

    @Test
    fun map_withGroups_keepsTheirIdsAndTitles() {
        val cards = cardsOf(
            groups = listOf(
                ContactGroup(id = 11L, title = "Coworkers"),
                ContactGroup(id = 22L, title = "Family"),
            ),
        )

        val state = mapState(cards = cards)

        assertEquals(listOf(11L, 22L), state.groups.map(ContactGroupUiModel::id))
        assertEquals(listOf("Coworkers", "Family"), state.groups.map(ContactGroupUiModel::title))
    }
}
