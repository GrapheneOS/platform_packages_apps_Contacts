package com.android.contacts.ui.contactcreation.preview

import com.android.contacts.ui.contactcreation.component.AddressType
import com.android.contacts.ui.contactcreation.component.EmailType
import com.android.contacts.ui.contactcreation.component.EventType
import com.android.contacts.ui.contactcreation.component.ImProtocol
import com.android.contacts.ui.contactcreation.component.PhoneType
import com.android.contacts.ui.contactcreation.component.RelationType
import com.android.contacts.ui.contactcreation.component.WebsiteType
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationUiState
import com.android.contacts.ui.contactcreation.model.EmailFieldState
import com.android.contacts.ui.contactcreation.model.EventFieldState
import com.android.contacts.ui.contactcreation.model.GroupFieldState
import com.android.contacts.ui.contactcreation.model.GroupInfo
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState

internal object PreviewData {

    val nameState = NameState(
        first = "Jane",
        last = "Doe",
    )

    val phones = listOf(
        PhoneFieldState(id = "phone-1", number = "555-1234", type = PhoneType.Mobile),
        PhoneFieldState(id = "phone-2", number = "555-5678", type = PhoneType.Work),
    )

    val singlePhone = listOf(
        PhoneFieldState(id = "phone-1", number = "555-1234", type = PhoneType.Mobile),
    )

    val emails = listOf(
        EmailFieldState(id = "email-1", address = "jane@example.com", type = EmailType.Home),
        EmailFieldState(id = "email-2", address = "jane@work.com", type = EmailType.Work),
    )

    val singleEmail = listOf(
        EmailFieldState(id = "email-1", address = "jane@example.com", type = EmailType.Home),
    )

    val addresses = listOf(
        AddressFieldState(
            id = "addr-1",
            street = "123 Main St",
            city = "Springfield",
            region = "IL",
            postcode = "62701",
            country = "US",
            type = AddressType.Home,
        ),
    )

    val organization = OrganizationFieldState(
        company = "Acme Corp",
        title = "Software Engineer",
    )

    val events = listOf(
        EventFieldState(id = "event-1", startDate = "1990-01-15", type = EventType.Birthday),
        EventFieldState(id = "event-2", startDate = "2020-06-20", type = EventType.Anniversary),
    )

    val relations = listOf(
        RelationFieldState(id = "rel-1", name = "John Doe", type = RelationType.Spouse),
    )

    val imAccounts = listOf(
        ImFieldState(id = "im-1", data = "jane_doe", protocol = ImProtocol.Jabber),
    )

    val websites = listOf(
        WebsiteFieldState(id = "web-1", url = "https://janedoe.dev", type = WebsiteType.Homepage),
    )

    val availableGroups = listOf(
        GroupInfo(groupId = 1L, title = "Friends"),
        GroupInfo(groupId = 2L, title = "Family"),
        GroupInfo(groupId = 3L, title = "Coworkers"),
    )

    val selectedGroups = listOf(
        GroupFieldState(groupId = 1L, title = "Friends"),
    )

    val fullUiState = ContactCreationUiState(
        nameState = nameState,
        phoneNumbers = phones,
        emails = emails,
        addresses = addresses,
        organization = organization,
        events = events,
        relations = relations,
        imAccounts = imAccounts,
        websites = websites,
        note = "Met at the conference",
        nickname = "JD",
        sipAddress = "jane@sip.example.com",
        groups = selectedGroups,
        availableGroups = availableGroups,
        accountName = "jane@gmail.com",
        isMoreFieldsExpanded = true,
        showSipField = true,
    )

    val emptyUiState = ContactCreationUiState()
}
