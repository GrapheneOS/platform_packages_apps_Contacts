package com.android.contacts.ui.contactcreation.model

import android.net.Uri
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType

// TooManyFunctions: Detekt counts each nested data class/object as a "function" in a sealed
// interface. This sealed type is the exhaustive action catalogue for the MVI pattern -- one
// subtype per user interaction. Splitting into sub-sealed types would break exhaustive `when`
// dispatch in the ViewModel without meaningful complexity reduction.
@Suppress("TooManyFunctions")
internal sealed interface ContactCreationAction {
    // Navigation
    data object NavigateBack : ContactCreationAction
    data object Save : ContactCreationAction
    data object ConfirmDiscard : ContactCreationAction
    data object DismissDiscardDialog : ContactCreationAction

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

    // Address
    data object AddAddress : ContactCreationAction
    data class RemoveAddress(val id: String) : ContactCreationAction
    data class UpdateAddressStreet(val id: String, val value: String) : ContactCreationAction
    data class UpdateAddressCity(val id: String, val value: String) : ContactCreationAction
    data class UpdateAddressRegion(val id: String, val value: String) : ContactCreationAction
    data class UpdateAddressPostcode(val id: String, val value: String) : ContactCreationAction
    data class UpdateAddressCountry(val id: String, val value: String) : ContactCreationAction
    data class UpdateAddressType(val id: String, val type: AddressType) : ContactCreationAction

    // Organization
    data class UpdateCompany(val value: String) : ContactCreationAction
    data class UpdateJobTitle(val value: String) : ContactCreationAction

    // Event
    data object AddEvent : ContactCreationAction
    data class RemoveEvent(val id: String) : ContactCreationAction
    data class UpdateEvent(val id: String, val value: String) : ContactCreationAction
    data class UpdateEventType(val id: String, val type: EventType) : ContactCreationAction

    // Relation
    data object AddRelation : ContactCreationAction
    data class RemoveRelation(val id: String) : ContactCreationAction
    data class UpdateRelation(val id: String, val value: String) : ContactCreationAction
    data class UpdateRelationType(val id: String, val type: RelationType) : ContactCreationAction

    // IM
    data object AddIm : ContactCreationAction
    data class RemoveIm(val id: String) : ContactCreationAction
    data class UpdateIm(val id: String, val value: String) : ContactCreationAction
    data class UpdateImProtocol(val id: String, val protocol: ImProtocol) : ContactCreationAction

    // Website
    data object AddWebsite : ContactCreationAction
    data class RemoveWebsite(val id: String) : ContactCreationAction
    data class UpdateWebsite(val id: String, val value: String) : ContactCreationAction
    data class UpdateWebsiteType(val id: String, val type: WebsiteType) : ContactCreationAction

    // Note
    data class UpdateNote(val value: String) : ContactCreationAction

    // Nickname
    data class UpdateNickname(val value: String) : ContactCreationAction

    // SIP
    data class UpdateSipAddress(val value: String) : ContactCreationAction

    // Groups
    data class ToggleGroup(val groupId: Long, val title: String) : ContactCreationAction

    // More fields
    data object ToggleMoreFields : ContactCreationAction

    // Photo
    data class SetPhoto(val uri: Uri) : ContactCreationAction
    data object RemovePhoto : ContactCreationAction
    data object RequestGallery : ContactCreationAction
    data object RequestCamera : ContactCreationAction

    // Account
    data object RequestAccountPicker : ContactCreationAction
    data class SelectAccount(val account: AccountWithDataSet) : ContactCreationAction
}
