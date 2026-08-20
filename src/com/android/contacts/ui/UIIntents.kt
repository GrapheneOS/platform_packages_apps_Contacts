package com.android.contacts.ui

import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import com.android.contacts.domain.accounts.model.AccountFilter
import com.android.contacts.ui.interactions.account.SelectAccountActivity
import com.android.contacts.ui.interactions.account.screen.SelectAccountViewModel
import com.android.contacts.ui.interactions.importing.ImportActivity
import com.android.contacts.ui.simimport.SimImportActivity

internal object UIIntents {
    const val EXTRA_SUBSCRIPTION_ID: String = "extraSubscriptionId"

    fun getImportDialogIntent(context: Context): Intent {
        return Intent(context, ImportActivity::class.java)
    }

    fun getSelectAccountDialogIntent(
        context: Context,
        @StringRes titleResId: Int? = null,
        accountFilter: AccountFilter? = null,
    ): Intent {
        return Intent(context, SelectAccountActivity::class.java)
            .putExtra(SelectAccountViewModel.KEY_TITLE_RES_ID, titleResId)
            .putExtra(SelectAccountViewModel.KEY_LIST_FILTER, accountFilter)
    }

    fun getSimImportIntent(
        context: Context,
        subscriptionId: Int?,
    ): Intent {
        return Intent(context, SimImportActivity::class.java)
            .putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
    }
}
