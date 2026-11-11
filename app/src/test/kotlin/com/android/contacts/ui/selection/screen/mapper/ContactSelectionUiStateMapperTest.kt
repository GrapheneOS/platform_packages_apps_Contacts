package com.android.contacts.ui.selection.screen.mapper

import android.content.pm.PackageManager
import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds.Phone
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.domain.contacts.model.ContactDirectory
import com.android.contacts.domain.contacts.model.ContactDirectoryResult
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import com.android.contacts.model.Contact
import com.android.contacts.tests.factory.AccountDisplayModelFactory
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.selection.screen.model.ContactListSection as Section
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionRequest
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState
import com.android.contacts.ui.selection.screen.model.ContactUiModel
import com.android.contacts.ui.selection.screen.model.SentToGroupTarget
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import io.mockk.every
import io.mockk.mockk
import java.nio.ByteBuffer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ContactSelectionUiStateMapperTest {

    private val packageManager = mockk<PackageManager>()
    private val accountDisplayModelMapper = mockk<AccountDisplayModelMapper>()

    private val subject: ContactSelectionUiStateMapper = ContactSelectionUiStateMapperImpl(
        resources = targetContext.resources,
        packageManager = packageManager,
        accountDisplayModelMapper = accountDisplayModelMapper,
    )

    @Before
    fun setUp() {
        every { accountDisplayModelMapper.map(any<AccountModel>()) } returns
            AccountDisplayModelFactory.build(isDeviceAccount = true)
    }

    // Title

    @Test
    fun title_mapsModeToTitle() {
        val expected = mapOf(
            Mode.Pick() to "Choose a contact",
            Mode.Pick(multiple = true) to "Choose Contacts",
            Mode.InsertOrEdit to "Add to contact",
            Mode.PickPhoneNumber() to "Choose a contact",
            Mode.PickPhoneNumber(multiple = true) to "Choose Contacts",
            Mode.PickEmail() to "Choose a contact",
            Mode.PickEmail(multiple = true) to "Choose Contacts",
            Mode.PickPostal() to "Choose a contact",
            Mode.CreateShortcut(ShortcutTarget.CALL) to "Choose contact",
            Mode.Join(1L) to "Link contacts",
            ADD_GROUP_MEMBERS to "Select",
            SEND_TO_GROUP to "Choose Contacts",
        )

        expected.forEach { (mode, title) ->
            assertEquals(mode.toString(), title, map(mode = mode).title)
        }
    }

    @Test
    fun title_withTitleOverride_usesOverride() {
        assertEquals("Pick a friend", map(titleOverride = "Pick a friend").title)
    }

    @Test
    fun title_withBlankTitleOverride_usesModeTitle() {
        assertEquals("Choose a contact", map(titleOverride = " ").title)
    }

    @Test
    fun title_withSelection_showsSelectedCount() {
        val state = ContactSelectionState(selectedKeys = setOf(local(1L), local(2L)))

        val result = map(titleOverride = "Pick a friend", state = state)

        assertEquals("2 selected", result.title)
        assertEquals(2, result.selectedCount)
    }

    // Description

    @Test
    fun description_forJoinWithTarget_mentionsTargetName() {
        val result = map(mode = Mode.Join(1L), joinTarget = contact("Jane Doe"))

        assertEquals("Choose the contact you want to link with Jane Doe:", result.description)
    }

    @Test
    fun description_forJoinWithUnnamedTarget_usesMissingName() {
        val result = map(mode = Mode.Join(1L), joinTarget = contact(" "))

        assertEquals("Choose the contact you want to link with (No name):", result.description)
    }

    @Test
    fun description_forJoinWithoutTarget_isNull() {
        assertNull(map(mode = Mode.Join(1L), joinTarget = null).description)
    }

    @Test
    fun description_forOtherModes_isNull() {
        assertNull(map(mode = Mode.Pick(), joinTarget = contact("Jane Doe")).description)
    }

    // List header

    @Test
    fun listHeader_forGroupMembersOnDeviceAccount_showsCount() {
        val result = map(mode = ADD_GROUP_MEMBERS, contacts = result(item(1L), item(2L)))

        assertEquals("2 contacts", result.listHeader)
    }

    @Test
    fun listHeader_forGroupMembersOnNamedAccount_showsCountAndAccount() {
        every { accountDisplayModelMapper.map(ADD_GROUP_MEMBERS.account) } returns
            AccountDisplayModelFactory.build(name = "john@example.com", isDeviceAccount = false)

        val result = map(mode = ADD_GROUP_MEMBERS, contacts = result(item(1L)))

        assertEquals("1 contact · john@example.com", result.listHeader)
    }

    @Test
    fun listHeader_forGroupMembersOnBlankAccountName_showsCountOnly() {
        every { accountDisplayModelMapper.map(ADD_GROUP_MEMBERS.account) } returns
            AccountDisplayModelFactory.build(name = " ", isDeviceAccount = false)

        val result = map(mode = ADD_GROUP_MEMBERS, contacts = result(item(1L)))

        assertEquals("1 contact", result.listHeader)
    }

    @Test
    fun listHeader_isNullWhileLoadingSearchingOrForOtherModes() {
        assertNull(map(mode = ADD_GROUP_MEMBERS, contacts = null).listHeader)
        assertNull(
            map(mode = ADD_GROUP_MEMBERS, contacts = result(item(1L)), query = "john").listHeader,
        )
        assertNull(map(mode = Mode.Pick(), contacts = result(item(1L))).listHeader)
    }

    // Empty message

    @Test
    fun emptyMessage_dependsOnSearch() {
        assertEquals(R.string.noContacts, map().emptyMessageId)
        assertEquals(R.string.listFoundAllContactsZero, map(query = "john").emptyMessageId)
    }

    @Test
    fun blankResultsQuery_isNotConsideredSearching() {
        assertEquals(R.string.noContacts, map(query = " ").emptyMessageId)
    }

    // List state

    @Test
    fun listState_withoutResults_isLoading() {
        val result = map(contacts = null)

        assertTrue(result.listState.isLoading)
        assertTrue(result.listState.contacts.isEmpty())
    }

    @Test
    fun listState_withResults_isNotLoading() {
        val result = map(contacts = result(item(1L, "Anna")))

        assertFalse(result.listState.isLoading)
        assertNull(result.listState.searchQuery)
    }

    @Test
    fun listState_whenResultsMatchOngoingSearch_isNotLoading() {
        val state = ContactSelectionState(searchState = SearchState.Searching(" john "))

        val result = map(state = state, contacts = result(item(1L)), query = "john")

        assertFalse(result.listState.isLoading)
        assertEquals("john", result.listState.searchQuery)
    }

    @Test
    fun listState_whenResultsAreForPreviousSearch_isLoading() {
        val state = ContactSelectionState(searchState = SearchState.Searching("johnny"))

        val result = map(state = state, contacts = result(item(1L)), query = "john")

        assertTrue(result.listState.isLoading)
    }

    // Sections

    @Test
    fun sections_groupContactsByLetter() {
        val result = map(
            contacts = result(
                item(1L, "anna"),
                item(2L, "Adam"),
                item(3L, "Bob"),
                item(4L, "Zed", sectionLabel = "Z"),
            ),
        )

        val sections = result.listState.contacts
        assertEquals(
            listOf(Section.Alphabetical("A"), Section.Alphabetical("B"), Section.Alphabetical("Z")),
            sections.keys.toList(),
        )
        assertEquals(
            listOf(uiModel(1L, "anna"), uiModel(2L, "Adam")),
            sections[Section.Alphabetical("A")],
        )
    }

    @Test
    fun sections_preferSectionLabelOverFirstLetter() {
        val result = map(contacts = result(item(1L, "Anna", sectionLabel = "あ")))

        assertEquals(listOf(Section.Alphabetical("あ")), result.listState.contacts.keys.toList())
    }

    @Test
    fun sections_withBlankSectionLabel_useFirstLetter() {
        val result = map(contacts = result(item(1L, "anna", sectionLabel = " ")))

        assertEquals(listOf(Section.Alphabetical("A")), result.listState.contacts.keys.toList())
    }

    @Test
    fun sections_withoutLeadingLetter_useOtherSection() {
        val result = map(contacts = result(item(1L, "+1 555 0100"), item(2L, "")))

        assertEquals(listOf(Section.Alphabetical("#")), result.listState.contacts.keys.toList())
    }

    @Test
    fun sections_skipLeadingFormatCharacters() {
        val result = map(contacts = result(item(1L, "‎john")))

        assertEquals(listOf(Section.Alphabetical("J")), result.listState.contacts.keys.toList())
    }

    @Test
    fun sections_whenSearching_useSingleUndefinedSection() {
        val result = map(
            contacts = result(
                item(1L, "Anna"),
                item(2L, "Bob"),
            ),
            query = "a",
        )

        assertEquals(
            mapOf(Section.Undefined to listOf(uiModel(1L, "Anna"), uiModel(2L, "Bob"))),
            result.listState.contacts,
        )
    }

    @Test
    fun sections_whenSearchingWithoutResults_areEmpty() {
        assertTrue(map(contacts = result(), query = "a").listState.contacts.isEmpty())
    }

    @Test
    fun sections_withFavoritesEnabled_showFavoritesFirst() {
        val result = map(
            mode = Mode.Pick(includeFavorites = true),
            contacts = result(item(1L, "Anna"), item(2L, "Bob", isFavorite = true)),
        )

        val sections = result.listState.contacts
        assertEquals(
            listOf(Section.Favorites, Section.Alphabetical("A"), Section.Alphabetical("B")),
            sections.keys.toList(),
        )
        assertEquals(listOf(uiModel(2L, "Bob")), sections[Section.Favorites])
    }

    @Test
    fun sections_withFavoritesEnabledButNoFavorites_omitFavorites() {
        val result = map(
            mode = Mode.Pick(includeFavorites = true),
            contacts = result(item(1L, "Anna")),
        )

        assertFalse(Section.Favorites in result.listState.contacts)
    }

    @Test
    fun sections_omitFavoritesWhenDisabledOrSearching() {
        val contacts = result(item(1L, "Anna", isFavorite = true))

        assertFalse(
            Section.Favorites in map(mode = Mode.Pick(), contacts = contacts).listState.contacts,
        )
        assertFalse(
            Section.Favorites in map(
                mode = Mode.Pick(includeFavorites = true),
                contacts = contacts,
                query = "a",
            ).listState.contacts,
        )
        assertFalse(
            Section.Favorites in map(mode = Mode.PickEmail(), contacts = contacts)
                .listState.contacts,
        )
    }

    @Test
    fun sections_showSuggestionsFirst() {
        val result = map(
            mode = Mode.Join(1L),
            contacts = GetContactsForSelectionResult(
                contacts = listOf(item(1L, "Anna")),
                suggestions = listOf(item(2L, "Bob")),
            ),
        )

        val sections = result.listState.contacts
        assertEquals(
            listOf(Section.Suggestions, Section.Alphabetical("A")),
            sections.keys.toList(),
        )
        assertEquals(listOf(uiModel(2L, "Bob")), sections[Section.Suggestions])
    }

    // Directories

    @Test
    fun sections_showDirectoriesAfterContacts() {
        val directoryContact = ContactItem(key = DIRECTORY_KEY, name = "Remote Contact")
        val result = map(
            contacts = GetContactsForSelectionResult(
                contacts = listOf(item(1L, "Anna")),
                directories = listOf(
                    ContactDirectoryResult(
                        directory = ContactDirectory(id = 10L, displayName = "Work"),
                        contacts = listOf(directoryContact),
                    ),
                ),
            ),
            query = "a",
        )

        val sections = result.listState.contacts
        val directorySection = Section.Directory(directoryId = 10L, name = "Work")
        assertEquals(listOf(Section.Undefined, directorySection), sections.keys.toList())
        assertEquals(
            listOf(ContactUiModel(key = DIRECTORY_KEY, name = "Remote Contact")),
            sections[directorySection],
        )
    }

    @Test
    fun sections_showLoadingDirectoriesAndHideEmptyOnes() {
        val result = map(
            contacts = GetContactsForSelectionResult(
                directories = listOf(
                    ContactDirectoryResult(
                        directory = ContactDirectory(id = 10L, displayName = "Loading"),
                        isLoading = true,
                    ),
                    ContactDirectoryResult(
                        directory = ContactDirectory(id = 11L, displayName = "Empty"),
                    ),
                ),
            ),
            query = "a",
        )

        assertEquals(
            mapOf(
                Section.Directory(directoryId = 10L, name = "Loading", isLoading = true) to
                    emptyList<ContactUiModel>(),
            ),
            result.listState.contacts,
        )
    }

    @Test
    fun directoryName_withoutDisplayName_usesTypeLabelFromPackage() {
        val packageResources = mockk<Resources>()
        every { packageResources.getString(42) } returns "Exchange"
        every {
            packageManager.getResourcesForApplication("com.example.directory")
        } returns packageResources

        assertEquals("Exchange", directorySectionName(DIRECTORY_WITH_TYPE))
    }

    @Test
    fun directoryName_whenPackageIsMissing_usesDefaultLabel() {
        every {
            packageManager.getResourcesForApplication(any<String>())
        } throws PackageManager.NameNotFoundException()

        assertEquals("Directory", directorySectionName(DIRECTORY_WITH_TYPE))
    }

    @Test
    fun directoryName_whenTypeResourceIsMissing_usesDefaultLabel() {
        val packageResources = mockk<Resources>()
        every { packageResources.getString(any()) } throws Resources.NotFoundException()
        every { packageManager.getResourcesForApplication(any<String>()) } returns packageResources

        assertEquals("Directory", directorySectionName(DIRECTORY_WITH_TYPE))
    }

    @Test
    fun directoryName_withoutDisplayNameOrType_usesDefaultLabel() {
        assertEquals("Directory", directorySectionName(ContactDirectory(id = 10L)))
        assertEquals(
            "Directory",
            directorySectionName(ContactDirectory(id = 10L, packageName = "com.example.directory")),
        )
    }

    // Contact UI models

    @Test
    fun contact_withBlankName_usesMissingName() {
        val result = map(contacts = result(item(1L, " ")), query = "a")

        assertEquals("(No name)", result.listState.contacts.values.single().single().name)
    }

    @Test
    fun contact_whenSelected_isMarkedSelected() {
        val state = ContactSelectionState(selectedKeys = setOf(local(2L)))

        val result = map(state = state, contacts = result(item(1L), item(2L)), query = "a")

        assertEquals(
            listOf(false, true),
            result.listState.contacts.values.single().map { it.isSelected },
        )
    }

    @Test
    fun contact_mapsPhotoToAvatarImage() {
        val bytes = ByteBuffer.wrap(byteArrayOf(1, 2, 3))
        val result = map(
            contacts = result(
                item(1L, photo = ContactPhoto.Uri("content://photo/1")),
                item(2L, photo = ContactPhoto.Bytes(bytes)),
                item(3L),
            ),
            query = "a",
        )

        assertEquals(
            listOf(
                ContactAvatarImage.Uri("content://photo/1"),
                ContactAvatarImage.Bytes(bytes),
                null,
            ),
            result.listState.contacts.values.single().map { it.avatarImage },
        )
    }

    @Test
    fun contactDescription_withValueOnly_isTheValue() {
        val field = ContactItem.Field(
            value = "john@example.com",
            kind = ContactItem.FieldKind.EMAIL,
        )

        assertEquals("john@example.com", descriptionOf(field))
    }

    @Test
    fun contactDescription_withType_prefixesTypeLabel() {
        val field = ContactItem.Field(
            value = "+1 555 0100",
            kind = ContactItem.FieldKind.PHONE,
            type = Phone.TYPE_MOBILE,
        )

        assertEquals("Mobile · +1 555 0100", descriptionOf(field))
    }

    @Test
    fun contactDescription_withCustomType_usesCustomLabel() {
        val field = ContactItem.Field(
            value = "+1 555 0100",
            kind = ContactItem.FieldKind.PHONE,
            type = Phone.TYPE_CUSTOM,
            label = "Boat",
        )

        assertEquals("Boat · +1 555 0100", descriptionOf(field))
    }

    @Test
    fun contactDescription_withUnspecifiedKind_ignoresType() {
        val field = ContactItem.Field(
            value = "value",
            kind = ContactItem.FieldKind.UNSPECIFIED,
            type = Phone.TYPE_MOBILE,
        )

        assertEquals("value", descriptionOf(field))
    }

    @Test
    fun contactDescription_withMultilineAddress_joinsLines() {
        val field = ContactItem.Field(
            value = "1600 Amphitheatre Parkway\n\n  Mountain View, CA  \n",
            kind = ContactItem.FieldKind.POSTAL_ADDRESS,
        )

        assertEquals(
            "1600 Amphitheatre Parkway, Mountain View, CA",
            descriptionOf(field),
        )
    }

    @Test
    fun contactDescription_withBlankValueOrNoField_isNull() {
        assertNull(
            descriptionOf(ContactItem.Field(value = "\n ", kind = ContactItem.FieldKind.EMAIL)),
        )
        assertNull(descriptionOf(null))
    }

    // Flags

    @Test
    fun flags_reflectMode() {
        val pick = map(mode = Mode.Pick())
        assertFalse(pick.allowMultiple)
        assertTrue(pick.canCreate)
        assertTrue(pick.showAvatars)

        val multiple = map(mode = Mode.Pick(multiple = true))
        assertTrue(multiple.allowMultiple)
        assertFalse(multiple.canCreate)

        assertTrue(map(mode = Mode.InsertOrEdit).canCreate)
        assertFalse(map(mode = Mode.PickEmail()).canCreate)
        assertFalse(map(mode = Mode.Pick(legacy = true)).showAvatars)
        assertFalse(map(mode = Mode.PickPhoneNumber(legacy = true)).showAvatars)
    }

    @Test
    fun canCreate_isFalseWhileSearching() {
        assertFalse(map(mode = Mode.Pick(), query = "john").canCreate)
    }

    @Test
    fun searchState_isPassedThrough() {
        val searchState = SearchState.Searching("john")

        val result = map(state = ContactSelectionState(searchState = searchState))

        assertEquals(searchState, result.searchState)
    }

    private fun map(
        mode: Mode = Mode.Pick(),
        titleOverride: String? = null,
        state: ContactSelectionState = ContactSelectionState(searchState = SearchState.Regular),
        contacts: GetContactsForSelectionResult? = result(),
        query: String? = null,
        joinTarget: Contact? = null,
    ): ContactSelectionUiState {
        return subject.map(
            request = ContactSelectionRequest(mode = mode, titleOverride = titleOverride),
            state = state,
            contactsResultWithQuery = contacts?.let { query to it },
            joinTarget = joinTarget,
        )
    }

    private fun descriptionOf(field: ContactItem.Field?): String? {
        val result = map(
            contacts = result(ContactItem(key = local(1L), name = "Anna", field = field)),
            query = "a",
        )
        return result.listState.contacts.values.single().single().description
    }

    private fun directorySectionName(directory: ContactDirectory): String {
        val result = map(
            contacts = GetContactsForSelectionResult(
                directories = listOf(
                    ContactDirectoryResult(directory = directory, isLoading = true),
                ),
            ),
            query = "a",
        )
        return (result.listState.contacts.keys.single() as Section.Directory).name
    }

    private fun result(vararg contacts: ContactItem) =
        GetContactsForSelectionResult(contacts = contacts.toList())

    private fun local(id: Long) = ContactItemKey.Local(id)

    private fun item(
        id: Long,
        name: String = "Contact $id",
        sectionLabel: String? = null,
        isFavorite: Boolean = false,
        photo: ContactPhoto? = null,
    ) = ContactItem(
        key = local(id),
        name = name,
        contactPhoto = photo,
        sectionLabel = sectionLabel,
        isFavorite = isFavorite,
    )

    private fun uiModel(id: Long, name: String) = ContactUiModel(key = local(id), name = name)

    private fun contact(displayName: String?): Contact = mockk {
        every { this@mockk.displayName } returns displayName
    }

    private companion object {
        val ADD_GROUP_MEMBERS = Mode.AddGroupMembers(
            account = AccountModelFactory.build(name = "john@example.com"),
            existingMemberIds = emptySet(),
        )
        val SEND_TO_GROUP = Mode.SendToGroup(
            target = SentToGroupTarget.SMS,
            itemIds = emptyList(),
            defaultSelection = emptyList(),
            chooserTitle = null,
        )
        val DIRECTORY_KEY = ContactItemKey.Directory(
            directoryId = 10L,
            id = 7L,
            lookupKey = "remote-lookup",
        )
        val DIRECTORY_WITH_TYPE = ContactDirectory(
            id = 10L,
            packageName = "com.example.directory",
            typeResourceId = 42,
        )
    }
}
