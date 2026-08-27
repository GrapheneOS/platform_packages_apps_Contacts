package com.android.contacts.ui.contactdetails.screen.mapper

import android.content.Context
import com.android.contacts.R
import com.android.contacts.data.calllog.model.CallLogEntryType
import com.android.contacts.domain.calllog.model.RecentCall
import com.android.contacts.ui.contactdetails.screen.model.RecentCallDirection
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class RecentCallsMapperImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()
    private val mapper = RecentCallsMapperImpl(context = context)

    @Test
    fun map_forAnAnsweredCall_showsTheCallDuration() {
        val uiModel = mapper.map(listOf(recentCall(duration = 80.seconds))).single()

        assertEquals(
            context.getString(R.string.contact_details_call_duration, "1m 20s"),
            uiModel.title,
        )
    }

    @Test
    fun map_forAShortCall_showsSecondsOnly() {
        val uiModel = mapper.map(listOf(recentCall(duration = 58.seconds))).single()

        assertEquals(
            context.getString(R.string.contact_details_call_duration, "58s"),
            uiModel.title,
        )
    }

    @Test
    fun map_forALongCall_showsHoursAndMinutes() {
        val uiModel = mapper.map(listOf(recentCall(duration = 3_960.seconds))).single()

        assertEquals(
            context.getString(R.string.contact_details_call_duration, "1h 6m"),
            uiModel.title,
        )
    }

    @Test
    fun map_forAMissedCall_showsTheTypeInstead() {
        val recentCall = recentCall(type = CallLogEntryType.MISSED, duration = Duration.ZERO)
        val uiModel = mapper.map(listOf(recentCall)).single()

        assertEquals(context.getString(R.string.contact_details_call_missed), uiModel.title)
    }

    @Test
    fun map_forACallWithoutDuration_showsTheTypeInstead() {
        val uiModel = mapper.map(listOf(recentCall(duration = Duration.ZERO))).single()

        assertEquals(context.getString(R.string.contact_details_call_incoming), uiModel.title)
    }

    @Test
    fun map_passesTheNumberLabelThrough() {
        val uiModel = mapper.map(listOf(recentCall(numberLabel = "Mobile"))).single()

        assertEquals("Mobile", uiModel.numberLabel)
    }

    @Test
    fun map_withoutANumberLabel_fallsBackToTheNumber() {
        val uiModel = mapper.map(listOf(recentCall(numberLabel = null))).single()

        assertEquals("4155551212", uiModel.numberLabel)
    }

    @Test
    fun map_picksADirectionPerType() {
        val recentCalls = listOf(
            recentCall(type = CallLogEntryType.INCOMING),
            recentCall(type = CallLogEntryType.OUTGOING),
            recentCall(type = CallLogEntryType.MISSED),
            recentCall(type = CallLogEntryType.VOICEMAIL),
            recentCall(type = CallLogEntryType.REJECTED),
            recentCall(type = CallLogEntryType.BLOCKED),
        )

        assertEquals(
            listOf(
                RecentCallDirection.INCOMING,
                RecentCallDirection.OUTGOING,
                RecentCallDirection.MISSED,
                RecentCallDirection.VOICEMAIL,
                RecentCallDirection.REJECTED,
                RecentCallDirection.BLOCKED,
            ),
            mapper.map(recentCalls).map { uiModel -> uiModel.direction },
        )
    }

    @Test
    fun map_forACallAnsweredElsewhere_showsItAsIncoming() {
        val recentCall = recentCall(type = CallLogEntryType.ANSWERED_EXTERNALLY)
        val uiModel = mapper.map(listOf(recentCall)).single()

        assertEquals(RecentCallDirection.INCOMING, uiModel.direction)
    }

    @Test
    fun map_forAnUnknownType_showsItAsMissed() {
        val recentCall = recentCall(type = CallLogEntryType.OTHER, duration = Duration.ZERO)
        val uiModel = mapper.map(listOf(recentCall)).single()

        assertEquals(RecentCallDirection.MISSED, uiModel.direction)
        assertEquals(context.getString(R.string.contact_details_call_missed), uiModel.title)
    }

    @Test
    fun map_forADeclinedCall_hidesTheDuration() {
        val recentCall = recentCall(type = CallLogEntryType.REJECTED, duration = 42.seconds)
        val uiModel = mapper.map(listOf(recentCall)).single()

        assertEquals(context.getString(R.string.contact_details_call_rejected), uiModel.title)
    }

    @Test
    fun map_forAVoicemail_keepsTheDuration() {
        val recentCall = recentCall(type = CallLogEntryType.VOICEMAIL, duration = 58.seconds)
        val uiModel = mapper.map(listOf(recentCall)).single()

        assertEquals(
            context.getString(R.string.contact_details_call_duration, "58s"),
            uiModel.title,
        )
    }

    @Test
    fun map_describesTheCallForScreenReaders() {
        val uiModel = mapper.map(listOf(recentCall(duration = 80.seconds))).single()

        assertEquals(
            context.getString(
                R.string.contact_details_recent_call_description,
                context.getString(R.string.contact_details_call_incoming),
                "1m 20s",
                uiModel.date,
            ),
            uiModel.contentDescription,
        )
    }

    @Test
    fun map_forACallWithoutADuration_describesItWithoutAnEmptyPart() {
        val recentCall = recentCall(type = CallLogEntryType.MISSED, duration = Duration.ZERO)
        val uiModel = mapper.map(listOf(recentCall)).single()

        assertEquals(
            context.getString(
                R.string.contact_details_recent_call_description_short,
                context.getString(R.string.contact_details_call_missed),
                uiModel.date,
            ),
            uiModel.contentDescription,
        )
    }

    private fun recentCall(
        type: CallLogEntryType = CallLogEntryType.INCOMING,
        duration: Duration = 42.seconds,
        numberLabel: String? = "Mobile",
    ): RecentCall {
        return RecentCall(
            number = "4155551212",
            numberLabel = numberLabel,
            date = 1_700_000_000_000L,
            duration = duration,
            type = type,
        )
    }
}
