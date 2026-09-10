package com.android.contacts.domain.accounts.model

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.contacts.R
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class AccountIconData(
    @StringRes val titleRes: Int = R.string.account_phone,
    @DrawableRes val iconRes: Int,
    val syncAdapterPackageName: String? = null,
    val applyGrayTint: Boolean = false,
) : Parcelable
