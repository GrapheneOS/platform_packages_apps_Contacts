package com.android.contacts.domain

import app.cash.turbine.test
import com.android.contacts.domain.util.AcquireWakeLock
import com.android.contacts.domain.vcard.model.ImportVCardError as Error
import com.android.contacts.domain.vcard.model.VCardDetails
import com.android.contacts.domain.vcard.model.VCardVersion
import com.android.contacts.domain.vcard.usecase.ImportVCardsImpl
import com.android.contacts.domain.vcard.usecase.ParseVCardDetails
import com.android.contacts.domain.vcard.usecase.VCardServiceRunner
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.tests.factory.AccountModelFactory
import com.android.contacts.tests.factory.ImportVCardSourceFactory
import com.android.contacts.vcard.ImportRequest
import com.android.contacts.vcard.NotificationImportExportListener
import com.android.contacts.vcard.VCardService
import com.android.vcard.VCardConfig
import com.android.vcard.exception.VCardException
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ImportVCardsTest {

    private val wakeLock = mockk<AcquireWakeLock.Wrapper>(relaxed = true)
    private val acquireWakeLock = mockk<AcquireWakeLock>(relaxed = true) {
        every { this@mockk.invoke(any(), any()) } returns wakeLock
    }
    private val parseVCardDetails = mockk<ParseVCardDetails>()
    private val vCardServiceRunner = mockk<VCardServiceRunner>()
    private val notificationImportExportListener = mockk<NotificationImportExportListener>()

    @Test
    fun whenParsingFails_emitErrorAndDoNotCreateService() =
        runTest {
            val account = AccountModelFactory.build()
            val source = ImportVCardSourceFactory.build()
            coEvery { parseVCardDetails.invoke(any()) } throws VCardException("")

            buildSubject()(account, listOf(source)).test {
                assertEquals(Error.NotSupported, awaitItem())
                awaitComplete()
            }

            verify(exactly = 0) { vCardServiceRunner() }
            verify { wakeLock.releaseIfHeld() }
        }

    @Test
    fun whenParsingSucceds_startsAndSendsRequestsToService() =
        runTest {
            val account = AccountModelFactory.build()
            val source = ImportVCardSourceFactory.build()
            val details = VCardDetails(
                estimatedType = VCardConfig.VCARD_TYPE_V21_GENERIC,
                estimatedCharset = null,
                version = VCardVersion.V21,
                entryCount = 1,
            )
            coEvery { parseVCardDetails.invoke(any()) } returns details
            val vCardService = mockk<VCardService>()
            val requestsSlot = slot<List<ImportRequest>>()
            every { vCardServiceRunner.invoke() } returns MutableStateFlow(vCardService)
            every { vCardService.handleImportRequest(capture(requestsSlot), any()) } returns Unit

            buildSubject()(account, listOf(source)).test {
                awaitComplete()
            }

            verify { vCardService.handleImportRequest(any(), notificationImportExportListener) }
            verify { wakeLock.releaseIfHeld() }

            val requests = requestsSlot.captured
            assertEquals(1, requests.size)
            val request = requests.first()
            assertEquals(
                AccountWithDataSet(account.name, account.type, account.dataSet).accountOrNull,
                request.account,
            )
            assertNull(request.data)
            assertEquals(source.uri, request.uri)
            assertEquals(source.name, request.displayName)
            assertEquals(details.estimatedCharset, request.estimatedCharset)
            assertEquals(details.estimatedType, request.estimatedVCardType)
            assertEquals(details.version.value, request.vcardVersion)
            assertEquals(details.entryCount, request.entryCount)
        }

    private fun buildSubject() = ImportVCardsImpl(
        acquireWakeLock = acquireWakeLock,
        parseVCardDetails = parseVCardDetails,
        vCardServiceRunner = vCardServiceRunner,
        notificationImportExportListener = notificationImportExportListener,
        coroutineDispatcher = UnconfinedTestDispatcher(),
    )
}
