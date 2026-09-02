package com.android.contacts.ui.vcardexport.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import androidx.core.text.BidiFormatter
import androidx.core.text.TextDirectionHeuristicsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.contacts.R
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardAction as Action
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardEffect
import com.android.contacts.vcard.VCardService

@Composable
internal fun ExportVCardDialog(
    effectHandler: ExportVCardEffectHandler,
    screenModel: ExportVCardScreenModel = viewModel<ExportVCardViewModel>(),
) {
    val uiState by screenModel.uiState.collectAsStateWithLifecycle()

    ExportVCardDialogEffects(
        screenModel,
        effectHandler,
    )

    ExportVCardContent(
        uiState = uiState,
        onAction = screenModel::onAction,
    )
}

@Composable
private fun ExportVCardDialogEffects(
    screenModel: ExportVCardScreenModel,
    effectHandler: ExportVCardEffectHandler,
) {
    val effectHandler by rememberUpdatedState(effectHandler)
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { screenModel.onAction(Action.PermissionRequestFinished) }
    val exportTitle = stringResource(R.string.exporting_vcard_filename)
    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.CreateDocument(VCardService.X_VCARD_MIME_TYPE) {
            override fun createIntent(
                context: Context,
                input: String,
            ): Intent {
                return super.createIntent(context, input)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .putExtra(
                        Intent.EXTRA_TITLE,
                        BidiFormatter.getInstance()
                            .unicodeWrap(exportTitle, TextDirectionHeuristicsCompat.LTR),
                    )
            }
        },
    ) { screenModel.onAction(Action.FileSelected(it)) }

    LaunchedEffect(screenModel) {
        screenModel.effects.collect { effect ->
            when (effect) {
                is ExportVCardEffect.RequestPermissions ->
                    permissionsLauncher.launch(effect.permissions.toTypedArray())

                is ExportVCardEffect.SelectFile ->
                    selectFileLauncher.launch(
                        BidiFormatter.getInstance()
                            .unicodeWrap(exportTitle, TextDirectionHeuristicsCompat.LTR),
                    )

                else ->
                    effectHandler.handle(effect)
            }
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        screenModel.onResume()
    }
}
