package com.android.contacts.ui.vcardexport

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.ui.vcardexport.screen.ExportVCardDialog
import com.android.contacts.ui.vcardexport.screen.ExportVCardEffectHandler
import com.android.contacts.ui.vcardexport.screen.ExportVCardScreenModel
import com.android.contacts.ui.vcardexport.screen.model.EXPORT_VCARD_BUTTON_SHARE_TEST_TAG
import com.android.contacts.ui.vcardexport.screen.model.EXPORT_VCARD_BUTTON_VCF_TEST_TAG
import com.android.contacts.ui.vcardexport.screen.model.EXPORT_VCARD_DIALOG_TEST_TAG
import com.android.contacts.ui.vcardexport.screen.model.ExportMode
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardUiState as State
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class ExportVCardDialogTest {

    private val fakeUiStateFlow = MutableStateFlow(State())
    private lateinit var screenModel: ExportVCardScreenModel
    private lateinit var effectHandler: ExportVCardEffectHandler

    @Before
    fun setup() {
        screenModel = mockk(relaxed = true)
        effectHandler = mockk(relaxed = true)
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun dialog_whenShowModeDialogIsFalse_isNotDisplayed() = runComposeUiTest {
        fakeUiStateFlow.value = State(
            showModeDialog = false,
            availableModes = persistentSetOf(ExportMode.VCARD_FILE, ExportMode.SHARE_ALL),
        )

        setScreenContent()

        onNodeWithTag(EXPORT_VCARD_DIALOG_TEST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun dialog_whenShowModeDialogIsTrue_isDisplayed() = runComposeUiTest {
        fakeUiStateFlow.value = State(
            showModeDialog = true,
            availableModes = persistentSetOf(ExportMode.VCARD_FILE, ExportMode.SHARE_ALL),
        )

        setScreenContent()

        onNodeWithTag(EXPORT_VCARD_DIALOG_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun vcfButton_whenAvailable_isDisplayed() = runComposeUiTest {
        fakeUiStateFlow.value = State(
            showModeDialog = true,
            availableModes = persistentSetOf(ExportMode.VCARD_FILE),
        )

        setScreenContent()

        onNodeWithTag(EXPORT_VCARD_BUTTON_VCF_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun vcfButton_whenNotAvailable_isNotDisplayed() = runComposeUiTest {
        fakeUiStateFlow.value = State(
            showModeDialog = true,
            availableModes = persistentSetOf(ExportMode.SHARE_ALL),
        )

        setScreenContent()

        onNodeWithTag(EXPORT_VCARD_BUTTON_VCF_TEST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun shareButton_whenAvailable_isDisplayed() = runComposeUiTest {
        fakeUiStateFlow.value = State(
            showModeDialog = true,
            availableModes = persistentSetOf(ExportMode.SHARE_ALL),
        )

        setScreenContent()

        onNodeWithTag(EXPORT_VCARD_BUTTON_SHARE_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun shareButton_whenNotAvailable_isNotDisplayed() = runComposeUiTest {
        fakeUiStateFlow.value = State(
            showModeDialog = true,
            availableModes = persistentSetOf(ExportMode.VCARD_FILE),
        )

        setScreenContent()

        onNodeWithTag(EXPORT_VCARD_BUTTON_SHARE_TEST_TAG).assertIsNotDisplayed()
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            ExportVCardDialog(
                effectHandler = effectHandler,
                screenModel = screenModel,
            )
        }
    }
}
