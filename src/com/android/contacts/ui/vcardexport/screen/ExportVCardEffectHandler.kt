package com.android.contacts.ui.vcardexport.screen

import android.app.Activity
import android.widget.Toast
import com.android.contacts.R
import com.android.contacts.ui.vcardexport.screen.model.ExportVCardEffect as Effect

internal interface ExportVCardEffectHandler {
    fun handle(effect: Effect)
}

internal class ExportVCardEffectHandlerImpl(
    private val activity: Activity,
) : ExportVCardEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            // Already handled by the view
            is Effect.RequestPermissions,
            Effect.SelectFile,
            -> Unit

            Effect.ShowError -> {
                Toast.makeText(
                    activity,
                    activity.getString(R.string.exporting_contact_failed_title),
                    Toast.LENGTH_LONG,
                ).show()
            }

            Effect.Close -> {
                activity.finish()
            }
        }
    }
}
