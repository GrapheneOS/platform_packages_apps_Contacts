package com.android.contacts.ui.interactions.callsubject

import android.app.Activity
import android.content.Intent
import android.telecom.TelecomManager
import com.android.contacts.CallUtil
import com.android.contacts.compat.telecom.TelecomManagerCompat
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectEffectHandler
import com.android.contacts.ui.interactions.callsubject.screen.CallSubjectEffectHandlerImpl
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectEffect as Effect
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
internal class CallSubjectEffectHandlerTest {

    private val activity = mockk<Activity>(relaxed = true)
    private val telecomManager = mockk<TelecomManager>(relaxed = true)
    private val callIntent = mockk<Intent>()

    private val effectHandler: CallSubjectEffectHandler = CallSubjectEffectHandlerImpl(
        activity = activity,
        telecomManager = telecomManager,
    )

    @Before
    fun setUp() {
        mockkStatic(CallUtil::class)
        every {
            CallUtil.getCallWithSubjectIntent(any(), any(), any())
        } returns callIntent
        mockkStatic(TelecomManagerCompat::class)
        every {
            TelecomManagerCompat.placeCall(any(), any(), any())
        } answers {}
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun close_finishesTheActivity() {
        effectHandler.handle(Effect.Close)

        verify { activity.finish() }
    }

    @Test
    fun placeCall_placesACallAndFinishesActivity() {
        effectHandler.handle(
            Effect.PlaceCall(
                subject = SUBJECT,
                number = NUMBER,
                phoneAccountHandle = null,
            ),
        )

        verify { TelecomManagerCompat.placeCall(activity, telecomManager, callIntent) }
        verify { activity.finish() }
    }

    private companion object {
        const val SUBJECT = "Running late"
        const val NUMBER = "+12345678901"
    }
}
