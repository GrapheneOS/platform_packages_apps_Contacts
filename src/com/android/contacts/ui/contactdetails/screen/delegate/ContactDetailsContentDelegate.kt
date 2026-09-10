@file:OptIn(ExperimentalCoroutinesApi::class)

package com.android.contacts.ui.contactdetails.screen.delegate

import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.contactdetails.model.ContactDetailsResult
import com.android.contacts.data.contactdetails.model.LoadedContact
import com.android.contacts.data.contactdetails.repository.ContactDetailsRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.domain.calllog.model.RecentCall
import com.android.contacts.domain.calllog.usecase.GetRecentCalls
import com.android.contacts.domain.contactdetails.model.ContactDetailsCards
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.model.ContactQuickAction
import com.android.contacts.domain.contactdetails.usecase.BuildContactDetailsCards
import com.android.contacts.domain.contactdetails.usecase.GetContactDetailsMenu
import com.android.contacts.domain.contactdetails.usecase.GetContactQuickActions
import com.android.contacts.domain.telecom.model.CallingSimOptions
import com.android.contacts.domain.telecom.usecase.GetCallingSimOptions
import com.android.contacts.ui.contactdetails.screen.mapper.ContactDetailsUiStateMapper
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsArguments
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsContent as Content
import com.android.contacts.ui.contactdetails.screen.model.PendingContactFlags
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal interface ContactDetailsContentDelegate {
    val content: StateFlow<Content>
    val loadedDetails: StateFlow<ContactDetails?>
    val loadedContact: StateFlow<LoadedContact?>

    fun bind(
        scope: CoroutineScope,
        arguments: Flow<ContactDetailsArguments>,
    )
}

internal class ContactDetailsContentDelegateImpl @Inject constructor(
    private val contactDetailsRepository: ContactDetailsRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val buildContactDetailsCards: BuildContactDetailsCards,
    private val getContactDetailsMenu: GetContactDetailsMenu,
    private val getContactQuickActions: GetContactQuickActions,
    private val getRecentCalls: GetRecentCalls,
    private val getCallingSimOptions: GetCallingSimOptions,
    private val contactDetailsUiStateMapper: ContactDetailsUiStateMapper,
    private val contactFlagsDelegate: ContactFlagsDelegate,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ContactDetailsContentDelegate {

    private val _content = MutableStateFlow<Content>(Content.Loading)
    override val content: StateFlow<Content> = _content.asStateFlow()

    private val _loadedDetails = MutableStateFlow<ContactDetails?>(null)
    override val loadedDetails: StateFlow<ContactDetails?> = _loadedDetails.asStateFlow()

    private val _loadedContact = MutableStateFlow<LoadedContact?>(null)
    override val loadedContact: StateFlow<LoadedContact?> = _loadedContact.asStateFlow()

    private var isBound = false

    override fun bind(
        scope: CoroutineScope,
        arguments: Flow<ContactDetailsArguments>,
    ) {
        if (isBound) {
            return
        }

        isBound = true

        arguments
            .flatMapLatest { contactArguments -> observeContent(contactArguments) }
            .onEach { loadedContent -> _content.value = loadedContent }
            .launchIn(scope)
    }

    private fun observeContent(arguments: ContactDetailsArguments): Flow<Content> {
        val derivedDetails = contactDetailsRepository
            .observeContactDetails(arguments.lookupUri, arguments.excludedMimeTypes)
            .map { result -> derive(result, arguments) }

        return combine(
            derivedDetails,
            contactFlagsDelegate.pendingFlags,
            displaySettingsRepository.observeDisplaySettings(),
        ) { derived, pending, displaySettings ->
            retainLoadedContact(derived)

            toContent(
                derived = derived,
                pending = pending,
                displayOrder = displaySettings.displayOrder,
            )
        }.flowOn(ioDispatcher)
    }

    private suspend fun derive(
        result: ContactDetailsResult,
        arguments: ContactDetailsArguments,
    ): DerivedContactDetails {
        return when (result) {
            is ContactDetailsResult.NotFound -> DerivedContactDetails.NotFound
            is ContactDetailsResult.Error -> DerivedContactDetails.Error

            is ContactDetailsResult.Loaded -> {
                val details = result.details

                DerivedContactDetails.Loaded(
                    details = details,
                    source = result.source,
                    cards = buildContactDetailsCards(details, arguments.prioritizedMimeType),
                    quickActions = getContactQuickActions(details),
                    recentCalls = getRecentCalls(details),
                    callingSimOptions = getCallingSimOptions(details),
                    menu = getContactDetailsMenu(details.capabilities),
                )
            }
        }
    }

    private fun toContent(
        derived: DerivedContactDetails,
        pending: PendingContactFlags,
        displayOrder: DisplayOrder,
    ): Content {
        return when (derived) {
            is DerivedContactDetails.NotFound -> Content.NotFound
            is DerivedContactDetails.Error -> Content.Error

            is DerivedContactDetails.Loaded -> contactDetailsUiStateMapper.map(
                details = pending.applyTo(derived.details),
                cards = derived.cards,
                quickActions = derived.quickActions,
                recentCalls = derived.recentCalls,
                callingSimOptions = derived.callingSimOptions,
                menu = derived.menu,
                displayOrder = displayOrder,
            )
        }
    }

    private fun retainLoadedContact(derived: DerivedContactDetails) {
        val loaded = derived as? DerivedContactDetails.Loaded ?: return

        _loadedDetails.value = loaded.details
        _loadedContact.value = loaded.source
        contactFlagsDelegate.clearApplied(loaded.details)
    }
}

private sealed interface DerivedContactDetails {

    data object NotFound : DerivedContactDetails

    data object Error : DerivedContactDetails

    data class Loaded(
        val details: ContactDetails,
        val source: LoadedContact,
        val cards: ContactDetailsCards,
        val quickActions: List<ContactQuickAction>,
        val recentCalls: List<RecentCall>,
        val callingSimOptions: CallingSimOptions?,
        val menu: ContactDetailsMenu,
    ) : DerivedContactDetails
}
