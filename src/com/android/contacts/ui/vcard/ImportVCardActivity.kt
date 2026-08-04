package com.android.contacts.ui.vcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.vcard.screen.ImportVCardDialog
import com.android.contacts.ui.vcard.screen.ImportVCardEffectHandlerImpl
import com.android.contacts.ui.vcard.screen.ImportVCardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImportVCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent.putExtra(ImportVCardViewModel.KEY_INITIAL_FILE, intent.data)

        val effectHandler = ImportVCardEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                ImportVCardDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }
}
