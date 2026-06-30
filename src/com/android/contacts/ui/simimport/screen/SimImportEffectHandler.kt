package com.android.contacts.ui.simimport.screen

import android.app.Activity
import com.android.contacts.ui.simimport.screen.model.SimImportEffect as Effect

internal interface SimImportEffectHandler {
    fun handle(effect: Effect)
}

internal class SimImportEffectHandlerImpl(private val activity: Activity) :
    SimImportEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            Effect.Close -> activity.finish()
        }
    }
}
