package com.android.contacts.ui.contactdetails.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_STAR_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_TITLE_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsTopAppBarTest {

    @Test
    fun whileTheHeaderIsVisible_hidesTheTitle() = runComposeUiTest {
        setBarContent(isTitleVisible = false)

        onNodeWithTag(CONTACT_DETAILS_TITLE_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun whenTheHeaderIsScrolledAway_showsTheContactName() = runComposeUiTest {
        setBarContent(isTitleVisible = true)

        onNodeWithText("Anna Smith").assertIsDisplayed()
    }

    @Test
    fun forAStarredContact_marksTheStarAsChecked() = runComposeUiTest {
        setBarContent(isStarred = true)

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).assertIsOn()
    }

    @Test
    fun forAnUnstarredContact_marksTheStarAsUnchecked() = runComposeUiTest {
        setBarContent(isStarred = false)

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).assertIsOff()
    }

    @Test
    fun whenTheStarIsToggled_reportsTheAction() = runComposeUiTest {
        val actions = mutableListOf<Action>()
        setBarContent(onAction = actions::add)

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).performClick()

        assertEquals(listOf(Action.StarClick), actions)
    }

    @Test
    fun withoutAMenu_showsNoActions() = runComposeUiTest {
        setBarContent(menu = null)

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).assertDoesNotExist()
    }

    private fun ComposeUiTest.setBarContent(
        title: String = "Anna Smith",
        isTitleVisible: Boolean = true,
        menu: ContactDetailsMenu? = contactDetailsMenu(),
        isStarred: Boolean = false,
        onAction: (Action) -> Unit = {},
    ) {
        setContent {
            ContactDetailsTopAppBar(
                title = title,
                isTitleVisible = isTitleVisible,
                menu = menu,
                isStarred = isStarred,
                onAction = onAction,
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
            )
        }
    }
}
