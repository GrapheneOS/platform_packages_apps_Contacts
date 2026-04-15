package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationAction

/**
 * Address section as a @Composable for Column-based layout.
 */
@Composable
internal fun AddressSectionContent(
    addresses: List<AddressFieldState>,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        addresses.forEachIndexed { index, address ->
            if (index > 0) {
                Spacer(modifier = Modifier.height(8.dp))
            }
            AddressFieldRow(
                address = address,
                index = index,
                onAction = onAction,
            )
        }
        AddRemoveFieldRow(
            addLabel = stringResource(R.string.contact_creation_add_address),
            onAdd = { onAction(ContactCreationAction.AddAddress) },
            addTestTag = TestTags.ADDRESS_ADD,
            removeLabel = if (addresses.isNotEmpty()) {
                stringResource(R.string.contact_creation_remove_address)
            } else {
                null
            },
            onRemove = if (addresses.isNotEmpty()) {
                { onAction(ContactCreationAction.RemoveAddress(addresses.last().id)) }
            } else {
                null
            },
        )
    }
}

@Composable
internal fun AddressFieldRow(
    address: AddressFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showCustomDialog by remember { mutableStateOf(false) }

    FieldRow(modifier = modifier) {
        AddressFieldColumns(
            address = address,
            index = index,
            onAction = onAction,
            onRequestCustomLabel = { showCustomDialog = true },
        )
    }

    if (showCustomDialog) {
        CustomLabelDialog(
            onConfirm = { label ->
                showCustomDialog = false
                onAction(
                    ContactCreationAction.UpdateAddressType(
                        address.id,
                        AddressType.Custom(label),
                    ),
                )
            },
            onDismiss = { showCustomDialog = false },
        )
    }
}

@Composable
private fun AddressFieldColumns(
    address: AddressFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    onRequestCustomLabel: () -> Unit,
) {
    Column {
        val context = LocalContext.current
        val selectorLabels = AddressType.selectorTypes.map { it.label(context) }
        FieldTypeSelector(
            currentLabel = address.type.label(context),
            labels = selectorLabels,
            onIndexSelected = { idx ->
                val selected = AddressType.selectorTypes[idx]
                if (selected is AddressType.Custom && selected.label.isEmpty()) {
                    onRequestCustomLabel()
                } else {
                    onAction(ContactCreationAction.UpdateAddressType(address.id, selected))
                }
            },
            modifier = Modifier.testTag(TestTags.addressType(index)),
        )
        AddressTextField(
            address.street,
            stringResource(R.string.postal_street),
            TestTags.addressStreet(index),
        ) {
            onAction(ContactCreationAction.UpdateAddressStreet(address.id, it))
        }
        AddressTextField(
            address.city,
            stringResource(R.string.postal_city),
            TestTags.addressCity(index),
        ) {
            onAction(ContactCreationAction.UpdateAddressCity(address.id, it))
        }
        AddressTextField(
            address.region,
            stringResource(R.string.postal_region),
            TestTags.addressRegion(index),
        ) {
            onAction(ContactCreationAction.UpdateAddressRegion(address.id, it))
        }
        AddressTextField(
            address.postcode,
            stringResource(R.string.postal_postcode),
            TestTags.addressPostcode(index),
        ) {
            onAction(ContactCreationAction.UpdateAddressPostcode(address.id, it))
        }
        AddressTextField(
            address.country,
            stringResource(R.string.postal_country),
            TestTags.addressCountry(index),
        ) {
            onAction(ContactCreationAction.UpdateAddressCountry(address.id, it))
        }
    }
}

@Composable
private fun AddressTextField(
    value: String,
    label: String,
    tag: String,
    onValueChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(tag),
        singleLine = true,
    )
}
