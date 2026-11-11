package com.android.contacts.ui.selection.screen.model

import com.android.contacts.domain.contacts.model.ContactItemKey

internal sealed interface ContactSelectionAction {

    data object CloseClick : ContactSelectionAction

    data object SearchClick : ContactSelectionAction

    data object SubmitClick : ContactSelectionAction

    data class SearchQueryChange(
        val query: String,
    ) : ContactSelectionAction

    data class ContactClick(
        val key: ContactItemKey,
    ) : ContactSelectionAction

    data object CreateClick : ContactSelectionAction
}
