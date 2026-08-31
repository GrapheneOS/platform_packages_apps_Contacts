package com.android.contacts.ui.contactdetails.screen.mapper.contactdetailsuistatemapper

import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactDisplayNameSource
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.ui.common.components.ContactAvatarImage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class ContactDetailsUiStateMapperHeaderTest : BaseContactDetailsUiStateMapperTest() {

    @Test
    fun map_withADisplayName_showsIt() {
        val state = mapState(contactDetails(displayName = "Alex Doe"))

        assertEquals("Alex Doe", state.header.displayName)
    }

    @Test
    fun map_withoutADisplayName_fallsBackToTheMissingNameLabel() {
        val state = mapState(contactDetails(displayName = null))

        assertEquals(context.getString(R.string.missing_name), state.header.displayName)
    }

    @Test
    fun map_withABlankDisplayName_fallsBackToTheMissingNameLabel() {
        val state = mapState(contactDetails(displayName = "  "))

        assertEquals(context.getString(R.string.missing_name), state.header.displayName)
    }

    @Test
    fun map_withTheFamilyNameFirstDisplayOrder_showsTheAlternativeName() {
        val details = contactDetails(
            displayName = "Alex Doe",
            alternativeDisplayName = "Doe, Alex",
        )

        val state = mapState(details, displayOrder = DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals("Doe, Alex", state.header.displayName)
    }

    @Test
    fun map_withTheFamilyNameFirstOrderAndNoAlternativeName_fallsBackToMissingName() {
        val details = contactDetails(
            displayName = "Alex Doe",
            alternativeDisplayName = null,
        )

        val state = mapState(details, displayOrder = DisplayOrder.FAMILY_NAME_FIRST)

        assertEquals(context.getString(R.string.missing_name), state.header.displayName)
    }

    @Test
    fun map_forAPhoneNumberName_marksTheDisplayNameAsLeftToRight() {
        val details = contactDetails(displayNameSource = ContactDisplayNameSource.PHONE)

        assertTrue(mapState(details).header.isDisplayNameLtr)
    }

    @Test
    fun map_forAStructuredName_leavesTheDisplayNameDirectionAlone() {
        val details = contactDetails(displayNameSource = ContactDisplayNameSource.STRUCTURED_NAME)

        assertFalse(mapState(details).header.isDisplayNameLtr)
    }

    @Test
    fun map_withAPhoneticNameThatDiffers_showsItQuoted() {
        val state = mapState(contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou"))

        assertEquals(listOf("“Alek Dou”"), state.header.subtitles)
    }

    @Test
    fun map_withAPhoneticNameEqualToTheDisplayName_hidesIt() {
        val state = mapState(contactDetails(displayName = "Alex Doe", phoneticName = "Alex Doe"))

        assertEquals(emptyList<String>(), state.header.subtitles)
    }

    @Test
    fun map_joinsTheNicknameAndTheQuotedPhoneticNameIntoTheFirstSubtitle() {
        val details = contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou")
        val cards = cardsOf(
            headerNicknames = listOf("Al"),
            headerOrganizations = listOf(listOf("Engineer", "R&D", "Acme")),
        )

        val header = mapState(details, cards).header

        assertEquals(listOf("Al • “Alek Dou”", "Engineer • R&D • Acme"), header.subtitles)
    }

    @Test
    fun map_withSeveralNicknames_joinsThemIntoTheFirstSubtitle() {
        val cards = cardsOf(headerNicknames = listOf("Al", "Ally"))

        assertEquals(listOf("Al, Ally"), mapState(cards = cards).header.subtitles)
    }

    @Test
    fun map_withSeveralOrganizations_givesEachOneItsOwnSubtitle() {
        val cards = cardsOf(
            headerOrganizations = listOf(listOf("Engineer", "Acme"), listOf("Custodian")),
        )

        assertEquals(
            listOf("Engineer • Acme", "Custodian"),
            mapState(cards = cards).header.subtitles,
        )
    }

    @Test
    fun map_withoutANickname_stillQuotesThePhoneticName() {
        val details = contactDetails(displayName = "Alex Doe", phoneticName = "Alek Dou")

        assertEquals(listOf("“Alek Dou”"), mapState(details).header.subtitles)
    }

    @Test
    fun map_withoutAnySubtitleSources_leavesTheSubtitlesEmpty() {
        val header = mapState(cards = cardsOf()).header

        assertEquals(emptyList<String>(), header.subtitles)
    }

    @Test
    fun map_passesTheQuickActionsThrough() {
        val state = mapState(contactDetails())

        assertEquals(QUICK_ACTIONS, state.quickActions)
    }

    @Test
    fun map_forAnOrganizationName_marksTheHeaderAsABusiness() {
        val details = contactDetails(displayNameSource = ContactDisplayNameSource.ORGANIZATION)

        assertTrue(mapState(details).header.isBusiness)
    }

    @Test
    fun map_withAPhoto_passesItAndTheAvatarSeed() {
        val photo = ContactPhoto.Uri("content://photo/7")
        val details = contactDetails(photo = photo, lookupKey = "lookup-key")

        val header = mapState(details).header

        assertEquals(ContactAvatarImage.Uri("content://photo/7"), header.photo)
        assertEquals("lookup-key", header.avatarSeed)
    }
}
