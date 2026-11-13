package com.android.contacts.ui.selection.screen.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface ContactListSection {

    data object Undefined : ContactListSection

    data object Suggestions : ContactListSection

    data object Favorites : ContactListSection

    data class Alphabetical(
        val letter: String,
    ) : ContactListSection

    data class Directory(
        val directoryId: Long,
        val name: String,
        val isLoading: Boolean = false,
    ) : ContactListSection
}
