package com.android.contacts.ui.vcard.importing.screen

import android.app.Activity
import android.widget.Toast
import com.android.contacts.R
import com.android.contacts.domain.vcard.model.ImportVCardError as Error
import com.android.contacts.ui.vcard.importing.screen.model.ImportVCardEffect

internal interface ImportVCardEffectHandler {
    fun handle(effect: ImportVCardEffect.OneOff)
}

internal class ImportVCardEffectHandlerImpl(
    private val activity: Activity,
) : ImportVCardEffectHandler {

    override fun handle(effect: ImportVCardEffect.OneOff) {
        when (effect) {
            is ImportVCardEffect.ShowImportError -> {
                Toast.makeText(
                    activity,
                    activity.getString(
                        when (effect.error) {
                            Error.OutOfMemory -> R.string.fail_reason_low_memory_during_import
                            Error.Io -> R.string.fail_reason_io_error
                            Error.NotSupported -> R.string.fail_reason_not_supported
                            Error.Unknown -> R.string.fail_reason_unknown
                        },
                    ),
                    Toast.LENGTH_LONG,
                ).show()
            }

            ImportVCardEffect.Close -> {
                activity.finish()
            }
        }
    }
}
