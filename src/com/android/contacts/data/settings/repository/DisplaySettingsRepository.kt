package com.android.contacts.data.settings.repository

import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.preference.ContactsPreferences
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface DisplaySettingsRepository {
    suspend fun getDisplaySettings(): DisplaySettings
    suspend fun setSortOrder(sortOrder: SortOrder)
    suspend fun setDisplayOrder(displayOrder: DisplayOrder)
    suspend fun setPhoneticNameDisplay(phoneticNameDisplay: PhoneticNameDisplay)
}

internal class DisplaySettingsRepositoryImpl @Inject constructor(
    private val contactsPreferences: ContactsPreferences,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : DisplaySettingsRepository {

    override suspend fun getDisplaySettings(): DisplaySettings {
        return withContext(ioDispatcher) {
            DisplaySettings(
                sortOrder = toSortOrder(contactsPreferences.sortOrder),
                isSortOrderChangeable = contactsPreferences.isSortOrderUserChangeable,
                displayOrder = toDisplayOrder(contactsPreferences.displayOrder),
                isDisplayOrderChangeable = contactsPreferences.isDisplayOrderUserChangeable,
                phoneticNameDisplay = toPhoneticNameDisplay(
                    contactsPreferences.phoneticNameDisplayPreference,
                ),
                isPhoneticNameDisplayChangeable =
                    contactsPreferences.isPhoneticNameDisplayPreferenceChangeable,
            )
        }
    }

    override suspend fun setSortOrder(sortOrder: SortOrder) {
        withContext(ioDispatcher) {
            val value = toSortOrderValue(sortOrder)
            if (value != contactsPreferences.sortOrder) {
                contactsPreferences.sortOrder = value
            }
        }
    }

    override suspend fun setDisplayOrder(displayOrder: DisplayOrder) {
        withContext(ioDispatcher) {
            val value = toDisplayOrderValue(displayOrder)
            if (value != contactsPreferences.displayOrder) {
                contactsPreferences.displayOrder = value
            }
        }
    }

    override suspend fun setPhoneticNameDisplay(phoneticNameDisplay: PhoneticNameDisplay) {
        withContext(ioDispatcher) {
            val value = toPhoneticNameDisplayValue(phoneticNameDisplay)
            if (value != contactsPreferences.phoneticNameDisplayPreference) {
                contactsPreferences.phoneticNameDisplayPreference = value
            }
        }
    }

    private fun toSortOrder(value: Int): SortOrder {
        return when (value) {
            ContactsPreferences.SORT_ORDER_ALTERNATIVE -> SortOrder.FAMILY_NAME_FIRST
            else -> SortOrder.GIVEN_NAME_FIRST
        }
    }

    private fun toSortOrderValue(sortOrder: SortOrder): Int {
        return when (sortOrder) {
            SortOrder.GIVEN_NAME_FIRST -> ContactsPreferences.SORT_ORDER_PRIMARY
            SortOrder.FAMILY_NAME_FIRST -> ContactsPreferences.SORT_ORDER_ALTERNATIVE
        }
    }

    private fun toDisplayOrder(value: Int): DisplayOrder {
        return when (value) {
            ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE -> DisplayOrder.FAMILY_NAME_FIRST
            else -> DisplayOrder.GIVEN_NAME_FIRST
        }
    }

    private fun toDisplayOrderValue(displayOrder: DisplayOrder): Int {
        return when (displayOrder) {
            DisplayOrder.GIVEN_NAME_FIRST -> ContactsPreferences.DISPLAY_ORDER_PRIMARY
            DisplayOrder.FAMILY_NAME_FIRST -> ContactsPreferences.DISPLAY_ORDER_ALTERNATIVE
        }
    }

    private fun toPhoneticNameDisplay(value: Int): PhoneticNameDisplay {
        return when (value) {
            ContactsPreferences.PHONETIC_NAME_DISPLAY_HIDE_IF_EMPTY ->
                PhoneticNameDisplay.HIDE_IF_EMPTY

            else -> PhoneticNameDisplay.SHOW_ALWAYS
        }
    }

    private fun toPhoneticNameDisplayValue(phoneticNameDisplay: PhoneticNameDisplay): Int {
        return when (phoneticNameDisplay) {
            PhoneticNameDisplay.SHOW_ALWAYS ->
                ContactsPreferences.PHONETIC_NAME_DISPLAY_SHOW_ALWAYS

            PhoneticNameDisplay.HIDE_IF_EMPTY ->
                ContactsPreferences.PHONETIC_NAME_DISPLAY_HIDE_IF_EMPTY
        }
    }
}
