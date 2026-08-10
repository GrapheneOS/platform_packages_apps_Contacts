package com.android.contacts.tests.factory

import android.content.ContentValues
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.Data
import com.android.contacts.model.account.AccountType.EditType
import com.android.contacts.model.account.BaseAccountType.SimpleInflater
import com.android.contacts.model.dataitem.DataItem
import com.android.contacts.model.dataitem.DataKind

internal fun collapsibleDataItem(
    mimeType: String?,
    data: String? = null,
    id: Long = 1L,
    type: Int? = null,
    label: String? = null,
    protocol: Int? = null,
    customProtocol: String? = null,
    isPrimary: Boolean = false,
    isSuperPrimary: Boolean = false,
    dataKind: DataKind? = collapsibleDataKind(),
): DataItem {
    val values = ContentValues().apply {
        put(Data._ID, id)
        put(Data.MIMETYPE, mimeType)
        put(Data.DATA1, data)
        type?.let { value -> put(Data.DATA2, value) }
        label?.let { value -> put(Data.DATA3, value) }
        protocol?.let { value -> put(Data.DATA5, value) }
        customProtocol?.let { value -> put(Data.DATA6, value) }
        put(Data.IS_PRIMARY, if (isPrimary) 1 else 0)
        put(Data.IS_SUPER_PRIMARY, if (isSuperPrimary) 1 else 0)
    }

    return DataItem.createFrom(values).also { item -> item.dataKind = dataKind }
}

internal fun collapsibleDataKind(maxLinesForDisplay: Int = 1): DataKind {
    return DataKind().apply {
        actionBody = SimpleInflater(Data.DATA1)
        typeColumn = Data.DATA2
        typeList = listOf(
            EditType(Phone.TYPE_MOBILE, -1),
            EditType(Phone.TYPE_HOME, -1),
            EditType(Phone.TYPE_WORK, -1),
            EditType(Phone.TYPE_OTHER, -1),
        )
        this.maxLinesForDisplay = maxLinesForDisplay
    }
}
