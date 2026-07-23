package com.android.contacts.ui.interactions.importing.screen

import android.app.Activity
import com.android.contacts.R
import com.android.contacts.editor.SelectAccountDialogFragment
import com.android.contacts.model.AccountTypeManager
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.UIIntents
import com.android.contacts.ui.interactions.importing.screen.model.ImportEffect as Effect
import com.android.contacts.util.AccountSelectionUtil

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

            Effect.OpenSelectAccount -> {
                SelectAccountDialogFragment.show(
                    @Suppress("DEPRECATION")
                    activity.fragmentManager,
                    R.string.dialog_new_contact_account,
                    AccountTypeManager.AccountFilter.CONTACTS_INSERTABLE,
                    null,
                )
            }

            is Effect.OpenVCardImport -> {
                AccountSelectionUtil.doImportFromVcfFile(
                    activity,
                    effect.account?.let { account ->
                        AccountWithDataSet(
                            account.name,
                            account.type,
                            account.dataSet,
                        )
                    },
                )
                activity.finish()
            }
        }
    }
}
