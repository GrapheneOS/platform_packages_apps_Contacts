package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.contactdetails.model.ContactEntryAction

@Immutable
internal data class ContactEntryUiModel(
    val id: Long,
    val icon: ContactEntryIcon?,
    val header: String?,
    val isHeaderLtr: Boolean,
    val subHeader: String?,
    val text: String?,
    val isSuperPrimary: Boolean,
    val isDefaultChangeable: Boolean,
    val action: ContactEntryAction?,
    val alternateAction: ContactEntryActionUiModel?,
    val thirdAction: ContactEntryActionUiModel?,
    val copyText: String?,
    val copyLabel: String?,
)
