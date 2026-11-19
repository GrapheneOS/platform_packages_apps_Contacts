package com.android.contacts.ui.group.detail.screen.model

import android.os.Parcelable
import com.android.contacts.domain.contacts.model.ContactItemKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class GroupState(
    val selectedKeys: Set<ContactItemKey> = emptySet(),
    val isDeleteConfirmationVisible: Boolean = false,
) : Parcelable
