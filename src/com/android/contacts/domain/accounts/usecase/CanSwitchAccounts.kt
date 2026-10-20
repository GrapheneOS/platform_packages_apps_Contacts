package com.android.contacts.domain.accounts.usecase

import com.android.contacts.domain.accounts.model.AccountFilter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal interface CanSwitchAccounts {
    operator fun invoke(): Flow<Boolean>

    // Temporary alternative for Java interoperability
    fun invokeWithCallback(callback: (Boolean) -> Unit): Cancellable

    fun interface Cancellable {
        fun cancel()
    }
}

internal class CanSwitchAccountsImpl @Inject constructor(
    private val loadAccounts: LoadAccounts,
) : CanSwitchAccounts {
    override operator fun invoke(): Flow<Boolean> {
        return loadAccounts(AccountFilter.DRAWER_DISPLAYABLE)
            .map { it.size > 1 }
            .distinctUntilChanged()
    }

    override fun invokeWithCallback(callback: (Boolean) -> Unit): CanSwitchAccounts.Cancellable {
        val job = invoke()
            .onEach(callback)
            .launchIn(CoroutineScope(Dispatchers.Default))
        return CanSwitchAccounts.Cancellable { job.cancel() }
    }
}
