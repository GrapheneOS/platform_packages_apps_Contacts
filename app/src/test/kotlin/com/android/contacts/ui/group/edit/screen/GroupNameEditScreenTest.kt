package com.android.contacts.ui.group.edit.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.R
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_CANCEL_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_DIALOG_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_DIALOG_TITLE_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_FIELD_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GROUP_NAME_EDIT_OK_TEST_TAG
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditAction as Action
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditInputError as InputError
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class GroupNameEditScreenTest {

    private val screenModel = mockk<GroupNameEditScreenModel>(relaxed = true)

    @Test
    fun whenLoading_doNotShowDialog() = runComposeUiTest {
        withState(State.Loading)
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_DIALOG_TEST_TAG).assertIsNotDisplayed()
    }

    @Test
    fun whenCreating_showCorrectTitle() = runComposeUiTest {
        withState(State.Ready(isEdit = false, name = "", maxLenght = 40))
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_DIALOG_TITLE_TEST_TAG)
            .assertTextEquals(targetContext.getString(R.string.group_name_dialog_insert_title))
    }

    @Test
    fun whenEditing_showCorrectTitle() = runComposeUiTest {
        withState(State.Ready(isEdit = true, name = "", maxLenght = 40))
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_DIALOG_TITLE_TEST_TAG)
            .assertTextEquals(targetContext.getString(R.string.group_name_dialog_update_title))
    }

    @Test
    fun whenNameIsProvided_setIt() = runComposeUiTest {
        withState(State.Ready(name = "Group Name", maxLenght = 40))
        setContent { Screen() }

        onNodeWithText("Group Name")
            .assertIsDisplayed()
    }

    @Test
    fun whenNameIsEmpty_okIsDisabled() = runComposeUiTest {
        withState(State.Ready(name = "", maxLenght = 40))
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_OK_TEST_TAG)
            .assertIsNotEnabled()
    }

    @Test
    fun whenNameIsChanged_sendAction() = runComposeUiTest {
        withState(State.Ready(name = "Old Name", maxLenght = 40))
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_FIELD_TEST_TAG)
            .performTextReplacement("New Name")

        verify { screenModel.onAction(Action.NameChanged("New Name")) }
    }

    @Test
    fun whenCancelIsClicked_sendAction() = runComposeUiTest {
        withState(State.Ready(name = "Group Name", maxLenght = 40))
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_CANCEL_TEST_TAG)
            .performClick()

        verify { screenModel.onAction(Action.Dismissed) }
    }

    @Test
    fun whenOkIsClicked_sendAction() = runComposeUiTest {
        withState(State.Ready(name = "Group Name", maxLenght = 40))
        setContent { Screen() }

        onNodeWithTag(GROUP_NAME_EDIT_OK_TEST_TAG)
            .performClick()

        verify { screenModel.onAction(Action.OkClicked) }
    }

    @Test
    fun whenThereIsAnInputError_showErrorAndDisbleOk() = runComposeUiTest {
        withState(
            State.Ready(
                name = "Group Name",
                maxLenght = 40,
                inputError = InputError.DUPLICATED_NAME,
            ),
        )
        setContent { Screen() }

        onNodeWithText(targetContext.getString(R.string.groupExistsErrorMessage))
            .assertIsDisplayed()
        onNodeWithTag(GROUP_NAME_EDIT_OK_TEST_TAG)
            .assertIsNotEnabled()
    }

    @Composable
    private fun Screen() {
        GroupNameEditScreen(
            effectHandler = mockk<GroupNameEditEffectHandler>(relaxed = true),
            screenModel = screenModel,
        )
    }

    private fun withState(state: State) {
        every { screenModel.uiState } returns MutableStateFlow(state)
    }
}
