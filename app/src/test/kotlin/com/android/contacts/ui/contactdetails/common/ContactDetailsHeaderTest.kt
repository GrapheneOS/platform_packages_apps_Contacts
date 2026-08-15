package com.android.contacts.ui.contactdetails.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.LayoutDirection
import com.android.contacts.tests.factory.contactHeaderUiModel
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsHeaderTest {

    @Test
    fun showsTheDisplayName() = runComposeUiTest {
        setContent {
            ContactDetailsHeader(header = contactHeaderUiModel(displayName = "Alex Doe"))
        }

        onNodeWithText("Alex Doe", substring = true).assertIsDisplayed()
    }

    @Test
    fun withAPhoneticName_showsIt() = runComposeUiTest {
        setContent {
            ContactDetailsHeader(
                header = contactHeaderUiModel(
                    displayName = "Alex Doe",
                    phoneticName = "Arekkusu Dou",
                ),
            )
        }

        onNodeWithText("Arekkusu Dou").assertIsDisplayed()
    }

    @Test
    fun withoutAPhoto_showsTheFirstLetterOfTheName() = runComposeUiTest {
        setContent {
            ContactDetailsHeader(header = contactHeaderUiModel(displayName = "alex doe"))
        }

        onNodeWithText("A").assertIsDisplayed()
    }

    @Test
    fun forAPhoneNumberName_showsNoFallbackLetter() = runComposeUiTest {
        setContent {
            ContactDetailsHeader(header = contactHeaderUiModel(displayName = "+1 555 0001"))
        }

        onNodeWithText("+1 555 0001", substring = true).assertIsDisplayed()
        onNodeWithText("5").assertDoesNotExist()
    }

    @Test
    fun inARightToLeftLayoutForAPhoneNumberName_wrapsTheNameAsLeftToRight() = runComposeUiTest {
        setContent {
            RightToLeftLayout {
                ContactDetailsHeader(
                    header = contactHeaderUiModel(
                        displayName = "+1 555 0001",
                        isDisplayNameLtr = true,
                    ),
                )
            }
        }

        onNodeWithText("+1 555 0001").assertDoesNotExist()
        onNodeWithText("+1 555 0001", substring = true).assertIsDisplayed()
    }

    @Test
    fun inARightToLeftLayoutForAnOrdinaryName_leavesTheNameAlone() = runComposeUiTest {
        setContent {
            RightToLeftLayout {
                ContactDetailsHeader(
                    header = contactHeaderUiModel(
                        displayName = "Alex Doe",
                        isDisplayNameLtr = false,
                    ),
                )
            }
        }

        onNodeWithText("Alex Doe").assertIsDisplayed()
    }

    @Composable
    private fun RightToLeftLayout(
        content: @Composable () -> Unit,
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl,
            content = content,
        )
    }
}
