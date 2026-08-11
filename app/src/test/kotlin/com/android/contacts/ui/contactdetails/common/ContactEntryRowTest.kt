package com.android.contacts.ui.contactdetails.common

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.tests.factory.contactEntryActionUiModel
import com.android.contacts.tests.factory.contactEntryUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ALTERNATE_ACTION_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactEntryRowTest {

    @Test
    fun showsTheHeaderSubHeaderAndText() = runComposeUiTest {
        setRowContent(
            entry = contactEntryUiModel(
                header = "555 0001",
                subHeader = "sub header",
                text = "Mobile",
            ),
        )

        onNodeWithText("555 0001", substring = true).assertIsDisplayed()
        onNodeWithText("sub header").assertIsDisplayed()
        onNodeWithText("Mobile").assertIsDisplayed()
    }

    @Test
    fun whenClicked_reportsTheClick() = runComposeUiTest {
        var clicks = 0
        setRowContent(onClick = { clicks++ })

        onNodeWithTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + 1L).performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun whenTheAlternateActionIsClicked_reportsTheAction() = runComposeUiTest {
        val action = ContactEntryAction.Sms(number = "555 0001")
        var clicked: ContactEntryAction? = null
        setRowContent(
            entry = contactEntryUiModel(
                alternateAction = contactEntryActionUiModel(action = action),
            ),
            onAlternateActionClick = { clicked = it },
        )

        onNodeWithTag(CONTACT_DETAILS_ALTERNATE_ACTION_TEST_TAG_PREFIX + 1L).performClick()

        assertEquals(action, clicked)
    }

    @Test
    fun showsTheContentDescriptionOfAnAction() = runComposeUiTest {
        setRowContent(
            entry = contactEntryUiModel(
                alternateAction = contactEntryActionUiModel(contentDescription = "Text 555 0001"),
            ),
        )

        onNodeWithContentDescription("Text 555 0001").assertIsDisplayed()
    }

    @Test
    fun whenLongClicked_offersCopying() = runComposeUiTest {
        setRowContent(entry = contactEntryUiModel(copyText = "555 0001"))

        longClickTheRow()

        onNodeWithText("Copy to clipboard").assertIsDisplayed()
    }

    @Test
    fun whenLongClickedOnAChangeableEntry_offersSettingTheDefault() = runComposeUiTest {
        setRowContent(entry = contactEntryUiModel(isDefaultChangeable = true))

        longClickTheRow()

        onNodeWithText("Set default").assertIsDisplayed()
    }

    @Test
    fun whenLongClickedOnTheDefaultEntry_offersClearingIt() = runComposeUiTest {
        setRowContent(
            entry = contactEntryUiModel(
                isSuperPrimary = true,
                isDefaultChangeable = true,
            ),
        )

        longClickTheRow()

        onNodeWithText("Clear default").assertIsDisplayed()
    }

    @Test
    fun withoutAPrimaryAction_opensTheActionsMenuOnTap() = runComposeUiTest {
        setRowContent(
            entry = contactEntryUiModel(copyText = "555 0001"),
            onClick = null,
        )

        onNodeWithTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + 1L).performClick()

        onNodeWithText("Copy to clipboard").assertIsDisplayed()
    }

    @Test
    fun whenLongClickedOnANonChangeableEntry_offersNoDefaultActions() = runComposeUiTest {
        setRowContent(entry = contactEntryUiModel(copyText = "555 0001"))

        longClickTheRow()

        onNodeWithText("Set default").assertDoesNotExist()
        onNodeWithText("Clear default").assertDoesNotExist()
    }

    @Test
    fun whenCopyingIsChosen_reportsIt() = runComposeUiTest {
        var copyClicks = 0
        setRowContent(
            entry = contactEntryUiModel(copyText = "555 0001"),
            onCopyClick = { copyClicks++ },
        )

        longClickTheRow()
        onNodeWithText("Copy to clipboard").performClick()

        assertEquals(1, copyClicks)
    }

    private fun ComposeUiTest.longClickTheRow() {
        onNodeWithTag(CONTACT_DETAILS_ENTRY_TEST_TAG_PREFIX + 1L).performTouchInput { longClick() }
    }

    private fun ComposeUiTest.setRowContent(
        entry: ContactEntryUiModel = contactEntryUiModel(),
        onClick: (() -> Unit)? = {},
        onCopyClick: () -> Unit = {},
        onAlternateActionClick: (ContactEntryAction) -> Unit = {},
    ) {
        setContent {
            ContactEntryRow(
                entry = entry,
                isIconVisible = true,
                onClick = onClick,
                onCopyClick = onCopyClick,
                onSetDefaultClick = {},
                onClearDefaultClick = {},
                onAlternateActionClick = onAlternateActionClick,
                onThirdActionClick = {},
            )
        }
    }
}
