package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.contactdetails.model.ContactEntryAction

@Immutable
internal data class ContactEntryActionUiModel(
    val action: ContactEntryAction,
    val icon: ContactEntryIcon,
    val contentDescription: String,
)
