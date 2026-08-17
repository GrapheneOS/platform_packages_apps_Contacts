package com.android.contacts.ui.contactdetails.common

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.tests.compose.RightToLeftLayout
import com.android.contacts.tests.factory.contactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactHeaderUiModel
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsHeaderTest {

    @Test
    fun showsTheDisplayName() = runComposeUiTest {
        setHeaderContent(contactHeaderUiModel(displayName = "Alex Doe"))

        onNodeWithText("Alex Doe", substring = true).assertIsDisplayed()
    }

    @Test
    fun withAPhoneticName_showsItUnderTheName() = runComposeUiTest {
        setHeaderContent(
            contactHeaderUiModel(
                displayName = "Alex Doe",
                subtitles = persistentListOf("Arekkusu Dou"),
            ),
        )

        onNodeWithText("Arekkusu Dou").assertIsDisplayed()
    }

    @Test
    fun withANickname_showsItUnderTheName() = runComposeUiTest {
        setHeaderContent(
            contactHeaderUiModel(displayName = "Alex Doe", subtitles = persistentListOf("Al")),
        )

        onNodeWithText("Al").assertIsDisplayed()
    }

    @Test
    fun withAnOrganization_showsItUnderTheName() = runComposeUiTest {
        setHeaderContent(
            contactHeaderUiModel(displayName = "Alex Doe", subtitles = persistentListOf("Acme")),
        )

        onNodeWithText("Acme").assertIsDisplayed()
    }

    @Test
    fun withoutANicknameOrAnOrganization_showsTheNameOnly() = runComposeUiTest {
        setHeaderContent(contactHeaderUiModel(displayName = "Alex Doe"))

        onNodeWithText("Al").assertDoesNotExist()
        onNodeWithText("Acme").assertDoesNotExist()
    }

    @Test
    fun withoutAPhoto_showsTheFirstLetterOfTheName() = runComposeUiTest {
        setHeaderContent(contactHeaderUiModel(displayName = "alex doe"))

        onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun forAPhoneNumberName_showsNoFallbackLetter() = runComposeUiTest {
        setHeaderContent(contactHeaderUiModel(displayName = "+1 555 0001"))

        onNodeWithText("+1 555 0001", substring = true).assertIsDisplayed()
        onNodeWithText("5").assertDoesNotExist()
    }

    @Test
    fun inARightToLeftLayoutForAPhoneNumberName_wrapsTheNameAsLeftToRight() = runComposeUiTest {
        setHeaderContent(
            header = contactHeaderUiModel(displayName = "+1 555 0001", isDisplayNameLtr = true),
            isRightToLeft = true,
        )

        onNodeWithText("+1 555 0001").assertDoesNotExist()
        onNodeWithText("+1 555 0001", substring = true).assertIsDisplayed()
    }

    @Test
    fun inARightToLeftLayoutForAnOrdinaryName_leavesTheNameAlone() = runComposeUiTest {
        setHeaderContent(
            header = contactHeaderUiModel(displayName = "Alex Doe", isDisplayNameLtr = false),
            isRightToLeft = true,
        )

        onNodeWithText("Alex Doe").assertIsDisplayed()
    }

    private fun ComposeUiTest.setHeaderContent(
        header: ContactHeaderUiModel,
        isRightToLeft: Boolean = false,
    ) {
        setContent {
            when {
                isRightToLeft -> RightToLeftLayout { ContactDetailsHeader(header = header) }
                else -> ContactDetailsHeader(header = header)
            }
        }
    }
}
