package com.android.contacts.ui.interactions.callsubject.screen

import android.app.Activity
import android.telecom.TelecomManager
import com.android.contacts.CallUtil
import com.android.contacts.compat.telecom.TelecomManagerCompat
import com.android.contacts.ui.interactions.callsubject.screen.model.CallSubjectEffect as Effect

internal interface CallSubjectEffectHandler {
    fun handle(effect: Effect)
}

internal class CallSubjectEffectHandlerImpl(
    private val activity: Activity,
    private val telecomManager: TelecomManager,
) : CallSubjectEffectHandler {
    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> close()
            is Effect.PlaceCall -> placeCall(effect)
        }
    }

    private fun close() {
        activity.finish()
    }

    private fun placeCall(effect: Effect.PlaceCall) {
        val intent = CallUtil.getCallWithSubjectIntent(
            effect.number,
            effect.phoneAccountHandle,
            effect.subject,
        )
        TelecomManagerCompat.placeCall(
            activity,
            telecomManager,
            intent,
        )
        close()
    }
}
