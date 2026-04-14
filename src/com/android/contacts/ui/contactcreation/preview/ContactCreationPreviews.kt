package com.android.contacts.ui.contactcreation.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.contacts.ui.contactcreation.ContactCreationEditorScreen
import com.android.contacts.ui.contactcreation.component.AccountChip
import com.android.contacts.ui.contactcreation.component.AddressFieldRow
import com.android.contacts.ui.contactcreation.component.EmailFieldRow
import com.android.contacts.ui.contactcreation.component.GroupCheckboxRow
import com.android.contacts.ui.contactcreation.component.MoreFieldsState
import com.android.contacts.ui.contactcreation.component.NameFields
import com.android.contacts.ui.contactcreation.component.OrganizationFields
import com.android.contacts.ui.contactcreation.component.PhoneFieldRow
import com.android.contacts.ui.contactcreation.component.PhotoAvatar
import com.android.contacts.ui.contactcreation.component.addressSection
import com.android.contacts.ui.contactcreation.component.emailSection
import com.android.contacts.ui.contactcreation.component.groupSection
import com.android.contacts.ui.contactcreation.component.moreFieldsSection
import com.android.contacts.ui.contactcreation.component.nameSection
import com.android.contacts.ui.contactcreation.component.phoneSection
import com.android.contacts.ui.contactcreation.component.photoSection
import com.android.contacts.ui.core.AppTheme

// region Full Screen Previews

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactCreationEditorScreenPreview() {
    AppTheme {
        ContactCreationEditorScreen(
            uiState = PreviewData.fullUiState,
            onAction = {},
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ContactCreationEditorScreenEmptyPreview() {
    AppTheme {
        ContactCreationEditorScreen(
            uiState = PreviewData.emptyUiState,
            onAction = {},
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ContactCreationEditorScreenDarkPreview() {
    AppTheme {
        ContactCreationEditorScreen(
            uiState = PreviewData.fullUiState,
            onAction = {},
        )
    }
}

// endregion

// region PhotoSection

@Preview(showBackground = true)
@Composable
private fun PhotoSectionNoPhotoPreview() {
    AppTheme {
        LazyColumn {
            photoSection(photoUri = null, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhotoAvatarNoPhotoPreview() {
    AppTheme {
        PhotoAvatar(
            photoUri = null,
            onAction = {},
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        )
    }
}

// endregion

// region NameSection

@Preview(showBackground = true)
@Composable
private fun NameSectionPreview() {
    AppTheme {
        LazyColumn {
            nameSection(nameState = PreviewData.nameState, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NameFieldsPreview() {
    AppTheme {
        NameFields(nameState = PreviewData.nameState, onAction = {})
    }
}

// endregion

// region PhoneSection

@Preview(showBackground = true)
@Composable
private fun PhoneSectionPreview() {
    AppTheme {
        LazyColumn {
            phoneSection(phones = PreviewData.phones, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneSectionSinglePreview() {
    AppTheme {
        LazyColumn {
            phoneSection(phones = PreviewData.singlePhone, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PhoneFieldRowPreview() {
    AppTheme {
        PhoneFieldRow(
            phone = PreviewData.phones[0],
            index = 0,
            showDelete = true,
            onAction = {},
        )
    }
}

// endregion

// region EmailSection

@Preview(showBackground = true)
@Composable
private fun EmailSectionPreview() {
    AppTheme {
        LazyColumn {
            emailSection(emails = PreviewData.emails, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSectionSinglePreview() {
    AppTheme {
        LazyColumn {
            emailSection(emails = PreviewData.singleEmail, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailFieldRowPreview() {
    AppTheme {
        EmailFieldRow(
            email = PreviewData.emails[0],
            index = 0,
            showDelete = true,
            onAction = {},
        )
    }
}

// endregion

// region AddressSection

@Preview(showBackground = true)
@Composable
private fun AddressSectionPreview() {
    AppTheme {
        LazyColumn {
            addressSection(addresses = PreviewData.addresses, onAction = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddressFieldRowPreview() {
    AppTheme {
        AddressFieldRow(
            address = PreviewData.addresses[0],
            index = 0,
            showDelete = false,
            onAction = {},
        )
    }
}

// endregion

// region OrganizationSection

@Preview(showBackground = true)
@Composable
private fun OrganizationFieldsPreview() {
    AppTheme {
        OrganizationFields(organization = PreviewData.organization, onAction = {})
    }
}

// endregion

// region MoreFieldsSection

@Preview(showBackground = true)
@Composable
private fun MoreFieldsSectionExpandedPreview() {
    AppTheme {
        LazyColumn {
            moreFieldsSection(
                state = MoreFieldsState(
                    isExpanded = true,
                    events = PreviewData.events,
                    relations = PreviewData.relations,
                    imAccounts = PreviewData.imAccounts,
                    websites = PreviewData.websites,
                    note = "Met at the conference",
                    nickname = "JD",
                    sipAddress = "jane@sip.example.com",
                    showSipField = true,
                ),
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MoreFieldsSectionCollapsedPreview() {
    AppTheme {
        LazyColumn {
            moreFieldsSection(
                state = MoreFieldsState(
                    isExpanded = false,
                    events = emptyList(),
                    relations = emptyList(),
                    imAccounts = emptyList(),
                    websites = emptyList(),
                    note = "",
                    nickname = "",
                    sipAddress = "",
                    showSipField = true,
                ),
                onAction = {},
            )
        }
    }
}

// endregion

// region GroupSection

@Preview(showBackground = true)
@Composable
private fun GroupSectionPreview() {
    AppTheme {
        LazyColumn {
            groupSection(
                availableGroups = PreviewData.availableGroups,
                selectedGroups = PreviewData.selectedGroups,
                onAction = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupCheckboxRowSelectedPreview() {
    AppTheme {
        GroupCheckboxRow(
            group = PreviewData.availableGroups[0],
            isSelected = true,
            index = 0,
            onAction = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GroupCheckboxRowUnselectedPreview() {
    AppTheme {
        GroupCheckboxRow(
            group = PreviewData.availableGroups[1],
            isSelected = false,
            index = 1,
            onAction = {},
        )
    }
}

// endregion

// region AccountChip

@Preview(showBackground = true)
@Composable
private fun AccountChipWithNamePreview() {
    AppTheme {
        AccountChip(accountName = "jane@gmail.com", onClick = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AccountChipDevicePreview() {
    AppTheme {
        Surface {
            AccountChip(accountName = null, onClick = {})
        }
    }
}

// endregion
