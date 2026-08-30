package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class ContactAccountUiModel(
    val name: String,
    val iconUri: String?,
)
