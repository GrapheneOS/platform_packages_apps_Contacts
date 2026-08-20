package com.android.contacts.ui.simimport.screen.model

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.drawable.Drawable
import androidx.compose.runtime.Immutable
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.account.AccountType

@Immutable
internal data class AccountUiModel(
    val account: AccountModel,
    val name: String?,
    val type: String? = null,
    private val iconData: AccountIconData? = null,
) {
    fun getDisplayIcon(context: Context): Drawable? {
        return iconData?.let {
            val icon = AccountType.getDisplayIcon(
                context,
                it.titleRes,
                it.iconRes,
                it.syncAdapterPackageName,
            )
            if (it.applyGrayTint) {
                icon.mutate().apply {
                    colorFilter = BlendModeColorFilter(
                        context.getColor(R.color.actionbar_icon_color_grey),
                        BlendMode.SRC_ATOP,
                    )
                }
            }
            icon
        }
    }
}
