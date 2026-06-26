package com.android.contacts.sim

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.contacts.database.SimContactDao
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.preference.ContactsPreferences
import com.android.contacts.sim.ui.SimImportViewModel
import com.android.contacts.util.concurrent.ContactsExecutors
import kotlinx.coroutines.asCoroutineDispatcher

class SimImportDependencies(context: Context) {

    // Data

    private val contactsPreferences: ContactsPreferences by lazy { ContactsPreferences(context) }
    private val accountTypeManager by lazy { AccountTypeManager.getInstance(context) }
    private val simContactDao by lazy { SimContactDao.create(context) }
    private val localBroadcastManager by lazy { LocalBroadcastManager.getInstance(context) }
    private val buildBroadcastReceiverFlow by lazy {
        BuildBroadcastReceiverFlow(localBroadcastManager)
    }

    // Domain

    private val loadAccounts by lazy {
        LoadAccounts(
            loadCoroutineContext = ContactsExecutors.getSimReadExecutor().asCoroutineDispatcher(),
            buildBroadcastReceiverFlow = buildBroadcastReceiverFlow::invoke,
            context = context,
            accountTypeManager = accountTypeManager,
        )
    }
    private val loadSimContacts by lazy {
        LoadSimContacts(
            loadCoroutineContext = ContactsExecutors.getSimReadExecutor().asCoroutineDispatcher(),
            buildBroadcastReceiverFlow = buildBroadcastReceiverFlow::invoke,
            getSimBySubscriptionId = simContactDao::getSimBySubscriptionId,
            loadContactsForSim = simContactDao::loadContactsForSim,
            findAccountsOfExistingSimContacts = simContactDao::findAccountsOfExistingSimContacts,
        )
    }
    private val startSimImport by lazy { StartSimImport(context = context) }

    // UI

    fun viewModel(subscriptionId: Int) = SimImportViewModel(
        subscriptionId = subscriptionId,
        getDefaultAccount = contactsPreferences::getDefaultAccount,
        loadAccounts = loadAccounts::invoke,
        loadSimContacts = loadSimContacts::invoke,
        startSimImport = startSimImport::invoke,
    )
}
