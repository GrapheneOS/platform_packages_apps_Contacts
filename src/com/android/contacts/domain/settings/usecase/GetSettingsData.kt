package com.android.contacts.domain.settings.usecase

import com.android.contacts.data.accounts.repository.AccountsRepository
import com.android.contacts.data.contactsfilter.repository.ContactsFilterRepository
import com.android.contacts.data.settings.repository.DisplaySettingsRepository
import com.android.contacts.data.settings.repository.SettingsAvailabilityRepository
import com.android.contacts.domain.settings.model.SettingsData
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal fun interface GetSettingsData {
    suspend operator fun invoke(): SettingsData
}

internal class GetSettingsDataImpl @Inject constructor(
    private val settingsAvailabilityRepository: SettingsAvailabilityRepository,
    private val displaySettingsRepository: DisplaySettingsRepository,
    private val accountsRepository: AccountsRepository,
    private val contactsFilterRepository: ContactsFilterRepository,
) : GetSettingsData {

    override suspend fun invoke(): SettingsData {
        return coroutineScope {
            val availability = async {
                settingsAvailabilityRepository.getSettingsAvailability()
            }
            val displaySettings = async {
                displaySettingsRepository.getDisplaySettings()
            }
            val defaultAccountLabel = async {
                accountsRepository.getDefaultAccountLabel()
            }
            val contactsFilter = async {
                contactsFilterRepository.getContactsFilter()
            }

            SettingsData(
                availability = availability.await(),
                displaySettings = displaySettings.await(),
                defaultAccountLabel = defaultAccountLabel.await(),
                contactsFilter = contactsFilter.await(),
            )
        }
    }
}
