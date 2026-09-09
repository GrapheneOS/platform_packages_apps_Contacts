package com.android.contacts.ui.interactions.showorcreate.screen

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.provider.ContactsContract
import com.android.contacts.activities.PeopleActivity
import com.android.contacts.ui.interactions.showorcreate.screen.model.ShowOrCreateEffect as Effect
import com.android.contacts.util.ImplicitIntentsUtil

internal interface ShowOrCreateEffectHandler {
    fun handle(effect: Effect)
}

internal class ShowOrCreateEffectHandlerImpl(
    private val activity: Activity,
) : ShowOrCreateEffectHandler {
    override fun handle(effect: Effect) {
        when (effect) {
            Effect.Close -> {
                activity.finish()
            }

            is Effect.ShowContact -> {
                ImplicitIntentsUtil.startActivityInApp(
                    activity,
                    Intent(Intent.ACTION_VIEW, effect.uri),
                )
                activity.finish()
            }

            is Effect.ShowContactList -> {
                val intent = Intent(Intent.ACTION_SEARCH)
                    .setComponent(ComponentName(activity, PeopleActivity::class.java))
                    .putExtras(effect.extras)
                activity.startActivity(intent)
                activity.finish()
            }

            is Effect.CreateContact -> {
                val intent = Intent(Intent.ACTION_INSERT)
                    .setDataAndType(
                        ContactsContract.RawContacts.CONTENT_URI,
                        ContactsContract.RawContacts.CONTENT_TYPE,
                    )
                    .putExtras(effect.extras)
                ImplicitIntentsUtil.startActivityInApp(activity, intent)
                activity.finish()
            }

            is Effect.CreateOrEditContact -> {
                val intent = Intent(Intent.ACTION_INSERT_OR_EDIT)
                    .setType(ContactsContract.RawContacts.CONTENT_ITEM_TYPE)
                    .putExtras(effect.extras)
                ImplicitIntentsUtil.startActivityInApp(activity, intent)
                activity.finish()
            }
        }
    }
}
