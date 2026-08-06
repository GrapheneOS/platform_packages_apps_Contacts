package com.android.contacts.ui.settings.screen.mapper

import android.content.Context
import android.text.BidiFormatter
import android.text.TextDirectionHeuristics
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.data.contactsfilter.model.ContactsFilter
import com.android.contacts.data.profile.model.ProfileData
import com.android.contacts.data.settings.model.DisplayOrder
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.model.PhoneticNameDisplay
import com.android.contacts.data.settings.model.SettingsAvailability
import com.android.contacts.data.settings.model.SortOrder
import com.android.contacts.domain.settings.model.SettingsData
import com.android.contacts.ui.settings.screen.model.SettingsGroupId
import com.android.contacts.ui.settings.screen.model.SettingsGroupUiModel
import com.android.contacts.ui.settings.screen.model.SettingsItemId
import com.android.contacts.ui.settings.screen.model.SettingsItemUiModel
import com.android.contacts.ui.settings.screen.model.SettingsUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface SettingsUiStateMapper {
    fun map(settingsData: SettingsData, profile: ProfileData?): SettingsUiState
}

internal class SettingsUiStateMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : SettingsUiStateMapper {

    override fun map(settingsData: SettingsData, profile: ProfileData?): SettingsUiState {
        return SettingsUiState(
            groups = buildGroups(settingsData, profile),
            sortOrder = settingsData.displaySettings.sortOrder,
            displayOrder = settingsData.displaySettings.displayOrder,
            phoneticNameDisplay = settingsData.displaySettings.phoneticNameDisplay,
        )
    }

    private fun buildGroups(
        settingsData: SettingsData,
        profile: ProfileData?,
    ): ImmutableList<SettingsGroupUiModel> {
        return listOfNotNull(
            buildGroup(SettingsGroupId.PROFILE, profileItems(profile)),
            buildGroup(SettingsGroupId.ACCOUNTS, accountItems(settingsData)),
            buildGroup(SettingsGroupId.DISPLAY, displayItems(settingsData.displaySettings)),
            buildGroup(SettingsGroupId.DATA, dataItems(settingsData.availability)),
            buildGroup(SettingsGroupId.ABOUT, aboutItems(settingsData.availability)),
        ).toImmutableList()
    }

    private fun buildGroup(
        id: SettingsGroupId,
        items: List<SettingsItemUiModel>,
    ): SettingsGroupUiModel? {
        return when {
            items.isEmpty() -> null
            else -> SettingsGroupUiModel(
                id = id,
                items = items.toImmutableList()
            )
        }
    }

    private fun profileItems(profile: ProfileData?): List<SettingsItemUiModel> {
        return listOf(
            buildItem(
                id = SettingsItemId.MY_INFO,
                titleResId = R.string.settings_my_info_title,
                summary = profileSummary(profile),
            ),
        )
    }

    private fun accountItems(settingsData: SettingsData): List<SettingsItemUiModel> {
        return listOf(
            buildItem(
                id = SettingsItemId.ACCOUNTS,
                titleResId = R.string.settings_accounts,
            ),
            buildItem(
                id = SettingsItemId.DEFAULT_ACCOUNT,
                titleResId = R.string.default_editor_account,
                summary = settingsData.defaultAccountLabel,
            ),
            buildItem(
                id = SettingsItemId.CONTACTS_FILTER,
                titleResId = R.string.menu_contacts_filter,
                summary = contactsFilterSummary(settingsData.contactsFilter),
            ),
        )
    }

    private fun displayItems(displaySettings: DisplaySettings): List<SettingsItemUiModel> {
        return buildList {
            if (displaySettings.isSortOrderChangeable) {
                add(
                    buildItem(
                        id = SettingsItemId.SORT_ORDER,
                        titleResId = R.string.display_options_sort_list_by,
                        summary = sortOrderSummary(displaySettings.sortOrder),
                    ),
                )
            }
            if (displaySettings.isDisplayOrderChangeable) {
                add(
                    buildItem(
                        id = SettingsItemId.DISPLAY_ORDER,
                        titleResId = R.string.display_options_view_names_as,
                        summary = displayOrderSummary(displaySettings.displayOrder),
                    ),
                )
            }
            if (displaySettings.isPhoneticNameDisplayChangeable) {
                add(
                    buildItem(
                        id = SettingsItemId.PHONETIC_NAME_DISPLAY,
                        titleResId = R.string.display_options_phonetic_name_fields,
                        summary = phoneticNameDisplaySummary(displaySettings.phoneticNameDisplay),
                    ),
                )
            }
        }
    }

    private fun dataItems(availability: SettingsAvailability): List<SettingsItemUiModel> {
        return buildList {
            add(
                buildItem(
                    id = SettingsItemId.IMPORT,
                    titleResId = R.string.menu_import,
                ),
            )
            if (availability.areContactsAvailable) {
                add(
                    buildItem(
                        id = SettingsItemId.EXPORT,
                        titleResId = R.string.menu_export,
                    ),
                )
            }
            if (availability.areBlockedNumbersAvailable) {
                add(
                    buildItem(
                        id = SettingsItemId.BLOCKED_NUMBERS,
                        titleResId = R.string.menu_blocked_numbers,
                    ),
                )
            }
        }
    }

    private fun aboutItems(availability: SettingsAvailability): List<SettingsItemUiModel> {
        return when {
            availability.isAboutAvailable -> listOf(
                buildItem(
                    id = SettingsItemId.ABOUT,
                    titleResId = R.string.setting_about,
                ),
            )

            else -> emptyList()
        }
    }

    private fun buildItem(
        id: SettingsItemId,
        @StringRes titleResId: Int,
        summary: String? = null,
    ): SettingsItemUiModel {
        return SettingsItemUiModel(
            id = id,
            title = context.getString(titleResId),
            summary = summary,
        )
    }

    private fun profileSummary(profile: ProfileData?): String? {
        if (profile == null) {
            return null
        }
        if (!profile.hasProfile) {
            return context.getString(R.string.set_up_profile)
        }

        val displayName = profile.displayName
            ?.takeIf { it.isNotEmpty() }
            ?: context.getString(R.string.missing_name)

        return when {
            profile.isDisplayNameFromPhoneNumber -> {
                BidiFormatter.getInstance().unicodeWrap(displayName, TextDirectionHeuristics.LTR)
            }

            else -> displayName
        }
    }

    private fun contactsFilterSummary(contactsFilter: ContactsFilter?): String? {
        return when (contactsFilter) {
            ContactsFilter.ALL_ACCOUNTS -> context.getString(R.string.list_filter_all_accounts)
            ContactsFilter.CUSTOM -> context.getString(R.string.listCustomView)
            null -> null
        }
    }

    private fun sortOrderSummary(sortOrder: SortOrder): String {
        return when (sortOrder) {
            SortOrder.GIVEN_NAME_FIRST ->
                context.getString(R.string.display_options_sort_by_given_name)

            SortOrder.FAMILY_NAME_FIRST ->
                context.getString(R.string.display_options_sort_by_family_name)
        }
    }

    private fun displayOrderSummary(displayOrder: DisplayOrder): String {
        return when (displayOrder) {
            DisplayOrder.GIVEN_NAME_FIRST ->
                context.getString(R.string.display_options_view_given_name_first)

            DisplayOrder.FAMILY_NAME_FIRST ->
                context.getString(R.string.display_options_view_family_name_first)
        }
    }

    private fun phoneticNameDisplaySummary(phoneticNameDisplay: PhoneticNameDisplay): String {
        return when (phoneticNameDisplay) {
            PhoneticNameDisplay.SHOW_ALWAYS ->
                context.getString(R.string.editor_options_always_show_phonetic_names)

            PhoneticNameDisplay.HIDE_IF_EMPTY ->
                context.getString(R.string.editor_options_hide_phonetic_names_if_empty)
        }
    }
}
