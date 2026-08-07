package com.android.contacts.ui.settings.about.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.settings.common.SETTINGS_CELL_SPACING
import com.android.contacts.ui.settings.common.SETTINGS_HORIZONTAL_PADDING
import com.android.contacts.ui.settings.common.SettingsCell
import com.android.contacts.ui.settings.common.SettingsTopAppBar
import com.android.contacts.ui.settings.screen.model.ABOUT_BUILD_VERSION_TEST_TAG
import com.android.contacts.ui.settings.screen.model.ABOUT_LICENSES_TEST_TAG

@Composable
internal fun AboutScreen(
    buildVersion: String?,
    onLicensesClick: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            SettingsTopAppBar(
                title = stringResource(R.string.setting_about),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier,
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
                        top = contentPadding.calculateTopPadding(),
                        bottom = contentPadding.calculateBottomPadding(),
                    ),
                ),
        ) {
            SettingsCell(
                title = stringResource(R.string.about_build_version),
                summary = buildVersion,
                isFirst = true,
                isLast = false,
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
            onLicensesClick = {},
            onNavigateBack = {},
        )
    }
}
