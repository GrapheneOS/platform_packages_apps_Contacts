package com.android.contacts.ui.selection.screen.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class ContactSelectionRequest(
    val mode: ContactSelectionMode = ContactSelectionMode.Pick(),
    val titleOverride: String? = null,
) : Parcelable
