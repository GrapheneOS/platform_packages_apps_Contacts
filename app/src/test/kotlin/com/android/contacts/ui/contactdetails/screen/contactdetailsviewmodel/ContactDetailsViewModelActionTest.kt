package com.android.contacts.ui.contactdetails.screen.contactdetailsviewmodel

import app.cash.turbine.test
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.data.contactdetails.model.DirectoryContactPrefill
import com.android.contacts.domain.contactdetails.model.ContactEntryAction
import com.android.contacts.tests.factory.contactCapabilities
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.ui.contactdetails.ContactDetailsActivity
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsEffect
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsNavEvent
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ContactDetailsViewModelActionTest : BaseContactDetailsViewModelTest() {

    @Test
    fun onAction_withBackClick_closesTheScreen() = runTest {
        val viewModel = createViewModel()

        viewModel.navigationEvents.test {
            viewModel.onAction(ContactDetailsAction.BackClick)

            assertEquals(ContactDetailsNavEvent.Close, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withStarClick_starsTheContact() = runTest {
        val viewModel = createViewModel().bindContact()
        viewModel.uiState.test {
            awaitItem()
            emitLoaded()
            awaitItem()

            viewModel.onAction(ContactDetailsAction.StarClick)
            advanceUntilIdle()

            coVerify { contactActionsRepository.setStarred(LOOKUP_URI, true) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withStarClickOnAStarredContact_removesTheStar() = runTest {
        val viewModel = loadedViewModel(contactDetails(isStarred = true))

        viewModel.onAction(ContactDetailsAction.StarClick)
        advanceUntilIdle()

        coVerify { contactActionsRepository.setStarred(LOOKUP_URI, false) }
    }

    @Test
    fun onAction_withStarClickBeforeTheContactLoads_doesNothing() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.StarClick)
        advanceUntilIdle()

        coVerify(exactly = 0) { contactActionsRepository.setStarred(any(), any()) }
    }

    @Test
    fun onAction_withEditClick_cachesTheContactAndOpensTheEditor() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.EditClick)

            assertEquals(editContactEffect(), awaitItem())
            verify { contactDetailsRepository.cacheLoadedContact() }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withAddDetailsClick_opensTheEditor() = runTest {
        val viewModel = loadedViewModel()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.AddDetailsClick)

            assertEquals(editContactEffect(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withMenuClicks_emitsTheMatchingHostEffects() = runTest {
        val viewModel = loadedViewModel(
            details = contactDetails(lookupUri = LOOKUP_URI, customRingtone = "content://tone/1"),
        )

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.DeleteClick)
            assertEquals(ContactDetailsEffect.ConfirmDelete(LOOKUP_URI), awaitItem())

            viewModel.onAction(ContactDetailsAction.ShareClick)
            assertEquals(ContactDetailsEffect.ShareContact("lookup-key"), awaitItem())

            viewModel.onAction(ContactDetailsAction.RingtoneClick)
            assertEquals(ContactDetailsEffect.PickRingtone("content://tone/1"), awaitItem())

            viewModel.onAction(ContactDetailsAction.JoinClick)
            assertEquals(ContactDetailsEffect.PickJoinTarget(CONTACT_ID_UNDER_TEST), awaitItem())

            viewModel.onAction(ContactDetailsAction.LinkedContactsClick)
            assertEquals(ContactDetailsEffect.ViewLinkedContacts(LOOKUP_URI), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withShortcutClick_asksForAPinnedShortcut() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(ContactDetailsAction.ShortcutClick)
        advanceUntilIdle()

        verify {
            contactShortcutRepository.requestPinShortcut(
                contactId = CONTACT_ID_UNDER_TEST,
                lookupKey = "lookup-key",
                displayName = any(),
            )
        }
    }

    @Test
    fun onAction_forADirectoryContact_asksTheHostToCopyIt() = runTest {
        val prefill = DirectoryContactPrefill(
            name = "Alex Doe",
            values = emptyList(),
            account = null,
            dataSet = null,
        )
        every { contactDetailsRepository.getDirectoryContactPrefill() } returns prefill
        val viewModel = loadedViewModel(
            details = contactDetails(
                lookupUri = LOOKUP_URI,
                capabilities = contactCapabilities(isDirectoryEntry = true),
            ),
        )

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.EditClick)

            assertEquals(ContactDetailsEffect.AddDirectoryContact(prefill), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_forAnInvisibleContact_addsItToTheDefaultGroup() = runTest {
        val viewModel = loadedViewModel(
            details = contactDetails(
                lookupUri = LOOKUP_URI,
                capabilities = contactCapabilities(isInvisibleAndAddable = true),
            ),
        )

        viewModel.onAction(ContactDetailsAction.EditClick)
        advanceUntilIdle()

        verify {
            contactDetailsRepository.addLoadedContactToDefaultGroup(
                ContactDetailsActivity::class.java
            )
        }
    }

    @Test
    fun onAction_withEntryClick_asksTheHostToPerformTheAction() = runTest {
        val action = ContactEntryAction.Call("4155551212")
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.EntryClick(action))

            assertEquals(ContactDetailsEffect.PerformEntryAction(action), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withCopyClick_asksTheHostToCopyTheText() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.effects.test {
            viewModel.onAction(ContactDetailsAction.CopyClick("Phone", "4155551212"))

            assertEquals(
                ContactDetailsEffect.CopyToClipboard(label = "Phone", text = "4155551212"),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onAction_withSetDefaultClick_marksTheDataItemAsDefault() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.SetDefaultClick(42L))
        advanceUntilIdle()

        coVerify { contactActionsRepository.setSuperPrimary(42L) }
    }

    @Test
    fun onAction_withClearDefaultClick_clearsTheDefault() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.ClearDefaultClick(42L))
        advanceUntilIdle()

        coVerify { contactActionsRepository.clearPrimary(42L) }
    }

    @Test
    fun onAction_withARingtonePicked_savesIt() = runTest {
        val viewModel = createViewModel().bindContact()

        viewModel.onAction(ContactDetailsAction.RingtonePicked("content://media/ringtone/7"))
        advanceUntilIdle()

        coVerify {
            contactActionsRepository.setRingtone(LOOKUP_URI, "content://media/ringtone/7")
        }
    }

    @Test
    fun onAction_withARingtonePickedBeforeBinding_doesNothing() = runTest {
        val viewModel = createViewModel()

        viewModel.onAction(ContactDetailsAction.RingtonePicked(null))
        advanceUntilIdle()

        coVerify(exactly = 0) { contactActionsRepository.setRingtone(any(), any()) }
    }

    @Test
    fun onAction_withSendToVoicemailClick_togglesTheFlag() = runTest {
        val viewModel = loadedViewModel(contactDetails(isSendToVoicemail = false))

        viewModel.onAction(ContactDetailsAction.SendToVoicemailClick)
        advanceUntilIdle()

        coVerify { contactActionsRepository.setSendToVoicemail(LOOKUP_URI, true) }
    }

    @Test
    fun onAction_withSendToVoicemailClickWhenEnabled_turnsTheFlagOff() = runTest {
        val viewModel = loadedViewModel(contactDetails(isSendToVoicemail = true))

        viewModel.onAction(ContactDetailsAction.SendToVoicemailClick)
        advanceUntilIdle()

        coVerify { contactActionsRepository.setSendToVoicemail(LOOKUP_URI, false) }
    }

    @Test
    fun onAction_withRepeatedStarClicks_togglesFromThePendingValue() = runTest {
        val viewModel = loadedViewModel(contactDetails(isStarred = false))

        viewModel.onAction(ContactDetailsAction.StarClick)
        viewModel.onAction(ContactDetailsAction.StarClick)
        advanceUntilIdle()

        coVerifyOrder {
            contactActionsRepository.setStarred(LOOKUP_URI, true)
            contactActionsRepository.setStarred(LOOKUP_URI, false)
        }
    }

    @Test
    fun onAction_withRepeatedSendToVoicemailClicks_togglesFromThePendingValue() = runTest {
        val viewModel = loadedViewModel(contactDetails(isSendToVoicemail = false))

        viewModel.onAction(ContactDetailsAction.SendToVoicemailClick)
        viewModel.onAction(ContactDetailsAction.SendToVoicemailClick)
        advanceUntilIdle()

        coVerifyOrder {
            contactActionsRepository.setSendToVoicemail(LOOKUP_URI, true)
            contactActionsRepository.setSendToVoicemail(LOOKUP_URI, false)
        }
    }

    @Test
    fun onAction_withAJoinTargetPicked_joinsTheContacts() = runTest {
        val viewModel = loadedViewModel()

        viewModel.onAction(ContactDetailsAction.JoinTargetPicked(11L))
        advanceUntilIdle()

        coVerify {
            contactActionsRepository.joinContacts(
                contactId = CONTACT_ID_UNDER_TEST,
                otherContactId = 11L,
                callbackActivity = ContactDetailsActivity::class.java,
            )
        }
    }

    @Test
    fun onAction_withAJoinTargetPicked_showsTheLinkingProgress() = runTest {
        val viewModel = loadedViewModel()

        viewModel.uiState.test {
            awaitItem()
            viewModel.onAction(ContactDetailsAction.JoinTargetPicked(11L))

            assertEquals(ContactLinkOperation.LINK, awaitItem().linkProgress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val CONTACT_ID_UNDER_TEST = 7L
    }

    private fun editContactEffect(): ContactDetailsEffect {
        return ContactDetailsEffect.EditContact(lookupUri = LOOKUP_URI, photoId = 0L)
    }
}
