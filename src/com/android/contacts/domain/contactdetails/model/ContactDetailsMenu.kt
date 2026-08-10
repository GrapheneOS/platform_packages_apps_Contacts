package com.android.contacts.domain.contactdetails.model

internal data class ContactDetailsMenu(
    val isStarVisible: Boolean,
    val editAction: ContactDetailsEditAction,
    val isJoinVisible: Boolean,
    val isLinkedContactsVisible: Boolean,
    val isDeleteVisible: Boolean,
    val isShareVisible: Boolean,
    val isShortcutVisible: Boolean,
    val isRingtoneVisible: Boolean,
)
