package com.android.contacts.domain.contacts.mapper

import com.android.contacts.data.contacts.model.DirectoryColumn
import com.android.contacts.domain.contacts.model.ContactDirectory
import javax.inject.Inject

internal interface ContactDirectoryMapper {
    fun map(column: DirectoryColumn): ContactDirectory
}

internal class ContactDirectoryMapperImpl @Inject constructor() : ContactDirectoryMapper {

    override fun map(column: DirectoryColumn): ContactDirectory {
        return ContactDirectory(
            id = column.id,
            displayName = column.displayName,
            packageName = column.packageName,
            typeResourceId = column.typeResourceId,
        )
    }
}
