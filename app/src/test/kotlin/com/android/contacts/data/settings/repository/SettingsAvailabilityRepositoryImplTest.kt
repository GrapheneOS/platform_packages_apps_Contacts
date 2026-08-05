package com.android.contacts.data.settings.repository

import android.content.Context
import android.provider.BlockedNumberContract
import android.provider.ContactsContract.ProviderStatus
import android.telephony.TelephonyManager
import com.android.contacts.compat.TelephonyManagerCompat
import com.android.contacts.list.ProviderStatusWatcher
import com.android.contactsbind.HelpUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsAvailabilityRepositoryImplTest {

    private val context = mockk<Context>(relaxed = true)
    private val telephonyManager = mockk<TelephonyManager>(relaxed = true)
    private val providerStatusWatcher = mockk<ProviderStatusWatcher>()

    private val repository = SettingsAvailabilityRepositoryImpl(
        context = context,
        providerStatusWatcher = providerStatusWatcher,
        telephonyManager = telephonyManager,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Before
    fun setUp() {
        mockkStatic(TelephonyManagerCompat::class)
        mockkStatic(BlockedNumberContract::class)
        mockkStatic(HelpUtils::class)
        every { providerStatusWatcher.providerStatus } returns ProviderStatus.STATUS_NORMAL
        givenBlockedNumbersSupport(isVoiceCapable = true, canBlockNumbers = true)
        every { HelpUtils.isHelpAndFeedbackAvailable() } returns false
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getSettingsAvailability_whenProviderStatusIsNormal_reportsContactsAsAvailable() = runTest {
        assertTrue(repository.getSettingsAvailability().areContactsAvailable)
    }

    @Test
    fun getSettingsAvailability_whenProviderStatusIsNotNormal_reportsContactsAsUnavailable() =
        runTest {
            every { providerStatusWatcher.providerStatus } returns ProviderStatus.STATUS_BUSY

            assertFalse(repository.getSettingsAvailability().areContactsAvailable)
        }

    @Test
    fun getSettingsAvailability_whenDeviceCanBlockNumbers_reportsBlockedNumbersAsAvailable() =
        runTest {
            assertTrue(repository.getSettingsAvailability().areBlockedNumbersAvailable)
        }

    @Test
    fun getSettingsAvailability_whenDeviceIsNotVoiceCapable_reportsBlockedNumbersAsUnavailable() =
        runTest {
            givenBlockedNumbersSupport(isVoiceCapable = false, canBlockNumbers = true)

            assertFalse(repository.getSettingsAvailability().areBlockedNumbersAvailable)
        }

    @Test
    fun getSettingsAvailability_whenUserCannotBlockNumbers_reportsBlockedNumbersAsUnavailable() =
        runTest {
            givenBlockedNumbersSupport(isVoiceCapable = true, canBlockNumbers = false)

            assertFalse(repository.getSettingsAvailability().areBlockedNumbersAvailable)
        }

    @Test
    fun getSettingsAvailability_whenHelpAndFeedbackIsUnavailable_reportsAboutAsAvailable() =
        runTest {
            assertTrue(repository.getSettingsAvailability().isAboutAvailable)
        }

    @Test
    fun getSettingsAvailability_whenHelpAndFeedbackIsAvailable_reportsAboutAsUnavailable() =
        runTest {
            every { HelpUtils.isHelpAndFeedbackAvailable() } returns true

            assertFalse(repository.getSettingsAvailability().isAboutAvailable)
        }

    private fun givenBlockedNumbersSupport(
        isVoiceCapable: Boolean,
        canBlockNumbers: Boolean,
    ) {
        every { TelephonyManagerCompat.isVoiceCapable(telephonyManager) } returns isVoiceCapable
        every { BlockedNumberContract.canCurrentUserBlockNumbers(context) } returns canBlockNumbers
    }
}
