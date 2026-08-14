package com.android.contacts.data.settings.repository

import app.cash.turbine.test
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.preference.ContactsPreferences
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DisplaySettingsRepositoryImplTest {

    private val contactsPreferences = mockk<ContactsPreferences>(relaxed = true)

    private val repository = DisplaySettingsRepositoryImpl(
        contactsPreferences = contactsPreferences,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    @Test
    fun observeDisplaySettings_whenPrimaryValuesAreStored_mapsToGivenNameFirst() = runTest {
        givenStoredValues(
            sortOrder = ContactsPreferences.SORT_ORDER_PRIMARY,
            displayOrder = ContactsPreferences.DISPLAY_ORDER_PRIMARY,
            phoneticNameDisplay = ContactsPreferences.PHONETIC_NAME_DISPLAY_SHOW_ALWAYS,
        )

        repository.observeDisplaySettings().test {
            val settings = awaitItem()

            assertEquals(SortOrder.GIVEN_NAME_FIRST, settings.sortOrder)
            assertEquals(DisplayOrder.GIVEN_NAME_FIRST, settings.displayOrder)
            assertEquals(PhoneticNameDisplay.SHOW_ALWAYS, settings.phoneticNameDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeDisplaySettings_whenAlternativeValuesAreStored_mapsToFamilyNameFirst() = runTest {
        givenStoredValues(
            sortOrder = ContactsPreferences.SORT_ORDER_ALTERNATIVE,
            displayOrder = ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE,
            phoneticNameDisplay = ContactsPreferences.PHONETIC_NAME_DISPLAY_HIDE_IF_EMPTY,
        )

        repository.observeDisplaySettings().test {
            val settings = awaitItem()

            assertEquals(SortOrder.FAMILY_NAME_FIRST, settings.sortOrder)
            assertEquals(DisplayOrder.FAMILY_NAME_FIRST, settings.displayOrder)
            assertEquals(PhoneticNameDisplay.HIDE_IF_EMPTY, settings.phoneticNameDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeDisplaySettings_whenStoredValuesAreUnknown_fallsBackToPrimaryOptions() = runTest {
        givenStoredValues(
            sortOrder = UNKNOWN_PREFERENCE_VALUE,
            displayOrder = UNKNOWN_PREFERENCE_VALUE,
            phoneticNameDisplay = UNKNOWN_PREFERENCE_VALUE,
        )

        repository.observeDisplaySettings().test {
            val settings = awaitItem()

            assertEquals(SortOrder.GIVEN_NAME_FIRST, settings.sortOrder)
            assertEquals(DisplayOrder.GIVEN_NAME_FIRST, settings.displayOrder)
            assertEquals(PhoneticNameDisplay.SHOW_ALWAYS, settings.phoneticNameDisplay)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeDisplaySettings_whenOptionsAreChangeable_reportsThemAsChangeable() = runTest {
        givenStoredValues()
        givenOptionsChangeable(true)

        repository.observeDisplaySettings().test {
            assertEquals(
                DisplaySettings(
                    sortOrder = SortOrder.GIVEN_NAME_FIRST,
                    isSortOrderChangeable = true,
                    displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
                    isDisplayOrderChangeable = true,
                    phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
                    isPhoneticNameDisplayChangeable = true,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeDisplaySettings_whenOptionsAreLocked_reportsThemAsNotChangeable() = runTest {
        givenStoredValues()
        givenOptionsChangeable(false)

        repository.observeDisplaySettings().test {
            val settings = awaitItem()

            assertFalse(settings.isSortOrderChangeable)
            assertFalse(settings.isDisplayOrderChangeable)
            assertFalse(settings.isPhoneticNameDisplayChangeable)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeDisplaySettings_whenASettingIsChanged_emitsTheStoredValueAgain() = runTest {
        givenStoredValues()
        givenSortOrderWritesAreStored()

        repository.observeDisplaySettings().test {
            assertEquals(SortOrder.GIVEN_NAME_FIRST, awaitItem().sortOrder)

            repository.setSortOrder(SortOrder.FAMILY_NAME_FIRST)

            assertEquals(SortOrder.FAMILY_NAME_FIRST, awaitItem().sortOrder)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeDisplaySettings_whenNothingIsWritten_doesNotEmitAgain() = runTest {
        givenStoredValues(sortOrder = ContactsPreferences.SORT_ORDER_PRIMARY)

        repository.observeDisplaySettings().test {
            assertEquals(SortOrder.GIVEN_NAME_FIRST, awaitItem().sortOrder)

            repository.setSortOrder(SortOrder.GIVEN_NAME_FIRST)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setSortOrder_whenGivenNameFirstIsSelected_storesPrimaryValue() = runTest {
        givenStoredValues(sortOrder = ContactsPreferences.SORT_ORDER_ALTERNATIVE)

        repository.setSortOrder(SortOrder.GIVEN_NAME_FIRST)

        verify { contactsPreferences.sortOrder = ContactsPreferences.SORT_ORDER_PRIMARY }
    }

    @Test
    fun setSortOrder_whenFamilyNameFirstIsSelected_storesAlternativeValue() = runTest {
        givenStoredValues(sortOrder = ContactsPreferences.SORT_ORDER_PRIMARY)

        repository.setSortOrder(SortOrder.FAMILY_NAME_FIRST)

        verify { contactsPreferences.sortOrder = ContactsPreferences.SORT_ORDER_ALTERNATIVE }
    }

    @Test
    fun setSortOrder_whenSelectedValueIsAlreadyStored_doesNotWrite() = runTest {
        givenStoredValues(sortOrder = ContactsPreferences.SORT_ORDER_PRIMARY)

        repository.setSortOrder(SortOrder.GIVEN_NAME_FIRST)

        verify(exactly = 0) { contactsPreferences.sortOrder = any() }
    }

    @Test
    fun setDisplayOrder_whenGivenNameFirstIsSelected_storesPrimaryValue() = runTest {
        givenStoredValues(displayOrder = ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE)

        repository.setDisplayOrder(DisplayOrder.GIVEN_NAME_FIRST)

        verify { contactsPreferences.displayOrder = ContactsPreferences.DISPLAY_ORDER_PRIMARY }
    }

    @Test
    fun setDisplayOrder_whenFamilyNameFirstIsSelected_storesAlternativeValue() = runTest {
        givenStoredValues(displayOrder = ContactsPreferences.DISPLAY_ORDER_PRIMARY)

        repository.setDisplayOrder(DisplayOrder.FAMILY_NAME_FIRST)

        verify { contactsPreferences.displayOrder = ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE }
    }

    @Test
    fun setDisplayOrder_whenSelectedValueIsAlreadyStored_doesNotWrite() = runTest {
        givenStoredValues(displayOrder = ContactsPreferences.DISPLAY_ORDER_PRIMARY)

        repository.setDisplayOrder(DisplayOrder.GIVEN_NAME_FIRST)

        verify(exactly = 0) { contactsPreferences.displayOrder = any() }
    }

    @Test
    fun setPhoneticNameDisplay_whenShowAlwaysIsSelected_storesShowAlwaysValue() = runTest {
        givenStoredValues(
            phoneticNameDisplay = ContactsPreferences.PHONETIC_NAME_DISPLAY_HIDE_IF_EMPTY,
        )

        repository.setPhoneticNameDisplay(PhoneticNameDisplay.SHOW_ALWAYS)

        verify {
            contactsPreferences.phoneticNameDisplayPreference =
                ContactsPreferences.PHONETIC_NAME_DISPLAY_SHOW_ALWAYS
        }
    }

    @Test
    fun setPhoneticNameDisplay_whenHideIfEmptyIsSelected_storesHideIfEmptyValue() = runTest {
        givenStoredValues(
            phoneticNameDisplay = ContactsPreferences.PHONETIC_NAME_DISPLAY_SHOW_ALWAYS,
        )

        repository.setPhoneticNameDisplay(PhoneticNameDisplay.HIDE_IF_EMPTY)

        verify {
            contactsPreferences.phoneticNameDisplayPreference =
                ContactsPreferences.PHONETIC_NAME_DISPLAY_HIDE_IF_EMPTY
        }
    }

    @Test
    fun setPhoneticNameDisplay_whenSelectedValueIsAlreadyStored_doesNotWrite() = runTest {
        givenStoredValues(
            phoneticNameDisplay = ContactsPreferences.PHONETIC_NAME_DISPLAY_SHOW_ALWAYS,
        )

        repository.setPhoneticNameDisplay(PhoneticNameDisplay.SHOW_ALWAYS)

        verify(exactly = 0) { contactsPreferences.phoneticNameDisplayPreference = any() }
    }

    private fun givenStoredValues(
        sortOrder: Int = ContactsPreferences.SORT_ORDER_PRIMARY,
        displayOrder: Int = ContactsPreferences.DISPLAY_ORDER_PRIMARY,
        phoneticNameDisplay: Int = ContactsPreferences.PHONETIC_NAME_DISPLAY_SHOW_ALWAYS,
    ) {
        every { contactsPreferences.sortOrder } returns sortOrder
        every { contactsPreferences.displayOrder } returns displayOrder
        every { contactsPreferences.phoneticNameDisplayPreference } returns phoneticNameDisplay
    }

    private fun givenSortOrderWritesAreStored() {
        var storedSortOrder = ContactsPreferences.SORT_ORDER_PRIMARY

        every { contactsPreferences.sortOrder } answers { storedSortOrder }
        every { contactsPreferences.sortOrder = any() } answers { storedSortOrder = firstArg() }
    }

    private fun givenOptionsChangeable(isChangeable: Boolean) {
        every { contactsPreferences.isSortOrderUserChangeable } returns isChangeable
        every { contactsPreferences.isDisplayOrderUserChangeable } returns isChangeable
        every { contactsPreferences.isPhoneticNameDisplayPreferenceChangeable } returns isChangeable
    }

    private companion object {
        const val UNKNOWN_PREFERENCE_VALUE = 42
    }
}
