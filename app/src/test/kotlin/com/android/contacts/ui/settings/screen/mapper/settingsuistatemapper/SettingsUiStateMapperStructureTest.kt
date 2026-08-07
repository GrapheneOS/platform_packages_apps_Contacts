package com.android.contacts.ui.settings.screen.mapper.settingsuistatemapper

import com.android.contacts.R
import com.android.contacts.ui.settings.screen.model.SettingsGroupId
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SettingsUiStateMapperStructureTest : BaseSettingsUiStateMapperTest() {

    private fun titleOf(uiState: SettingsUiState, id: SettingsGroupId): String? {
        return uiState.groups.first { it.id == id }.title
    }

    @Test
    fun map_buildsEveryGroupInScreenOrder() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertEquals(
            listOf(
                SettingsGroupId.PROFILE,
                SettingsGroupId.ACCOUNTS,
                SettingsGroupId.DISPLAY,
                SettingsGroupId.DATA,
                SettingsGroupId.PERMISSIONS,
                SettingsGroupId.ABOUT,
            ),
            uiState.groups.map { it.id },
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
                SettingsItemId.CALL_LOG_PERMISSION,
                SettingsItemId.ABOUT,
            ),
            itemIds(allItems(uiState)),
        )
    }

    @Test
    fun map_headsThePermissionsSection() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertEquals(
            "string-${R.string.settings_section_permissions}",
            titleOf(uiState, SettingsGroupId.PERMISSIONS),
        )
    }

    @Test
    fun map_headsTheDisplayAndDataSections() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertEquals(
            "string-${R.string.settings_section_display_options}",
            titleOf(uiState, SettingsGroupId.DISPLAY),
        )
        assertEquals(
            "string-${R.string.settings_section_manage_contacts}",
            titleOf(uiState, SettingsGroupId.DATA),
        )
    }

    @Test
    fun map_leavesTheRemainingSectionsWithoutAHeading() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertNull(titleOf(uiState, SettingsGroupId.PROFILE))
        assertNull(titleOf(uiState, SettingsGroupId.ACCOUNTS))
        assertNull(titleOf(uiState, SettingsGroupId.ABOUT))
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

        assertTrue(uiState.groups.none { it.id == SettingsGroupId.DISPLAY })
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
            itemIds(uiState.groups.first { it.id == SettingsGroupId.DISPLAY }.items),
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
            itemIds(uiState.groups.first { it.id == SettingsGroupId.DATA }.items),
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
            itemIds(uiState.groups.first { it.id == SettingsGroupId.DATA }.items),
        )
    }

    @Test
    fun map_whenAboutIsUnavailable_dropsTheAboutGroup() {
        val settingsData = settingsData(
            availability = AVAILABILITY.copy(isAboutAvailable = false),
        )

        val uiState = mapper.map(settingsData = settingsData, profile = null)

        assertTrue(uiState.groups.none { it.id == SettingsGroupId.ABOUT })
    }
}
