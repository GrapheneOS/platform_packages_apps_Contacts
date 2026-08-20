package com.android.contacts.domain.accounts.model

import androidx.annotation.StringRes
import com.android.contacts.R

internal data class AccountIconData(
    @StringRes val titleRes: Int = R.string.account_phone,
    val iconRes: Int,
    val syncAdapterPackageName: String? = null,
    val applyGrayTint: Boolean = false,
)
