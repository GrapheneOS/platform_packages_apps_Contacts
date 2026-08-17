package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.contactdetails.model.ContactEntryAction

@Immutable
internal data class ContactQuickActionUiModel(
    val icon: ContactEntryIcon,
    val label: String,
    val action: ContactEntryAction?,
)
