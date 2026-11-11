package com.android.contacts.domain.contacts.usecase

import app.cash.turbine.test
import com.android.contacts.data.contacts.model.ContactColumn
import com.android.contacts.data.contacts.model.ContactDataColumn
import com.android.contacts.data.contacts.model.ContactDataKind
import com.android.contacts.data.contacts.model.DirectoryColumn
import com.android.contacts.data.contacts.model.DirectoryContactColumn
import com.android.contacts.data.contacts.model.LegacyContactColumn
import com.android.contacts.data.contacts.model.LegacyContactsKind
import com.android.contacts.data.contacts.repository.ContactsRepository
import com.android.contacts.data.contacts.repository.DirectoriesRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.domain.contacts.mapper.ContactDirectoryMapperImpl
import com.android.contacts.domain.contacts.mapper.ContactItemMapperImpl
import com.android.contacts.domain.contacts.model.ContactDirectory
import com.android.contacts.domain.contacts.model.ContactDirectoryResult
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.ContactSelectionFilter
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import com.android.contacts.tests.factory.AccountModelFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class GetContactsForSelectionTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val contactsRepository = mockk<ContactsRepository>()
    private val directoriesRepository = mockk<DirectoriesRepository>()
    private val displaySettingsRepository = mockk<DisplaySettingsRepository>()
    private val displaySettings = MutableStateFlow(DISPLAY_SETTINGS)

    private val subject: GetContactsForSelection = GetContactsForSelectionImpl(
        contactsRepository = contactsRepository,
        directoriesRepository = directoriesRepository,
        displaySettingsRepository = displaySettingsRepository,
        contactItemMapper = ContactItemMapperImpl(),
        contactDirectoryMapper = ContactDirectoryMapperImpl(),
        defaultDispatcher = dispatcher,
    )

    @Before
    fun setUp() {
        every { displaySettingsRepository.observeDisplaySettings() } returns displaySettings
        every {
            contactsRepository.queryContacts(any(), any(), any(), any())
        } returns flowOf(emptyList())
        every { contactsRepository.queryJoinSuggestions(any(), any()) } returns flowOf(emptyList())
        every {
            contactsRepository.queryContactData(any(), any(), any(), any())
        } returns flowOf(emptyList())
        every { contactsRepository.queryLegacyContacts(any(), any()) } returns flowOf(emptyList())
        every { directoriesRepository.observeRemote() } returns flowOf(emptyList())
    }

    // Contacts

    @Test
    fun contacts_queriesContactsWithParameters() = runTest(dispatcher) {
        val account = AccountModelFactory.build()
        displaySettings.update { it.copy(sortOrder = SortOrder.FAMILY_NAME_FIRST) }

        subject(ContactSelectionFilter.Contacts(account = account), QUERY).first()

        verify {
            contactsRepository.queryContacts(
                searchQuery = QUERY,
                sortOrder = SortOrder.FAMILY_NAME_FIRST,
                account = account,
            )
        }
    }

    @Test
    fun contacts_mapsContactsWithDisplayOrder() = runTest(dispatcher) {
        displaySettings.update { it.copy(displayOrder = DisplayOrder.FAMILY_NAME_FIRST) }
        every {
            contactsRepository.queryContacts(any(), any(), any(), any())
        } returns flowOf(listOf(contactColumn(1L)))

        val result = subject(ContactSelectionFilter.Contacts(), null).first()

        assertEquals(
            GetContactsForSelectionResult(
                contacts = listOf(contactItem(1L, DisplayOrder.FAMILY_NAME_FIRST)),
            ),
            result,
        )
    }

    @Test
    fun contacts_excludesContactsWithExcludedIds() = runTest(dispatcher) {
        every {
            contactsRepository.queryContacts(any(), any(), any(), any())
        } returns flowOf(listOf(contactColumn(1L), contactColumn(2L), contactColumn(3L)))

        val result = subject(
            ContactSelectionFilter.Contacts(excludeIds = setOf(1L, 3L)),
            null,
        ).first()

        assertEquals(listOf(contactItem(2L)), result.contacts)
    }

    @Test
    fun contacts_withoutIncludesDirectories_doesNotQueryDirectories() = runTest(dispatcher) {
        val result = subject(ContactSelectionFilter.Contacts(), QUERY).first()

        assertEquals(emptyList<ContactDirectoryResult>(), result.directories)
        verify(exactly = 0) { directoriesRepository.observeRemote() }
    }

    @Test
    fun contacts_withDirectoriesAndNullQuery_doesNotQueryDirectories() = runTest(dispatcher) {
        subject(ContactSelectionFilter.Contacts(includesDirectories = true), null).first()

        verify(exactly = 0) { directoriesRepository.observeRemote() }
    }

    @Test
    fun contacts_withDirectoriesAndAccount_doesNotQueryDirectories() = runTest(dispatcher) {
        val filter = ContactSelectionFilter.Contacts(
            account = AccountModelFactory.build(),
            includesDirectories = true,
        )

        subject(filter, QUERY).first()

        verify(exactly = 0) { directoriesRepository.observeRemote() }
    }

    @Test
    fun contacts_withDirectoriesAndNoRemoteDirectories_returnsNoDirectories() =
        runTest(dispatcher) {
            every {
                contactsRepository.queryContacts(any(), any(), any(), any())
            } returns flowOf(listOf(contactColumn(1L)))

            val result = subject(DIRECTORIES_FILTER, QUERY).first()

            assertEquals(
                GetContactsForSelectionResult(contacts = listOf(contactItem(1L))),
                result,
            )
        }

    @Test
    fun contacts_withDirectories_emitsLoadingThenDirectoryContacts() = runTest(dispatcher) {
        every {
            contactsRepository.queryContacts(any(), any(), any(), any())
        } returns flowOf(listOf(contactColumn(1L)))
        every { directoriesRepository.observeRemote() } returns flowOf(listOf(DIRECTORY_COLUMN))
        val directoryContacts = MutableSharedFlow<List<DirectoryContactColumn>>(replay = 1)
        every {
            contactsRepository.queryDirectoryContacts(DIRECTORY_COLUMN.id, QUERY)
        } returns directoryContacts

        subject(DIRECTORIES_FILTER, QUERY).test {
            assertEquals(
                GetContactsForSelectionResult(
                    contacts = listOf(contactItem(1L)),
                    directories = listOf(
                        ContactDirectoryResult(directory = DIRECTORY, isLoading = true),
                    ),
                ),
                awaitItem(),
            )

            directoryContacts.emit(listOf(directoryContactColumn(DIRECTORY_COLUMN.id, 7L)))

            assertEquals(
                GetContactsForSelectionResult(
                    contacts = listOf(contactItem(1L)),
                    directories = listOf(
                        ContactDirectoryResult(
                            directory = DIRECTORY,
                            contacts = listOf(directoryContactItem(DIRECTORY_COLUMN.id, 7L)),
                        ),
                    ),
                ),
                awaitItem(),
            )
        }
    }

    @Test
    fun contacts_withMultipleDirectories_returnsResultPerDirectoryInOrder() =
        runTest(dispatcher) {
            val otherDirectoryColumn = DirectoryColumn(id = 20L, displayName = "Other")
            every {
                directoriesRepository.observeRemote()
            } returns flowOf(listOf(DIRECTORY_COLUMN, otherDirectoryColumn))
            every {
                contactsRepository.queryDirectoryContacts(any(), QUERY)
            } answers {
                val directoryId = firstArg<Long>()
                flowOf(listOf(directoryContactColumn(directoryId, directoryId + 1)))
            }

            val result = subject(DIRECTORIES_FILTER, QUERY).first { result ->
                result.directories.none { it.isLoading }
            }

            assertEquals(
                listOf(
                    ContactDirectoryResult(
                        directory = DIRECTORY,
                        contacts = listOf(directoryContactItem(DIRECTORY_COLUMN.id, 11L)),
                    ),
                    ContactDirectoryResult(
                        directory = ContactDirectory(id = 20L, displayName = "Other"),
                        contacts = listOf(directoryContactItem(20L, 21L)),
                    ),
                ),
                result.directories,
            )
        }

    @Test
    fun contacts_withDirectories_mapsDirectoryContactsWithDisplayOrder() = runTest(dispatcher) {
        displaySettings.update { it.copy(displayOrder = DisplayOrder.FAMILY_NAME_FIRST) }
        every { directoriesRepository.observeRemote() } returns flowOf(listOf(DIRECTORY_COLUMN))
        every {
            contactsRepository.queryDirectoryContacts(any(), any())
        } returns flowOf(listOf(directoryContactColumn(DIRECTORY_COLUMN.id, 7L)))

        val result = subject(DIRECTORIES_FILTER, QUERY).first { result ->
            result.directories.none { it.isLoading }
        }

        assertEquals(
            listOf(directoryContactItem(DIRECTORY_COLUMN.id, 7L, DisplayOrder.FAMILY_NAME_FIRST)),
            result.directories.single().contacts,
        )
    }

    // JoinCandidates

    @Test
    fun joinCandidates_returnsContactsAndSuggestions() = runTest(dispatcher) {
        displaySettings.update {
            it.copy(
                sortOrder = SortOrder.FAMILY_NAME_FIRST,
                displayOrder = DisplayOrder.FAMILY_NAME_FIRST,
            )
        }
        every {
            contactsRepository.queryContacts(QUERY, SortOrder.FAMILY_NAME_FIRST, null, 5L)
        } returns flowOf(listOf(contactColumn(1L)))
        every {
            contactsRepository.queryJoinSuggestions(5L, QUERY)
        } returns flowOf(listOf(contactColumn(2L)))

        val result = subject(ContactSelectionFilter.JoinCandidates(targetContactId = 5L), QUERY)
            .first()

        assertEquals(
            GetContactsForSelectionResult(
                contacts = listOf(contactItem(1L, DisplayOrder.FAMILY_NAME_FIRST)),
                suggestions = listOf(contactItem(2L, DisplayOrder.FAMILY_NAME_FIRST)),
            ),
            result,
        )
    }

    // Contact data

    @Test
    fun phoneNumbers_queriesPhoneData() = runTest(dispatcher) {
        val filter = ContactSelectionFilter.PhoneNumbers(
            callableOnly = true,
            restrictToContactIds = setOf(1L, 2L),
        )
        every {
            contactsRepository.queryContactData(
                kind = ContactDataKind.Phone(callableOnly = true),
                searchQuery = QUERY,
                sortOrder = SortOrder.GIVEN_NAME_FIRST,
                restrictToContactIds = setOf(1L, 2L),
            )
        } returns flowOf(listOf(contactDataColumn(1L, ContactDataKind.Phone(true), "+1 555 0100")))

        val result = subject(filter, QUERY).first()

        assertEquals(
            GetContactsForSelectionResult(
                contacts = listOf(
                    contactDataItem(1L, ContactItem.FieldKind.PHONE, "+1 555 0100"),
                ),
            ),
            result,
        )
    }

    @Test
    fun emails_queriesEmailData() = runTest(dispatcher) {
        val filter = ContactSelectionFilter.Emails(restrictToContactIds = setOf(3L))
        every {
            contactsRepository.queryContactData(
                kind = ContactDataKind.Email,
                searchQuery = QUERY,
                sortOrder = SortOrder.GIVEN_NAME_FIRST,
                restrictToContactIds = setOf(3L),
            )
        } returns flowOf(listOf(contactDataColumn(3L, ContactDataKind.Email, "john@example.com")))

        val result = subject(filter, QUERY).first()

        assertEquals(
            listOf(contactDataItem(3L, ContactItem.FieldKind.EMAIL, "john@example.com")),
            result.contacts,
        )
    }

    @Test
    fun postalAddresses_queriesPostalAddressData() = runTest(dispatcher) {
        val address = "1600 Amphitheatre Parkway"
        every {
            contactsRepository.queryContactData(
                kind = ContactDataKind.PostalAddress,
                searchQuery = QUERY,
                sortOrder = SortOrder.GIVEN_NAME_FIRST,
                restrictToContactIds = null,
            )
        } returns flowOf(listOf(contactDataColumn(4L, ContactDataKind.PostalAddress, address)))

        val result = subject(ContactSelectionFilter.PostalAddresses, QUERY).first()

        assertEquals(
            listOf(contactDataItem(4L, ContactItem.FieldKind.POSTAL_ADDRESS, address)),
            result.contacts,
        )
    }

    @Test
    fun contactData_mapsWithDisplayOrder() = runTest(dispatcher) {
        displaySettings.update { it.copy(displayOrder = DisplayOrder.FAMILY_NAME_FIRST) }
        every {
            contactsRepository.queryContactData(any(), any(), any(), any())
        } returns flowOf(listOf(contactDataColumn(1L, ContactDataKind.Email, "a@example.com")))

        val result = subject(ContactSelectionFilter.Emails(), null).first()

        assertEquals(alternativeName(1L), result.contacts.single().name)
    }

    // Legacy

    @Test
    fun legacyFilters_queryLegacyContactsWithKind() = runTest(dispatcher) {
        val expected = mapOf(
            ContactSelectionFilter.Legacy.People to LegacyContactsKind.PEOPLE,
            ContactSelectionFilter.Legacy.Phones to LegacyContactsKind.PHONES,
            ContactSelectionFilter.Legacy.PostalAddresses to LegacyContactsKind.POSTAL_ADDRESSES,
        )

        expected.forEach { (filter, kind) ->
            every {
                contactsRepository.queryLegacyContacts(kind, QUERY)
            } returns flowOf(
                listOf(LegacyContactColumn(id = 9L, kind = kind, displayName = "Sam Green")),
            )

            val result = subject(filter, QUERY).first()

            assertEquals(
                GetContactsForSelectionResult(
                    contacts = listOf(ContactItem(ContactItemKey.Local(9L), name = "Sam Green")),
                ),
                result,
            )
        }
    }

    // Display settings

    @Test
    fun whenSortOrderChanges_queriesAgain() = runTest(dispatcher) {
        subject(ContactSelectionFilter.Contacts(), QUERY).test {
            awaitItem()

            displaySettings.update { it.copy(sortOrder = SortOrder.FAMILY_NAME_FIRST) }
            awaitItem()

            verify(exactly = 1) {
                contactsRepository.queryContacts(QUERY, SortOrder.GIVEN_NAME_FIRST, null, null)
            }
            verify(exactly = 1) {
                contactsRepository.queryContacts(QUERY, SortOrder.FAMILY_NAME_FIRST, null, null)
            }
        }
    }

    @Test
    fun whenDisplayOrderChanges_remapsNames() = runTest(dispatcher) {
        every {
            contactsRepository.queryContacts(any(), any(), any(), any())
        } returns flowOf(listOf(contactColumn(1L)))

        subject(ContactSelectionFilter.Contacts(), null).test {
            assertEquals(primaryName(1L), awaitItem().contacts.single().name)

            displaySettings.update { it.copy(displayOrder = DisplayOrder.FAMILY_NAME_FIRST) }

            assertEquals(alternativeName(1L), awaitItem().contacts.single().name)
        }
    }

    private fun primaryName(id: Long) = "Contact $id"

    private fun alternativeName(id: Long) = "$id, Contact"

    private fun contactColumn(id: Long) = ContactColumn(
        id = id,
        displayNamePrimary = primaryName(id),
        displayNameAlternative = alternativeName(id),
    )

    private fun contactItem(
        id: Long,
        displayOrder: DisplayOrder = DisplayOrder.GIVEN_NAME_FIRST,
    ) = ContactItem(
        key = ContactItemKey.Local(id),
        name = name(id, displayOrder),
    )

    private fun contactDataColumn(id: Long, kind: ContactDataKind, value: String) =
        ContactDataColumn(
            id = id,
            kind = kind,
            displayNamePrimary = primaryName(id),
            displayNameAlternative = alternativeName(id),
            value = value,
        )

    private fun contactDataItem(id: Long, kind: ContactItem.FieldKind, value: String) =
        ContactItem(
            key = ContactItemKey.Local(id),
            name = primaryName(id),
            field = ContactItem.Field(value = value, kind = kind),
        )

    private fun directoryContactColumn(directoryId: Long, id: Long) = DirectoryContactColumn(
        directoryId = directoryId,
        id = id,
        lookupKey = "lookup-$id",
        displayNamePrimary = primaryName(id),
        displayNameAlternative = alternativeName(id),
    )

    private fun directoryContactItem(
        directoryId: Long,
        id: Long,
        displayOrder: DisplayOrder = DisplayOrder.GIVEN_NAME_FIRST,
    ) = ContactItem(
        key = ContactItemKey.Directory(
            directoryId = directoryId,
            id = id,
            lookupKey = "lookup-$id",
        ),
        name = name(id, displayOrder),
    )

    private fun name(id: Long, displayOrder: DisplayOrder) = when (displayOrder) {
        DisplayOrder.GIVEN_NAME_FIRST -> primaryName(id)
        DisplayOrder.FAMILY_NAME_FIRST -> alternativeName(id)
    }

    companion object {
        private const val QUERY = "john"

        private val DISPLAY_SETTINGS = DisplaySettings(
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            isSortOrderChangeable = true,
            displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
            isDisplayOrderChangeable = true,
            phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
            isPhoneticNameDisplayChangeable = true,
        )

        private val DIRECTORIES_FILTER = ContactSelectionFilter.Contacts(includesDirectories = true)

        private val DIRECTORY_COLUMN = DirectoryColumn(
            id = 10L,
            displayName = "Work Directory",
            packageName = "com.example.directory",
            typeResourceId = 42,
        )

        private val DIRECTORY = ContactDirectory(
            id = 10L,
            displayName = "Work Directory",
            packageName = "com.example.directory",
            typeResourceId = 42,
        )
    }
}
