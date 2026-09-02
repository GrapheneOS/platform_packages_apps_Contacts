package com.android.contacts.ui.interactions.account.screen

import android.app.Activity
import android.content.Intent
import com.android.contacts.ui.interactions.account.SelectAccountActivity
import com.android.contacts.ui.interactions.account.screen.model.SelectAccountEffect as Effect

internal interface SelectAccountEffectHandler {
    fun handle(effect: Effect)
}

internal class SelectAccountEffectHandlerImpl(
    private val activity: Activity,
) : SelectAccountEffectHandler {
    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> {
                when {
                    effect.account != null ->
                        activity.setResult(
                            Activity.RESULT_OK,
                            Intent()
                                .putExtra(SelectAccountActivity.EXTRA_ACCOUNT, effect.account),
                        )
                    else -> activity.setResult(Activity.RESULT_CANCELED)
                }
                activity.finish()
            }
        }
    }
}
