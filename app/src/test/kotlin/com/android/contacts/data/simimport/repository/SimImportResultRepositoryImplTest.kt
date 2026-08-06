package com.android.contacts.data.simimport.repository

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.cash.turbine.TurbineTestContext
import app.cash.turbine.test
import com.android.contacts.SimImportService
import com.android.contacts.data.simimport.model.SimImportResult
import com.android.contacts.util.core.CurrentTimeProvider
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SimImportResultRepositoryImplTest {

    private val localBroadcastManager = mockk<LocalBroadcastManager>(relaxed = true)
    private val receiverSlot = slot<BroadcastReceiver>()
    private val intentFilterSlot = slot<IntentFilter>()

    private val repository = SimImportResultRepositoryImpl(
        localBroadcastManager = localBroadcastManager,
        currentTimeProvider = CurrentTimeProvider { NOW_MILLIS },
    )

    @Before
    fun setUp() {
        every {
            localBroadcastManager.registerReceiver(capture(receiverSlot), capture(intentFilterSlot))
        } just runs
    }

    @Test
    fun observeSimImportResults_whenImportSucceeded_emitsSuccessWithCount() = runTest {
        observeResults {
            sendResult(resultCode = SimImportService.RESULT_SUCCESS, count = 3)

            assertEquals(SimImportResult.Success(importedCount = 3), awaitItem())
        }
    }

    @Test
    fun observeSimImportResults_whenNothingWasImported_emitsNothing() = runTest {
        observeResults {
            sendResult(resultCode = SimImportService.RESULT_SUCCESS, count = 0)

            expectNoEvents()
        }
    }

    @Test
    fun observeSimImportResults_whenImportCountIsMissing_emitsNothing() = runTest {
        observeResults {
            sendResult(resultCode = SimImportService.RESULT_SUCCESS, count = null)

            expectNoEvents()
        }
    }

    @Test
    fun observeSimImportResults_whenImportFailed_emitsFailure() = runTest {
        observeResults {
            sendResult(resultCode = SimImportService.RESULT_FAILURE)

            assertEquals(SimImportResult.Failure, awaitItem())
        }
    }

    @Test
    fun observeSimImportResults_whenResultIsUnknown_emitsNothing() = runTest {
        observeResults {
            sendResult(resultCode = SimImportService.RESULT_UNKNOWN)

            expectNoEvents()
        }
    }

    @Test
    fun observeSimImportResults_whenResultCodeIsMissing_emitsNothing() = runTest {
        observeResults {
            receiverSlot.captured.onReceive(null, Intent())

            expectNoEvents()
        }
    }

    @Test
    fun observeSimImportResults_whenResultIsOlderThanThirtySeconds_emitsNothing() = runTest {
        observeResults {
            sendResult(
                resultCode = SimImportService.RESULT_FAILURE,
                requestedAtMillis = NOW_MILLIS - 30_001L,
            )

            expectNoEvents()
        }
    }

    @Test
    fun observeSimImportResults_whenResultIsExactlyThirtySecondsOld_emitsIt() = runTest {
        observeResults {
            sendResult(
                resultCode = SimImportService.RESULT_FAILURE,
                requestedAtMillis = NOW_MILLIS - 30_000L,
            )

            assertEquals(SimImportResult.Failure, awaitItem())
        }
    }

    @Test
    fun observeSimImportResults_whenRequestTimeIsMissing_emitsIt() = runTest {
        observeResults {
            sendResult(
                resultCode = SimImportService.RESULT_FAILURE,
                requestedAtMillis = null,
            )

            assertEquals(SimImportResult.Failure, awaitItem())
        }
    }

    @Test
    fun observeSimImportResults_whenCollected_registersReceiverForImportBroadcast() = runTest {
        observeResults {}

        assertEquals(
            SimImportService.BROADCAST_SIM_IMPORT_COMPLETE,
            intentFilterSlot.captured.getAction(0),
        )
    }

    @Test
    fun observeSimImportResults_whenCollectionStops_unregistersReceiver() = runTest {
        observeResults {}

        verify { localBroadcastManager.unregisterReceiver(receiverSlot.captured) }
    }

    private suspend fun TestScope.observeResults(
        assertions: suspend TurbineTestContext<SimImportResult>.() -> Unit,
    ) {
        repository.observeSimImportResults().test {
            runCurrent()
            assertions()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun sendResult(
        resultCode: Int,
        count: Int? = 1,
        requestedAtMillis: Long? = NOW_MILLIS,
    ) {
        val intent = Intent(SimImportService.BROADCAST_SIM_IMPORT_COMPLETE)
            .putExtra(SimImportService.EXTRA_RESULT_CODE, resultCode)
        count?.let {
            intent.putExtra(SimImportService.EXTRA_RESULT_COUNT, it)
        }
        requestedAtMillis?.let {
            intent.putExtra(SimImportService.EXTRA_OPERATION_REQUESTED_AT_TIME, it)
        }

        receiverSlot.captured.onReceive(null, intent)
    }

    private companion object {
        const val NOW_MILLIS = 1_000_000L
    }
}
