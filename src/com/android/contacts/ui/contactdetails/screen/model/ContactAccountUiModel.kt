package com.android.contacts.ui.contactdetails.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountIconData

@Immutable
internal data class ContactAccountUiModel(
    val name: String,
    val iconData: AccountIconData?,
)
