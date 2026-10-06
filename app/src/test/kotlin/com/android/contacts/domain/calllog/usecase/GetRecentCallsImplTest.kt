package com.android.contacts.domain.calllog.usecase

import com.android.contacts.data.calllog.model.CallLogEntry
import com.android.contacts.data.calllog.model.CallLogEntryType
import com.android.contacts.data.calllog.repository.CallLogRepository
import com.android.contacts.data.permissions.repository.PermissionsRepository
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapable
import com.android.contacts.domain.calllog.model.RecentCall
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.contactDetailsOf
import com.android.contacts.tests.factory.phone
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

internal class GetRecentCallsImplTest {

    private val callLogRepository = mockk<CallLogRepository>()
    private val permissionsRepository = mockk<PermissionsRepository>()
    private val isDeviceVoiceCapable = mockk<IsDeviceVoiceCapable>()

    private val getRecentCalls = GetRecentCallsImpl(
        callLogRepository = callLogRepository,
        permissionsRepository = permissionsRepository,
        isDeviceVoiceCapable = isDeviceVoiceCapable,
    )

    @Before
    fun setUp() {
        every { isDeviceVoiceCapable() } returns true
        coEvery { permissionsRepository.isCallLogGranted() } returns true
        coEvery { callLogRepository.getRecentCalls(any(), any()) } returns emptyList()
    }

    @Test
    fun invoke_withoutTheCallLogPermission_returnsNothing() = runTest {
        coEvery { permissionsRepository.isCallLogGranted() } returns false

        assertTrue(getRecentCalls(contactDetailsOf(phone(number = NUMBER))).isEmpty())
        coVerify(exactly = 0) { callLogRepository.getRecentCalls(any(), any()) }
    }

    @Test
    fun invoke_onADeviceThatCannotCall_returnsNothing() = runTest {
        every { isDeviceVoiceCapable() } returns false

        assertTrue(getRecentCalls(contactDetailsOf(phone(number = NUMBER))).isEmpty())
        coVerify(exactly = 0) { callLogRepository.getRecentCalls(any(), any()) }
    }

    @Test
    fun invoke_withoutPhoneNumbers_returnsNothing() = runTest {
        assertTrue(getRecentCalls(contactDetails()).isEmpty())
        coVerify(exactly = 0) { callLogRepository.getRecentCalls(any(), any()) }
    }

    @Test
    fun invoke_mapsTheCallsNewestFirst() = runTest {
        coEvery { callLogRepository.getRecentCalls(NUMBER, any()) } returns listOf(
            entry(date = 100L, type = CallLogEntryType.OUTGOING),
            entry(date = 300L, type = CallLogEntryType.MISSED),
        )
        val details = contactDetailsOf(phone(number = NUMBER, typeLabel = "Mobile"))

        assertEquals(
            listOf(
                RecentCall(
                    number = NUMBER,
                    numberLabel = "Mobile",
                    date = 300L,
                    duration = 42.seconds,
                    type = CallLogEntryType.MISSED,
                ),
                RecentCall(
                    number = NUMBER,
                    numberLabel = "Mobile",
                    date = 100L,
                    duration = 42.seconds,
                    type = CallLogEntryType.OUTGOING,
                ),
            ),
            getRecentCalls(details),
        )
    }

    @Test
    fun invoke_withSeveralNumbers_dropsCallsSharingADate() = runTest {
        coEvery { callLogRepository.getRecentCalls(NUMBER, any()) } returns
            listOf(entry(date = 100L))
        coEvery { callLogRepository.getRecentCalls(OTHER_NUMBER, any()) } returns
            listOf(entry(date = 100L, number = OTHER_NUMBER))
        val details = contactDetailsOf(
            phone(id = 1L, number = NUMBER),
            phone(id = 2L, number = OTHER_NUMBER),
        )

        assertEquals(1, getRecentCalls(details).size)
    }

    @Test
    fun invoke_withMoreCallsThanTheLimit_keepsTheNewestThree() = runTest {
        coEvery { callLogRepository.getRecentCalls(NUMBER, any()) } returns
            (1L..5L).map { date -> entry(date = date) }
        val details = contactDetailsOf(phone(number = NUMBER))

        assertEquals(listOf(5L, 4L, 3L), getRecentCalls(details).map(RecentCall::date))
    }

    private fun entry(
        date: Long,
        type: CallLogEntryType = CallLogEntryType.INCOMING,
        number: String = NUMBER,
    ): CallLogEntry {
        return CallLogEntry(
            number = number,
            date = date,
            duration = 42.seconds,
            type = type,
        )
    }

    private companion object {
        const val NUMBER = "4155551212"
        const val OTHER_NUMBER = "4155553434"
    }
}
