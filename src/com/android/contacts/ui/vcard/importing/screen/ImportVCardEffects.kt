package com.android.contacts.ui.vcard.importing.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.android.contacts.R
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.ui.interactions.account.SelectAccountActivity
import com.android.contacts.ui.vcard.importing.screen.model.ImportVCardAction
import com.android.contacts.ui.vcard.importing.screen.model.ImportVCardEffect
import com.android.contacts.vcard.VCardService

@Composable
internal fun ImportVCardEffects(
    screenModel: ImportVCardScreenModel,
    effectHandler: ImportVCardEffectHandler,
) {
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { screenModel.onAction(ImportVCardAction.PermissionRequestFinished) }

    val selectAccountLauncher = rememberLauncherForActivityResult(
        contract = SelectAccountActivity.Contract(),
    ) { screenModel.onAction(ImportVCardAction.AccountSelected(it)) }

    val selectFilesLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContracts.OpenMultipleDocuments() {
            override fun createIntent(
                context: Context,
                input: Array<String>,
            ): Intent {
                return super.createIntent(context, input)
                    .addCategory(Intent.CATEGORY_OPENABLE)
            }
        },
    ) { screenModel.onAction(ImportVCardAction.FilesSelected(it)) }

    LaunchedEffect(screenModel) {
        screenModel.effects
            .collect { effect ->
                when (effect) {
                    is ImportVCardEffect.RequestPermissions -> {
                        permissionsLauncher.launch(effect.permissions.toTypedArray())
                    }

                    is ImportVCardEffect.SelectAccount -> {
                        val request = SelectAccountActivity.Contract.Request(
                            titleResId = R.string.dialog_new_contact_account,
                            accountFilter = AccountTypeManager.AccountFilter.CONTACTS_INSERTABLE,
                        )
                        selectAccountLauncher.launch(request)
                    }

                    is ImportVCardEffect.SelectFiles -> {
                        selectFilesLauncher.launch(arrayOf(VCardService.X_VCARD_MIME_TYPE))
                    }

                    is ImportVCardEffect.OneOff -> {
                        effectHandler.handle(effect)
                    }
                }
            }
    }
}
