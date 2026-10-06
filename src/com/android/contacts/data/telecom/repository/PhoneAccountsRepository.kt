package com.android.contacts.data.telecom.repository

import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.android.contacts.data.telecom.model.CallingSim
import com.android.contacts.data.telecom.model.PhoneAccountId
import com.android.contacts.di.core.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface PhoneAccountsRepository {
    suspend fun getCallCapableSims(): List<CallingSim>
}

internal class PhoneAccountsRepositoryImpl @Inject constructor(
    private val telecomManager: TelecomManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PhoneAccountsRepository {

    override suspend fun getCallCapableSims(): List<CallingSim> {
        return withContext(ioDispatcher) {
            callCapableHandles().mapNotNull { handle ->
                toCallingSim(handle)
            }
        }
    }

    private fun callCapableHandles(): List<PhoneAccountHandle> {
        return try {
            telecomManager.callCapablePhoneAccounts
        } catch (error: SecurityException) {
            Log.w(TAG, "Could not read the call capable phone accounts", error)
            emptyList()
        }
    }

    private fun toCallingSim(handle: PhoneAccountHandle): CallingSim? {
        val account = telecomManager.getPhoneAccount(handle)
        val label = account?.label?.toString()?.takeIf { value -> value.isNotBlank() }
            ?: account?.address?.schemeSpecificPart

        return when {
            label != null -> CallingSim(
                accountId = PhoneAccountId(
                    componentName = handle.componentName.flattenToString(),
                    id = handle.id,
                ),
                label = label,
            )

            else -> null
        }
    }

    private companion object {
        const val TAG = "PhoneAccountsRepository"
    }
}
