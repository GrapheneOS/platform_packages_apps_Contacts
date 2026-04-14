package com.android.contacts.ui.contactcreation.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.android.contacts.R
import com.android.contacts.ui.contactcreation.TestTags
import com.android.contacts.ui.contactcreation.model.AddressFieldState
import com.android.contacts.ui.contactcreation.model.ContactCreationAction
import com.android.contacts.ui.core.animateItemIfMotionAllowed

internal fun LazyListScope.addressSection(
    addresses: List<AddressFieldState>,
    onAction: (ContactCreationAction) -> Unit,
) {
    itemsIndexed(
        items = addresses,
        key = { _, item -> item.id },
        contentType = { _, _ -> "address_field" },
    ) { index, address ->
        AddressFieldRow(
            address = address,
            index = index,
            showDelete = addresses.size > 1,
            onAction = onAction,
            modifier = animateItemIfMotionAllowed(),
        )
    }
    item(key = "address_add", contentType = "address_add") {
        TextButton(
            onClick = { onAction(ContactCreationAction.AddAddress) },
            modifier = Modifier
                .padding(start = 16.dp)
                .testTag(TestTags.ADDRESS_ADD),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = stringResource(R.string.contact_creation_add_address),
            )
            Text(stringResource(R.string.contact_creation_add_address))
        }
    }
}

@Composable
internal fun AddressFieldRow(
    address: AddressFieldState,
    index: Int,
    showDelete: Boolean,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Filled.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp, top = 16.dp),
        )
        AddressFieldColumns(
            address = address,
            index = index,
            onAction = onAction,
            modifier = Modifier.weight(1f),
        )
        if (showDelete) {
            IconButton(
                onClick = { onAction(ContactCreationAction.RemoveAddress(address.id)) },
                modifier = Modifier.testTag(TestTags.addressDelete(index)),
            ) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.contact_creation_remove_address),
                )
            }
        }
    }
}

@Composable
private fun AddressFieldColumns(
    address: AddressFieldState,
    index: Int,
    onAction: (ContactCreationAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
