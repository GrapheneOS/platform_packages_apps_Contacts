package com.android.contacts.ui.common.mapper

import android.content.pm.PackageManager
import android.content.res.Resources
import com.android.contacts.domain.contacts.model.ContactDirectory
import com.android.contacts.domain.contacts.model.ContactDirectoryResult
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.tests.targetContext
import com.android.contacts.ui.common.model.ContactListSection as Section
import com.android.contacts.ui.common.model.ContactSectionedList
import com.android.contacts.ui.common.model.ContactUiModel
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class ContactSectionedListMapperTest {

    private val packageManager = mockk<PackageManager>()

    private val subject: ContactSectionedListMapper = ContactSectionedListMapperImpl(
        resources = targetContext.resources,
        packageManager = packageManager,
        contactUiModelMapper = ContactUiModelMapperImpl(targetContext.resources),
    )

    // Letters

    @Test
    fun map_groupsContactsByLetter() {
        val sections = map(
            contacts = listOf(
                item(1L, "anna"),
                item(2L, "Adam"),
                item(3L, "Bob"),
                item(4L, "Zed", sectionLabel = "Z"),
            ),
        )

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
    fun map_prefersSectionLabelOverFirstLetter() {
        val sections = map(contacts = listOf(item(1L, "Anna", sectionLabel = "あ")))

        assertEquals(listOf(Section.Alphabetical("あ")), sections.keys.toList())
    }

    @Test
    fun map_withBlankSectionLabel_usesFirstLetter() {
        val sections = map(contacts = listOf(item(1L, "anna", sectionLabel = " ")))

        assertEquals(listOf(Section.Alphabetical("A")), sections.keys.toList())
    }

    @Test
    fun map_withoutLeadingLetter_usesOtherSection() {
        val sections = map(contacts = listOf(item(1L, "+1 555 0100"), item(2L, "")))

        assertEquals(listOf(Section.Alphabetical("#")), sections.keys.toList())
    }

    @Test
    fun map_skipsLeadingFormatCharacters() {
        val sections = map(contacts = listOf(item(1L, "‎john")))

        assertEquals(listOf(Section.Alphabetical("J")), sections.keys.toList())
    }

    @Test
    fun map_withoutLetters_usesSingleUndefinedSection() {
        val sections = map(
            contacts = listOf(item(1L, "Anna"), item(2L, "Bob")),
            showLetters = false,
        )

        assertEquals(
            mapOf(Section.Undefined to listOf(uiModel(1L, "Anna"), uiModel(2L, "Bob"))),
            sections,
        )
    }

    @Test
    fun map_withoutContacts_isEmpty() {
        assertTrue(map(contacts = emptyList()).isEmpty())
        assertTrue(map(contacts = emptyList(), showLetters = false).isEmpty())
    }

    // Selection

    @Test
    fun map_marksSelectedContacts() {
        val sections = map(
            contacts = listOf(item(1L), item(2L)),
            selectedKeys = setOf(local(2L)),
            showLetters = false,
        )

        assertEquals(listOf(false, true), sections.values.single().map { it.isSelected })
    }

    // Favorites

    @Test
    fun map_withFavorites_showsFavoritesFirst() {
        val sections = map(
            contacts = listOf(item(1L, "Anna"), item(2L, "Bob", isFavorite = true)),
            showFavorites = true,
        )

        assertEquals(
            listOf(Section.Favorites, Section.Alphabetical("A"), Section.Alphabetical("B")),
            sections.keys.toList(),
        )
        assertEquals(listOf(uiModel(2L, "Bob")), sections[Section.Favorites])
    }

    @Test
    fun map_withFavoritesButNoFavoriteContacts_omitsFavorites() {
        val sections = map(contacts = listOf(item(1L, "Anna")), showFavorites = true)

        assertFalse(Section.Favorites in sections)
    }

    @Test
    fun map_withoutFavorites_omitsFavorites() {
        val sections = map(contacts = listOf(item(1L, "Anna", isFavorite = true)))

        assertFalse(Section.Favorites in sections)
    }

    // Suggestions

    @Test
    fun map_showsSuggestionsFirst() {
        val sections = map(
            contacts = listOf(item(1L, "Anna")),
            suggestions = listOf(item(2L, "Bob")),
            showFavorites = true,
        )

        assertEquals(
            listOf(Section.Suggestions, Section.Alphabetical("A")),
            sections.keys.toList(),
        )
        assertEquals(listOf(uiModel(2L, "Bob")), sections[Section.Suggestions])
    }

    // Directories

    @Test
    fun map_showsDirectoriesAfterContacts() {
        val directoryContact = ContactItem(key = DIRECTORY_KEY, name = "Remote Contact")
        val sections = map(
            contacts = listOf(item(1L, "Anna")),
            directories = listOf(
                ContactDirectoryResult(
                    directory = ContactDirectory(id = 10L, displayName = "Work"),
                    contacts = listOf(directoryContact),
                ),
            ),
            showLetters = false,
        )

        val directorySection = Section.Directory(directoryId = 10L, name = "Work")
        assertEquals(listOf(Section.Undefined, directorySection), sections.keys.toList())
        assertEquals(
            listOf(ContactUiModel(key = DIRECTORY_KEY, name = "Remote Contact")),
            sections[directorySection],
        )
    }

    @Test
    fun map_showsLoadingDirectoriesAndHidesEmptyOnes() {
        val sections = map(
            contacts = emptyList(),
            directories = listOf(
                ContactDirectoryResult(
                    directory = ContactDirectory(id = 10L, displayName = "Loading"),
                    isLoading = true,
                ),
                ContactDirectoryResult(
                    directory = ContactDirectory(id = 11L, displayName = "Empty"),
                ),
            ),
        )

        assertEquals(
            mapOf(
                Section.Directory(directoryId = 10L, name = "Loading", isLoading = true) to
                    emptyList<ContactUiModel>(),
            ),
            sections,
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

    private fun map(
        contacts: List<ContactItem>,
        suggestions: List<ContactItem> = emptyList(),
        directories: List<ContactDirectoryResult> = emptyList(),
        selectedKeys: Set<ContactItemKey> = emptySet(),
        showFavorites: Boolean = false,
        showLetters: Boolean = true,
    ): ContactSectionedList {
        return subject.map(
            contacts = contacts,
            suggestions = suggestions,
            directories = directories,
            selectedKeys = selectedKeys,
            showFavorites = showFavorites,
            showLetters = showLetters,
        )
    }

    private fun directorySectionName(directory: ContactDirectory): String {
        val sections = map(
            contacts = emptyList(),
            directories = listOf(ContactDirectoryResult(directory = directory, isLoading = true)),
        )
        return (sections.keys.single() as Section.Directory).name
    }

    private fun local(id: Long) = ContactItemKey.Local(id)

    private fun item(
        id: Long,
        name: String = "Contact $id",
        sectionLabel: String? = null,
        isFavorite: Boolean = false,
    ) = ContactItem(
        key = local(id),
        name = name,
        sectionLabel = sectionLabel,
        isFavorite = isFavorite,
    )

    private fun uiModel(id: Long, name: String) = ContactUiModel(key = local(id), name = name)

    private companion object {
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
