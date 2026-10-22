package com.android.contacts.ui.interactions.callsubject

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.R
import com.android.contacts.tests.resources
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectDialog
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectEffectHandler
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectScreenModel
import com.android.contacts.ui.interactions.callsubject.screen.model.CALL_SUBJECT_CALL_BUTTON
import com.android.contacts.ui.interactions.callsubject.screen.model.CALL_SUBJECT_HISTORY_BUTTON
import com.android.contacts.ui.interactions.callsubject.screen.model.CALL_SUBJECT_TEXT_FIELD
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectAction as Action
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectUiState as State
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
class CallSubjectDialogTest {

    private val fakeUiStateFlow = MutableStateFlow(DEFAULT_STATE)
    private val screenModel = mockk<CallSubjectScreenModel>(relaxed = true)
    private val effectHandler = mockk<CallSubjectEffectHandler>(relaxed = true)

    @Before
    fun setup() {
        every { screenModel.uiState } returns fakeUiStateFlow
    }

    @Test
    fun showsNameAndNumber() = runComposeUiTest {
        setScreenContent()

        onNodeWithText(DEFAULT_STATE.name).assertIsDisplayed()
        onNodeWithText(
            resources.getString(
                R.string.call_subject_type_and_number,
                DEFAULT_STATE.numberLabel,
                DEFAULT_STATE.number,
            ),
        ).assertIsDisplayed()
    }

    @Test
    fun hidesNumberWithoutLabel() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = DEFAULT_STATE.copy(numberLabel = null)

        onNodeWithText(DEFAULT_STATE.number!!, substring = true).assertDoesNotExist()
    }

    @Test
    fun showsSubjectSizeAndLimit() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = DEFAULT_STATE.copy(subject = "Hello", subjectLimit = 16)

        onNodeWithText(resources.getString(R.string.call_subject_limit, 5, 16))
            .assertIsDisplayed()
    }

    @Test
    fun changesSubject() = runComposeUiTest {
        setScreenContent()

        onNodeWithTag(CALL_SUBJECT_TEXT_FIELD).performTextInput("Hello")

        verify { screenModel.onAction(Action.SubjectChange("Hello")) }
    }

    @Test
    fun limitsSubject() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = DEFAULT_STATE.copy(subjectLimit = 4)

        onNodeWithTag(CALL_SUBJECT_TEXT_FIELD).performTextInput("Hello")

        verify { screenModel.onAction(Action.SubjectChange("Hell")) }
    }

    @Test
    fun hidesHistoryButtonWithoutHistory() = runComposeUiTest {
        setScreenContent()

        onNodeWithTag(CALL_SUBJECT_HISTORY_BUTTON).assertDoesNotExist()
    }

    @Test
    fun picksSubjectFromHistory() = runComposeUiTest {
        setScreenContent()

        fakeUiStateFlow.value = DEFAULT_STATE.copy(
            history = persistentListOf("Running late", "Call me back"),
        )

        onNodeWithTag(CALL_SUBJECT_HISTORY_BUTTON).performClick()
        onNodeWithText("Call me back").performClick()

        verify { screenModel.onAction(Action.HistorySubjectPick("Call me back")) }
    }

    @Test
    fun calls() = runComposeUiTest {
        setScreenContent()

        onNodeWithTag(CALL_SUBJECT_CALL_BUTTON).performClick()

        verify { screenModel.onAction(Action.CallClick) }
    }

    private fun ComposeUiTest.setScreenContent() {
        setContent {
            CallSubjectDialog(
                effectHandler = effectHandler,
                screenModel = screenModel,
            )
        }
    }

    private companion object {
        val DEFAULT_STATE = State(
            avatarImage = null,
            isBusiness = false,
            name = "John Smith",
            number = "+1 234-567-8901",
            numberLabel = "Work",
        )
    }
}
