package com.android.contacts.domain.settings.usecase

import app.cash.turbine.test
import com.android.contacts.data.accounts.repository.AccountsRepository
import com.android.contacts.data.appinfo.repository.AppInfoRepository
import com.android.contacts.data.contactsfilter.model.ContactsFilter
import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepository
import com.android.contacts.data.permissions.repository.PermissionsRepository
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SettingsAvailability
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.domain.settings.model.SettingsData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetSettingsDataImplTest {

    private val settingsAvailabilityRepository = mockk<SettingsAvailabilityRepository>()
    private val displaySettingsRepository = mockk<DisplaySettingsRepository>()
    private val accountsRepository = mockk<AccountsRepository>()
    private val contactsFilterRepository = mockk<ContactsFilterRepository>()
    private val appInfoRepository = mockk<AppInfoRepository>()
    private val permissionsRepository = mockk<PermissionsRepository>()

    private val useCase = GetSettingsDataImpl(
        settingsAvailabilityRepository = settingsAvailabilityRepository,
        displaySettingsRepository = displaySettingsRepository,
        accountsRepository = accountsRepository,
        contactsFilterRepository = contactsFilterRepository,
        appInfoRepository = appInfoRepository,
        permissionsRepository = permissionsRepository,
    )

    @Before
    fun setUp() {
        coEvery { settingsAvailabilityRepository.getSettingsAvailability() } returns AVAILABILITY
        every { displaySettingsRepository.observeDisplaySettings() } returns
            flowOf(DISPLAY_SETTINGS)
        coEvery { accountsRepository.getDefaultAccountLabel() } returns "Device"
        coEvery { contactsFilterRepository.getContactsFilter() } returns ContactsFilter.CUSTOM
        coEvery { appInfoRepository.getBuildVersion() } returns BUILD_VERSION
        coEvery { permissionsRepository.isCallLogGranted() } returns true
    }

    @Test
    fun invoke_collectsEverySource() = runTest {
        useCase().test {
            assertEquals(
                SettingsData(
                    availability = AVAILABILITY,
                    displaySettings = DISPLAY_SETTINGS,
                    defaultAccountLabel = "Device",
                    contactsFilter = ContactsFilter.CUSTOM,
                    buildVersion = BUILD_VERSION,
                    isCallLogPermissionGranted = true,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_whenThereIsNoDefaultAccountOrFilter_keepsThemNull() = runTest {
        coEvery { accountsRepository.getDefaultAccountLabel() } returns null
        coEvery { contactsFilterRepository.getContactsFilter() } returns null

        useCase().test {
            val settingsData = awaitItem()

            assertNull(settingsData.defaultAccountLabel)
            assertNull(settingsData.contactsFilter)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun invoke_readsSourcesInParallel() = runTest {
        val slowAvailability = CompletableDeferred<SettingsAvailability>()
        coEvery { settingsAvailabilityRepository.getSettingsAvailability() } coAnswers {
            slowAvailability.await()
        }

        val settingsData = async { useCase().first() }
        runCurrent()

        coVerify(exactly = 1) { accountsRepository.getDefaultAccountLabel() }
        coVerify(exactly = 1) { contactsFilterRepository.getContactsFilter() }
        coVerify(exactly = 1) { appInfoRepository.getBuildVersion() }

        slowAvailability.complete(AVAILABILITY)
        assertEquals(AVAILABILITY, settingsData.await().availability)
    }

    @Test
    fun invoke_whenDisplaySettingsChange_emitsTheDataAgain() = runTest {
        val displaySettings = MutableSharedFlow<DisplaySettings>(extraBufferCapacity = 1)
        every { displaySettingsRepository.observeDisplaySettings() } returns displaySettings

        useCase().test {
            displaySettings.emit(DISPLAY_SETTINGS)

            assertEquals(DISPLAY_SETTINGS, awaitItem().displaySettings)

            val alternative = DISPLAY_SETTINGS.copy(sortOrder = SortOrder.FAMILY_NAME_FIRST)
            displaySettings.emit(alternative)

            assertEquals(alternative, awaitItem().displaySettings)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val BUILD_VERSION = "1.7.40"

        val AVAILABILITY = SettingsAvailability(
            areContactsAvailable = true,
            areBlockedNumbersAvailable = true,
            isAboutAvailable = true,
        )

        val DISPLAY_SETTINGS = DisplaySettings(
            sortOrder = SortOrder.GIVEN_NAME_FIRST,
            isSortOrderChangeable = true,
            displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
            isDisplayOrderChangeable = true,
            phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
            isPhoneticNameDisplayChangeable = true,
        )
    }
}
