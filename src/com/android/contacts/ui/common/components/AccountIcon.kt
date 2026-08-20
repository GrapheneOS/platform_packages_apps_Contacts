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
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.core.ContactsPreviewColumn
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
internal fun AccountIcon(account: AccountUiModel) {
    val context = LocalContext.current
    Image(
        painter = account.getDisplayIcon(context)
            ?.let { rememberDrawablePainter(it) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription set in parent
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(24.dp),
    )
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
