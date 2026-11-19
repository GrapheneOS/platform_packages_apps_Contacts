package com.android.contacts.ui.common.mapper

import android.content.pm.PackageManager
import android.content.res.Resources
import android.util.Log
import com.android.contacts.R
import com.android.contacts.domain.contacts.model.ContactDirectory
import com.android.contacts.domain.contacts.model.ContactDirectoryResult
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.model.ContactListSection as Section
import com.android.contacts.ui.common.model.ContactSectionedList
import com.android.contacts.ui.common.model.ContactUiModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

internal interface ContactSectionedListMapper {
    fun map(
        contacts: List<ContactItem>,
        suggestions: List<ContactItem> = emptyList(),
        directories: List<ContactDirectoryResult> = emptyList(),
        selectedKeys: Set<ContactItemKey>,
        showFavorites: Boolean = false,
        showLetters: Boolean = true,
    ): ContactSectionedList
}

internal class ContactSectionedListMapperImpl @Inject constructor(
    private val resources: Resources,
    private val packageManager: PackageManager,
    private val contactUiModelMapper: ContactUiModelMapper,
) : ContactSectionedListMapper {

    override fun map(
        contacts: List<ContactItem>,
        suggestions: List<ContactItem>,
        directories: List<ContactDirectoryResult>,
        selectedKeys: Set<ContactItemKey>,
        showFavorites: Boolean,
        showLetters: Boolean,
    ): ContactSectionedList {
        val sections = LinkedHashMap<Section, List<ContactUiModel>>()

        if (suggestions.any()) {
            sections[Section.Suggestions] = suggestions.toUiModels(selectedKeys)
        }

        if (showFavorites) {
            contacts
                .filter { it.isFavorite }
                .takeIf { it.any() }
                ?.let { favorites ->
                    sections[Section.Favorites] = favorites.toUiModels(selectedKeys)
                }
        }

        when {
            showLetters -> sections.putAll(mapAlphabetical(contacts, selectedKeys))
            contacts.any() -> sections[Section.Undefined] = contacts.toUiModels(selectedKeys)
        }

        directories
            .filter { it.isLoading || it.contacts.any() }
            .forEach { result ->
                val section = Section.Directory(
                    directoryId = result.directory.id,
                    name = directoryName(result.directory),
                    isLoading = result.isLoading,
                )
                sections[section] = result.contacts.toUiModels(selectedKeys)
            }

        return sections.toSectionedList()
    }

    private fun mapAlphabetical(
        contacts: List<ContactItem>,
        selectedKeys: Set<ContactItemKey>,
    ): ContactSectionedList {
        return contacts
            .groupBy<ContactItem, Section> { contact ->
                Section.Alphabetical(sectionLetter(contact))
            }
            .mapValues { (_, sectionContacts) -> sectionContacts.toUiModels(selectedKeys) }
            .toSectionedList()
    }

    private fun List<ContactItem>.toUiModels(
        selectedKeys: Set<ContactItemKey>,
    ): List<ContactUiModel> {
        return map { contactUiModelMapper.map(contact = it, isSelected = it.key in selectedKeys) }
    }

    private fun Map<Section, List<ContactUiModel>>.toSectionedList(): ContactSectionedList {
        return mapValues { (_, list) -> list.toImmutableList() }.toImmutableMap()
    }

    private fun sectionLetter(contact: ContactItem): String {
        return contact.sectionLabel
            ?.takeIf { it.isNotBlank() }
            ?: firstLetter(contact)
    }

    private fun firstLetter(contact: ContactItem): String {
        val firstChar = contact.name
            .trim()
            .dropWhile { it.category == CharCategory.FORMAT }
            .firstOrNull()
        return when {
            firstChar == null || !firstChar.isLetter() -> OTHER_SECTION
            else -> firstChar.uppercaseChar().toString()
        }
    }

    private fun directoryName(directory: ContactDirectory): String {
        return directory.displayName
            ?: directoryTypeLabel(directory)
            ?: resources.getString(R.string.directory_search_label)
    }

    private fun directoryTypeLabel(directory: ContactDirectory): String? {
        if (directory.packageName == null || directory.typeResourceId == null) return null

        return try {
            packageManager.getResourcesForApplication(directory.packageName)
                .getString(directory.typeResourceId)
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w(TAG, "Cannot obtain resources from package: ${directory.packageName}", e)
            null
        } catch (e: Resources.NotFoundException) {
            Log.w(TAG, "Cannot obtain directory type from resources: ${directory.packageName}", e)
            null
        }
    }

    private companion object {
        const val TAG = "ContactSectionedListMapper"
        const val OTHER_SECTION = "#"
    }
}
