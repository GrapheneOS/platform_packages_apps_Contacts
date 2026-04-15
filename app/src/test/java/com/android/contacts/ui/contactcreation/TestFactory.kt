package com.android.contacts.ui.contactcreation

import android.net.Uri
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
import com.android.contacts.ui.contactcreation.model.ImFieldState
import com.android.contacts.ui.contactcreation.model.NameState
import com.android.contacts.ui.contactcreation.model.OrganizationFieldState
import com.android.contacts.ui.contactcreation.model.PhoneFieldState
import com.android.contacts.ui.contactcreation.model.RelationFieldState
import com.android.contacts.ui.contactcreation.model.WebsiteFieldState

internal object TestFactory {

    fun fullState() = ContactCreationUiState(
        nameState = NameState(first = "Jane", last = "Doe"),
        phoneNumbers = listOf(
            PhoneFieldState(id = "phone-1", number = "555-1234", type = PhoneType.Mobile)
        ),
        emails = listOf(
            EmailFieldState(id = "email-1", address = "test@example.com", type = EmailType.Home)
        ),
        addresses = listOf(
            AddressFieldState(
                id = "addr-1",
                street = "123 Main St",
                city = "Springfield",
                type = AddressType.Home,
            ),
        ),
        organization = OrganizationFieldState(company = "Acme Corp", title = "Engineer"),
        events = listOf(
            EventFieldState(id = "event-1", startDate = "1990-01-15", type = EventType.Birthday)
        ),
        relations = listOf(
            RelationFieldState(id = "rel-1", name = "Jane Doe", type = RelationType.Spouse)
        ),
        imAccounts = listOf(
            ImFieldState(id = "im-1", data = "user@jabber", protocol = ImProtocol.Jabber)
        ),
        websites = listOf(
            WebsiteFieldState(
                id = "web-1",
                url = "https://example.com",
                type = WebsiteType.Homepage
            )
        ),
        note = "Important note",
        nickname = "JD",
        sipAddress = "sip:jane@voip.example.com",
        groups = listOf(GroupFieldState(groupId = 1L, title = "Friends")),
        photoUri = Uri.parse("content://media/external/images/99"),
        showOrganization = true,
        showNote = true,
        showNickname = true,
        showSipAddress = true,
    )
}
