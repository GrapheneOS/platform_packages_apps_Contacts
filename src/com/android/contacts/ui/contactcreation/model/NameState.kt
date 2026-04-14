package com.android.contacts.ui.contactcreation.model

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
internal data class NameState(
    val prefix: String = "",
    val first: String = "",
    val middle: String = "",
    val last: String = "",
    val suffix: String = "",
) : Parcelable {
    fun hasData(): Boolean =
        prefix.isNotBlank() || first.isNotBlank() ||
            middle.isNotBlank() || last.isNotBlank() || suffix.isNotBlank()
}
