package com.android.contacts.data.contactdetails.source

import android.content.Context
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ContactLoaderSourceImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val source = ContactLoaderSourceImpl(context = context)

    @Test
    fun create_returnsALoaderForTheLookupUri() {
        val loader = source.create(LOOKUP_URI)

        assertEquals(LOOKUP_URI, loader.lookupUri)
    }

    private companion object {
        val LOOKUP_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")
    }
}
