package com.android.contacts.ui.contactdetails.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.Dp
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactEntryGroupUiModel
import com.android.contacts.tests.factory.contactEntryUiModel
import com.android.contacts.tests.factory.contactHeaderUiModel
import com.android.contacts.tests.factory.contactQuickActionUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_HEADER_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens
import com.android.contacts.ui.contactdetails.screen.model.ContactEntryIcon
import com.android.contacts.ui.core.ContactsPreviewTheme
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w411dp-h891dp")
class ContactDetailsOverlayLayoutTest {

    @Test
    fun theQuickActionsRowSitsRightUnderTheHeader() = runComposeUiTest {
        setContentWith(contactHeaderUiModel())

        assertReservedHeaderSpace()
    }

    @Test
    fun withEverySubtitle_theQuickActionsRowStillSitsRightUnderTheHeader() = runComposeUiTest {
        setContentWith(
            contactHeaderUiModel(subtitles = persistentListOf("Arekkusu", "Al", "Acme")),
        )

        assertReservedHeaderSpace()
    }

    private fun ComposeUiTest.assertReservedHeaderSpace() {
        val headerBottom = bottomOf(CONTACT_DETAILS_HEADER_TEST_TAG)

        assertEquals(
            headerBottom + ContactDetailsTokens.cardGroupSpacing,
            topOf(CONTACT_DETAILS_QUICK_ACTIONS_TEST_TAG),
        )
    }

    private fun ComposeUiTest.setContentWith(header: ContactHeaderUiModel) {
        setContent {
            ContactsPreviewTheme {
                ContactDetailsContent(
                    uiState = State(content = loadedContent(header)),
                    onAction = {},
                )
            }
        }
    }

    private fun loadedContent(header: ContactHeaderUiModel): Content.Loaded {
        return Content.Loaded(
            recentCalls = persistentListOf(),
            groups = persistentListOf(),
            header = header,
            quickActions = persistentListOf(
                contactQuickActionUiModel(icon = ContactEntryIcon.CALL, label = "Call"),
                contactQuickActionUiModel(icon = ContactEntryIcon.MESSAGE, label = "Text"),
                contactQuickActionUiModel(icon = ContactEntryIcon.VIDEO_CALL, label = "Video"),
                contactQuickActionUiModel(icon = ContactEntryIcon.EMAIL, label = "Email"),
            ),
            contactCard = persistentListOf(
                contactEntryGroupUiModel(entries = persistentListOf(contactEntryUiModel())),
            ),
            notes = persistentListOf(),
            settings = persistentListOf(),
            emptyPrompt = null,
            menu = contactDetailsMenu(),
            isStarred = false,
        )
    }

    private fun ComposeUiTest.topOf(testTag: String): Dp {
        return onNodeWithTag(testTag).getUnclippedBoundsInRoot().top
    }

    private fun ComposeUiTest.bottomOf(testTag: String): Dp {
        return onNodeWithTag(testTag).getUnclippedBoundsInRoot().bottom
    }
}
