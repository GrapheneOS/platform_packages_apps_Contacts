package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.data.contactdetails.model.ContactPhoto

@Immutable
internal data class ContactHeaderUiModel(
    val displayName: String,
    val phoneticName: String?,
    val photo: ContactPhoto?,
    val avatarSeed: String?,
    val isBusiness: Boolean,
)
