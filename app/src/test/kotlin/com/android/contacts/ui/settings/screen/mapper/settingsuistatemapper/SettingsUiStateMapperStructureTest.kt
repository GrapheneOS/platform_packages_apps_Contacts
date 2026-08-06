package com.android.contacts.ui.settings.screen.mapper.settingsuistatemapper

import com.android.contacts.ui.settings.screen.model.SettingsGroupId
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SettingsUiStateMapperStructureTest : BaseSettingsUiStateMapperTest() {

    @Test
    fun map_buildsEveryGroupInScreenOrder() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertEquals(
            listOf(
                SettingsGroupId.PROFILE,
                SettingsGroupId.ACCOUNTS,
                SettingsGroupId.DISPLAY,
                SettingsGroupId.DATA,
                SettingsGroupId.ABOUT,
            ),
            uiState.groups?.map { it.id },
        )
    }

    @Test
    fun map_keepsItemsInScreenOrder() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertEquals(
            listOf(
                SettingsItemId.MY_INFO,
                SettingsItemId.ACCOUNTS,
                SettingsItemId.DEFAULT_ACCOUNT,
                SettingsItemId.CONTACTS_FILTER,
                SettingsItemId.SORT_ORDER,
                SettingsItemId.DISPLAY_ORDER,
                SettingsItemId.PHONETIC_NAME_DISPLAY,
                SettingsItemId.IMPORT,
                SettingsItemId.EXPORT,
                SettingsItemId.BLOCKED_NUMBERS,
                SettingsItemId.ABOUT,
            ),
            itemIds(allItems(uiState)),
        )
    }

    @Test
    fun map_whenDisplayOptionsAreLocked_dropsTheWholeDisplayGroup() {
        val settingsData = settingsData(
            displaySettings = DISPLAY_SETTINGS.copy(
                isSortOrderChangeable = false,
                isDisplayOrderChangeable = false,
                isPhoneticNameDisplayChangeable = false,
            ),
        )

        val uiState = mapper.map(settingsData = settingsData, profile = null)

        assertTrue(uiState.groups?.none { it.id == SettingsGroupId.DISPLAY } == true)
    }

    @Test
    fun map_whenOnlySortOrderIsChangeable_keepsOnlyThatItem() {
        val settingsData = settingsData(
            displaySettings = DISPLAY_SETTINGS.copy(
                isDisplayOrderChangeable = false,
                isPhoneticNameDisplayChangeable = false,
            ),
        )

        val uiState = mapper.map(settingsData = settingsData, profile = null)

        assertEquals(
            listOf(SettingsItemId.SORT_ORDER),
            itemIds(uiState.groups?.first { it.id == SettingsGroupId.DISPLAY }?.items),
        )
    }

    @Test
    fun map_whenContactsAreUnavailable_dropsExport() {
        val settingsData = settingsData(
            availability = AVAILABILITY.copy(areContactsAvailable = false),
        )

        val uiState = mapper.map(settingsData = settingsData, profile = null)

        assertEquals(
            listOf(SettingsItemId.IMPORT, SettingsItemId.BLOCKED_NUMBERS),
            itemIds(uiState.groups?.first { it.id == SettingsGroupId.DATA }?.items),
        )
    }

    @Test
    fun map_whenNumbersCannotBeBlocked_dropsBlockedNumbers() {
        val settingsData = settingsData(
            availability = AVAILABILITY.copy(areBlockedNumbersAvailable = false),
        )

        val uiState = mapper.map(settingsData = settingsData, profile = null)

        assertEquals(
            listOf(SettingsItemId.IMPORT, SettingsItemId.EXPORT),
            itemIds(uiState.groups?.first { it.id == SettingsGroupId.DATA }?.items),
        )
    }

    @Test
    fun map_whenAboutIsUnavailable_dropsTheAboutGroup() {
        val settingsData = settingsData(
            availability = AVAILABILITY.copy(isAboutAvailable = false),
        )

        val uiState = mapper.map(settingsData = settingsData, profile = null)

        assertTrue(uiState.groups?.none { it.id == SettingsGroupId.ABOUT } == true)
    }
}
