package com.android.contacts.ui.selection.screen.model

import android.os.Parcelable
import com.android.contacts.domain.accounts.model.AccountModel
import kotlinx.parcelize.Parcelize

internal sealed interface ContactSelectionMode : Parcelable {

    @Parcelize
    data class Pick(
        val multiple: Boolean = false,
        val includeFavorites: Boolean = false,
        val legacy: Boolean = false,
        val searchQuery: String? = null,
    ) : ContactSelectionMode

    @Parcelize
    data object InsertOrEdit : ContactSelectionMode

    @Parcelize
    data class PickPhoneNumber(
        val multiple: Boolean = false,
        val legacy: Boolean = false,
    ) : ContactSelectionMode

    @Parcelize
    data class PickEmail(
        val multiple: Boolean = false,
    ) : ContactSelectionMode

    @Parcelize
    data class PickPostal(
        val legacy: Boolean = false,
    ) : ContactSelectionMode

    @Parcelize
    data class CreateShortcut(
        val target: ShortcutTarget,
    ) : ContactSelectionMode

    @Parcelize
    data class Join(
        val targetContactId: Long,
    ) : ContactSelectionMode

    @Parcelize
    data class AddGroupMembers(
        val account: AccountModel,
        val existingMemberIds: Set<Long>,
    ) : ContactSelectionMode

    @Parcelize
    data class SendToGroup(
        val target: SentToGroupTarget,
        val itemIds: List<Long>,
        val defaultSelection: List<Long>,
        val chooserTitle: String?,
    ) : ContactSelectionMode

    val isMultiple: Boolean
        get() = when (this) {
            is Pick -> multiple
            is PickPhoneNumber -> multiple
            is PickEmail -> multiple
            is AddGroupMembers -> true
            is SendToGroup -> true
            else -> false
        }

    val isLegacy: Boolean
        get() = when (this) {
            is Pick -> legacy
            is PickPhoneNumber -> legacy
            is PickPostal -> legacy
            else -> false
        }

    val isSearchSupported: Boolean
        get() = this !is PickPostal

    val canCreateContact: Boolean
        get() = (this is Pick && !isMultiple && !isLegacy && searchQuery == null) ||
            this is InsertOrEdit
}
