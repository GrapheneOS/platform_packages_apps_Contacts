package com.android.contacts.domain.calllog.usecase

import com.android.contacts.data.calllog.model.CallLogEntry
import com.android.contacts.data.calllog.repository.CallLogRepository
import com.android.contacts.data.contactdetails.model.ContactDataItem
import com.android.contacts.data.contactdetails.model.ContactDetails
import com.android.contacts.data.permissions.repository.PermissionsRepository
import com.android.contacts.data.telecom.source.IsDeviceVoiceCapable
import com.android.contacts.domain.calllog.model.RecentCall
import javax.inject.Inject

internal fun interface GetRecentCalls {
    suspend operator fun invoke(details: ContactDetails): List<RecentCall>
}

internal class GetRecentCallsImpl @Inject constructor(
    private val callLogRepository: CallLogRepository,
    private val permissionsRepository: PermissionsRepository,
    private val isDeviceVoiceCapable: IsDeviceVoiceCapable,
) : GetRecentCalls {

    override suspend operator fun invoke(details: ContactDetails): List<RecentCall> {
        if (!isDeviceVoiceCapable() || !permissionsRepository.isCallLogGranted()) {
            return emptyList()
        }

        val labelsByNumber = labelsByNumber(details)

        return recentEntries(labelsByNumber.keys).map { entry ->
            toRecentCall(
                entry = entry,
                numberLabel = labelsByNumber[entry.number],
            )
        }
    }

    private fun labelsByNumber(details: ContactDetails): Map<String, String?> {
        return details.dataItems
            .filterIsInstance<ContactDataItem.Phone>()
            .mapNotNull { phone ->
                val number = phone.number?.takeIf { value -> value.isNotBlank() }

                number?.to(phone.typeLabel)
            }
            .toMap()
    }

    private suspend fun recentEntries(numbers: Set<String>): List<CallLogEntry> {
        val entries = mutableListOf<CallLogEntry>()

        for (number in numbers) {
            entries += callLogRepository.getRecentCalls(number, RECENT_CALL_LIMIT)
        }

        return entries
            .distinctBy { entry -> entry.date }
            .sortedByDescending { entry -> entry.date }
            .take(RECENT_CALL_LIMIT)
    }

    private fun toRecentCall(
        entry: CallLogEntry,
        numberLabel: String?,
    ): RecentCall {
        return RecentCall(
            number = entry.number,
            numberLabel = numberLabel,
            date = entry.date,
            duration = entry.duration,
            type = entry.type,
        )
    }

    private companion object {
        const val RECENT_CALL_LIMIT = 3
    }
}
