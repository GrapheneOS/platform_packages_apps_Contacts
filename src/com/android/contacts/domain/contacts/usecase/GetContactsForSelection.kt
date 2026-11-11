package com.android.contacts.domain.contacts.usecase

import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.contacts.model.ContactDataColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.contacts.model.DirectoryColumn
import com.android.contacts.data.contacts.model.LegacyContactColumn
import com.android.contacts.data.contacts.model.LegacyContactsKind
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.data.contacts.repository.DirectoriesRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.di.core.DefaultDispatcher
import com.android.contacts.domain.contacts.mapper.ContactDirectoryMapper
import com.android.contacts.domain.contacts.mapper.ContactItemMapper
import com.android.contacts.domain.contacts.model.ContactDirectoryResult
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.ContactSelectionFilter
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

internal fun interface GetContactsForSelection {
    operator fun invoke(
        filter: ContactSelectionFilter,
        searchQuery: String?,
    ): Flow<GetContactsForSelectionResult>
}

internal class GetContactsForSelectionImpl @Inject constructor(
    private val contactsRepository: ContactsRepository,
    private val directoriesRepository: DirectoriesRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val contactItemMapper: ContactItemMapper,
    private val contactDirectoryMapper: ContactDirectoryMapper,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : GetContactsForSelection {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun invoke(
        filter: ContactSelectionFilter,
        searchQuery: String?,
    ): Flow<GetContactsForSelectionResult> {
        return displaySettingsRepository.observeDisplaySettings()
            .map { it.sortOrder to it.displayOrder }
            .distinctUntilChanged()
            .flatMapLatest { (sortOrder, displayOrder) ->
                query(filter, searchQuery, sortOrder, displayOrder)
            }
            .flowOn(defaultDispatcher)
    }

    private fun query(
        filter: ContactSelectionFilter,
        searchQuery: String?,
        sortOrder: SortOrder,
        displayOrder: DisplayOrder,
    ): Flow<GetContactsForSelectionResult> {
        return when (filter) {
            is ContactSelectionFilter.Contacts ->
                queryContacts(
                    filter = filter,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    displayOrder = displayOrder,
                )

            is ContactSelectionFilter.JoinCandidates ->
                queryJoinCandidates(
                    targetContactId = filter.targetContactId,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    displayOrder = displayOrder,
                )

            is ContactSelectionFilter.PhoneNumbers ->
                contactsRepository.queryContactData(
                    kind = ContactDataKind.Phone(callableOnly = filter.callableOnly),
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    restrictToContactIds = filter.restrictToContactIds,
                ).toResult(displayOrder)

            is ContactSelectionFilter.Emails ->
                contactsRepository.queryContactData(
                    kind = ContactDataKind.Email,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                    restrictToContactIds = filter.restrictToContactIds,
                ).toResult(displayOrder)

            ContactSelectionFilter.PostalAddresses ->
                contactsRepository.queryContactData(
                    kind = ContactDataKind.PostalAddress,
                    searchQuery = searchQuery,
                    sortOrder = sortOrder,
                ).toResult(displayOrder)

            ContactSelectionFilter.Legacy.People ->
                contactsRepository
                    .queryLegacyContacts(LegacyContactsKind.PEOPLE, searchQuery)
                    .toResult()

            ContactSelectionFilter.Legacy.Phones ->
                contactsRepository
                    .queryLegacyContacts(LegacyContactsKind.PHONES, searchQuery)
                    .toResult()

            ContactSelectionFilter.Legacy.PostalAddresses ->
                contactsRepository
                    .queryLegacyContacts(LegacyContactsKind.POSTAL_ADDRESSES, searchQuery)
                    .toResult()
        }
    }

    private fun queryContacts(
        filter: ContactSelectionFilter.Contacts,
        searchQuery: String?,
        sortOrder: SortOrder,
        displayOrder: DisplayOrder,
    ): Flow<GetContactsForSelectionResult> {
        val contacts = contactsRepository.queryContacts(
            searchQuery = searchQuery,
            sortOrder = sortOrder,
            account = filter.account,
        ).map { columns ->
            columns
                .toContactItems(displayOrder)
                .excludeContacts(filter.excludeIds)
        }

        if (!filter.includesDirectories || filter.account != null || searchQuery == null) {
            return contacts.map { GetContactsForSelectionResult(contacts = it) }
        }

        return combine(
            contacts,
            queryDirectories(searchQuery, displayOrder),
        ) { localContacts, directories ->
            GetContactsForSelectionResult(
                contacts = localContacts,
                directories = directories,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun queryDirectories(
        searchQuery: String,
        displayOrder: DisplayOrder,
    ): Flow<List<ContactDirectoryResult>> {
        return directoriesRepository.observeRemote()
            .flatMapLatest { directories ->
                when {
                    directories.isEmpty() -> flowOf(emptyList())
                    else -> combine(
                        directories.map { directory ->
                            queryDirectory(directory, searchQuery, displayOrder)
                        },
                    ) { it.toList() }
                }
            }
    }

    private fun queryDirectory(
        directory: DirectoryColumn,
        searchQuery: String,
        displayOrder: DisplayOrder,
    ): Flow<ContactDirectoryResult> {
        val contactDirectory = contactDirectoryMapper.map(directory)
        return contactsRepository
            .queryDirectoryContacts(directory.id, searchQuery)
            .map { columns ->
                ContactDirectoryResult(
                    directory = contactDirectory,
                    contacts = columns.map { contactItemMapper.map(it, displayOrder) },
                )
            }
            .onStart { emit(ContactDirectoryResult(contactDirectory, isLoading = true)) }
    }

    private fun queryJoinCandidates(
        targetContactId: Long,
        searchQuery: String?,
        sortOrder: SortOrder,
        displayOrder: DisplayOrder,
    ): Flow<GetContactsForSelectionResult> {
        return combine(
            contactsRepository.queryContacts(
                searchQuery = searchQuery,
                sortOrder = sortOrder,
                excludedContactId = targetContactId,
            ),
            contactsRepository.queryJoinSuggestions(
                targetContactId = targetContactId,
                searchQuery = searchQuery,
            ),
        ) { contacts, suggestions ->
            GetContactsForSelectionResult(
                contacts = contacts.toContactItems(displayOrder),
                suggestions = suggestions.toContactItems(displayOrder),
            )
        }
    }

    private fun List<ContactColumn>.toContactItems(displayOrder: DisplayOrder): List<ContactItem> {
        return map { contactItemMapper.map(it, displayOrder) }
    }

    private fun List<ContactItem>.excludeContacts(excludeIds: Set<Long>): List<ContactItem> {
        return filterNot { it.key is ContactItemKey.Local && it.key.id in excludeIds }
    }

    private fun Flow<List<ContactDataColumn>>.toResult(
        displayOrder: DisplayOrder,
    ): Flow<GetContactsForSelectionResult> {
        return map { columns ->
            GetContactsForSelectionResult(
                contacts = columns.map { contactItemMapper.map(it, displayOrder) },
            )
        }
    }

    private fun Flow<List<LegacyContactColumn>>.toResult(): Flow<GetContactsForSelectionResult> {
        return map { columns ->
            GetContactsForSelectionResult(
                contacts = columns.map(contactItemMapper::map),
            )
        }
    }
}
