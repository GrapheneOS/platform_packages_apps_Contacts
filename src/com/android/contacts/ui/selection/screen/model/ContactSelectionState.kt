package com.android.contacts.ui.selection.screen.model

import android.os.Parcelable
import com.android.contacts.domain.contacts.model.ContactItemKey
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ContactSelectionState(
    val searchState: ContactSelectionSearchState = ContactSelectionSearchState.Unavailable,
    val selectedKeys: Set<ContactItemKey> = emptySet(),
) : Parcelable
