package com.android.contacts.domain.util

import android.content.BroadcastReceiver
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.cash.turbine.test
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class BuildBroadcastReceiverFlowImplTest {

    private val localBroadcastManager = mockk<LocalBroadcastManager>(relaxed = true)
    private val receiverSlot = slot<BroadcastReceiver>()
    private val intentFilter = IntentFilter(TEST_ACTION)

    @Test
    fun invoke_whenBroadcastIsReceived_emitsUnit() = runTest {
        captureRegisteredReceiver()
        val subject = BuildBroadcastReceiverFlowImpl(localBroadcastManager)

        subject(intentFilter).test {
            runCurrent()

            receiverSlot.captured.onReceive(null, null)

            assertEquals(Unit, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenBroadcastIsReceivedTwice_emitsTwice() = runTest {
        captureRegisteredReceiver()
        val subject = BuildBroadcastReceiverFlowImpl(localBroadcastManager)

        subject(intentFilter).test {
            runCurrent()

            receiverSlot.captured.onReceive(null, null)
            receiverSlot.captured.onReceive(null, null)

            assertEquals(Unit, awaitItem())
            assertEquals(Unit, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenNoBroadcastIsReceived_emitsNothing() = runTest {
        captureRegisteredReceiver()
        val subject = BuildBroadcastReceiverFlowImpl(localBroadcastManager)

        subject(intentFilter).test {
            runCurrent()

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenCollected_registersReceiverForGivenFilter() = runTest {
        captureRegisteredReceiver()
        val subject = BuildBroadcastReceiverFlowImpl(localBroadcastManager)

        val job = launch { subject(intentFilter).collect() }
        runCurrent()

        verify(exactly = 1) {
            localBroadcastManager.registerReceiver(any(), intentFilter)
        }

        job.cancelAndJoin()
    }

    @Test
    fun invoke_whenCollectionIsCancelled_unregistersReceiver() = runTest {
        captureRegisteredReceiver()
        val subject = BuildBroadcastReceiverFlowImpl(localBroadcastManager)

        val job = launch { subject(intentFilter).collect() }
        runCurrent()
        verify(exactly = 0) {
            localBroadcastManager.unregisterReceiver(any())
        }

        job.cancelAndJoin()

        verify(exactly = 1) {
            localBroadcastManager.unregisterReceiver(receiverSlot.captured)
        }
    }

    private fun captureRegisteredReceiver() {
        every {
            localBroadcastManager.registerReceiver(capture(receiverSlot), any())
        } just runs
    }

    private companion object {
        const val TEST_ACTION = "com.android.contacts.tests.TEST_ACTION"
    }
}
