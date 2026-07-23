package com.android.contacts.ui.interactions.account

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.R
import com.android.contacts.tests.factory.AccountUiModelFactory
import com.android.contacts.tests.resources
import com.android.contacts.ui.interactions.account.screen.SelectAccountDialog
import com.android.contacts.ui.interactions.account.screen.SelectAccountEffectHandler
import com.android.contacts.ui.interactions.account.screen.SelectAccountScreenModel
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountAction as Action
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountUiState as State
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
class SelectAccountDialogTest {

    private val fakeUiStateFlow = MutableStateFlow(State())
    private lateinit var screenModel: SelectAccountScreenModel
    private lateinit var effectHandler: SelectAccountEffectHandler

    @Before
    fun setup() {
        screenModel = mockk(relaxed = true)
        effectHandler = mockk(relaxed = true)
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun showsTitleId() = runComposeUiTest {
        setScreenContent()

        val titleId = R.string.dialog_new_group_account
        fakeUiStateFlow.value = State(titleId = titleId)

        onNodeWithText(resources.getString(titleId)).assertIsDisplayed()
    }

    @Test
    fun showsAccount() = runComposeUiTest {
        setScreenContent()

        val account = AccountUiModelFactory.build(
            name = "John Smith",
            type = "Example"
        )
        fakeUiStateFlow.value = State(accounts = persistentListOf(account))

        onNodeWithText(account.name!!).assertIsDisplayed()
        onNodeWithText(account.type!!).assertIsDisplayed()
    }

    @Test
    fun selectsAccount() = runComposeUiTest {
        setScreenContent()

        val account = AccountUiModelFactory.build()
        fakeUiStateFlow.value = State(accounts = persistentListOf(account))

        onNodeWithText(account.name!!).performClick()

        verify { screenModel.onAction(Action.AccountSelected(account)) }
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            SelectAccountDialog(
                effectHandler = effectHandler,
                screenModel = screenModel,
            )
        }
    }
}
