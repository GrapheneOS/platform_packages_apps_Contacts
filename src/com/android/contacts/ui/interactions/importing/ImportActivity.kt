package com.android.contacts.ui.interactions.importing

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.editor.SelectAccountDialogFragment
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.importing.screen.ImportDialog
import com.android.contacts.ui.interactions.importing.screen.ImportEffectHandlerImpl
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class ImportActivity :
    FragmentActivity(),
    SelectAccountDialogFragment.Listener {

    @Inject
    internal lateinit var accountModelMapper: AccountModelMapper

    // The Activity finished once an Account is chosen, so we don't need to worry about
    // the same account being chosen multiple times.
    private val accountChosen = MutableStateFlow<AccountModel?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val effectHandler = ImportEffectHandlerImpl(
            activity = this,
        )

        setContent {
            val accountChosen by accountChosen.collectAsStateWithLifecycle()
            AppTheme {
                ImportDialog(
                    effectHandler = effectHandler,
                    accountChosen = accountChosen,
                )
            }
        }
    }

    override fun onAccountChosen(
        account: AccountWithDataSet?,
        extraArgs: Bundle?,
    ) {
        accountChosen.value = account?.let(accountModelMapper::map)
    }

    override fun onAccountSelectorCancelled() {
        // No need to do anything
    }
}
