package com.android.contacts.ui.interactions.importing

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.tests.SimCardOptionFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.resources
import com.android.contacts.ui.interactions.importing.screen.ImportDialog
import com.android.contacts.ui.interactions.importing.screen.ImportEffectHandler
import com.android.contacts.ui.interactions.importing.screen.ImportScreenModel
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_EMPTY_MESSAGE_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_PROGRESS_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.IMPORT_VCARD_BUTTON_TEST_TAG
import com.android.contacts.ui.interactions.importing.screen.model.ImportAction as Action
import com.android.contacts.ui.interactions.importing.screen.model.ImportUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class ImportDialogTest {

    private val fakeUiStateFlow = MutableStateFlow(State())
    private lateinit var screenModel: ImportScreenModel
    private lateinit var effectHandler: ImportEffectHandler
    private var accountChosen: AccountModel? = null

    @Before
    fun setup() {
        screenModel = mockk(relaxed = true)
        effectHandler = mockk(relaxed = true)
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun showsOrHidesProgressIndicator() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = State(
            isVCardImportAvailable = null,
            simCardOptions = null,
        )
        onNodeWithTag(IMPORT_PROGRESS_TEST_TAG).assertIsDisplayed()

        fakeUiStateFlow.value = State(
            isVCardImportAvailable = true,
            simCardOptions = persistentListOf(),
        )
        onNodeWithTag(IMPORT_PROGRESS_TEST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun showsEmptyState() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = State(
            isVCardImportAvailable = false,
            simCardOptions = persistentListOf(),
        )
        onNodeWithTag(IMPORT_EMPTY_MESSAGE_TEST_TAG).assertIsDisplayed()
    }

    @Test
    fun showsOrHidesVCardOption() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = State(
            isVCardImportAvailable = true,
            simCardOptions = persistentListOf(),
        )
        onNodeWithTag(IMPORT_VCARD_BUTTON_TEST_TAG).assertIsDisplayed()

        fakeUiStateFlow.value = State(
            isVCardImportAvailable = false,
            simCardOptions = persistentListOf(),
        )
        onNodeWithTag(IMPORT_VCARD_BUTTON_TEST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun showsSimCardOptions() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = State(
            isVCardImportAvailable = false,
            simCardOptions = persistentListOf(
                SimCardOptionFactory.build(),
                SimCardOptionFactory.build(name = "Test")
            ),
        )

        onNodeWithText(
            resources.getString(R.string.import_from_sim_summary_fmt, 1)
        ).assertIsDisplayed()
        onNodeWithText(
            resources.getString(R.string.import_from_sim_summary_fmt, "Test")
        ).assertIsDisplayed()
    }

    @Test
    fun whenAccountChosenIsProvided_callOnAction() = runComposeUiTest {
        accountChosen = AccountModelFactory.build()

        setScreenContent()

        verify { screenModel.onAction(Action.AccountChosen(accountChosen!!)) }
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            ImportDialog(
                effectHandler = effectHandler,
                screenModel = screenModel,
                accountChosen = accountChosen,
            )
        }
    }
}
