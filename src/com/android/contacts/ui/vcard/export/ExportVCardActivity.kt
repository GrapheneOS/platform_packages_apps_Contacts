package com.android.contacts.ui.vcard.export

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.vcard.export.screen.ExportVCardDialog
import com.android.contacts.ui.vcard.export.screen.ExportVCardEffectHandlerImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExportVCardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = ExportVCardEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                ExportVCardDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }
}
