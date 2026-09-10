package com.android.contacts.ui.common.components

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.account.AccountType
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter

internal val AccountIconSize = 24.dp

@Composable
internal fun AccountIcon(
    account: AccountUiModel,
    modifier: Modifier = Modifier,
) {
    AccountIcon(
        iconData = account.iconData,
        modifier = modifier,
    )
}

@Composable
internal fun AccountIcon(
    iconData: AccountIconData?,
    modifier: Modifier = Modifier,
    size: Dp = AccountIconSize,
) {
    val context = LocalContext.current
    val icon = iconData?.let { data -> displayIcon(context, data) }

    Image(
        painter = icon
            ?.let { drawable -> rememberDrawablePainter(drawable) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription set in parent
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = modifier.size(size),
    )
}

private fun displayIcon(
    context: Context,
    iconData: AccountIconData,
): Drawable? {
    val icon = AccountType.getDisplayIcon(
        context,
        iconData.titleRes,
        iconData.iconRes,
        iconData.syncAdapterPackageName,
    )

    return when {
        icon == null || !iconData.applyGrayTint -> icon

        else -> icon.mutate().apply {
            colorFilter = BlendModeColorFilter(
                context.getColor(R.color.actionbar_icon_color_grey),
                BlendMode.SRC_ATOP,
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AccountIconPreview() {
    fun buildAccount(iconData: AccountIconData?): AccountUiModel {
        val account1 = AccountModel(name = "user@example.org")
        return AccountUiModel(account = account1, name = account1.name, iconData = iconData)
    }

    ContactsPreviewColumn {
        // Empty
        AccountIcon(buildAccount(iconData = null))

        // SIM
        AccountIcon(
            buildAccount(
                iconData = AccountIconData(
                    iconRes = R.drawable.quantum_ic_sim_card_vd_theme_24,
                    applyGrayTint = true,
                ),
            ),
        )

        // Fallback
        AccountIcon(
            buildAccount(
                iconData = AccountIconData(
                    iconRes = R.drawable.quantum_ic_smartphone_vd_theme_24,
                    applyGrayTint = true,
                ),
            ),
        )
    }
}
