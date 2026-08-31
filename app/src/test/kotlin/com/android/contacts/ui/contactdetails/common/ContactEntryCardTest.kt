package com.android.contacts.ui.contactdetails.common

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.tests.factory.contactEntryGroupUiModel
import com.android.contacts.tests.factory.contactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryGroupUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactEntryCardTest {

    @Test
    fun showsEveryEntryOfEveryGroup() = runComposeUiTest {
        setCardContent(
            groups = persistentListOf(
                contactEntryGroupUiModel(entries = entries(count = 2)),
                contactEntryGroupUiModel(
                    entries = listOf(contactEntryUiModel(id = 3L, header = "email 1")),
                ),
            ),
        )

        onNodeWithText("entry 1").assertIsDisplayed()
        onNodeWithText("entry 2").assertIsDisplayed()
        onNodeWithText("email 1").assertIsDisplayed()
    }

    @Test
    fun whenAnEntryIsClicked_reportsIt() = runComposeUiTest {
        val entry = contactEntryUiModel(
            id = 1L,
            action = ContactEntryAction.Call(number = "555 0001"),
        )
        var clicked: ContactEntryUiModel? = null
        setCardContent(
            groups = persistentListOf(contactEntryGroupUiModel(entries = listOf(entry))),
            onEntryClick = { clicked = it },
        )

        onNodeWithTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + 1L).performClick()

        assertEquals(entry, clicked)
    }

    @Test
    fun anEntryWithAnAction_isEnabled() = runComposeUiTest {
        setCardContent(
            groups = persistentListOf(
                contactEntryGroupUiModel(
                    entries = listOf(
                        contactEntryUiModel(
                            id = 1L,
                            action = ContactEntryAction.Call(number = "555 0001"),
                        ),
                    ),
                ),
            ),
        )

        onNodeWithTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + 1L).assertIsEnabled()
    }

    @Test
    fun anEntryWithoutAnActionOrAMenu_isDisabled() = runComposeUiTest {
        setCardContent(
            groups = persistentListOf(
                contactEntryGroupUiModel(
                    entries = listOf(contactEntryUiModel(id = 1L, action = null)),
                ),
            ),
        )

        onNodeWithTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + 1L).assertIsNotEnabled()
    }

    @Test
    fun entriesOfAGroup_startAtTheSameOffset() = runComposeUiTest {
        setCardContent(
            groups = persistentListOf(contactEntryGroupUiModel(entries = entries(count = 2))),
        )

        assertEquals(
            onNodeWithText("entry 1").getUnclippedBoundsInRoot().left,
            onNodeWithText("entry 2").getUnclippedBoundsInRoot().left,
        )
    }

    @Test
    fun withinAGroup_showsTheIconOnceForEachDistinctIcon() {
        val groups = listOf(
            contactEntryGroupUiModel(
                entries = listOf(
                    contactEntryUiModel(id = 1L, icon = ContactEntryIcon.BIRTHDAY),
                    contactEntryUiModel(id = 2L, icon = ContactEntryIcon.EVENT),
                    contactEntryUiModel(id = 3L, icon = ContactEntryIcon.EVENT),
                ),
            ),
        )

        val cells = contactEntryCells(groups)

        assertEquals(
            listOf(true, true, false),
            cells.map(ContactEntryCellModel::isIconVisible),
        )
    }

    @Test
    fun theFirstEntryOfEachGroup_showsItsIcon() {
        val groups = listOf(
            contactEntryGroupUiModel(
                entries = listOf(
                    contactEntryUiModel(id = 1L, icon = ContactEntryIcon.CALL),
                    contactEntryUiModel(id = 2L, icon = ContactEntryIcon.CALL),
                ),
            ),
            contactEntryGroupUiModel(
                entries = listOf(contactEntryUiModel(id = 3L, icon = ContactEntryIcon.EMAIL)),
            ),
        )

        val cells = contactEntryCells(groups)

        assertEquals(
            listOf(true, false, true),
            cells.map(ContactEntryCellModel::isIconVisible),
        )
    }

    private fun entries(count: Int): List<ContactEntryUiModel> {
        return (1..count).map { index ->
            contactEntryUiModel(id = index.toLong(), header = "entry $index")
        }
    }

    private fun ComposeUiTest.setCardContent(
        groups: ImmutableList<ContactEntryGroupUiModel>,
        onEntryClick: (ContactEntryUiModel) -> Unit = {},
    ) {
        setContent {
            ContactEntryCard(
                groups = groups,
                onEntryClick = onEntryClick,
                onEntryCopyClick = {},
                onEntrySetDefaultClick = {},
                onEntryClearDefaultClick = {},
                onEntryCallingSimClick = {},
                onEntryActionClick = {},
            )
        }
    }
}
