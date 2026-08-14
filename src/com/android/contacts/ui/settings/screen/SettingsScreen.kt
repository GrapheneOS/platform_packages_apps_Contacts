package com.android.contacts.ui.settings.screen

import android.content.res.Resources
import android.icu.text.MessageFormat
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.settings.screen.model.SettingsEffect as Effect
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
internal fun SettingsScreen(
    effectHandler: SettingsEffectHandler,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    screenModel: SettingsScreenModel = viewModel<SettingsViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()
    val effectHandler by rememberUpdatedState(effectHandler)
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        screenModel.refreshState()
    }

    LaunchedEffect(screenModel) {
        screenModel.effects.collect { effect ->
            when (effect) {
                is Effect.Message -> {
                    launch {
                        snackbarHostState.showSnackbar(
                            message = message(resources, effect),
                            duration = SnackbarDuration.Long,
                        )
                    }
                }

                is Effect.Host -> {
                    effectHandler.handle(effect)
                }
            }
        }
    }

    SettingsNavHost(
        uiState = uiState,
        onAction = screenModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

private fun message(
    resources: Resources,
    effect: Effect.Message,
): String {
    return when (effect) {
        is Effect.ShowSimImportSuccess -> simImportSuccessMessage(resources, effect.importedCount)
        Effect.ShowSimImportFailure -> resources.getString(R.string.sim_import_failed_toast)
    }
}

private fun simImportSuccessMessage(
    resources: Resources,
    importedCount: Int,
): String {
    val format = MessageFormat(
        resources.getString(R.string.sim_import_success_toast_fmt),
        Locale.getDefault(),
    )

    return format.format(mapOf("count" to importedCount))
}
