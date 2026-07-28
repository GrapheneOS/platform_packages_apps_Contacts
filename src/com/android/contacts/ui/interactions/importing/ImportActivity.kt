package com.android.contacts.ui.interactions.importing

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.android.contacts.domain.accounts.mapper.AccountModelMapper
import com.android.contacts.domain.accounts.model.AccountModel
import com.android.contacts.ui.core.AppTheme
import com.android.contacts.ui.interactions.account.SelectAccountActivity
import com.android.contacts.ui.interactions.importing.screen.ImportDialog
import com.android.contacts.ui.interactions.importing.screen.ImportEffectHandlerImpl
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class ImportActivity : FragmentActivity() {

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

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == REQUEST_SELECT_ACCOUNT && resultCode == RESULT_OK -> {
                data
                    ?.getParcelableExtra(
                        SelectAccountActivity.EXTRA_ACCOUNT,
                        AccountModel::class.java
                    )
                    ?.let { accountChosen.value = it }
            }
        }
    }

    internal companion object {
        const val REQUEST_SELECT_ACCOUNT = 100
    }
}
