package com.android.contacts.domain.telecom.usecase

import com.android.contacts.data.telecom.model.VideoCallingCapability
import com.android.contacts.data.telecom.source.VideoCallingCapabilitySource
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CanVideoCallImplTest {

    private val videoCallingCapabilitySource = mockk<VideoCallingCapabilitySource>()

    private val canVideoCall = CanVideoCallImpl(
        videoCallingCapabilitySource = videoCallingCapabilitySource,
    )

    @Test
    fun invoke_whenVideoCallingIsDisabled_returnsFalse() {
        givenCapability(isEnabled = false, requiresPresence = false)

        assertFalse(canVideoCall(isCarrierVideoCallCapable = true))
    }

    @Test
    fun invoke_whenVideoCallingIgnoresPresence_returnsTrueForAnyNumber() {
        givenCapability(isEnabled = true, requiresPresence = false)

        assertTrue(canVideoCall(isCarrierVideoCallCapable = false))
    }

    @Test
    fun invoke_whenPresenceDecidesAndTheCarrierSupportsIt_returnsTrue() {
        givenCapability(isEnabled = true, requiresPresence = true)

        assertTrue(canVideoCall(isCarrierVideoCallCapable = true))
    }

    @Test
    fun invoke_whenPresenceDecidesAndTheCarrierDoesNot_returnsFalse() {
        givenCapability(isEnabled = true, requiresPresence = true)

        assertFalse(canVideoCall(isCarrierVideoCallCapable = false))
    }

    private fun givenCapability(
        isEnabled: Boolean,
        requiresPresence: Boolean,
    ) {
        every { videoCallingCapabilitySource() } returns VideoCallingCapability(
            isEnabled = isEnabled,
            requiresPresence = requiresPresence,
        )
    }
}
