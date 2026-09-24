package com.android.contacts.ui.editor.springboard.screen.model

import androidx.compose.runtime.Immutable
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.ui.common.components.ContactAvatarImage

@Immutable
internal data class RawContactUiModel(
    val id: Long,
    val avatarImage: ContactAvatarImage?,
    val displayName: String,
    val accountLabel: String?,
    val accountIconData: AccountIconData?,
)
