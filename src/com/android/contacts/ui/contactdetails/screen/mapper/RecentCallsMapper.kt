package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import android.text.format.DateUtils
import androidx.annotation.StringRes
import com.android.contacts.R
import com.android.contacts.data.calllog.model.CallLogEntryType
import com.android.contacts.domain.calllog.model.RecentCall
import com.android.contacts.ui.contactdetails.screen.model.RecentCallDirection
import com.android.contacts.ui.contactdetails.screen.model.RecentCallUiModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

internal interface RecentCallsMapper {
    fun map(recentCalls: List<RecentCall>): ImmutableList<RecentCallUiModel>
}

internal class RecentCallsMapperImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : RecentCallsMapper {

    override fun map(recentCalls: List<RecentCall>): ImmutableList<RecentCallUiModel> {
        return recentCalls
            .map { recentCall -> mapRecentCall(recentCall) }
            .toImmutableList()
    }

    private fun mapRecentCall(recentCall: RecentCall): RecentCallUiModel {
        val type = context.getString(typeLabel(recentCall.type))
        val duration = duration(recentCall)
        val date = date(recentCall.date)

        return RecentCallUiModel(
            title = title(type, duration),
            numberLabel = recentCall.numberLabel ?: recentCall.number,
            date = date,
            direction = direction(recentCall.type),
            contentDescription = description(type, duration, date),
        )
    }

    private fun description(
        type: String,
        duration: String?,
        date: String,
    ): String {
        if (duration == null) {
            return context.getString(
                R.string.contact_details_recent_call_description_short,
                type,
                date,
            )
        }

        return context.getString(
            R.string.contact_details_recent_call_description,
            type,
            duration,
            date,
        )
    }

    private fun title(
        type: String,
        duration: String?,
    ): String {
        if (duration == null) {
            return type
        }

        return context.getString(R.string.contact_details_call_duration, duration)
    }

    private fun duration(recentCall: RecentCall): String? {
        if (recentCall.type in UNANSWERED_TYPES || !recentCall.duration.isPositive()) {
            return null
        }

        return recentCall.duration.toComponents { hours, minutes, seconds, _ ->
            when {
                hours > 0 -> context.getString(
                    R.string.contact_details_call_duration_hours,
                    hours,
                    minutes,
                )

                minutes > 0 -> context.getString(
                    R.string.contact_details_call_duration_minutes,
                    minutes,
                    seconds,
                )

                else -> context.getString(R.string.contact_details_call_duration_seconds, seconds)
            }
        }
    }

    private fun date(date: Long): String {
        return DateUtils.formatDateTime(
            context,
            date,
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_NO_YEAR,
        )
    }

    @StringRes
    private fun typeLabel(type: CallLogEntryType): Int {
        return when (type) {
            CallLogEntryType.INCOMING -> R.string.contact_details_call_incoming
            CallLogEntryType.OUTGOING -> R.string.contact_details_call_outgoing
            CallLogEntryType.MISSED -> R.string.contact_details_call_missed
            CallLogEntryType.VOICEMAIL -> R.string.contact_details_call_voicemail
            CallLogEntryType.REJECTED -> R.string.contact_details_call_rejected
            CallLogEntryType.BLOCKED -> R.string.contact_details_call_blocked
            CallLogEntryType.ANSWERED_EXTERNALLY -> R.string.contact_details_call_answered_elsewhere
            CallLogEntryType.OTHER -> R.string.contact_details_call_missed
        }
    }

    private fun direction(type: CallLogEntryType): RecentCallDirection {
        return when (type) {
            CallLogEntryType.OUTGOING -> RecentCallDirection.OUTGOING
            CallLogEntryType.VOICEMAIL -> RecentCallDirection.VOICEMAIL
            CallLogEntryType.REJECTED -> RecentCallDirection.REJECTED
            CallLogEntryType.BLOCKED -> RecentCallDirection.BLOCKED

            CallLogEntryType.MISSED,
            CallLogEntryType.OTHER,
            -> RecentCallDirection.MISSED

            CallLogEntryType.INCOMING,
            CallLogEntryType.ANSWERED_EXTERNALLY,
            -> RecentCallDirection.INCOMING
        }
    }

    private companion object {
        val UNANSWERED_TYPES = setOf(
            CallLogEntryType.MISSED,
            CallLogEntryType.REJECTED,
            CallLogEntryType.BLOCKED,
        )
    }
}
