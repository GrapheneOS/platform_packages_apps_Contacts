package com.android.contacts.ui.interactions.importing.screen.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.AnnotatedString

@Immutable
internal data class SimCardOption(
    val subscriptionId: Int,
    val name: String? = null,
    val contactsCount: Int? = null,
    val phone: AnnotatedString? = null,
)
