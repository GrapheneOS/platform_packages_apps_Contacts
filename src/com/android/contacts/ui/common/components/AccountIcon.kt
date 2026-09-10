package com.android.contacts.ui.common.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.domain.accounts.model.AccountIconData
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
internal fun AccountIcon(
    data: AccountIconData?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Image(
        painter = data?.getDisplayIcon(context)
            ?.let { rememberDrawablePainter(it) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription set in parent
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(24.dp),
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
            )
        )

        // Fallback
        AccountIcon(
            AccountIconData(
                iconRes = R.drawable.quantum_ic_smartphone_vd_theme_24,
                applyGrayTint = true,
            )
        )
    }
}
