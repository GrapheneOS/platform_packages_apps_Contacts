package com.android.contacts.ui.selection.screen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

internal sealed interface ContactSelectionSearchState : Parcelable {

    @Parcelize
    data object Unavailable : ContactSelectionSearchState

    @Parcelize
    data object Regular : ContactSelectionSearchState

    @Parcelize
    data class Searching(
        val query: String,
    ) : ContactSelectionSearchState

    val effectiveQuery
        get() = (this as? Searching)
            ?.query
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
}
