package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class ContactSettingUiModel(
    val icon: ContactSettingIcon,
    val title: String,
    val subtitle: String?,
    val action: ContactDetailsAction,
    val isDestructive: Boolean,
)
