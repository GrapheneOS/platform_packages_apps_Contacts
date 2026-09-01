package com.android.contacts.ui.interactions.account

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.account.screen.SelectAccountDialog
import com.android.contacts.ui.interactions.account.screen.SelectAccountEffectHandlerImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SelectAccountActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = SelectAccountEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                SelectAccountDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    internal companion object {
        const val EXTRA_ACCOUNT = "account"
    }
}
