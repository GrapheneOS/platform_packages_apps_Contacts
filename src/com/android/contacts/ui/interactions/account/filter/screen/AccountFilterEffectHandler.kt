package com.android.contacts.ui.interactions.account.filter.screen

import android.app.Activity
import android.content.Intent
import com.android.contacts.ui.interactions.account.filter.AccountFilterActivity
import com.android.contacts.ui.interactions.account.filter.screen.model.AccountFilterEffect as Effect

internal interface AccountFilterEffectHandler {
    fun handle(effect: Effect)
}

internal class AccountFilterEffectHandlerImpl(
    private val activity: Activity,
) : AccountFilterEffectHandler {
    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> {
                when {
                    effect.filterPicked != null ->
                        activity.setResult(
                            Activity.RESULT_OK,
                            Intent()
                                .putExtra(AccountFilterActivity.EXTRA_FILTER, effect.filterPicked),
                        )
                    else -> activity.setResult(Activity.RESULT_CANCELED)
                }
                activity.finish()
            }
        }
    }
}
