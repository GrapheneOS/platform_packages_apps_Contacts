package com.android.contacts.data.callsubject.repository

import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import android.util.Log
import com.android.contacts.data.callsubject.model.CallSubjectConfiguration
import com.android.contacts.data.callsubject.model.DEFAULT_CALL_SUBJECT_LIMIT
import com.android.contacts.di.core.IoDispatcher
import java.nio.charset.Charset
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal fun interface GetCallSubjectConfiguration {
    suspend operator fun invoke(phoneAccountHandle: PhoneAccountHandle?): CallSubjectConfiguration
}

internal class GetCallSubjectConfigurationImpl @Inject constructor(
    private val telecomManager: TelecomManager,
    @param:IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
) : GetCallSubjectConfiguration {

    override suspend fun invoke(phoneAccountHandle: PhoneAccountHandle?): CallSubjectConfiguration {
        return withContext(coroutineDispatcher) {
            val phoneAccountExtras = phoneAccountHandle
                ?.let { telecomManager.getPhoneAccount(it) }
                ?.extras
                ?: return@withContext CallSubjectConfiguration()

            val limit = phoneAccountExtras.getInt(
                PhoneAccount.EXTRA_CALL_SUBJECT_MAX_LENGTH,
                DEFAULT_CALL_SUBJECT_LIMIT,
            )
            val charsetName = phoneAccountExtras.getString(
                PhoneAccount.EXTRA_CALL_SUBJECT_CHARACTER_ENCODING,
            )
            val encoding = charsetName
                ?.takeIf { it.isNotEmpty() }
                ?.let {
                    try {
                        Charset.forName(charsetName)
                    } catch (e: IllegalArgumentException) {
                        Log.w(TAG, "Invalid charset: $charsetName", e)
                        null
                    }
                }

            CallSubjectConfiguration(
                limit = limit,
                encoding = encoding,
            )
        }
    }

    private companion object {
        const val TAG = "GetCallSubjectConfiguration"
    }
}
