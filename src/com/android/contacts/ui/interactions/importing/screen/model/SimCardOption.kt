package com.android.contacts.ui.interactions.importing.screen.model

import androidx.compose.runtime.Immutable

@Immutable
internal data class SimCardOption(
    val subscriptionId: Int,
    val name: String? = null,
    val contactsCount: Int? = null,
    val phone: String? = null,
)
