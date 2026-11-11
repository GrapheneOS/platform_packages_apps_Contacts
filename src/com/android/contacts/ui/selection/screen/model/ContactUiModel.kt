package com.android.contacts.ui.selection.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.contacts.model.ContactItemKey
import com.android.contacts.ui.common.components.ContactAvatarImage

@Immutable
internal data class ContactUiModel(
    val key: ContactItemKey,
    val name: String,
    val description: String? = null,
    val avatarImage: ContactAvatarImage? = null,
    val isSelected: Boolean = false,
)
