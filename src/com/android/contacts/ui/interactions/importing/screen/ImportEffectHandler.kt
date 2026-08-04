package com.android.contacts.ui.interactions.importing.screen

import android.app.Activity
import com.android.contacts.ui.UIIntents
import com.android.contacts.ui.interactions.importing.screen.model.ImportEffect as Effect

internal interface ImportEffectHandler {
    fun handle(effect: Effect)
}

internal class ImportEffectHandlerImpl(
    private val activity: Activity,
) : ImportEffectHandler {
    override fun handle(effect: Effect) {
        when (effect) {
            Effect.Close -> {
                activity.finish()
            }

            is Effect.OpenSimImport -> {
                activity.startActivity(
                    UIIntents.getSimImportIntent(activity, effect.subscriptionId),
                )
            }

            is Effect.OpenVCardImport -> {
                activity.startActivity(UIIntents.getImportVCardIntent(activity))
                activity.finish()
            }
        }
    }
}
