package com.android.contacts.data.settings.repository

import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.preference.ContactsPreferences
import com.android.contacts.preference.PhoneticNameDisplayPreference
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
    fun getDisplaySettings_whenPrimaryValuesAreStored_mapsToGivenNameFirst() = runTest {
        givenStoredValues(
            sortOrder = ContactsPreferences.SORT_ORDER_PRIMARY,
            displayOrder = ContactsPreferences.DISPLAY_ORDER_PRIMARY,
            phoneticNameDisplay = PhoneticNameDisplayPreference.SHOW_ALWAYS,
        )

        val settings = repository.getDisplaySettings()

        assertEquals(SortOrder.GIVEN_NAME_FIRST, settings.sortOrder)
        assertEquals(DisplayOrder.GIVEN_NAME_FIRST, settings.displayOrder)
        assertEquals(PhoneticNameDisplay.SHOW_ALWAYS, settings.phoneticNameDisplay)
    }

    @Test
    fun getDisplaySettings_whenAlternativeValuesAreStored_mapsToFamilyNameFirst() = runTest {
        givenStoredValues(
            sortOrder = ContactsPreferences.SORT_ORDER_ALTERNATIVE,
            displayOrder = ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE,
            phoneticNameDisplay = PhoneticNameDisplayPreference.HIDE_IF_EMPTY,
        )

        val settings = repository.getDisplaySettings()

        assertEquals(SortOrder.FAMILY_NAME_FIRST, settings.sortOrder)
        assertEquals(DisplayOrder.FAMILY_NAME_FIRST, settings.displayOrder)
        assertEquals(PhoneticNameDisplay.HIDE_IF_EMPTY, settings.phoneticNameDisplay)
    }

    @Test
    fun getDisplaySettings_whenStoredValuesAreUnknown_fallsBackToPrimaryOptions() = runTest {
        givenStoredValues(
            sortOrder = UNKNOWN_PREFERENCE_VALUE,
            displayOrder = UNKNOWN_PREFERENCE_VALUE,
            phoneticNameDisplay = UNKNOWN_PREFERENCE_VALUE,
        )

        val settings = repository.getDisplaySettings()

        assertEquals(SortOrder.GIVEN_NAME_FIRST, settings.sortOrder)
        assertEquals(DisplayOrder.GIVEN_NAME_FIRST, settings.displayOrder)
        assertEquals(PhoneticNameDisplay.SHOW_ALWAYS, settings.phoneticNameDisplay)
    }

    @Test
    fun getDisplaySettings_whenOptionsAreChangeable_reportsThemAsChangeable() = runTest {
        givenStoredValues()
        givenOptionsChangeable(true)

        val settings = repository.getDisplaySettings()

        assertEquals(
            DisplaySettings(
                sortOrder = SortOrder.GIVEN_NAME_FIRST,
                isSortOrderChangeable = true,
                displayOrder = DisplayOrder.GIVEN_NAME_FIRST,
                isDisplayOrderChangeable = true,
                phoneticNameDisplay = PhoneticNameDisplay.SHOW_ALWAYS,
                isPhoneticNameDisplayChangeable = true,
            ),
            settings,
        )
    }

    @Test
    fun getDisplaySettings_whenOptionsAreLocked_reportsThemAsNotChangeable() = runTest {
        givenStoredValues()
        givenOptionsChangeable(false)

        val settings = repository.getDisplaySettings()

        assertFalse(settings.isSortOrderChangeable)
        assertFalse(settings.isDisplayOrderChangeable)
        assertFalse(settings.isPhoneticNameDisplayChangeable)
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
        givenStoredValues(phoneticNameDisplay = PhoneticNameDisplayPreference.HIDE_IF_EMPTY)

        repository.setPhoneticNameDisplay(PhoneticNameDisplay.SHOW_ALWAYS)

        verify {
            contactsPreferences.phoneticNameDisplayPreference =
                PhoneticNameDisplayPreference.SHOW_ALWAYS
        }
    }

    @Test
    fun setPhoneticNameDisplay_whenHideIfEmptyIsSelected_storesHideIfEmptyValue() = runTest {
        givenStoredValues(phoneticNameDisplay = PhoneticNameDisplayPreference.SHOW_ALWAYS)

        repository.setPhoneticNameDisplay(PhoneticNameDisplay.HIDE_IF_EMPTY)

        verify {
            contactsPreferences.phoneticNameDisplayPreference =
                PhoneticNameDisplayPreference.HIDE_IF_EMPTY
        }
    }

    @Test
    fun setPhoneticNameDisplay_whenSelectedValueIsAlreadyStored_doesNotWrite() = runTest {
        givenStoredValues(phoneticNameDisplay = PhoneticNameDisplayPreference.SHOW_ALWAYS)

        repository.setPhoneticNameDisplay(PhoneticNameDisplay.SHOW_ALWAYS)

        verify(exactly = 0) { contactsPreferences.phoneticNameDisplayPreference = any() }
    }

    private fun givenStoredValues(
        sortOrder: Int = ContactsPreferences.SORT_ORDER_PRIMARY,
        displayOrder: Int = ContactsPreferences.DISPLAY_ORDER_PRIMARY,
        phoneticNameDisplay: Int = PhoneticNameDisplayPreference.SHOW_ALWAYS,
    ) {
        every { contactsPreferences.sortOrder } returns sortOrder
        every { contactsPreferences.displayOrder } returns displayOrder
        every { contactsPreferences.phoneticNameDisplayPreference } returns phoneticNameDisplay
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
