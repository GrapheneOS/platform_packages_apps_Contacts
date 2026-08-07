package com.android.contacts.ui.settings.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.android.contacts.ui.settings.about.ui.AboutScreen
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsNavRoute
import com.android.contacts.ui.settings.screen.model.SettingsUiState

@Composable
internal fun SettingsContent(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    var route by rememberSaveable(stateSaver = SettingsNavRouteSaver) {
        mutableStateOf(SettingsNavRoute.Main)
    }

    BackHandler(enabled = route != SettingsNavRoute.Main) {
        route = SettingsNavRoute.Main
    }

    SettingsNavHost(
        route = route,
        uiState = uiState,
        onAction = onAction,
        onNavigateBack = onNavigateBack,
        onRouteChange = { route = it },
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun SettingsNavHost(
    route: SettingsNavRoute,
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    onNavigateBack: () -> Unit,
    onRouteChange: (SettingsNavRoute) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(
        targetState = route,
        transitionSpec = { routeTransition() },
        label = "settings_navigation",
        modifier = modifier,
    ) { currentRoute ->
        when (currentRoute) {
            SettingsNavRoute.Main -> {
                SettingsMainScreen(
                    uiState = uiState,
                    onAction = onAction,
                    onNavigateBack = onNavigateBack,
                    onNavigateToAbout = { onRouteChange(SettingsNavRoute.About) },
                    snackbarHostState = snackbarHostState,
                )
            }

            SettingsNavRoute.About -> {
                AboutScreen(
                    buildVersion = uiState.buildVersion,
                    onLicensesClick = { onAction(Action.LicensesClicked) },
                    onNavigateBack = { onRouteChange(SettingsNavRoute.Main) },
                )
            }
        }
    }
}
