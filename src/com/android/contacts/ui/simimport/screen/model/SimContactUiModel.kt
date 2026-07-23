package com.android.contacts.ui.simimport.screen.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class SimContactUiModel(
    val recordNumber: Int,
    val name: String? = null,
    val phone: String? = null,
    val emails: ImmutableList<String>? = null,
) {
    val label: String
        get() = when {
            !name.isNullOrBlank() -> name
            !phone.isNullOrBlank() -> phone
            !emails.isNullOrEmpty() -> emails[0]
            // This isn't really possible because we skip empty SIM contacts during loading
            else -> ""
        }
}
