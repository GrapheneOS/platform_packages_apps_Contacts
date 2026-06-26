package com.android.contacts.sim

import android.content.Context
import com.android.contacts.SimImportService
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountWithDataSet

class StartSimImport(private val context: Context) {
    operator fun invoke(
        subscriptionId: Int,
        contacts: List<SimContact>,
        account: AccountWithDataSet,
    ) {
        SimImportService.startImport(
            context,
            subscriptionId,
            ArrayList(contacts),
            account,
        )
    }
}
