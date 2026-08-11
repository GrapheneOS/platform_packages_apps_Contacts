package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.data.contactdetails.model.ContactLinkOperation

@Immutable
internal data class ContactDetailsUiState(
    val content: ContactDetailsContent = ContactDetailsContent.Loading,
    val linkProgress: ContactLinkOperation? = null,
)
