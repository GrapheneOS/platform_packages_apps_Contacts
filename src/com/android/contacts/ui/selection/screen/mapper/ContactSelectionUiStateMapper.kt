package com.android.contacts.ui.selection.screen.mapper

import android.content.pm.PackageManager
import android.content.res.Resources
import android.provider.ContactsContract.CommonDataKinds.Email
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal
import android.util.Log
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.data.contactdetails.model.ContactPhoto
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.contacts.model.ContactDirectory
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import com.android.contacts.ui.common.components.ContactAvatarImage
import com.android.contacts.ui.common.util.getMessageFormat
import com.android.contacts.ui.selection.screen.model.ContactListSection as Section
import com.android.contacts.ui.selection.screen.model.ContactSelectionListState
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionRequest
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState
import com.android.contacts.ui.selection.screen.model.ContactUiModel
import com.android.contacts.ui.selection.screen.model.ShortcutTarget
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap

internal fun interface ContactSelectionUiStateMapper {
    fun map(
        request: ContactSelectionRequest,
        state: ContactSelectionState,
        contactsResultWithQuery: Pair<String?, GetContactsForSelectionResult>?,
        joinTarget: ContactItem?,
    ): ContactSelectionUiState
}

internal class ContactSelectionUiStateMapperImpl @Inject constructor(
    private val resources: Resources,
    private val packageManager: PackageManager,
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
) : ContactSelectionUiStateMapper {

    override fun map(
        request: ContactSelectionRequest,
        state: ContactSelectionState,
        contactsResultWithQuery: Pair<String?, GetContactsForSelectionResult>?,
        joinTarget: ContactItem?,
    ): ContactSelectionUiState {
        val mode = request.mode
        val ongoingSearchQuery = (state.searchState as? SearchState.Searching)?.effectiveQuery
        val resultsSearchQuery = contactsResultWithQuery?.first
        val contactsResult = contactsResultWithQuery?.second
        val isSearching = resultsSearchQuery?.isNotBlank() == true

        return ContactSelectionUiState(
            title = title(request, state.selectedKeys),
            description = description(mode, joinTarget),
            listHeader = listHeader(mode, contactsResult, isSearching),
            emptyMessageId = when {
                isSearching -> R.string.listFoundAllContactsZero
                else -> R.string.noContacts
            },
            listState = when {
                contactsResult == null -> ContactSelectionListState(isLoading = true)
                else -> ContactSelectionListState(
                    isLoading = isSearching && ongoingSearchQuery != resultsSearchQuery,
                    searchQuery = resultsSearchQuery,
                    contacts = mapSections(
                        contactsResult = contactsResult,
                        selectedKeys = state.selectedKeys,
                        showFavorites = mode is Mode.Pick && mode.includeFavorites && !isSearching,
                        showLetters = !isSearching,
                    ),
                )
            },
            searchState = state.searchState,
            allowMultiple = mode.isMultiple,
            selectedCount = state.selectedKeys.size,
            canCreate = mode.canCreateContact && !isSearching,
            showAvatars = !mode.isLegacy,
        )
    }

    private fun title(
        request: ContactSelectionRequest,
        selectedKeys: Set<ContactItemKey>,
    ): String {
        return when {
            selectedKeys.isEmpty() -> {
                request.titleOverride?.takeIf { it.isNotBlank() }
                    ?: resources.getString(titleId(request.mode))
            }
            else -> {
                resources.getQuantityString(
                    R.plurals.contactPickerSelectedTitle,
                    selectedKeys.size,
                    selectedKeys.size,
                )
            }
        }
    }

    private fun description(
        mode: Mode,
        joinTarget: ContactItem?,
    ): String? {
        if (mode !is Mode.Join || joinTarget == null) return null
        return resources.getString(R.string.blurbJoinContactDataWith, joinTarget.displayName())
    }

    private fun listHeader(
        mode: Mode,
        contactsResult: GetContactsForSelectionResult?,
        isSearching: Boolean,
    ): String? {
        if (mode !is Mode.AddGroupMembers || contactsResult == null || isSearching) return null

        val accountDisplay = accountDisplayModelMapper.map(mode.account)
        val accountName = when {
            accountDisplay.isDeviceAccount -> null
            else -> accountDisplay.name?.takeIf { it.isNotBlank() }
        }
        val contactsCount = contactsResult.contacts.size

        return when {
            accountName != null -> resources.getMessageFormat(
                R.string.contacts_count_with_account,
                mapOf(
                    "count" to contactsCount,
                    "account" to accountName,
                ),
            )
            else -> resources.getMessageFormat(
                R.string.contacts_count,
                mapOf("count" to contactsCount),
            )
        }
    }

    @StringRes
    private fun titleId(mode: Mode): Int {
        return when (mode) {
            is Mode.Pick if mode.multiple ->
                R.string.pickerSelectContactsActivityTitle
            is Mode.Pick ->
                R.string.contactPickerActivityTitle
            Mode.InsertOrEdit ->
                R.string.contactInsertOrEditActivityTitle
            is Mode.PickPhoneNumber if mode.multiple ->
                R.string.pickerSelectContactsActivityTitle
            is Mode.PickPhoneNumber ->
                R.string.contactPickerActivityTitle
            is Mode.PickEmail if mode.multiple ->
                R.string.pickerSelectContactsActivityTitle
            is Mode.PickEmail ->
                R.string.contactPickerActivityTitle
            is Mode.PickPostal ->
                R.string.contactPickerActivityTitle
            is Mode.CreateShortcut ->
                R.string.shortcutActivityTitle
            is Mode.Join ->
                R.string.titleJoinContactDataWith
            is Mode.AddGroupMembers ->
                R.string.groupMemberPickerActivityTitle
            is Mode.SendToGroup ->
                R.string.pickerSelectContactsActivityTitle
        }
    }

    private fun mapSections(
        contactsResult: GetContactsForSelectionResult,
        selectedKeys: Set<ContactItemKey>,
        showFavorites: Boolean,
        showLetters: Boolean,
    ): ImmutableMap<Section, ImmutableList<ContactUiModel>> {
        val sections = LinkedHashMap<Section, List<ContactUiModel>>()
        val contacts = contactsResult.contacts

        if (contactsResult.suggestions.any()) {
            sections[Section.Suggestions] = contactsResult.suggestions.toUiModels(selectedKeys)
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
            showLetters -> {
                sections.putAll(
                    contacts
                        .groupBy { contact -> Section.Alphabetical(sectionLetter(contact)) }
                        .mapValues { (_, contacts) -> contacts.toUiModels(selectedKeys) },
                )
            }
            else -> {
                if (contacts.any()) {
                    sections[Section.Undefined] = contacts.toUiModels(selectedKeys)
                }
            }
        }

        contactsResult.directories
            .filter { it.isLoading || it.contacts.any() }
            .forEach { result ->
                val section = Section.Directory(
                    directoryId = result.directory.id,
                    name = directoryName(result.directory),
                    isLoading = result.isLoading,
                )
                sections[section] = result.contacts.toUiModels(selectedKeys)
            }

        return sections
            .mapValues { (_, list) -> list.toImmutableList() }
            .toImmutableMap()
    }

    private fun directoryName(directory: ContactDirectory): String {
        return directory.displayName
            ?: directoryTypeLabel(directory)
            ?: resources.getString(R.string.directory_search_label)
    }

    private fun directoryTypeLabel(directory: ContactDirectory): String? {
        if (directory.packageName == null || directory.typeResourceId == null) return null

        @Suppress("TooGenericExceptionCaught")
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

    private fun List<ContactItem>.toUiModels(
        selectedKeys: Set<ContactItemKey>,
    ): List<ContactUiModel> {
        return map { it.toUiModel(selectedKeys = selectedKeys) }
    }

    private fun ContactItem.toUiModel(selectedKeys: Set<ContactItemKey>): ContactUiModel {
        return ContactUiModel(
            key = key,
            name = displayName(),
            description = description(),
            avatarImage = contactPhoto?.toAvatarImage(),
            isSelected = key in selectedKeys,
        )
    }

    private fun ContactItem.displayName(): String {
        return name.takeIf { it.isNotBlank() }
            ?: resources.getString(R.string.missing_name)
    }

    private fun ContactItem.description(): String? {
        // Postal addresses may span across multiple lines
        val value = field?.value
            ?.lines()
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?.takeIf { it.any() }
            ?.joinToString(", ")
        val label = fieldLabel()
        return when {
            value == null -> null
            label == null -> value
            else -> "$label · $value"
        }
    }

    private fun ContactItem.fieldLabel(): String? {
        val type = field?.type ?: return null
        return when (field.kind) {
            ContactItem.FieldKind.PHONE ->
                Phone.getTypeLabel(resources, type, field.label)
            ContactItem.FieldKind.EMAIL ->
                Email.getTypeLabel(resources, type, field.label)
            ContactItem.FieldKind.POSTAL_ADDRESS ->
                StructuredPostal.getTypeLabel(resources, type, field.label)
            ContactItem.FieldKind.UNSPECIFIED ->
                null
        }
            ?.toString()
            ?.takeIf { it.isNotBlank() }
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

    private fun ContactPhoto.toAvatarImage(): ContactAvatarImage {
        return when (this) {
            is ContactPhoto.Bytes -> ContactAvatarImage.Bytes(value)
            is ContactPhoto.Uri -> ContactAvatarImage.Uri(value)
        }
    }

    private companion object {
        const val TAG = "ContactSelectionUiState"
        const val OTHER_SECTION = "#"
    }
}
