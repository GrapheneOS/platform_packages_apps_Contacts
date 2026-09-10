package com.android.contacts.data.contactdetails.source

import android.content.Context
import android.net.Uri
import com.android.contacts.model.ContactLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
internal class ContactLoaderSourceImplTest {

    private val context: Context = RuntimeEnvironment.getApplication()

    private val source = ContactLoaderSourceImpl(context = context)

    @Test
    fun create_returnsALoaderForTheLookupUri() {
        val loader = source.create(LOOKUP_URI)

        assertEquals(LOOKUP_URI, loader.lookupUri)
    }

    @Test
    fun create_loadsGroupMetaDataSoInvisibleContactsAreDetectable() {
        val loader = source.create(LOOKUP_URI)

        assertTrue(loaderFlag(loader, "mLoadGroupMetaData"))
    }

    @Test
    fun create_postsTheViewNotificationToSyncAdapters() {
        val loader = source.create(LOOKUP_URI)

        assertTrue(loaderFlag(loader, "mPostViewNotification"))
    }

    @Test
    fun create_computesFormattedPhoneNumbers() {
        val loader = source.create(LOOKUP_URI)

        assertTrue(loaderFlag(loader, "mComputeFormattedPhoneNumber"))
    }

    private fun loaderFlag(
        loader: ContactLoader,
        name: String,
    ): Boolean {
        val field = ContactLoader::class.java.getDeclaredField(name)
        field.isAccessible = true

        return field.getBoolean(loader)
    }

    private companion object {
        val LOOKUP_URI: Uri = Uri.parse("content://com.android.contacts/contacts/lookup/key/7")
    }
}
