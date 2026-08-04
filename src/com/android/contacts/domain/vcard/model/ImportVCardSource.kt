package com.android.contacts.domain.vcard.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImportVCardSource(
    val uri: Uri,
    val name: String,
) : Parcelable
