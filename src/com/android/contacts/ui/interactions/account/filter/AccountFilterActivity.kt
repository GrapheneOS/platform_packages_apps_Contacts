package com.android.contacts.ui.interactions.account.filter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.android.contacts.domain.accounts.model.ContactsAccountFilter
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.account.filter.screen.AccountFilterDialog
import com.android.contacts.ui.interactions.account.filter.screen.AccountFilterEffectHandlerImpl
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFilterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = AccountFilterEffectHandlerImpl(
            activity = this,
        )

        setContent {
            AppTheme {
                AccountFilterDialog(
                    effectHandler = effectHandler,
                )
            }
        }
    }

    internal companion object {
        const val EXTRA_FILTER = "filter"

        internal fun buildIntent(context: Context, currentFilter: ContactsAccountFilter): Intent {
            return Intent(context, AccountFilterActivity::class.java)
                .putExtra(EXTRA_FILTER, currentFilter)
        }
    }
}
