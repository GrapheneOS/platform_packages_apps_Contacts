package com.android.contacts.ui.group.edit.screen.model

import com.android.contacts.domain.accounts.model.AccountModel

internal interface OpenNewGroupDialogListener {
    fun openNewGroupDialog(account: AccountModel)
}
