package com.android.contacts.ui.editor.springboard.screen

import android.content.ContentUris
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.android.contacts.domain.contacts.model.RawContactWithAccount
import com.android.contacts.domain.contacts.model.RawContactsResult
import com.android.contacts.domain.contacts.usecase.LoadRawContacts
import com.android.contacts.tests.AccountDisplayModelFactory
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.RawContactUiModelFactory
import com.android.contacts.tests.RawContactWithAccountFactory
import com.android.contacts.ui.editor.springboard.ContactEditorSpringBoardActivity.Companion.EXTRA_SHOW_READ_ONLY
import com.android.contacts.ui.editor.springboard.ContactEditorSpringBoardActivity.Companion.EXTRA_URI
import com.android.contacts.ui.editor.springboard.screen.mapper.RawContactUiModelMapper
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardAction as Action
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardEffect as Effect
import com.android.contacts.ui.editor.springboard.screen.model.ContactEditorSpringBoardUiState as State
import com.android.contacts.util.core.GetUriType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
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
internal class ContactEditorSpringBoardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getUriType = mockk<GetUriType>(relaxed = true)
    private val loadRawContacts = mockk<LoadRawContacts>(relaxed = true)
    private val rawContactUiModelMapper = mockk<RawContactUiModelMapper>(relaxed = true)

    @Before
    fun setUp() {
        mockkStatic(ContentUris::class)
        every { rawContactUiModelMapper.map(any(), any()) } returns RAW_CONTACT_UI_MODEL
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun whenUriIsNull_showErrorAndClose() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val viewModel = createViewModel(uri = null)

            viewModel.effects.test {
                assertEquals(Effect.ShowErrorAndClose, awaitItem())
            }
        }

    @Test
    fun whenUriIsForRawContact_openEditContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            every { uri.authority } returns ContactsContract.AUTHORITY
            every { ContentUris.parseId(any()) } returns 123L
            coEvery { getUriType(any()) } returns ContactsContract.RawContacts.CONTENT_ITEM_TYPE

            val viewModel = createViewModel(uri = uri)

            viewModel.effects.test {
                assertEquals(Effect.EditContact(uri, 123L), awaitItem())
            }
        }

    @Test
    fun whenUriIsLegacy_showErrorAndClose() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            every { uri.authority } returns android.provider.Contacts.AUTHORITY

            val viewModel = createViewModel(uri = uri)

            viewModel.effects.test {
                assertEquals(Effect.ShowErrorAndClose, awaitItem())
            }
        }

    @Test
    fun onContactUri_whenLoadRawContactsIsEmpty_createContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            givenResult(buildResult(contacts = emptyList()))

            val viewModel = createViewModel(uri = uri)

            viewModel.effects.test {
                assertEquals(Effect.CreateContact(uri), awaitItem())
            }
            verify { loadRawContacts(contactUri = uri, any()) }
        }

    @Test
    fun onContactUri_whenShowReadOnlyIsTrue_showLinkedContacts() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val contact = RawContactWithAccountFactory.build()
            val result = givenResult(buildResult(contacts = listOf(contact)))

            val viewModel = createViewModel(uri = uri, showReadOnly = true)

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(
                    State.ShowLinkedContacts(
                        persistentListOf(RAW_CONTACT_UI_MODEL),
                        result.isUserProfile,
                    ),
                    expectMostRecentItem(),
                )
            }
            verify { loadRawContacts(contactUri = uri, onlyWritable = false) }
            verify { rawContactUiModelMapper.map(contact, result.isUserProfile) }
        }

    @Test
    fun onContactUri_whenShowReadOnlyIsFalseAndThereAreMultipleContacts_showPickContactToEdit() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val accountWritable = AccountDisplayModelFactory.build(areContactsWritable = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L, account = accountWritable)
            val contactUi1 = RAW_CONTACT_UI_MODEL
            val contact2 = RawContactWithAccountFactory.build(id = 2L, account = accountWritable)
            val contactUi2 = RAW_CONTACT_UI_MODEL.copy(id = 2L)
            val result = givenResult(buildResult(contacts = listOf(contact1, contact2)))

            every { rawContactUiModelMapper.map(contact1, any()) } returns contactUi1
            every { rawContactUiModelMapper.map(contact2, any()) } returns contactUi2

            val viewModel = createViewModel(uri = uri, showReadOnly = false)

            viewModel.uiState.test {
                advanceUntilIdle()
                assertEquals(
                    State.ShowPickContactToEdit(persistentListOf(contactUi1, contactUi2)),
                    expectMostRecentItem(),
                )
            }
            verify { loadRawContacts(contactUri = uri, onlyWritable = true) }
            verify { rawContactUiModelMapper.map(contact1, result.isUserProfile) }
            verify { rawContactUiModelMapper.map(contact2, result.isUserProfile) }
        }

    @Test
    fun onContactUri_whenShowReadOnlyIsFalseAndThereIsOneWritableContact_editContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val accountWritable = AccountDisplayModelFactory.build(areContactsWritable = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L, account = accountWritable)
            givenResult(buildResult(contacts = listOf(contact1)))

            val viewModel = createViewModel(uri = uri, showReadOnly = false)

            viewModel.effects.test {
                assertEquals(
                    Effect.EditContact(uri, contact1.id),
                    awaitItem(),
                )
            }
            verify { loadRawContacts(contactUri = uri, onlyWritable = true) }
            verify(exactly = 0) { rawContactUiModelMapper.map(contact1, any()) }
        }

    @Test
    fun onContactUri_whenShowReadOnlyIsFalseAndNoWritableContacts_createContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val accountReadOnly = AccountDisplayModelFactory.build(areContactsWritable = false)
            val contact1 = RawContactWithAccountFactory.build(id = 1L, account = accountReadOnly)
            givenResult(buildResult(contacts = listOf(contact1)))

            val viewModel = createViewModel(uri = uri, showReadOnly = false)

            viewModel.effects.test {
                assertEquals(Effect.CreateContact(uri), awaitItem())
            }
            verify { loadRawContacts(contactUri = uri, onlyWritable = true) }
            verify(exactly = 0) { rawContactUiModelMapper.map(contact1, any()) }
        }

    @Test
    fun onAddClick_selectContactToJoin() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L)
            val result = givenResult(buildResult(contacts = listOf(contact1)))

            val viewModel = createViewModel(uri = uri, showReadOnly = true)

            viewModel.effects.test {
                advanceUntilIdle()
                viewModel.onAction(Action.AddClicked)
                assertEquals(Effect.SelectContactToJoin(result.contactId), awaitItem())
            }
        }

    @Test
    fun onContactClicked_editContact() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L)
            givenResult(buildResult(contacts = listOf(contact1)))

            val viewModel = createViewModel(uri = uri, showReadOnly = true)

            viewModel.effects.test {
                advanceUntilIdle()
                viewModel.onAction(Action.ContactClicked(contact1.id))
                assertEquals(Effect.EditContact(uri, contact1.id), awaitItem())
            }
        }

    @Test
    fun onAddContactSelected_joinContacts() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L)
            val result = givenResult(buildResult(contacts = listOf(contact1)))

            val contact2Id = 123L
            val contact2Uri = mockk<Uri>(relaxed = true)
            every { ContentUris.parseId(contact2Uri) } returns contact2Id

            val viewModel = createViewModel(uri = uri, showReadOnly = true)

            viewModel.effects.test {
                advanceUntilIdle()
                viewModel.onAction(Action.AddContactSelected(contact2Uri))
                assertEquals(Effect.JoinContacts(result.contactId, contact2Id), awaitItem())
            }
        }

    @Test
    fun onUnlinkClicked_showUnlinkConfirmation_thenConfirmToUnlinkRawContacts() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L)
            val contact2 = RawContactWithAccountFactory.build(id = 2L)
            givenResult(buildResult(contacts = listOf(contact1, contact2)))

            val viewModel = createViewModel(uri = uri, showReadOnly = true)

            viewModel.uiState.test {
                advanceUntilIdle()
                viewModel.onAction(Action.UnlinkClicked)
                advanceUntilIdle()
                assertEquals(State.ShowUnlinkConfirmation, expectMostRecentItem())
                viewModel.onAction(Action.UnlinkConfirmed)
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.effects.test {
                assertEquals(
                    Effect.UnlinkRawContacts(listOf(contact1.id, contact2.id)),
                    expectMostRecentItem(),
                )
            }
        }

    @Test
    fun onUnlinkClicked_showUnlinkConfirmation_thenDismissToClose() =
        runTest(context = mainDispatcherRule.testDispatcher) {
            val uri = mockk<Uri>(relaxed = true)
            val contact1 = RawContactWithAccountFactory.build(id = 1L)
            val contact2 = RawContactWithAccountFactory.build(id = 2L)
            givenResult(buildResult(contacts = listOf(contact1, contact2)))

            val viewModel = createViewModel(uri = uri, showReadOnly = true)

            viewModel.uiState.test {
                advanceUntilIdle()
                viewModel.onAction(Action.UnlinkClicked)
                advanceUntilIdle()
                assertEquals(State.ShowUnlinkConfirmation, expectMostRecentItem())
                viewModel.onAction(Action.UnlinkDismissed)
                cancelAndIgnoreRemainingEvents()
            }
            viewModel.effects.test {
                assertEquals(Effect.Close, expectMostRecentItem())
            }
        }

    private fun createViewModel(
        uri: Uri? = "content://contact/1".toUri(),
        showReadOnly: Boolean? = null,
        result: RawContactsResult? = null,
    ): ContactEditorSpringBoardScreenModel {
        return ContactEditorSpringBoardViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    EXTRA_URI to uri,
                    EXTRA_SHOW_READ_ONLY to showReadOnly,
                    ContactEditorSpringBoardViewModel.KEY_RESULT to result,
                ),
            ),
            getUriType = getUriType,
            loadRawContacts = loadRawContacts,
            rawContactUiModelMapper = rawContactUiModelMapper,
        )
    }

    private fun givenResult(result: RawContactsResult): RawContactsResult {
        every { loadRawContacts(any(), any()) } returns flowOf(result)
        return result
    }

    private fun buildResult(contacts: List<RawContactWithAccount>): RawContactsResult {
        return RawContactsResult(
            contactId = 999L,
            isUserProfile = false,
            rawContacts = contacts,
        )
    }

    private companion object {
        val RAW_CONTACT_UI_MODEL = RawContactUiModelFactory.build()
    }
}
