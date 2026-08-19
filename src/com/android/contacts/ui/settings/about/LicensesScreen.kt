package com.android.contacts.ui.settings.about

import android.webkit.WebView
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.viewinterop.AndroidView
import com.android.contacts.R
import com.android.contacts.ui.core.ContactsPreviewTheme
import com.android.contacts.ui.settings.common.SettingsTopAppBar
import com.android.contacts.ui.settings.screen.model.ABOUT_LICENSES_SCREEN_TEST_TAG

private const val LICENSE_FILE = "file:///android_asset/licenses.html"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LicensesScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    Scaffold(
        topBar = {
            SettingsTopAppBar(
                title = stringResource(R.string.activity_title_licenses),
                onNavigateBack = onNavigateBack,
            )
        },
        modifier = modifier.testTag(ABOUT_LICENSES_SCREEN_TEST_TAG),
    ) { contentPadding ->
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding(),
                    bottom = contentPadding.calculateBottomPadding(),
                ),
            factory = { context ->
                WebView(context).apply {
                    loadUrl(LICENSE_FILE)
                }
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun LicensesScreenPreview() {
    ContactsPreviewTheme {
        LicensesScreen(
            onNavigateBack = {},
        )
    }
}
