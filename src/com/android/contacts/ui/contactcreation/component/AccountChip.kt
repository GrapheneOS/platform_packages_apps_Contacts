package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.ContactCreationAction

internal fun LazyListScope.accountChipItem(
    accountName: String?,
    @Suppress("UNUSED_PARAMETER") onAction: (ContactCreationAction) -> Unit,
) {
    item(key = "account_chip", contentType = "account_chip") {
        AccountChip(
            accountName = accountName,
            onClick = { /* Phase 2: account picker sheet */ },
        )
    }
}

@Composable
internal fun AccountChip(
    accountName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(accountName ?: stringResource(R.string.contact_creation_device_account)) },
        modifier = modifier
            .padding(horizontal = 16.dp)
            .testTag(TestTags.ACCOUNT_CHIP),
    )
}
