package com.android.contacts.ui.group.edit.screen

import android.app.Activity
import com.android.contacts.ui.group.edit.screen.model.GroupNameEditEffect as Effect

internal interface GroupNameEditEffectHandler {
    fun handle(effect: Effect)
}

internal class GroupNameEditEffectHandlerImpl(
    private val activity: Activity,
) : GroupNameEditEffectHandler {

    override fun handle(effect: Effect) {
        when (effect) {
            is Effect.Close -> {
                activity.setResult(
                    when (effect.isSuccessful) {
                        true -> Activity.RESULT_OK
                        false -> Activity.RESULT_CANCELED
                    }
                )
                activity.finish()
            }
        }
    }
}
