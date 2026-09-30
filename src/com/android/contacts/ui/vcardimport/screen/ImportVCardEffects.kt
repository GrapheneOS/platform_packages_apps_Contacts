package com.android.contacts.ui.vcardimport.screen

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.android.contacts.R
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.ui.interactions.account.SelectAccountActivity
import com.android.contacts.ui.vcardimport.screen.model.ImportVCardAction as Action
import com.android.contacts.ui.vcardimport.screen.model.ImportVCardEffect as Effect
import com.android.contacts.vcard.VCardService

@Composable
internal fun ImportVCardEffects(
    screenModel: ImportVCardScreenModel,
    effectHandler: ImportVCardEffectHandler,
) {
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { screenModel.onAction(Action.PermissionRequestFinished) }

    val selectAccountLauncher = rememberLauncherForActivityResult(
        contract = SelectAccountActivity.Contract(),
    ) { screenModel.onAction(Action.AccountSelected(it)) }

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
    ) { screenModel.onAction(Action.FilesSelected(it)) }

    LaunchedEffect(screenModel) {
        screenModel.effects
            .collect { effect ->
                when (effect) {
                    is Effect.RequestPermissions -> {
                        permissionsLauncher.launch(effect.permissions.toTypedArray())
                    }

                    is Effect.SelectAccount -> {
                        val request = SelectAccountActivity.Contract.Request(
                            titleResId = R.string.dialog_new_contact_account,
                            accountFilter = AccountTypeManager.AccountFilter.CONTACTS_INSERTABLE,
                        )
                        selectAccountLauncher.launch(request)
                    }

                    is Effect.SelectFiles -> {
                        selectFilesLauncher.launch(arrayOf(VCardService.X_VCARD_MIME_TYPE))
                    }

                    is Effect.OneOff -> {
                        effectHandler.handle(effect)
                    }
                }
            }
    }
}
