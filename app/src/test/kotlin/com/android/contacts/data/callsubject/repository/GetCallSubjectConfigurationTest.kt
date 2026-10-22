package com.android.contacts.data.callsubject.repository

import android.os.Bundle
import android.telecom.PhoneAccount
import android.telecom.PhoneAccountHandle
import android.telecom.TelecomManager
import com.android.contacts.data.callsubject.model.CallSubjectConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class GetCallSubjectConfigurationTest {

    private val telecomManager = mockk<TelecomManager>()
    private val phoneAccountHandle = mockk<PhoneAccountHandle>()

    private val getCallSubjectConfiguration: GetCallSubjectConfiguration =
        GetCallSubjectConfigurationImpl(
            telecomManager = telecomManager,
            coroutineDispatcher = UnconfinedTestDispatcher(),
        )

    @Test
    fun invoke_withoutAPhoneAccountHandle_returnsTheDefaults() = runTest {
        assertEquals(CallSubjectConfiguration(), getCallSubjectConfiguration(null))
        verify(exactly = 0) { telecomManager.getPhoneAccount(any()) }
    }

    @Test
    fun invoke_whenThePhoneAccountIsNotFound_returnsTheDefaults() = runTest {
        every { telecomManager.getPhoneAccount(phoneAccountHandle) } returns null

        assertEquals(CallSubjectConfiguration(), getCallSubjectConfiguration(phoneAccountHandle))
    }

    @Test
    fun invoke_whenThePhoneAccountHasNoExtras_returnsTheDefaults() = runTest {
        givenPhoneAccount(extras = null)

        assertEquals(CallSubjectConfiguration(), getCallSubjectConfiguration(phoneAccountHandle))
    }

    @Test
    fun invoke_whenTheExtrasAreEmpty_returnsTheDefaults() = runTest {
        givenPhoneAccount(extras = Bundle())

        assertEquals(CallSubjectConfiguration(), getCallSubjectConfiguration(phoneAccountHandle))
    }

    @Test
    fun invoke_whenTheExtrasHaveALimitAndEncoding_returnsThem() = runTest {
        givenPhoneAccountExtras(
            limit = 64,
            charsetName = "UTF-8",
        )

        assertEquals(
            CallSubjectConfiguration(limit = 64, encoding = Charsets.UTF_8),
            getCallSubjectConfiguration(phoneAccountHandle),
        )
    }

    @Test
    fun invoke_whenTheEncodingIsEmpty_hasNoEncoding() = runTest {
        givenPhoneAccountExtras(charsetName = "")

        assertNull(getCallSubjectConfiguration(phoneAccountHandle).encoding)
    }

    @Test
    fun invoke_whenTheEncodingIsInvalid_hasNoEncoding() = runTest {
        givenPhoneAccountExtras(charsetName = "invalid")

        assertNull(getCallSubjectConfiguration(phoneAccountHandle).encoding)
    }

    private fun givenPhoneAccountExtras(
        limit: Int? = null,
        charsetName: String? = null,
    ) {
        givenPhoneAccount(
            extras = Bundle().apply {
                limit?.let {
                    putInt(PhoneAccount.EXTRA_CALL_SUBJECT_MAX_LENGTH, it)
                }
                charsetName?.let {
                    putString(PhoneAccount.EXTRA_CALL_SUBJECT_CHARACTER_ENCODING, it)
                }
            },
        )
    }

    private fun givenPhoneAccount(extras: Bundle?) {
        val phoneAccount = mockk<PhoneAccount> {
            every { this@mockk.extras } returns extras
        }
        every { telecomManager.getPhoneAccount(phoneAccountHandle) } returns phoneAccount
    }
}
