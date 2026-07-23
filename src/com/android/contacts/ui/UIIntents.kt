package com.android.contacts.ui

import android.content.Context
import android.content.Intent
import com.android.contacts.ui.interactions.importing.ImportActivity
import com.android.contacts.ui.simimport.SimImportActivity

internal object UIIntents {
    const val EXTRA_SUBSCRIPTION_ID: String = "extraSubscriptionId"

    fun getSimImportIntent(context: Context, subscriptionId: Int?) =
        Intent(context, SimImportActivity::class.java)
            .putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)

    fun getImportDialogIntent(context: Context) =
        Intent(context, ImportActivity::class.java)
}
