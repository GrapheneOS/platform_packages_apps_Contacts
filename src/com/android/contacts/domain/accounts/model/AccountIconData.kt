package com.android.contacts.domain.accounts.model

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.model.account.AccountType

internal data class AccountIconData(
    @StringRes val titleRes: Int = R.string.account_phone,
    @DrawableRes val iconRes: Int,
    val syncAdapterPackageName: String? = null,
    val applyGrayTint: Boolean = false,
) {
    fun getDisplayIcon(context: Context): Drawable? {
        val icon = AccountType.getDisplayIcon(
            context,
            titleRes,
            iconRes,
            syncAdapterPackageName,
        )
        if (applyGrayTint) {
            icon.mutate().apply {
                colorFilter = BlendModeColorFilter(
                    context.getColor(R.color.actionbar_icon_color_grey),
                    BlendMode.SRC_ATOP,
                )
            }
        }
        return icon
    }
}
