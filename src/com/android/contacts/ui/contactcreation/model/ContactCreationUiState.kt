package com.android.contacts.ui.contactcreation.model

import android.net.Uri
import android.os.Parcelable
import androidx.compose.runtime.Immutable
import com.android.contacts.model.account.AccountWithDataSet
import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import java.util.UUID
import kotlinx.parcelize.Parcelize

@Immutable
@Parcelize
internal data class ContactCreationUiState(
    val nameState: NameState = NameState(),
    val phoneNumbers: List<PhoneFieldState> = listOf(PhoneFieldState()),
    val emails: List<EmailFieldState> = listOf(EmailFieldState()),
    val addresses: List<AddressFieldState> = emptyList(),
    val organization: OrganizationFieldState = OrganizationFieldState(),
    val events: List<EventFieldState> = emptyList(),
    val relations: List<RelationFieldState> = emptyList(),
    val imAccounts: List<ImFieldState> = emptyList(),
    val websites: List<WebsiteFieldState> = emptyList(),
    val note: String = "",
    val nickname: String = "",
    val sipAddress: String = "",
    val groups: List<GroupFieldState> = emptyList(),
    val availableGroups: List<GroupInfo> = emptyList(),
    val photoUri: Uri? = null,
    val selectedAccount: AccountWithDataSet? = null,
    val accountName: String? = null,
    val isSaving: Boolean = false,
    val showOrganization: Boolean = false,
    val showNote: Boolean = false,
    val showNickname: Boolean = false,
    val showSipAddress: Boolean = false,
    val showSipField: Boolean = true,
    val showDiscardDialog: Boolean = false,
) : Parcelable {
    fun hasPendingChanges(): Boolean =
        nameState.hasData() ||
            phoneNumbers.any { it.number.isNotBlank() } ||
            emails.any { it.address.isNotBlank() } ||
            addresses.any { it.hasData() } ||
            organization.hasData() ||
            events.any { it.startDate.isNotBlank() } ||
            relations.any { it.name.isNotBlank() } ||
            imAccounts.any { it.data.isNotBlank() } ||
            websites.any { it.url.isNotBlank() } ||
            note.isNotBlank() ||
            nickname.isNotBlank() ||
            sipAddress.isNotBlank() ||
            groups.isNotEmpty() ||
            photoUri != null

    val showAddressChip: Boolean get() = addresses.isEmpty()

    val showOrgChip: Boolean
        get() = !showOrganization && organization.company.isBlank() && organization.title.isBlank()

    val showNoteChip: Boolean get() = !showNote && note.isBlank()

    val showGroupsChip: Boolean get() = groups.isEmpty() && availableGroups.isNotEmpty()

    @Suppress("ComplexCondition")
    val showOtherChip: Boolean
        get() = events.isEmpty() || relations.isEmpty() || imAccounts.isEmpty() ||
            websites.isEmpty() || (!showNickname && nickname.isBlank()) ||
            (!showSipAddress && sipAddress.isBlank() && showSipField)

    val hasAnyChip: Boolean
        get() = showAddressChip || showOrgChip || showNoteChip || showGroupsChip || showOtherChip
}

@Immutable
@Parcelize
internal data class PhoneFieldState(
    val id: String = UUID.randomUUID().toString(),
    val number: String = "",
    val type: PhoneType = PhoneType.Mobile,
) : Parcelable

@Immutable
@Parcelize
internal data class EmailFieldState(
    val id: String = UUID.randomUUID().toString(),
    val address: String = "",
    val type: EmailType = EmailType.Home,
) : Parcelable

@Immutable
@Parcelize
internal data class AddressFieldState(
    val id: String = UUID.randomUUID().toString(),
    val street: String = "",
    val city: String = "",
    val region: String = "",
    val postcode: String = "",
    val country: String = "",
    val type: AddressType = AddressType.Home,
) : Parcelable {
    fun hasData(): Boolean =
        street.isNotBlank() || city.isNotBlank() || region.isNotBlank() ||
            postcode.isNotBlank() || country.isNotBlank()
}

@Immutable
@Parcelize
internal data class OrganizationFieldState(val company: String = "", val title: String = "") :
    Parcelable {
    fun hasData(): Boolean = company.isNotBlank() || title.isNotBlank()
}

@Immutable
@Parcelize
internal data class EventFieldState(
    val id: String = UUID.randomUUID().toString(),
    val startDate: String = "",
    val type: EventType = EventType.Birthday,
) : Parcelable

@Immutable
@Parcelize
internal data class RelationFieldState(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val type: RelationType = RelationType.Spouse,
) : Parcelable

@Immutable
@Parcelize
internal data class ImFieldState(
    val id: String = UUID.randomUUID().toString(),
    val data: String = "",
    val protocol: ImProtocol = ImProtocol.Jabber,
) : Parcelable

@Immutable
@Parcelize
internal data class WebsiteFieldState(
    val id: String = UUID.randomUUID().toString(),
    val url: String = "",
    val type: WebsiteType = WebsiteType.Homepage,
) : Parcelable

@Immutable
@Parcelize
internal data class GroupFieldState(val groupId: Long, val title: String) : Parcelable

@Immutable
@Parcelize
internal data class GroupInfo(val groupId: Long, val title: String) : Parcelable
