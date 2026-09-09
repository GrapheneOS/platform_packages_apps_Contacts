package com.android.contacts.ui.interactions.showorcreate

import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.data.contacts.model.ContactLookupQuery
import com.android.contacts.data.contacts.model.ContactLookupResult
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.ui.interactions.showorcreate.screen.ShowOrCreateViewModel
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateAction as Action
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateEffect as Effect
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateUiState as State
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShowOrCreateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val contactsRepository = mockk<ContactsRepository>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(Uri::class)
        val uriStringSlot = slot<String>()
        every { Uri.parse(capture(uriStringSlot)) } returns
            mockk<Uri>(relaxed = true) {
                every { scheme } answers {
                    uriStringSlot.captured.split(":").firstOrNull()
                }
                every { schemeSpecificPart } answers {
                    uriStringSlot.captured.split(":").getOrNull(1)
                }
            }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun whenTheSchemeIsInvalid_close() =
        runTest(mainDispatcherRule.testDispatcher) {
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "invalid".toUri(),
                ),
            )
            subject.effects.test {
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun whenTelSchemeIsProvided_lookupContactsByPhone() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { contactsRepository.lookup(any()) } returns flowOf(emptyList())
            val phone = "123456789"
            createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "tel:$phone".toUri(),
                ),
            )
            advanceUntilIdle()
            verify { contactsRepository.lookup(ContactLookupQuery.Phone(phone)) }
        }

    @Test
    fun whenMailtoSchemeIsProvided_lookupContactsByEmail() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { contactsRepository.lookup(any()) } returns flowOf(emptyList())
            val email = "user@example.org"
            createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:$email".toUri(),
                ),
            )
            advanceUntilIdle()
            verify { contactsRepository.lookup(ContactLookupQuery.Email(email)) }
        }

    @Test
    fun whenOneContactIsFound_openIt() =
        runTest(mainDispatcherRule.testDispatcher) {
            val contactUri = mockk<Uri>()
            val contactResult = ContactLookupResult(1, "1", contactUri)
            every { contactsRepository.lookup(any()) } returns flowOf(listOf(contactResult))
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:user@example.org".toUri(),
                ),
            )
            subject.effects.test {
                advanceUntilIdle()
                assertEquals(Effect.ShowContact(contactUri), awaitItem())
            }
        }

    @Test
    fun whenMultipleContactsAreFound_openList() =
        runTest(mainDispatcherRule.testDispatcher) {
            val contactResult1 = ContactLookupResult(1, "1", mockk<Uri>())
            val contactResult2 = ContactLookupResult(2, "2", mockk<Uri>())
            val results = listOf(contactResult1, contactResult2)
            every { contactsRepository.lookup(any()) } returns flowOf(results)
            val email = "user@example.org"
            val bundle = mockk<Bundle>(relaxed = true) {
                every { deepCopy() } returns this@mockk
            }
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:$email".toUri(),
                    ShowOrCreateViewModel.EXTRA_EXTRAS to bundle,
                ),
            )
            subject.effects.test {
                advanceUntilIdle()
                assertEquals(
                    Effect.ShowContactList::class.java,
                    awaitItem().javaClass,
                )
            }
            verify { bundle.putString(ContactsContract.Intents.Insert.EMAIL, email) }
        }

    @Test
    fun whenNoContactIsFound_showDialog() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { contactsRepository.lookup(any()) } returns flowOf(emptyList())
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:user@example.org".toUri(),
                ),
            )
            advanceUntilIdle()
            assertEquals(
                State.ConfirmingCreate::class.java,
                subject.uiState.value.javaClass,
            )
        }

    @Test
    fun whenCreateIsConfirmed_openCreateOrEditContact() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { contactsRepository.lookup(any()) } returns flowOf(emptyList())
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:user@example.org".toUri(),
                ),
            )
            subject.effects.test {
                advanceUntilIdle()
                subject.onAction(Action.CreateConfirm)
                advanceUntilIdle()
                assertEquals(
                    Effect.CreateOrEditContact::class.java,
                    awaitItem().javaClass,
                )
            }
        }

    @Test
    fun whenCreateIsDismissed_close() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { contactsRepository.lookup(any()) } returns flowOf(emptyList())
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:user@example.org".toUri(),
                ),
            )
            subject.effects.test {
                advanceUntilIdle()
                subject.onAction(Action.CreateDismiss)
                advanceUntilIdle()
                assertEquals(Effect.Close, awaitItem())
            }
        }

    @Test
    fun whenNoContactIsFoundAndForceCreate_openCreateContact() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { contactsRepository.lookup(any()) } returns flowOf(emptyList())
            val subject = createViewModel(
                savedState = mapOf(
                    ShowOrCreateViewModel.EXTRA_DATA to "mailto:user@example.org".toUri(),
                    ContactsContract.Intents.EXTRA_FORCE_CREATE to true,
                ),
            )
            subject.effects.test {
                advanceUntilIdle()
                assertEquals(
                    Effect.CreateContact::class.java,
                    awaitItem().javaClass,
                )
            }
        }

    private fun createViewModel(
        savedState: Map<String, Any?>,
    ) = ShowOrCreateViewModel(
        savedStateHandle = SavedStateHandle(savedState),
        contactsRepository = contactsRepository,
    )
}
