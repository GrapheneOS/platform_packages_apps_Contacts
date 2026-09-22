package com.android.contacts.data.contactdetails.source

import android.content.Context
import android.net.Uri
import com.android.contacts.model.ContactLoader
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal interface ContactLoaderSource {
    fun create(lookupUri: Uri): ContactLoader
}

internal class ContactLoaderSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ContactLoaderSource {

    override fun create(lookupUri: Uri): ContactLoader {
        return ContactLoader(
            context,
            lookupUri,
            LOAD_GROUP_META_DATA,
            POST_VIEW_NOTIFICATION,
            COMPUTE_FORMATTED_PHONE_NUMBER,
        )
    }

    private companion object {
        const val LOAD_GROUP_META_DATA = true
        const val POST_VIEW_NOTIFICATION = true
        const val COMPUTE_FORMATTED_PHONE_NUMBER = true
    }
}
