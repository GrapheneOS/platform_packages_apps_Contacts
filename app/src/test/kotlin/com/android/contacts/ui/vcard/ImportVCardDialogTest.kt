package com.android.contacts.ui.vcard

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.vcard.screen.ImportVCardDialog
import com.android.contacts.ui.vcard.screen.ImportVCardEffectHandler
import com.android.contacts.ui.vcard.screen.ImportVCardScreenModel
import com.android.contacts.ui.vcard.screen.model.IMPORT_VCARD_CANCEL_TEST_TAG
import com.android.contacts.ui.vcard.screen.model.IMPORT_VCARD_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.screen.model.ImportVCardAction as Action
import com.android.contacts.ui.vcard.screen.model.ImportVCardUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class ImportVCardDialogTest {

    private val fakeUiStateFlow = MutableStateFlow<State>(State.Preparing)
    private lateinit var screenModel: ImportVCardScreenModel
    private lateinit var effectHandler: ImportVCardEffectHandler

    @Before
    fun setup() {
        screenModel = mockk(relaxed = true)
        effectHandler = mockk(relaxed = true)
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun dialog_whenImporting_isShown() = runComposeUiTest {
        fakeUiStateFlow.value = State.Importing

        setScreenContent()

        onNodeWithTag(IMPORT_VCARD_DIALOG_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun dialog_whenCancelling_isShown() = runComposeUiTest {
        fakeUiStateFlow.value = State.Cancelling

        setScreenContent()

        onNodeWithTag(IMPORT_VCARD_DIALOG_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun cancelClick_whenImporting_sendsAction() = runComposeUiTest {
        fakeUiStateFlow.value = State.Importing

        setScreenContent()

        onNodeWithTag(IMPORT_VCARD_CANCEL_TEST_TAG).performClick()

        verify { screenModel.onAction(Action.CancelClicked) }
    }

    @Test
    fun cancel_whenCancelling_isDisabled() = runComposeUiTest {
        fakeUiStateFlow.value = State.Cancelling

        setScreenContent()

        onNodeWithTag(IMPORT_VCARD_CANCEL_TEST_TAG).assertIsNotEnabled()
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            ImportVCardDialog(
                effectHandler = effectHandler,
                screenModel = screenModel,
            )
        }
    }
}
