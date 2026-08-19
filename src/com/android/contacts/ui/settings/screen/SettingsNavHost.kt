package com.android.contacts.ui.settings.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.android.contacts.ui.settings.about.AboutScreen
import com.android.contacts.ui.settings.screen.model.SettingsAction as Action
import com.android.contacts.ui.settings.screen.model.SettingsNavRoute
import com.android.contacts.ui.settings.screen.model.SettingsUiState

@Composable
internal fun SettingsNavHost(
    uiState: SettingsUiState,
    onAction: (Action) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val stateHolder = rememberSaveableStateHolder()
    var route by rememberSaveable(stateSaver = SettingsNavRouteSaver) {
        mutableStateOf(SettingsNavRoute.Main)
    }

    BackHandler(enabled = route != SettingsNavRoute.Main) {
        route = SettingsNavRoute.Main
    }

    AnimatedContent(
        targetState = route,
        transitionSpec = { routeTransition() },
        label = "settings_navigation",
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) { currentRoute ->
        stateHolder.SaveableStateProvider(currentRoute.key) {
            when (currentRoute) {
                SettingsNavRoute.Main -> {
                    SettingsMainScreen(
                        uiState = uiState,
                        onAction = onAction,
                        onNavigateBack = onNavigateBack,
                        onNavigateToAbout = { route = SettingsNavRoute.About },
                        snackbarHostState = snackbarHostState,
                    )
                }

                SettingsNavRoute.About -> {
                    AboutScreen(
                        buildVersion = uiState.buildVersion,
                        onBuildVersionLongClick = { onAction(Action.BuildVersionLongClicked) },
                        onLicensesClick = { onAction(Action.LicensesClicked) },
                        onNavigateBack = { route = SettingsNavRoute.Main },
                        snackbarHostState = snackbarHostState,
                    )
                }
            }
        }
    }
}
