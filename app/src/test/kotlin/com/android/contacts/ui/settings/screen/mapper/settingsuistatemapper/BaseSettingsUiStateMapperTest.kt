package com.android.contacts.ui.settings.screen.mapper.settingsuistatemapper

import android.content.Context
import com.android.contacts.data.contactsfilter.model.ContactsFilter
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SettingsAvailability
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.domain.settings.model.SettingsData
import com.android.contacts.ui.settings.screen.mapper.SettingsUiStateMapperImpl
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import com.android.contacts.ui.settings.screen.model.SettingsItemUiModel
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import io.mockk.every
import io.mockk.mockk
import org.junit.Before

internal abstract class BaseSettingsUiStateMapperTest {

    protected val context = mockk<Context>()

    protected val mapper = SettingsUiStateMapperImpl(context)

    @Before
    fun setUpStrings() {
        every { context.getString(any()) } answers { "string-${firstArg<Int>()}" }
    }

    protected fun settingsData(
        availability: SettingsAvailability = AVAILABILITY,
        displaySettings: DisplaySettings = DISPLAY_SETTINGS,
        defaultAccountLabel: String? = null,
        contactsFilter: ContactsFilter? = null,
    ): SettingsData {
        return SettingsData(
            availability = availability,
            displaySettings = displaySettings,
            defaultAccountLabel = defaultAccountLabel,
            contactsFilter = contactsFilter,
        )
    }

    protected fun itemIds(items: List<SettingsItemUiModel>): List<SettingsItemId> {
        return items.map { it.id }
    }

    protected fun allItems(uiState: SettingsUiState): List<SettingsItemUiModel> {
        return uiState.groups.flatMap { it.items }
    }

    protected fun summaryOf(uiState: SettingsUiState, id: SettingsItemId): String? {
        return allItems(uiState).first { it.id == id }.summary
    }

    protected companion object {
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
