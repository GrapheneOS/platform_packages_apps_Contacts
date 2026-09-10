package com.android.contacts.ui.common.components

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.model.account.AccountType
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.google.accompanist.drawablepainter.rememberDrawablePainter

internal val AccountIconSize = 24.dp

@Composable
internal fun AccountIcon(
    iconData: AccountIconData?,
    modifier: Modifier = Modifier,
    size: Dp = AccountIconSize,
) {
    val context = LocalContext.current
    val icon = iconData?.let { data -> displayIcon(context, data) }
    val tint = colorResource(R.color.actionbar_icon_color_grey)

    Image(
        painter = icon
            ?.let { drawable -> rememberDrawablePainter(drawable) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription set in parent
        contentDescription = null,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(tint, BlendMode.SrcAtop)
            .takeIf { icon != null && iconData.applyGrayTint },
        modifier = modifier.size(size),
    )
}

private fun displayIcon(
    context: Context,
    iconData: AccountIconData,
): Drawable? {
    return AccountType.getDisplayIcon(
        context,
        iconData.titleRes,
        iconData.iconRes,
        iconData.syncAdapterPackageName,
    )
}

@PreviewLightDark
@Composable
private fun AccountIconPreview() {
    ContactsPreviewColumn {
        // Empty
        AccountIcon(null)

        // SIM
        AccountIcon(
            AccountIconData(
                iconRes = R.drawable.quantum_ic_sim_card_vd_theme_24,
                applyGrayTint = true,
            ),
        )

        // Fallback
        AccountIcon(
            AccountIconData(
                iconRes = R.drawable.quantum_ic_smartphone_vd_theme_24,
                applyGrayTint = true,
            ),
        )
    }
}
