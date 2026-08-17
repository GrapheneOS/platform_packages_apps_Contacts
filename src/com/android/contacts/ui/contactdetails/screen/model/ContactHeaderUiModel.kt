package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.ui.common.components.ContactAvatarImage
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class ContactHeaderUiModel(
    val displayName: String,
    val subtitles: ImmutableList<String>,
    val photo: ContactAvatarImage?,
    val avatarSeed: String?,
    val isBusiness: Boolean,
    val isDisplayNameLtr: Boolean,
)
