package com.android.contacts.domain.util

import android.content.Context
import com.android.contacts.CallUtil
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CanVideoCallImplTest {

    private val context = mockk<Context>(relaxed = true)

    private val canVideoCall = CanVideoCallImpl(context = context)

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun invoke_whenVideoCallingIsDisabled_returnsFalse() {
        givenVideoCallingAvailability(CallUtil.VIDEO_CALLING_DISABLED)

        assertFalse(canVideoCall(isCarrierVideoCallCapable = true))
    }

    @Test
    fun invoke_whenVideoCallingIgnoresPresence_returnsTrueForAnyNumber() {
        givenVideoCallingAvailability(CallUtil.VIDEO_CALLING_ENABLED)

        assertTrue(canVideoCall(isCarrierVideoCallCapable = false))
    }

    @Test
    fun invoke_whenPresenceDecidesAndTheCarrierSupportsIt_returnsTrue() {
        givenVideoCallingAvailability(
            CallUtil.VIDEO_CALLING_ENABLED or CallUtil.VIDEO_CALLING_PRESENCE,
        )

        assertTrue(canVideoCall(isCarrierVideoCallCapable = true))
    }

    @Test
    fun invoke_whenPresenceDecidesAndTheCarrierDoesNot_returnsFalse() {
        givenVideoCallingAvailability(
            CallUtil.VIDEO_CALLING_ENABLED or CallUtil.VIDEO_CALLING_PRESENCE,
        )

        assertFalse(canVideoCall(isCarrierVideoCallCapable = false))
    }

    private fun givenVideoCallingAvailability(availability: Int) {
        mockkStatic(CallUtil::class)
        every { CallUtil.getVideoCallingAvailability(context) } returns availability
    }
}
