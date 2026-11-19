package com.android.contacts.ui.selection.screen.mapper

import android.content.res.Resources
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.domain.accounts.mapper.AccountDisplayModelMapper
import com.android.contacts.domain.contacts.model.ContactItem
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.domain.contacts.model.GetContactsForSelectionResult
import com.android.contacts.ui.common.mapper.ContactSectionedListMapper
import com.android.contacts.ui.common.mapper.ContactUiModelMapper
import com.android.contacts.ui.common.util.getMessageFormat
import com.android.contacts.ui.selection.screen.model.ContactSelectionListState
import com.android.contacts.ui.selection.screen.model.ContactSelectionMode as Mode
import com.android.contacts.ui.selection.screen.model.ContactSelectionRequest
import com.android.contacts.ui.selection.screen.model.ContactSelectionSearchState as SearchState
import com.android.contacts.ui.selection.screen.model.ContactSelectionState
import com.android.contacts.ui.selection.screen.model.ContactSelectionUiState
import javax.inject.Inject

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
    private val accountDisplayModelMapper: AccountDisplayModelMapper,
    private val contactUiModelMapper: ContactUiModelMapper,
    private val contactSectionedListMapper: ContactSectionedListMapper,
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
                    contacts = contactSectionedListMapper.map(
                        contacts = contactsResult.contacts,
                        suggestions = contactsResult.suggestions,
                        directories = contactsResult.directories,
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
        return resources.getString(
            R.string.blurbJoinContactDataWith,
            contactUiModelMapper.map(joinTarget).name,
        )
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
}
