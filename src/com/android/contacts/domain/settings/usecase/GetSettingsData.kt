package com.android.contacts.domain.settings.usecase

import com.android.contacts.data.accounts.repository.AccountsRepository
import com.android.contacts.data.appinfo.repository.AppInfoRepository
import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepository
import com.android.contacts.data.permissions.repository.PermissionsRepository
import com.android.contacts.data.settings.model.DisplaySettings
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.domain.settings.model.SettingsData
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal fun interface GetSettingsData {
    operator fun invoke(): Flow<SettingsData>
}

internal class GetSettingsDataImpl @Inject constructor(
    private val settingsAvailabilityRepository: SettingsAvailabilityRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val accountsRepository: AccountsRepository,
    private val contactsFilterRepository: ContactsFilterRepository,
    private val appInfoRepository: AppInfoRepository,
    private val permissionsRepository: PermissionsRepository,
) : GetSettingsData {

    override fun invoke(): Flow<SettingsData> {
        return displaySettingsRepository.observeDisplaySettings()
            .map { displaySettings ->
                settingsData(displaySettings)
            }
    }

    private suspend fun settingsData(displaySettings: DisplaySettings): SettingsData {
        return coroutineScope {
            val availability = async {
                settingsAvailabilityRepository.getSettingsAvailability()
            }
            val defaultAccountLabel = async {
                accountsRepository.getDefaultAccountLabel()
            }
            val contactsFilter = async {
                contactsFilterRepository.getContactsFilter()
            }
            val buildVersion = async {
                appInfoRepository.getBuildVersion()
            }
            val isCallLogPermissionGranted = async {
                permissionsRepository.isCallLogGranted()
            }

            SettingsData(
                availability = availability.await(),
                displaySettings = displaySettings,
                defaultAccountLabel = defaultAccountLabel.await(),
                contactsFilter = contactsFilter.await(),
                buildVersion = buildVersion.await(),
                isCallLogPermissionGranted = isCallLogPermissionGranted.await(),
            )
        }
    }
}
