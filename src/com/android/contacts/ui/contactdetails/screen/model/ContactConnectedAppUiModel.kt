package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal data class ContactConnectedAppUiModel(
    val packageName: String,
    val label: String,
    val iconUri: String?,
    val entries: ImmutableList<ContactEntryUiModel>,
)
