package com.android.contacts.ui.vcardimport.screen

import android.app.Activity
import android.widget.Toast
import com.android.contacts.R
import com.android.contacts.domain.vcard.model.ImportVCardError as Error
import com.android.contacts.ui.vcardimport.screen.model.ImportVCardEffect as Effect

internal interface ImportVCardEffectHandler {
    fun handle(effect: Effect)
}

internal class ImportVCardEffectHandlerImpl(
    private val activity: Activity,
) : ImportVCardEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            // Already handled by the view
            is Effect.RequestPermissions,
            Effect.SelectAccount,
            Effect.SelectFiles,
            -> Unit

            is Effect.ShowImportError -> {
                Toast.makeText(
                    activity,
                    activity.getString(
                        when (effect.error) {
                            Error.OutOfMemory -> R.string.fail_reason_low_memory_during_import
                            Error.Io -> R.string.fail_reason_io_error
                            Error.NotSupported -> R.string.fail_reason_not_supported
                        },
                    ),
                    Toast.LENGTH_LONG,
                ).show()
            }

            Effect.Close -> {
                activity.finish()
            }
        }
    }
}
