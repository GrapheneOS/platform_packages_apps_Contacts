package com.android.contacts.ui.contactdetails.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import com.android.contacts.ui.common.components.cellShape
import com.android.contacts.ui.contactdetails.common.ContactDetailsAccountRow
import com.android.contacts.ui.contactdetails.common.ContactDetailsActionRow
import com.android.contacts.ui.contactdetails.common.ContactDetailsRecentCallRow
import com.android.contacts.ui.contactdetails.common.ContactDetailsTokens as Tokens
import com.android.contacts.ui.contactdetails.common.imageVector
import com.android.contacts.ui.contactdetails.screen.model.CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX
import com.android.contacts.ui.contactdetails.screen.model.ContactAccountUiModel
import com.android.contacts.ui.contactdetails.screen.model.ContactDetailsAction as Action
import com.android.contacts.ui.contactdetails.screen.model.ContactSettingUiModel
import com.android.contacts.ui.contactdetails.screen.model.RecentCallUiModel
import kotlinx.collections.immutable.ImmutableList

@Composable
internal fun ContactDetailsSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Tokens.cardSpacing),
        modifier = modifier,
    ) {
        ContactDetailsSectionHeader(title = title)

        content()
    }
}

@Composable
private fun ContactDetailsSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = cellShape(
            isFirst = true,
            isLast = false,
        ),
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(
                horizontal = Tokens.rowHorizontalPadding,
                vertical = Tokens.sectionHeaderPadding,
            ),
        )
    }
}

@Composable
internal fun ContactDetailsAccounts(
    accounts: ImmutableList<ContactAccountUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Tokens.cardSpacing),
        modifier = modifier,
    ) {
        accounts.forEachIndexed { index, account ->
            ContactDetailsAccountRow(
                account = account,
                isFirst = index == 0,
                isLast = index == accounts.lastIndex,
            )
        }
    }
}

@Composable
internal fun ContactDetailsRecentCalls(
    recentCalls: ImmutableList<RecentCallUiModel>,
    onRecentCallClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Tokens.cardSpacing),
        modifier = modifier,
    ) {
        recentCalls.forEachIndexed { index, recentCall ->
            ContactDetailsRecentCallRow(
                recentCall = recentCall,
                isLast = index == recentCalls.lastIndex,
                onClick = onRecentCallClick,
            )
        }
    }
}

@Composable
internal fun ContactDetailsSettings(
    settings: ImmutableList<ContactSettingUiModel>,
    onAction: (Action) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Tokens.cardSpacing),
        modifier = modifier,
    ) {
        settings.forEachIndexed { index, setting ->
            val isChecked = setting.isChecked

            ContactDetailsActionRow(
                icon = setting.icon.imageVector(),
                title = setting.title,
                subtitle = setting.subtitle,
                contentColor = when {
                    setting.isDestructive -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                },
                isFirst = false,
                isLast = index == settings.lastIndex,
                onClick = { onAction(setting.action) },
                trailingContent = isChecked?.let { checked ->
                    { SettingSwitch(isChecked = checked) }
                },
                modifier = Modifier.testTag(settingTestTag(setting)),
            )
        }
    }
}

@Composable
private fun SettingSwitch(isChecked: Boolean) {
    Switch(
        checked = isChecked,
        onCheckedChange = null,
        modifier = Modifier.semantics {
            toggleableState = ToggleableState(isChecked)
        },
    )
}

private fun settingTestTag(setting: ContactSettingUiModel): String {
    return CONTACT_DETAILS_SETTING_TEST_TAG_PREFIX + setting.icon.name
}
