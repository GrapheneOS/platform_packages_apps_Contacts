package com.android.contacts.ui.vcard.cancel.screen

import android.app.Activity
import com.android.contacts.ui.vcard.cancel.screen.model.CancelImportExportEffect as Effect

internal interface CancelImportExportEffectHandler {
    fun handle(effect: Effect)
}

internal class CancelImportExportEffectHandlerImpl(
    private val activity: Activity,
) : CancelImportExportEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            Effect.CLOSE -> {
                activity.finish()
            }
        }
    }
}
