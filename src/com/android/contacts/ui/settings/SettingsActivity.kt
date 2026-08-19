package com.android.contacts.ui.settings

import android.content.ClipboardManager
import android.os.Bundle
import android.telecom.TelecomManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.editor.SelectAccountDialogFragment
import com.android.contacts.interactions.ImportDialogFragment
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.settings.screen.SettingsEffectHandlerImpl
import com.android.contacts.ui.settings.screen.SettingsScreen
import com.android.contacts.util.AccountSelectionUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity :
    ComponentActivity(),
    SelectAccountDialogFragment.Listener {

    @Inject
    lateinit var telecomManager: TelecomManager

    @Inject
    lateinit var clipboardManager: ClipboardManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val effectHandler = SettingsEffectHandlerImpl(
            activity = this,
            newLocalProfileExtra = intent.getStringExtra(EXTRA_NEW_LOCAL_PROFILE),
            telecomManager = telecomManager,
            clipboardManager = clipboardManager,
        )

        setContent {
            AppTheme {
                SettingsScreen(
                    effectHandler = effectHandler,
                    onNavigateBack = ::finish,
                )
            }
        }
    }

    override fun onAccountChosen(
        account: AccountWithDataSet,
        extraArgs: Bundle,
    ) {
        AccountSelectionUtil.doImport(
            this,
            extraArgs.getInt(ImportDialogFragment.KEY_RES_ID),
            account,
            extraArgs.getInt(ImportDialogFragment.KEY_SUBSCRIPTION_ID),
        )
    }

    override fun onAccountSelectorCancelled() = Unit

    companion object {
        const val EXTRA_NEW_LOCAL_PROFILE: String = "newLocalProfile"
    }
}
