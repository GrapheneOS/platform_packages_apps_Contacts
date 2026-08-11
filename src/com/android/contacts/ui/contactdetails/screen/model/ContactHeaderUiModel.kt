package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.ui.common.components.ContactAvatarImage

@Immutable
internal data class ContactHeaderUiModel(
    val displayName: String,
    val phoneticName: String?,
    val photo: ContactAvatarImage?,
    val avatarSeed: String?,
    val isBusiness: Boolean,
)
