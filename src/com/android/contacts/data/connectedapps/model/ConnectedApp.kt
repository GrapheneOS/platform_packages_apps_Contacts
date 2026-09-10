package com.android.contacts.data.connectedapps.model

internal data class ConnectedApp(
    val packageName: String,
    val label: String,
    val iconUri: String?,
)
