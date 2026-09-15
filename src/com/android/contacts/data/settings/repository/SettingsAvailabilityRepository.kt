package com.android.contacts.data.settings.repository

import android.content.Context
import android.provider.BlockedNumberContract
import android.provider.ContactsContract.ProviderStatus
import android.telephony.TelephonyManager
import com.android.contacts.R
import com.android.contacts.compat.TelephonyManagerCompat
import com.android.contacts.data.settings.model.SettingsAvailability
import com.android.contacts.di.core.IoDispatcher
import com.android.contacts.list.ProviderStatusWatcher
import com.android.contactsbind.HelpUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

internal interface SettingsAvailabilityRepository {
    suspend fun getSettingsAvailability(): SettingsAvailability
}

internal class SettingsAvailabilityRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val providerStatusWatcher: ProviderStatusWatcher,
    private val telephonyManager: TelephonyManager,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : SettingsAvailabilityRepository {

    override suspend fun getSettingsAvailability(): SettingsAvailability {
        return withContext(ioDispatcher) {
            SettingsAvailability(
                areContactsAvailable = areContactsAvailable(),
                areBlockedNumbersAvailable = areBlockedNumbersAvailable(),
                isAboutAvailable = !HelpUtils.isHelpAndFeedbackAvailable(),
                isImportFromVCardAvailable = isImportFromVCardAvailable(),
            )
        }
    }

    private fun areContactsAvailable(): Boolean {
        return providerStatusWatcher.providerStatus == ProviderStatus.STATUS_NORMAL
    }

    private fun areBlockedNumbersAvailable(): Boolean {
        return TelephonyManagerCompat.isVoiceCapable(telephonyManager) &&
            BlockedNumberContract.canCurrentUserBlockNumbers(context)
    }

    private fun isImportFromVCardAvailable(): Boolean {
        return context.resources.getBoolean(R.bool.config_allow_import_from_vcf_file)
    }
}
