package com.android.contacts.domain.sim.usecase

import android.content.Context
import com.android.contacts.SimImportService
import com.android.contacts.model.SimContact
import com.android.contacts.model.account.AccountWithDataSet
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList

fun interface StartSimImport {
    operator fun invoke(
        subscriptionId: Int,
        contacts: ImmutableList<SimContact>,
        account: AccountWithDataSet,
    )
}

internal class StartSimImportImpl @Inject constructor(
    @param:ApplicationContext
    private val context: Context,
) : StartSimImport {
    override operator fun invoke(
        subscriptionId: Int,
        contacts: ImmutableList<SimContact>,
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
