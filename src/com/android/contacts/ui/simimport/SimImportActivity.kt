package com.android.contacts.ui.simimport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.ui.core.AppScaffold
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.simimport.screen.SimImportEffectHandlerImpl
import com.android.contacts.ui.simimport.screen.SimImportScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SimImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = SimImportEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                AppScaffold {
                    SimImportScreen(
                        effectHandler = effectHandler,
                    )
                }
            }
        }
    }
}
