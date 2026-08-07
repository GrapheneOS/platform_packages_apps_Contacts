package com.android.contacts.ui.settings.screen.mapper.settingsuistatemapper

import com.android.contacts.R
import com.android.contacts.data.contactsfilter.model.ContactsFilter
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class SettingsUiStateMapperSummaryTest : BaseSettingsUiStateMapperTest() {

    @Test
    fun map_whenProfileIsNotLoaded_leavesMyInfoWithoutSummary() {
        val uiState = mapper.map(settingsData = settingsData(), profile = null)

        assertNull(summaryOf(uiState, SettingsItemId.MY_INFO))
    }

    @Test
    fun map_whenThereIsNoProfile_offersToSetItUp() {
        val uiState = mapper.map(
            settingsData = settingsData(),
            profile = ProfileData(hasProfile = false),
        )

        assertEquals(
            "string-${R.string.set_up_profile}",
            summaryOf(uiState, SettingsItemId.MY_INFO),
        )
    }

    @Test
    fun map_whenProfileHasName_showsIt() {
        val uiState = mapper.map(
            settingsData = settingsData(),
            profile = ProfileData(hasProfile = true, displayName = "Anna Smith"),
        )

        assertEquals("Anna Smith", summaryOf(uiState, SettingsItemId.MY_INFO))
    }

    @Test
    fun map_whenProfileHasNoName_showsMissingNamePlaceholder() {
        val uiState = mapper.map(
            settingsData = settingsData(),
            profile = ProfileData(hasProfile = true, displayName = ""),
        )

        assertEquals(
            "string-${R.string.missing_name}",
            summaryOf(uiState, SettingsItemId.MY_INFO),
        )
    }

    @Test
    fun map_whenProfileNameIsAPhoneNumber_keepsTheNumberReadable() {
        val uiState = mapper.map(
            settingsData = settingsData(),
            profile = ProfileData(
                hasProfile = true,
                displayName = "+31 6 1234 5678",
                isDisplayNameFromPhoneNumber = true,
            ),
        )

        val summary = summaryOf(uiState, SettingsItemId.MY_INFO)

        assertTrue(summary.orEmpty().contains("+31 6 1234 5678"))
    }

    @Test
    fun map_whenFilterIsAllAccounts_showsAllAccountsSummary() {
        val uiState = mapper.map(
            settingsData = settingsData(contactsFilter = ContactsFilter.ALL_ACCOUNTS),
            profile = null,
        )

        assertEquals(
            "string-${R.string.list_filter_all_accounts}",
            summaryOf(uiState, SettingsItemId.CONTACTS_FILTER),
        )
    }

    @Test
    fun map_whenFilterIsCustom_showsCustomSummary() {
        val uiState = mapper.map(
            settingsData = settingsData(contactsFilter = ContactsFilter.CUSTOM),
            profile = null,
        )

        assertEquals(
            "string-${R.string.listCustomView}",
            summaryOf(uiState, SettingsItemId.CONTACTS_FILTER),
        )
    }

    @Test
    fun map_whenThereIsNoFilter_leavesFilterWithoutSummary() {
        val uiState = mapper.map(settingsData = settingsData(contactsFilter = null), profile = null)

        assertNull(summaryOf(uiState, SettingsItemId.CONTACTS_FILTER))
    }

    @Test
    fun map_whenCallLogPermissionIsGranted_showsItAsAllowed() {
        val uiState = mapper.map(
            settingsData = settingsData(isCallLogPermissionGranted = true),
            profile = null,
        )

        assertEquals(
            "string-${R.string.settings_permission_allowed}",
            summaryOf(uiState, SettingsItemId.CALL_LOG_PERMISSION),
        )
    }

    @Test
    fun map_whenCallLogPermissionIsDenied_showsItAsNotAllowed() {
        val uiState = mapper.map(
            settingsData = settingsData(isCallLogPermissionGranted = false),
            profile = null,
        )

        assertEquals(
            "string-${R.string.settings_permission_not_allowed}",
            summaryOf(uiState, SettingsItemId.CALL_LOG_PERMISSION),
        )
    }

    @Test
    fun map_showsDefaultAccountLabelAsSummary() {
        val uiState = mapper.map(
            settingsData = settingsData(defaultAccountLabel = "Device"),
            profile = null,
        )

        assertEquals("Device", summaryOf(uiState, SettingsItemId.DEFAULT_ACCOUNT))
    }

    @Test
    fun map_showsSelectedDisplayOptionsAsSummaries() {
        val uiState = mapper.map(settingsData = alternativeDisplayOptions(), profile = null)

        assertEquals(
            "string-${R.string.display_options_sort_by_family_name}",
            summaryOf(uiState, SettingsItemId.SORT_ORDER),
        )
        assertEquals(
            "string-${R.string.display_options_view_family_name_first}",
            summaryOf(uiState, SettingsItemId.DISPLAY_ORDER),
        )
        assertEquals(
            "string-${R.string.editor_options_hide_phonetic_names_if_empty}",
            summaryOf(uiState, SettingsItemId.PHONETIC_NAME_DISPLAY),
        )
    }

    @Test
    fun map_copiesBuildVersionForTheAboutScreen() {
        val uiState = mapper.map(
            settingsData = settingsData(buildVersion = "1.7.40"),
            profile = null,
        )

        assertEquals("1.7.40", uiState.buildVersion)
    }

    @Test
    fun map_copiesSelectedDisplayOptionsForTheDialogs() {
        val uiState = mapper.map(settingsData = alternativeDisplayOptions(), profile = null)

        assertEquals(SortOrder.FAMILY_NAME_FIRST, uiState.sortOrder)
        assertEquals(DisplayOrder.FAMILY_NAME_FIRST, uiState.displayOrder)
        assertEquals(PhoneticNameDisplay.HIDE_IF_EMPTY, uiState.phoneticNameDisplay)
    }

    private fun alternativeDisplayOptions() = settingsData(
        displaySettings = DISPLAY_SETTINGS.copy(
            sortOrder = SortOrder.FAMILY_NAME_FIRST,
            displayOrder = DisplayOrder.FAMILY_NAME_FIRST,
            phoneticNameDisplay = PhoneticNameDisplay.HIDE_IF_EMPTY,
        ),
    )
}
