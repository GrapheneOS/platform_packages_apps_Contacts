package com.android.contacts.domain.calllog.usecase

import com.android.contacts.data.calllog.model.CallLogEntry
import com.android.contacts.data.calllog.model.CallLogEntryType
import com.android.contacts.data.calllog.repository.CallLogRepository
import com.android.contacts.data.permissions.repository.PermissionsRepository
import com.android.contacts.domain.calllog.model.RecentCall
import com.android.contacts.domain.util.IsDeviceVoiceCapable
import com.android.contacts.tests.factory.contactDetails
import com.android.contacts.tests.factory.phone
import io.mockk.coEvery
import kotlin.time.Duration.Companion.seconds
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetRecentCallsImplTest {

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

        val details = contactDetails(dataItems = listOf(phone(number = "4155551212")))

        assertTrue(getRecentCalls(details).isEmpty())
        coVerify(exactly = 0) { callLogRepository.getRecentCalls(any(), any()) }
    }

    @Test
    fun invoke_onADeviceThatCannotCall_returnsNothing() = runTest {
        every { isDeviceVoiceCapable() } returns false

        val details = contactDetails(dataItems = listOf(phone(number = "4155551212")))

        assertTrue(getRecentCalls(details).isEmpty())
        coVerify(exactly = 0) { callLogRepository.getRecentCalls(any(), any()) }
    }

    @Test
    fun invoke_withoutPhoneNumbers_returnsNothing() = runTest {
        assertTrue(getRecentCalls(contactDetails()).isEmpty())
        coVerify(exactly = 0) { callLogRepository.getRecentCalls(any(), any()) }
    }

    @Test
    fun invoke_mapsTheCallsNewestFirst() = runTest {
        coEvery { callLogRepository.getRecentCalls("4155551212", any()) } returns listOf(
            entry(date = 100L, type = CallLogEntryType.OUTGOING),
            entry(date = 300L, type = CallLogEntryType.MISSED),
        )

        val details = contactDetails(
            dataItems = listOf(phone(number = "4155551212", typeLabel = "Mobile")),
        )

        assertEquals(
            listOf(
                RecentCall(
                    number = "4155551212",
                    numberLabel = "Mobile",
                    date = 300L,
                    duration = 42.seconds,
                    type = CallLogEntryType.MISSED,
                ),
                RecentCall(
                    number = "4155551212",
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
        coEvery { callLogRepository.getRecentCalls("4155551212", any()) } returns
            listOf(entry(date = 100L))
        coEvery { callLogRepository.getRecentCalls("4155553434", any()) } returns
            listOf(entry(date = 100L, number = "4155553434"))

        val details = contactDetails(
            dataItems = listOf(
                phone(id = 1L, number = "4155551212"),
                phone(id = 2L, number = "4155553434"),
            ),
        )

        assertEquals(1, getRecentCalls(details).size)
    }

    @Test
    fun invoke_withMoreCallsThanTheLimit_keepsTheNewestThree() = runTest {
        coEvery { callLogRepository.getRecentCalls("4155551212", any()) } returns
            (1L..5L).map { date -> entry(date = date) }
        val details = contactDetails(dataItems = listOf(phone(number = "4155551212")))

        assertEquals(listOf(5L, 4L, 3L), getRecentCalls(details).map(RecentCall::date))
    }

    private fun entry(
        date: Long,
        type: CallLogEntryType = CallLogEntryType.INCOMING,
        number: String = "4155551212",
    ): CallLogEntry {
        return CallLogEntry(
            number = number,
            date = date,
            duration = 42.seconds,
            type = type,
        )
    }
}
