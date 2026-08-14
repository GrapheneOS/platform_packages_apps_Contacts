package com.android.contacts.ui.settings.about

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.settings.screen.model.ABOUT_BUILD_VERSION_TEST_TAG
import com.android.contacts.ui.settings.screen.model.ABOUT_LICENSES_TEST_TAG
import org.junit.Assert.assertEquals
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
                buildVersion = BUILD_VERSION,
                onBuildVersionLongClick = {},
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertIsDisplayed()
        onNodeWithText(BUILD_VERSION).assertIsDisplayed()
    }

    @Test
    fun whenBuildVersionIsUnknown_showsTheRowWithoutASummary() = runComposeUiTest {
        setContent {
            AboutScreen(
                buildVersion = null,
                onBuildVersionLongClick = {},
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertTextEquals(BUILD_VERSION_TITLE)
    }

    @Test
    fun whenBuildVersionIsLongPressed_reportsIt() = runComposeUiTest {
        var buildVersionLongClicks = 0
        setContent {
            AboutScreen(
                buildVersion = BUILD_VERSION,
                onBuildVersionLongClick = { buildVersionLongClicks++ },
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).performTouchInput { longClick() }

        assertEquals(1, buildVersionLongClicks)
    }

    @Test
    fun buildVersionRowIsNotClickable() = runComposeUiTest {
        setContent {
            AboutScreen(
                buildVersion = BUILD_VERSION,
                onBuildVersionLongClick = {},
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).assertHasNoClickAction()
        onNodeWithTag(ABOUT_LICENSES_TEST_TAG).assertHasClickAction()
    }

    @Test
    fun whenBuildVersionIsUnknown_longPressReportsNothing() = runComposeUiTest {
        var buildVersionLongClicks = 0
        setContent {
            AboutScreen(
                buildVersion = null,
                onBuildVersionLongClick = { buildVersionLongClicks++ },
                onLicensesClick = {},
                onNavigateBack = {},
            )
        }

        onNodeWithTag(ABOUT_BUILD_VERSION_TEST_TAG).performTouchInput { longClick() }

        assertEquals(0, buildVersionLongClicks)
    }

    private companion object {
        const val BUILD_VERSION = "1.7.40"
        const val BUILD_VERSION_TITLE = "Build version"
    }
}
