package com.android.contacts.ui.contactdetails.screen

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runComposeUiTest
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.tests.factory.contactHeaderUiModel
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_STAR_TEST_TAG
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect as Effect
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent as NavEvent
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
internal class ContactDetailsScreenTest {

    private val effects = MutableSharedFlow<Effect>(extraBufferCapacity = 1)
    private val navigationEvents = MutableSharedFlow<NavEvent>(extraBufferCapacity = 1)
    private val screenModel = mockk<ContactDetailsScreenModel>(relaxed = true)

    @Before
    fun setUp() {
        every { screenModel.uiState } returns MutableStateFlow(State(content = loadedContent()))
        every { screenModel.effects } returns effects
        every { screenModel.navigationEvents } returns navigationEvents
    }

    @Test
    fun showsTheContactState() = runComposeUiTest {
        setScreenContent()

        onNodeWithText("Anna Smith").assertIsDisplayed()
    }

    @Test
    fun effects_arePassedOn() = runComposeUiTest {
        val received = mutableListOf<Effect>()
        setScreenContent(onEffect = { effect -> received += effect })

        effects.tryEmit(Effect.ShareContact(lookupKey = "lookup-key"))
        waitForIdle()

        assertEquals(listOf(Effect.ShareContact(lookupKey = "lookup-key")), received)
    }

    @Test
    fun whenTheScreenModelClosesTheScreen_navigatesBack() = runComposeUiTest {
        var backCount = 0
        setScreenContent(onNavigateBack = { backCount++ })

        navigationEvents.tryEmit(NavEvent.Close)
        waitForIdle()

        assertEquals(1, backCount)
    }

    @Test
    fun actions_areRoutedToTheScreenModel() = runComposeUiTest {
        setScreenContent()

        onNodeWithTag(CONTACT_DETAILS_STAR_TEST_TAG).performClick()

        verify { screenModel.onAction(Action.StarClick) }
    }

    private fun loadedContent(): Content.Loaded {
        return Content.Loaded(
            recentCalls = persistentListOf(),
            groups = persistentListOf(),
            header = contactHeaderUiModel(displayName = "Anna Smith"),
            quickActions = persistentListOf(),
            contactCard = persistentListOf(),
            notes = persistentListOf(),
            settings = persistentListOf(),
            emptyPrompt = null,
            menu = contactDetailsMenu(),
            isStarred = false,
        )
    }

    private fun ComposeUiTest.setScreenContent(
        onEffect: (Effect) -> Unit = {},
        onNavigateBack: () -> Unit = {},
    ) {
        setContent {
            ContactDetailsScreen(
                onEffect = onEffect,
                onNavigateBack = onNavigateBack,
                screenModel = screenModel,
            )
        }
    }
}
