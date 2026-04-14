package com.android.contacts.ui.contactcreation.model

import android.net.Uri
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.PhoneType

internal sealed interface ContactCreationAction {
    // Navigation
    data object NavigateBack : ContactCreationAction
    data object Save : ContactCreationAction
    data object ConfirmDiscard : ContactCreationAction

    // Name
    data class UpdatePrefix(val value: String) : ContactCreationAction
    data class UpdateFirstName(val value: String) : ContactCreationAction
    data class UpdateMiddleName(val value: String) : ContactCreationAction
    data class UpdateLastName(val value: String) : ContactCreationAction
    data class UpdateSuffix(val value: String) : ContactCreationAction

    // Phone
    data object AddPhone : ContactCreationAction
    data class RemovePhone(val id: String) : ContactCreationAction
    data class UpdatePhone(val id: String, val value: String) : ContactCreationAction
    data class UpdatePhoneType(val id: String, val type: PhoneType) : ContactCreationAction

    // Email
    data object AddEmail : ContactCreationAction
    data class RemoveEmail(val id: String) : ContactCreationAction
    data class UpdateEmail(val id: String, val value: String) : ContactCreationAction
    data class UpdateEmailType(val id: String, val type: EmailType) : ContactCreationAction

    // Photo
    data class SetPhoto(val uri: Uri) : ContactCreationAction
    data object RemovePhoto : ContactCreationAction

    // Account
    data class SelectAccount(val account: AccountWithDataSet) : ContactCreationAction
}
