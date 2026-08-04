package com.android.contacts.ui.interactions.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.StringRes
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.ui.UIIntents
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

    internal class Contract : ActivityResultContract<Contract.Request, AccountModel?>() {
        override fun createIntent(
            context: Context,
            input: Request,
        ): Intent {
            return UIIntents.getSelectAccountDialogIntent(
                context = context,
                titleResId = input.titleResId,
                accountFilter = input.accountFilter,
            )
        }

        override fun parseResult(
            resultCode: Int,
            intent: Intent?,
        ): AccountModel? {
            return when (resultCode) {
                RESULT_OK -> intent?.getParcelableExtra(EXTRA_ACCOUNT, AccountModel::class.java)
                else -> null
            }
        }

        data class Request(
            @param:StringRes val titleResId: Int? = null,
            val accountFilter: AccountTypeManager.AccountFilter? = null,
        )
    }

    internal companion object {
        const val EXTRA_ACCOUNT = "account"
    }
}
