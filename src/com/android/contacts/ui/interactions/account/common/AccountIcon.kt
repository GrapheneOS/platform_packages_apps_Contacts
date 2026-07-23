package com.android.contacts.ui.interactions.account.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.simimport.screen.model.AccountUiModel
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
internal fun AccountIcon(
    account: AccountUiModel,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = account.icon?.let { rememberDrawablePainter(it) }
            ?: painterResource(R.drawable.accounts_empty),
        // contentDescription should be set in parent
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.size(24.dp),
    )
}
