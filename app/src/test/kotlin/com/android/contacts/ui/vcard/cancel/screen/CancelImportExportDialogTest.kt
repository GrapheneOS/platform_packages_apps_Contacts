package com.android.contacts.ui.vcard.cancel.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_CONFIRM_BUTTON_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_CONFIRM_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_DISMISS_BUTTON_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CANCEL_ERROR_DIALOG_TEST_TAG
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportAction as Action
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportUiState as State
import com.android.contacts.ui.vcard.cancel.screen.model.VCardOperationType
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
internal class CancelImportExportDialogTest {

    private val fakeUiStateFlow = MutableStateFlow<State>(
        State.Confirming(
            type = VCardOperationType.IMPORT,
            displayName = "",
        ),
    )
    private lateinit var screenModel: CancelImportExportScreenModel
    private lateinit var effectHandler: CancelImportExportEffectHandler

    @Before
    fun setup() {
        screenModel = mockk(relaxed = true)
        effectHandler = mockk(relaxed = true)
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun whenConfirming_isShown() = runComposeUiTest {
        fakeUiStateFlow.value = State.Confirming(
            type = VCardOperationType.IMPORT,
            displayName = "contacts.vcf",
        )

        setScreenContent()

        onNodeWithTag(CANCEL_CONFIRM_DIALOG_TEST_TAG).assertIsDisplayed()
        onNodeWithText("contacts.vcf", substring = true).assertIsDisplayed()
        onNodeWithTag(CANCEL_CONFIRM_BUTTON_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CANCEL_DISMISS_BUTTON_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun whenConfirming_onDismiss_sendAction() = runComposeUiTest {
        fakeUiStateFlow.value = State.Confirming(
            type = VCardOperationType.IMPORT,
            displayName = "contacts.vcf",
        )

        setScreenContent()

        onNodeWithTag(CANCEL_DISMISS_BUTTON_TEST_TAG).performClick()
        verify { screenModel.onAction(Action.DISMISS) }
    }

    @Test
    fun whenConfirming_onConfirm_sendAction() = runComposeUiTest {
        fakeUiStateFlow.value = State.Confirming(
            type = VCardOperationType.IMPORT,
            displayName = "contacts.vcf",
        )

        setScreenContent()

        onNodeWithTag(CANCEL_CONFIRM_BUTTON_TEST_TAG).performClick()
        verify { screenModel.onAction(Action.CONFIRM) }
    }

    @Test
    fun whenError_isShown() = runComposeUiTest {
        fakeUiStateFlow.value = State.Error
        setScreenContent()

        onNodeWithTag(CANCEL_ERROR_DIALOG_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CANCEL_CONFIRM_BUTTON_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CANCEL_DISMISS_BUTTON_TEST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun whenError_onConfirm_sendDismissAction() = runComposeUiTest {
        fakeUiStateFlow.value = State.Error
        setScreenContent()

        onNodeWithTag(CANCEL_ERROR_DIALOG_TEST_TAG).assertIsDisplayed()
        onNodeWithTag(CANCEL_CONFIRM_BUTTON_TEST_TAG).performClick()
        verify { screenModel.onAction(Action.DISMISS) }
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            CancelImportExportDialog(
                effectHandler = effectHandler,
                screenModel = screenModel,
            )
        }
    }
}
