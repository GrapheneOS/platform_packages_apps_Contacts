package com.android.contacts.ui.common.text

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.tests.compose.RightToLeftLayout
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class LtrTextTest {

    @Test
    fun inAnLtrContext_leavesLatinTextUnchanged() = runComposeUiTest {
        setContent {
            Text(text = "555 0001".asLtrText())
        }

        onNodeWithText("555 0001").assertIsDisplayed()
    }

    @Test
    fun inAnRtlContext_wrapsTheTextWithoutChangingItsCharacters() = runComposeUiTest {
        setContent {
            RightToLeftLayout {
                Text(text = "+1 555 0001".asLtrText())
            }
        }

        onNodeWithText("+1 555 0001", substring = true).assertIsDisplayed()
    }
}
