package com.android.contacts.ui.settings.about.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.settings.screen.model.ABOUT_BUILD_VERSION_TEST_TAG
import com.android.contacts.ui.settings.screen.model.ABOUT_LICENSES_TEST_TAG
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class AboutScreenTest {

    @Test
    fun showsTheBuildVersion() = runComposeUiTest {
        setContent {
            AboutScreen(
                buildVersion = "1.7.40",
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertIsDisplayed()
        onNodeWithText("1.7.40").assertIsDisplayed()
    }

    @Test
    fun whenBuildVersionIsUnknown_showsTheRowWithoutASummary() = runComposeUiTest {
        setContent {
            AboutScreen(
                buildVersion = null,
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertTextEquals(BUILD_VERSION_TITLE)
    }

    @Test
    fun buildVersionRowIsNotClickable() = runComposeUiTest {
        setContent {
            AboutScreen(
                buildVersion = "1.7.40",
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertHasNoClickAction()
        onNodeWithTag(ABOUT_LICENSES_TEST_TAG).assertHasClickAction()
    }

    private companion object {
        const val BUILD_VERSION_TITLE = "Build version"
    }
}
