package com.android.contacts.ui.contactcreation.model

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.PhoneType
import java.util.UUID
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
internal data class ContactCreationUiState(
    val nameState: NameState = NameState(),
    val phoneNumbers: List<PhoneFieldState> = listOf(PhoneFieldState()),
    val emails: List<EmailFieldState> = listOf(EmailFieldState()),
    val photoUri: Uri? = null,
    val selectedAccount: AccountWithDataSet? = null,
    val accountName: String? = null,
    val isSaving: Boolean = false,
) : Parcelable {
    fun hasPendingChanges(): Boolean =
        nameState.hasData() ||
            phoneNumbers.any { it.number.isNotBlank() } ||
            emails.any { it.address.isNotBlank() } ||
            photoUri != null
}

@Parcelize
internal data class PhoneFieldState(
    val id: String = UUID.randomUUID().toString(),
    val number: String = "",
    val type: PhoneType = PhoneType.Mobile,
) : Parcelable

@Parcelize
internal data class EmailFieldState(
    val id: String = UUID.randomUUID().toString(),
    val address: String = "",
    val type: EmailType = EmailType.Home,
) : Parcelable
