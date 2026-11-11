package com.android.contacts.data.tests

import android.content.ContentResolver
import android.database.ContentObserver
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.slot

@Suppress("ArrayInDataClass")
internal data class RecordedQuery(
    val uri: Uri,
    val projection: Array<String>?,
    val selection: String?,
    val selectionArgs: Array<String>?,
    val sortOrder: String?,
)

internal fun ContentResolver.givenQueryRows(
    vararg rows: Array<Any?>,
    extras: Bundle? = null,
): List<RecordedQuery> {
    return givenQueryResult {
        MatrixCursor(it.projection).apply {
            rows.forEach(::addRow)
            if (extras != null) setExtras(extras)
        }
    }
}

internal fun ContentResolver.givenQueryResult(
    result: (RecordedQuery) -> Cursor?,
): List<RecordedQuery> {
    val queries = mutableListOf<RecordedQuery>()
    every {
        query(any(), any(), any(), any(), any())
    } answers {
        val query = RecordedQuery(
            uri = arg(0),
            projection = arg(1),
            selection = arg(2),
            selectionArgs = arg(3),
            sortOrder = arg(4),
        )
        queries += query
        result(query)
    }
    return queries
}

internal fun ContentResolver.givenRegisteredContentObserver(
    uriSlot: CapturingSlot<Uri> = slot(),
): CapturingSlot<ContentObserver> {
    val observerSlot = slot<ContentObserver>()
    every {
        registerContentObserver(capture(uriSlot), true, capture(observerSlot))
    } returns Unit
    return observerSlot
}

internal fun addressBookIndexExtras(
    titles: Array<String>,
    counts: IntArray,
): Bundle {
    return Bundle().apply {
        putStringArray(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_TITLES, titles)
        putIntArray(ContactsContract.Contacts.EXTRA_ADDRESS_BOOK_INDEX_COUNTS, counts)
    }
}
