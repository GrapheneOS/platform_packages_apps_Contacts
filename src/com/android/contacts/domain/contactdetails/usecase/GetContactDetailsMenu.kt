package com.android.contacts.domain.contactdetails.usecase

import com.android.contacts.data.contactdetails.model.ContactCapabilities
import com.android.contacts.data.contactdetails.repository.ContactShortcutRepository
import com.android.contacts.domain.contactdetails.model.ContactDetailsEditAction
import com.android.contacts.domain.contactdetails.model.ContactDetailsMenu
import com.android.contacts.domain.contactdetails.usecase.telephony.IsDeviceVoiceCapable
import javax.inject.Inject

internal fun interface GetContactDetailsMenu {
    operator fun invoke(capabilities: ContactCapabilities): ContactDetailsMenu
}

internal class GetContactDetailsMenuImpl @Inject constructor(
    private val contactShortcutRepository: ContactShortcutRepository,
    private val isDeviceVoiceCapable: IsDeviceVoiceCapable,
) : GetContactDetailsMenu {

    override operator fun invoke(capabilities: ContactCapabilities): ContactDetailsMenu {
        val isEditable = !capabilities.isDirectoryEntry
        val isShareable = !capabilities.isDirectoryEntry
        val isRegularContact = isEditable && !capabilities.isUserProfile
        val isJoinVisible = isRegularContact &&
            !capabilities.isInvisibleAndAddable &&
            !capabilities.hasMultipleRawContacts

        return ContactDetailsMenu(
            isStarVisible = isRegularContact,
            editAction = editAction(capabilities, isEditable),
            isJoinVisible = isJoinVisible,
            isLinkedContactsVisible = capabilities.hasMultipleRawContacts && !isJoinVisible,
            isDeleteVisible = isRegularContact,
            isShareVisible = isShareable,
            isShortcutVisible = isRegularContact &&
                contactShortcutRepository.isPinShortcutSupported(),
            isRingtoneVisible = isRegularContact && isDeviceVoiceCapable(),
        )
    }

    private fun editAction(
        capabilities: ContactCapabilities,
        isEditable: Boolean,
    ): ContactDetailsEditAction {
        val isAddable = capabilities.isAddableDirectoryContact ||
            capabilities.isInvisibleAndAddable

        return when {
            isAddable -> ContactDetailsEditAction.ADD
            isEditable -> ContactDetailsEditAction.EDIT
            else -> ContactDetailsEditAction.HIDDEN
        }
    }
}
