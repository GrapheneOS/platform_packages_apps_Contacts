package com.android.contacts.domain.accounts.model

import android.graphics.drawable.Drawable

/*
 * Immutable domain model to match {@link com.android.contacts.model.account.AccountDisplayInfo}
 */
internal data class AccountDisplayModel(
    val name: String?,
    val type: String? = null,
    val dataSet: String? = null,
    val icon: Drawable? = null,
    val isDeviceAccount: Boolean = true,
) {
    fun toModel(): AccountModel {
        return AccountModel(
            name = name,
            type = type,
            dataSet = dataSet,
        )
    }
}
