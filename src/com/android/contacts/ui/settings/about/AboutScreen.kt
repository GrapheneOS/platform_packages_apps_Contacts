package com.android.contacts.ui.settings.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.settings.common.SETTINGS_CELL_SPACING
import com.android.contacts.ui.settings.common.SETTINGS_HORIZONTAL_PADDING
import com.android.contacts.ui.settings.common.SETTINGS_TOP_PADDING
import com.android.contacts.ui.settings.common.SettingsCell
import com.android.contacts.ui.settings.common.SettingsTopAppBar
import com.android.contacts.ui.settings.screen.model.ABOUT_BUILD_VERSION_TEST_TAG
import com.android.contacts.ui.settings.screen.model.ABOUT_LICENSES_TEST_TAG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreen(
    buildVersion: String?,
    onBuildVersionLongClick: () -> Unit,
    onLicensesClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            SettingsTopAppBar(
                title = stringResource(R.string.setting_about),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
    ) { contentPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(SETTINGS_CELL_SPACING),
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    PaddingValues(
                        start = SETTINGS_HORIZONTAL_PADDING +
                            contentPadding.calculateStartPadding(layoutDirection),
                        end = SETTINGS_HORIZONTAL_PADDING +
                            contentPadding.calculateEndPadding(layoutDirection),
                        top = SETTINGS_TOP_PADDING + contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                ),
        ) {
            SettingsCell(
                title = stringResource(R.string.about_build_version),
                summary = buildVersion,
                isFirst = true,
                isLast = false,
                onLongClick = onBuildVersionLongClick.takeIf { buildVersion != null },
                onLongClickLabel = stringResource(R.string.copy_text),
                modifier = Modifier.testTag(ABOUT_BUILD_VERSION_TEST_TAG),
            )
            SettingsCell(
                title = stringResource(R.string.about_open_source_licenses),
                summary = stringResource(R.string.about_open_source_licenses_summary),
                isFirst = false,
                isLast = true,
                onClick = onLicensesClick,
                modifier = Modifier.testTag(ABOUT_LICENSES_TEST_TAG),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AboutScreenPreview() {
    ContactsPreviewTheme {
        AboutScreen(
            buildVersion = "1.7.40",
            onBuildVersionLongClick = {},
            onLicensesClick = {},
            onNavigateBack = {},
        )
    }
}
