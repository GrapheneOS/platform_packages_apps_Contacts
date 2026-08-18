package com.android.contacts.ui.contactdetails.screen.contactdetailsviewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.ContactLinkOperation
import com.android.contacts.data.contactdetails.repository.ContactActionsRepository
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.contactdetails.repository.ContactShortcutRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.usecase.BuildContactDetailsCards
import com.android.contacts.domain.contactdetails.usecase.GetContactDetailsMenu
import com.android.contacts.domain.contactdetails.usecase.GetContactQuickActions
import com.android.contacts.tests.MainDispatcherRule
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsMenu
import com.android.contacts.ui.contactdetails.ContactDetailsActivity
import com.android.contacts.ui.contactdetails.screen.ContactDetailsViewModel
import com.android.contacts.ui.contactdetails.screen.mapper.ContactDetailsUiStateMapper
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
internal abstract class BaseContactDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    protected val contactDetailsRepository = mockk<ContactDetailsRepository>(relaxed = true)
    protected val contactActionsRepository = mockk<ContactActionsRepository>(relaxed = true)
    protected val contactShortcutRepository = mockk<ContactShortcutRepository>(relaxed = true)
    protected val buildContactDetailsCards = mockk<BuildContactDetailsCards>()
    protected val getContactDetailsMenu = mockk<GetContactDetailsMenu>()
    protected val getContactQuickActions = mockk<GetContactQuickActions>()
    protected val displaySettingsRepository = mockk<DisplaySettingsRepository>()
    protected val contactDetailsUiStateMapper = mockk<ContactDetailsUiStateMapper>()

    protected val results = MutableSharedFlow<ContactDetailsResult>(replay = 1)
    protected val linkOperations = MutableSharedFlow<ContactLinkOperation>()
    protected val savedStateHandle = SavedStateHandle()

    protected val loadedState = MutableStateFlow(LOADED_CONTENT)

    @Before
    fun setUpBase() {
        every { contactDetailsRepository.observeContactDetails(any(), any()) } returns results
        every { contactActionsRepository.observeLinkOperations() } returns linkOperations
        every { contactActionsRepository.getPendingLinkOperation() } returns null
        every { buildContactDetailsCards(any(), any()) } returns EMPTY_CARDS
        every { getContactDetailsMenu(any()) } returns contactDetailsMenu()
        every { getContactQuickActions(any()) } returns emptyList()
        every { displaySettingsRepository.observeDisplaySettings() } returns flowOf(DISPLAY_SETTINGS)
        every {
            contactDetailsUiStateMapper.map(any(), any(), any(), any(), any())
        } answers { loadedState.value }
    }

    protected fun createViewModel(): ContactDetailsViewModel {
        return ContactDetailsViewModel(
            savedStateHandle = savedStateHandle,
            contactDetailsRepository = contactDetailsRepository,
            contactActionsRepository = contactActionsRepository,
            buildContactDetailsCards = buildContactDetailsCards,
            getContactDetailsMenu = getContactDetailsMenu,
            getContactQuickActions = getContactQuickActions,
            contactDetailsUiStateMapper = contactDetailsUiStateMapper,
            contactShortcutRepository = contactShortcutRepository,
            displaySettingsRepository = displaySettingsRepository,
        )
    }

    protected fun TestScope.loadedViewModel(
        details: ContactDetails = contactDetails(lookupUri = LOOKUP_URI),
    ): ContactDetailsViewModel {
        val viewModel = createViewModel().bindContact()

        viewModel.uiState.launchIn(backgroundScope)
        results.tryEmit(ContactDetailsResult.Loaded(details))
        advanceUntilIdle()

        return viewModel
    }

    protected fun ContactDetailsViewModel.bindContact(): ContactDetailsViewModel {
        bind(
            lookupUri = LOOKUP_URI,
            excludedMimeTypes = emptySet(),
            prioritizedMimeType = null,
            callbackActivity = ContactDetailsActivity::class.java,
        )

        return this
    }

    protected suspend fun emitLoaded(details: ContactDetails = contactDetails()) {
        results.emit(ContactDetailsResult.Loaded(details))
    }

    protected companion object {
        val LOOKUP_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")

        val DISPLAY_SETTINGS = DisplaySettings(
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            isSortOrderChangeable = true,
            displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
            isDisplayOrderChangeable = true,
            phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
            isPhoneticNameDisplayChangeable = true,
        )

        val EMPTY_CARDS = ContactDetailsCards(
            contactCard = emptyList(),
            notes = emptyList(),
            headerNickname = null,
            headerOrganizationParts = emptyList(),
            groups = emptyList(),
        )

        val LOADED_CONTENT = ContactDetailsContent.Loaded(
            groups = persistentListOf(),
            header = mockk(relaxed = true),
            quickActions = persistentListOf(),
            contactCard = persistentListOf(),
            notes = persistentListOf(),
            settings = persistentListOf(),
            emptyPrompt = null,
            menu = contactDetailsMenu(),
            isStarred = false,
        )
    }
}
